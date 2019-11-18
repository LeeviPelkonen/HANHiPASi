/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package arkki

import android.annotation.SuppressLint
import android.graphics.*
import android.graphics.Bitmap.Config
import android.media.ImageReader.OnImageAvailableListener
import android.media.MediaPlayer
import android.opengl.Visibility
import android.os.SystemClock
import android.util.Log
import android.util.Size
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.annotation.RawRes
import org.jetbrains.anko.doAsync
import java.io.IOException
import arkki.env.BorderedText
import arkki.env.ImageUtils
import arkki.env.Logger
import arkki.tflite.Classifier
import arkki.tflite.Classifier.Device
import arkki.tflite.Classifier.Model
import kotlinx.android.synthetic.main.bird_info.*
import org.tensorflow.lite.examples.classification.R

class ClassifierActivity : CameraActivity(), OnImageAvailableListener {
    private var rgbFrameBitmap: Bitmap? = null
    private var croppedBitmap: Bitmap? = null
    private var cropCopyBitmap: Bitmap? = null
    private var lastProcessingTimeMs: Long = 0
    private var sensorOrientation: Int? = null
    private var classifier: Classifier? = null
    private var frameToCropTransform: Matrix? = null
    private var cropToFrameTransform: Matrix? = null
    private var borderedText: BorderedText? = null
    private var infoIsShowing: Boolean = false
    private lateinit var mediaPlayer: MediaPlayer
    private val soundList = listOf("Lapasorsa", "Punasotka", "Ristisorsa", "Ruskosuohaukka")


    protected override val layoutId: Int
        get() = R.layout.camera_connection_fragment

    protected override val desiredPreviewFrameSize: Size
        get() = DESIRED_PREVIEW_SIZE

    public override fun onPreviewSizeChosen(size: Size, rotation: Int) {
        val textSizePx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, resources.displayMetrics)
        borderedText = BorderedText(textSizePx)
        borderedText!!.setTypeface(Typeface.MONOSPACE)

        recreateClassifier(getModel(), getDevice(), getNumThreads())
        if (classifier == null) {
            LOGGER.e("No classifier on preview!")
            return
        }

        previewWidth = size.width
        previewHeight = size.height

        sensorOrientation = rotation - screenOrientation
        LOGGER.i("Camera orientation relative to screen canvas: $sensorOrientation")

        LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight)
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888)
        croppedBitmap = Bitmap.createBitmap(
                classifier!!.imageSizeX, classifier!!.imageSizeY, Config.ARGB_8888)

        frameToCropTransform = ImageUtils.getTransformationMatrix(
                previewWidth,
                previewHeight,
                classifier!!.imageSizeX,
                classifier!!.imageSizeY,
                sensorOrientation!!,
                MAINTAIN_ASPECT)

        cropToFrameTransform = Matrix()
        frameToCropTransform!!.invert(cropToFrameTransform)
    }

    override fun processImage() {
        rgbFrameBitmap!!.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight)
        val canvas = Canvas(croppedBitmap!!)
        canvas.drawBitmap(rgbFrameBitmap!!, frameToCropTransform!!, null)
        /*
        runInBackground {
            if (classifier != null) {
                val startTime = SystemClock.uptimeMillis()
                val results = classifier!!.recognizeImage(croppedBitmap)
                lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime
                LOGGER.v("Detect: %s", results)
                cropCopyBitmap = Bitmap.createBitmap(croppedBitmap!!)

                runOnUiThread {
                    showResultsInBottomSheet(results)
                    showFrameInfo(previewWidth.toString() + "x" + previewHeight)
                    showCropInfo(cropCopyBitmap!!.width.toString() + "x" + cropCopyBitmap!!.height)
                    showCameraResolution(canvas.width.toString() + "x" + canvas.height)
                    showRotationInfo(sensorOrientation.toString())
                    showInference(lastProcessingTimeMs.toString() + "ms")
                }
            }
            readyForNextImage()
        }*/
        doAsync {
            if (classifier != null) {
                val startTime = SystemClock.uptimeMillis()
                val results = classifier!!.recognizeImage(croppedBitmap!!)
                lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime
                LOGGER.v("Detect: %s", results)
                cropCopyBitmap = Bitmap.createBitmap(croppedBitmap!!)

                runOnUiThread {
                    showResultsInBottomSheet(results.recognitions)
                    /*showFrameInfo(previewWidth.toString() + "x" + previewHeight)
                    showCropInfo(cropCopyBitmap!!.width.toString() + "x" + cropCopyBitmap!!.height)
                    showCameraResolution(canvas.width.toString() + "x" + canvas.height)
                    showRotationInfo(sensorOrientation.toString())
                    showInference(lastProcessingTimeMs.toString() + "ms")*/
                    if (results.bird != null) {
                        if (!infoIsShowing) {
                            showPopupWindow(results.bird)
                        }
                        infoIsShowing = true
                    }
                }
            }
            readyForNextImage()
        }
    }

    @SuppressLint("DefaultLocale")
    private fun showPopupWindow(bird: String?) {
        val inflater: LayoutInflater = LayoutInflater.from(this@ClassifierActivity)
        val view = inflater.inflate(R.layout.bird_info, LinearLayout(this@ClassifierActivity))
        val title = view.findViewById<TextView>(R.id.tvBirdName)
        val btnExit = view.findViewById<Button>(R.id.buttonExit)
        val soundButton = view.findViewById<ImageView>(R.id.ivSoundButton)
        val soundId = resources.getIdentifier(bird!!.toLowerCase(), "raw", packageName)
        title.text = bird
        mediaPlayer = MediaPlayer()

        if (soundList.contains(bird)) {
            soundButton.visibility = View.VISIBLE
        }

        val width = LinearLayout.LayoutParams.MATCH_PARENT
        val height = LinearLayout.LayoutParams.MATCH_PARENT
        val popupWindow = PopupWindow(view, width, height, true)

        popupWindow.animationStyle = R.style.popup_window_animation_phone
        popupWindow.showAtLocation(view, Gravity.CENTER, 0,0)

        btnExit.setOnClickListener {
            popupWindow.dismiss()
        }

        soundButton.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.reset()
                soundButton.setImageResource(R.drawable.speaker)
            }
            else {
                mediaPlayer = MediaPlayer.create(this, soundId)
                mediaPlayer.start()
                soundButton.setImageResource(R.drawable.speaker_mute)
            }
        }

        popupWindow.setOnDismissListener {
            infoIsShowing = false
            if (mediaPlayer.isPlaying){
                mediaPlayer.stop()
                mediaPlayer.release()
            }
        }

    }

    override fun onInferenceConfigurationChanged() {
        if (croppedBitmap == null) {
            // Defer creation until we're getting camera frames.
            return
        }
        val device = getDevice()
        val model = getModel()
        val numThreads = getNumThreads()
        //runInBackground { recreateClassifier(model, device, numThreads) }
        doAsync { recreateClassifier(model, device, numThreads) }
    }

    private fun recreateClassifier(model: Model, device: Device, numThreads: Int) {
        if (classifier != null) {
            LOGGER.d("Closing classifier.")
            classifier!!.close()
            classifier = null
        }
        if (device == Device.GPU && model == Model.QUANTIZED) {
            LOGGER.d("Not creating classifier: GPU doesn't support quantized models.")
            runOnUiThread {
                Toast.makeText(this, "GPU does not yet supported quantized models.", Toast.LENGTH_LONG)
                        .show()
            }
            return
        }
        try {
            LOGGER.d(
                    "Creating classifier (model=%s, device=%s, numThreads=%d)", model, device, numThreads)
            classifier = Classifier.create(this, model, device, numThreads)
        } catch (e: IOException) {
            LOGGER.e(e, "Failed to create classifier.")
        }

    }

    companion object {
        private val LOGGER = Logger()
        private val MAINTAIN_ASPECT = true
        private val DESIRED_PREVIEW_SIZE = Size(640, 480)
        private val TEXT_SIZE_DIP = 10f
    }
}
