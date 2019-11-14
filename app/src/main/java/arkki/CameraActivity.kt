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

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.Image.Plane
import android.media.ImageReader
import android.media.ImageReader.OnImageAvailableListener
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Trace
import android.util.Log
import androidx.annotation.UiThread
import com.google.android.material.bottomsheet.BottomSheetBehavior
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.util.Size
import android.view.Surface
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import arkki.env.ImageUtils
import arkki.env.Logger
import arkki.tflite.Classifier.Device
import arkki.tflite.Classifier.Model
import arkki.tflite.Classifier.Recognition
import org.tensorflow.lite.examples.classification.R

abstract class CameraActivity : AppCompatActivity(), OnImageAvailableListener, Camera.PreviewCallback, View.OnClickListener, AdapterView.OnItemSelectedListener {
    protected var previewWidth = 0
    protected var previewHeight = 0
    private var handler: Handler? = null
    private var handlerThread: HandlerThread? = null
    private var useCamera2API: Boolean = false
    private var isProcessingFrame = false
    private val yuvBytes = arrayOfNulls<ByteArray>(3)
    private var rgbBytes: IntArray? = null
    private var luminanceStride: Int = 0
    private var postInferenceCallback: Runnable? = null
    private var imageConverter: Runnable? = null
    private var bottomSheetLayout: LinearLayout? = null
    private var gestureLayout: LinearLayout? = null
    private var sheetBehavior: BottomSheetBehavior<*>? = null
    lateinit var recognitionTextView: TextView
    lateinit var recognition1TextView: TextView
    lateinit var recognition2TextView: TextView
    lateinit var recognitionValueTextView: TextView
    lateinit var recognition1ValueTextView: TextView
    lateinit var recognition2ValueTextView: TextView
    lateinit var frameValueTextView: TextView
    lateinit var cropValueTextView: TextView
    lateinit var cameraResolutionTextView: TextView
    lateinit var rotationTextView: TextView
    lateinit var inferenceTimeTextView: TextView
    lateinit var bottomSheetArrowImageView: ImageView
    private var plusImageView: ImageView? = null
    private var minusImageView: ImageView? = null
    private var modelSpinner: Spinner? = null
    private var deviceSpinner: Spinner? = null
    private var threadsTextView: TextView? = null

    private var model = Model.QUANTIZED
    private var device = Device.CPU
    private var numThreads = -1

    protected val luminance: ByteArray?
        get() = yuvBytes[0]

    protected val screenOrientation: Int
        get() {
            when (windowManager.defaultDisplay.rotation) {
                Surface.ROTATION_270 -> return 270
                Surface.ROTATION_180 -> return 180
                Surface.ROTATION_90 -> return 90
                else -> return 0
            }
        }

    protected abstract val layoutId: Int

    protected abstract val desiredPreviewFrameSize: Size

    override fun onCreate(savedInstanceState: Bundle?) {
        LOGGER.d("onCreate $this")
        super.onCreate(null)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContentView(R.layout.activity_camera)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        if (hasPermission()) {
            setFragment()
        } else {
            requestPermission()
        }

        threadsTextView = findViewById(R.id.threads)
        plusImageView = findViewById(R.id.plus)
        minusImageView = findViewById(R.id.minus)
        modelSpinner = findViewById(R.id.model_spinner)
        deviceSpinner = findViewById(R.id.device_spinner)
        bottomSheetLayout = findViewById(R.id.bottom_sheet_layout)
        gestureLayout = findViewById(R.id.gesture_layout)
        sheetBehavior = BottomSheetBehavior.from(bottomSheetLayout!!)
        bottomSheetArrowImageView = findViewById(R.id.bottom_sheet_arrow)

        val vto = gestureLayout!!.viewTreeObserver
        vto.addOnGlobalLayoutListener(
                object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            gestureLayout!!.viewTreeObserver.removeGlobalOnLayoutListener(this)
                        } else {
                            gestureLayout!!.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        }
                        //                int width = bottomSheetLayout.getMeasuredWidth();
                        val height = gestureLayout!!.measuredHeight

                        sheetBehavior!!.peekHeight = height
                    }
                })
        sheetBehavior!!.isHideable = false

        sheetBehavior!!.setBottomSheetCallback(
                object : BottomSheetBehavior.BottomSheetCallback() {
                    @SuppressLint("SwitchIntDef")
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        when (newState) {
                            BottomSheetBehavior.STATE_HIDDEN -> {
                            }
                            BottomSheetBehavior.STATE_EXPANDED -> {
                                bottomSheetArrowImageView.setImageResource(R.drawable.icn_chevron_down)
                            }
                            BottomSheetBehavior.STATE_COLLAPSED -> {
                                bottomSheetArrowImageView.setImageResource(R.drawable.icn_chevron_up)
                            }
                            BottomSheetBehavior.STATE_DRAGGING -> {
                            }
                            BottomSheetBehavior.STATE_SETTLING -> bottomSheetArrowImageView.setImageResource(R.drawable.icn_chevron_up)
                        }
                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {}
                })

        recognitionTextView = findViewById(R.id.detected_item)
        recognitionValueTextView = findViewById(R.id.detected_item_value)
        recognition1TextView = findViewById(R.id.detected_item1)
        recognition1ValueTextView = findViewById(R.id.detected_item1_value)
        recognition2TextView = findViewById(R.id.detected_item2)
        recognition2ValueTextView = findViewById(R.id.detected_item2_value)

        frameValueTextView = findViewById(R.id.frame_info)
        cropValueTextView = findViewById(R.id.crop_info)
        cameraResolutionTextView = findViewById(R.id.view_info)
        rotationTextView = findViewById(R.id.rotation_info)
        inferenceTimeTextView = findViewById(R.id.inference_info)

        modelSpinner!!.onItemSelectedListener = this
        deviceSpinner!!.onItemSelectedListener = this

        plusImageView!!.setOnClickListener(this)
        minusImageView!!.setOnClickListener(this)

        model = Model.valueOf(modelSpinner!!.selectedItem.toString().toUpperCase())
        device = Device.valueOf(deviceSpinner!!.selectedItem.toString())
        numThreads = Integer.parseInt(threadsTextView!!.text.toString().trim { it <= ' ' })
    }

    protected fun getRgbBytes(): IntArray? {
        imageConverter!!.run()
        return rgbBytes
    }

    /** Callback for android.hardware.Camera API  */
    override fun onPreviewFrame(bytes: ByteArray, camera: Camera) {
        if (isProcessingFrame) {
            LOGGER.w("Dropping frame!")
            return
        }

        try {
            // Initialize the storage bitmaps once when the resolution is known.
            if (rgbBytes == null) {
                val previewSize = camera.parameters.previewSize
                previewHeight = previewSize.height
                previewWidth = previewSize.width
                rgbBytes = IntArray(previewWidth * previewHeight)
                onPreviewSizeChosen(Size(previewSize.width, previewSize.height), 90)
            }
        } catch (e: Exception) {
            LOGGER.e(e, "Exception!")
            return
        }

        isProcessingFrame = true
        yuvBytes[0] = bytes
        luminanceStride = previewWidth

        imageConverter = Runnable { ImageUtils.convertYUV420SPToARGB8888(bytes, previewWidth, previewHeight, rgbBytes!!) }

        postInferenceCallback = Runnable {
            camera.addCallbackBuffer(bytes)
            isProcessingFrame = false
        }
        processImage()
    }

    /** Callback for Camera2 API  */
    override fun onImageAvailable(reader: ImageReader) {
        // We need wait until we have some size from onPreviewSizeChosen
        if (previewWidth == 0 || previewHeight == 0) {
            return
        }
        if (rgbBytes == null) {
            rgbBytes = IntArray(previewWidth * previewHeight)
        }
        try {
            val image = reader.acquireLatestImage() ?: return

            if (isProcessingFrame) {
                image.close()
                return
            }
            isProcessingFrame = true
            Trace.beginSection("imageAvailable")
            val planes = image.planes
            fillBytes(planes, yuvBytes)
            luminanceStride = planes[0].rowStride
            val uvRowStride = planes[1].rowStride
            val uvPixelStride = planes[1].pixelStride

            imageConverter = Runnable {
                ImageUtils.convertYUV420ToARGB8888(
                        yuvBytes[0],
                        yuvBytes[1],
                        yuvBytes[2],
                        previewWidth,
                        previewHeight,
                        luminanceStride,
                        uvRowStride,
                        uvPixelStride,
                        rgbBytes!!)
            }

            postInferenceCallback = Runnable {
                image.close()
                isProcessingFrame = false
            }

            processImage()
        } catch (e: Exception) {
            LOGGER.e(e, "Exception!")
            Trace.endSection()
            return
        }

        Trace.endSection()
    }

    @Synchronized
    public override fun onStart() {
        LOGGER.d("onStart $this")
        super.onStart()
    }

    @Synchronized
    public override fun onResume() {
        LOGGER.d("onResume $this")
        super.onResume()

        handlerThread = HandlerThread("inference")
        handlerThread!!.start()
        handler = Handler(handlerThread!!.looper)
    }

    @Synchronized
    public override fun onPause() {
        LOGGER.d("onPause $this")

        handlerThread!!.quitSafely()
        try {
            handlerThread!!.join()
            handlerThread = null
            handler = null
        } catch (e: InterruptedException) {
            LOGGER.e(e, "Exception!")
        }

        super.onPause()
    }

    @Synchronized
    public override fun onStop() {
        LOGGER.d("onStop $this")
        super.onStop()
    }

    @Synchronized
    public override fun onDestroy() {
        LOGGER.d("onDestroy $this")
        super.onDestroy()
    }

    @Synchronized
    protected fun runInBackground(r: Runnable) {
        if (handler != null) {
            handler!!.post(r)
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSIONS_REQUEST) {
            if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setFragment()
            } else {
                requestPermission()
            }
        }
    }

    private fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkSelfPermission(PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(PERMISSION_CAMERA)) {
                Toast.makeText(
                        this@CameraActivity,
                        "Camera permission is required for this demo",
                        Toast.LENGTH_LONG)
                        .show()
            }
            requestPermissions(arrayOf(PERMISSION_CAMERA), PERMISSIONS_REQUEST)
        }
    }

    // Returns true if the device supports the required hardware level, or better.
    private fun isHardwareLevelSupported(
            characteristics: CameraCharacteristics, requiredLevel: Int): Boolean {
        val deviceLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)!!
        return if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
            requiredLevel == deviceLevel
        } else requiredLevel <= deviceLevel
        // deviceLevel is not LEGACY, can use numerical sort
    }

    private fun chooseCamera(): String? {
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            for (cameraId in manager.cameraIdList) {
                val characteristics = manager.getCameraCharacteristics(cameraId)

                // We don't use a front facing camera in this sample.
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue
                }

                val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                        ?: continue

// Fallback to camera1 API for internal cameras that don't have full support.
                // This should help with legacy situations where using the camera2 API causes
                // distorted or otherwise broken previews.
                useCamera2API = facing == CameraCharacteristics.LENS_FACING_EXTERNAL || isHardwareLevelSupported(
                        characteristics, CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL)
                LOGGER.i("Camera API lv2?: %s", useCamera2API)
                return cameraId
            }
        } catch (e: CameraAccessException) {
            LOGGER.e(e, "Not allowed to access camera")
        }

        return null
    }

    protected fun setFragment() {
        val cameraId = chooseCamera()

        val fragment: androidx.fragment.app.Fragment
        if (useCamera2API) {
            val camera2Fragment = CameraConnectionFragment.newInstance(
                    object : CameraConnectionFragment.ConnectionCallback {
                        override fun onPreviewSizeChosen(size: Size?, cameraRotation: Int) {
                            previewHeight = size!!.height
                            previewWidth = size.width
                            Log.d("dbg", "size: $size, cameraRotation: $cameraRotation, previewHeight: $previewHeight, previewWidth: $previewWidth")
                            this@CameraActivity.onPreviewSizeChosen(size, cameraRotation)
                        }
                    },
                    /*
                    CameraConnectionFragment.ConnectionCallback { size, rotation ->
                        previewHeight = size.height
                        previewWidth = size.width
                        onPreviewSizeChosen(size, rotation)
                    }
                    */
                    this,
                    layoutId,
                    desiredPreviewFrameSize)

            camera2Fragment.setCamera(cameraId)
            fragment = camera2Fragment
        } else {
            fragment = LegacyCameraConnectionFragment(this, layoutId, desiredPreviewFrameSize)
        }

        supportFragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
        //fragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
    }

    private fun fillBytes(planes: Array<Plane>, yuvBytes: Array<ByteArray?>) {
        // Because of the variable row stride it's not possible to know in
        // advance the actual necessary dimensions of the yuv planes.
        for (i in planes.indices) {
            val buffer = planes[i].buffer
            if (yuvBytes[i] == null) {
                LOGGER.d("Initializing buffer %d at size %d", i, buffer.capacity())
                yuvBytes[i] = ByteArray(buffer.capacity())
            }
            buffer.get(yuvBytes[i])
        }
    }

    protected fun readyForNextImage() {
        if (postInferenceCallback != null) {
            postInferenceCallback!!.run()
        }
    }

    @UiThread
    protected fun showResultsInBottomSheet(results: List<Recognition>?) {
        if (results != null && results.size >= 3) {
            val recognition = results[0]
            if (recognition != null) {
                if (recognition.title != null) recognitionTextView.text = recognition.title
                if (recognition.confidence != null)
                    recognitionValueTextView.setText(
                            String.format("%.2f", 100 * recognition.confidence!!) + "%")
            }

            val recognition1 = results[1]
            if (recognition1 != null) {
                if (recognition1.title != null) recognition1TextView.text = recognition1.title
                if (recognition1.confidence != null)
                    recognition1ValueTextView.setText(
                            String.format("%.2f", 100 * recognition1.confidence!!) + "%")
            }

            val recognition2 = results[2]
            if (recognition2 != null) {
                if (recognition2.title != null) recognition2TextView.text = recognition2.title
                if (recognition2.confidence != null)
                    recognition2ValueTextView.setText(
                            String.format("%.2f", 100 * recognition2.confidence!!) + "%")
            }
        }
    }

    protected fun showFrameInfo(frameInfo: String) {
        frameValueTextView.text = frameInfo
    }

    protected fun showCropInfo(cropInfo: String) {
        cropValueTextView.text = cropInfo
    }

    protected fun showCameraResolution(cameraInfo: String) {
        cameraResolutionTextView.text = previewWidth.toString() + "x" + previewHeight
    }

    protected fun showRotationInfo(rotation: String) {
        rotationTextView.text = rotation
    }

    protected fun showInference(inferenceTime: String) {
        inferenceTimeTextView.text = inferenceTime
    }

    fun getModel(): Model {
        return model
    }

    private fun setModel(model: Model) {
        if (this.model != model) {
            LOGGER.d("Updating  model: $model")
            this.model = model
            onInferenceConfigurationChanged()
        }
    }

    fun getDevice(): Device {
        return device
    }

    private fun setDevice(device: Device) {
        if (this.device != device) {
            LOGGER.d("Updating  device: $device")
            this.device = device
            val threadsEnabled = device == Device.CPU
            plusImageView!!.isEnabled = threadsEnabled
            minusImageView!!.isEnabled = threadsEnabled
            threadsTextView!!.text = if (threadsEnabled) numThreads.toString() else "N/A"
            onInferenceConfigurationChanged()
        }
    }

    fun getNumThreads(): Int {
        return numThreads
    }

    private fun setNumThreads(numThreads: Int) {
        if (this.numThreads != numThreads) {
            LOGGER.d("Updating  numThreads: $numThreads")
            this.numThreads = numThreads
            onInferenceConfigurationChanged()
        }
    }

    protected abstract fun processImage()

    protected abstract fun onPreviewSizeChosen(size: Size, rotation: Int)

    protected abstract fun onInferenceConfigurationChanged()

    override fun onClick(v: View) {
        if (v.id == R.id.plus) {
            val threads = threadsTextView!!.text.toString().trim { it <= ' ' }
            var numThreads = Integer.parseInt(threads)
            if (numThreads >= 9) return
            setNumThreads(++numThreads)
            threadsTextView!!.text = numThreads.toString()
        } else if (v.id == R.id.minus) {
            val threads = threadsTextView!!.text.toString().trim { it <= ' ' }
            var numThreads = Integer.parseInt(threads)
            if (numThreads == 1) {
                return
            }
            setNumThreads(--numThreads)
            threadsTextView!!.text = numThreads.toString()
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
        if (parent === modelSpinner) {
            setModel(Model.valueOf(parent.getItemAtPosition(pos).toString().toUpperCase()))
        } else if (parent === deviceSpinner) {
            setDevice(Device.valueOf(parent.getItemAtPosition(pos).toString()))
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // Do nothing.
    }

    companion object {
        private val LOGGER = Logger()

        private val PERMISSIONS_REQUEST = 1

        private val PERMISSION_CAMERA = Manifest.permission.CAMERA
    }
}
