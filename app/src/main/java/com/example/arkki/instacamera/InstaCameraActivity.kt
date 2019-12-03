package com.example.arkki.instacamera

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.transition.Slide
import android.transition.TransitionManager
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.graphics.get
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.arkki.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_insta_camera.*
import maes.tech.intentanim.CustomIntent
import org.jetbrains.anko.image
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.toast
import org.tensorflow.lite.examples.classification.R
import java.io.*


class InstaCameraActivity : AppCompatActivity() {

    private val REQUEST_IMAGE_CAPTURE = 1
    private var mCurrentSticker: Int = 0
    private var mCurrentPhotoPath: String = ""
    private var imageFile: File? = null
    private lateinit var navigationBar: BottomNavigationView
    private var buttonsVisible = false
    private var firstImage : Bitmap? = null
    private var secondImage: Bitmap? = null
    private var imageTaken = false
    private var xCord = 0f
    private var yCord = 0f
    private var xScreen = 0
    private var yScreen = 0
    private var xSticker = 0
    private var ySticker = 0
    private lateinit var popupWindow: PopupWindow


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insta_camera)
        getPixelSize()

        navigationBar = findViewById(R.id.bottomNavigationView)
        navigationBar.setOnNavigationItemSelectedListener {
            when (it.toString()) {
                "Koti" -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    CustomIntent.customType(this, "up-to-bottom")
                }
                "Linnut" -> {
                    val intent = Intent(this, ClassifierActivity::class.java)
                    startActivity(intent)
                    CustomIntent.customType(this, "up-to-bottom")
                }
                "Peli" -> {
                    val intent = Intent(this, BirdGame::class.java)
                    startActivity(intent)
                    CustomIntent.customType(this, "up-to-bottom")
                }
                "Trivia" -> {
                    Log.d("dbg", "trivia")
                    val intent = Intent(this, QuestionnaireActivity::class.java)
                    startActivity(intent)
                    CustomIntent.customType(this, "up-to-bottom")
                }
                "Kamera" -> {
                    Log.d("dbg", "kamera")
                }

            }

            Log.d("dbg", "$it")

            return@setOnNavigationItemSelectedListener true
        }

        if (hasPermission()) {
            saveImage()
        } else {
            requestPermission()
        }

        captureButton.setOnClickListener { saveImage() }
        emojiButton.setOnClickListener { popUp() }
        plusButton.setOnClickListener { openButtons() }
        shareButton.setOnClickListener { shareImage() }

        //tracking the movement for the sticker
        imageView.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    /*
                    calculating the stickers location
                    the middle of the sticker               (xSticker / 2)
                    the ratio of image to screen size       (firstImage!!.width.toFloat()) / xScreen)
                    taking raw input for the movement        event.rawX
                    */
                    xCord = (event.rawX * (firstImage!!.width.toFloat()) / xScreen) - (xSticker/2)
                    yCord = (event.rawY * (firstImage!!.height.toFloat()) / yScreen) - (ySticker/2)
                    return@setOnTouchListener true
                }
                MotionEvent.ACTION_UP -> {
                    if(imageTaken){
                        saveStickerImage(mCurrentSticker)
                    }
                    return@setOnTouchListener true
                }
            }
            v?.onTouchEvent(event) ?: true
        }
    }

    //draws the bitmaps on top of each other
    //firstImage = camera image, secondImage = sticker
    private fun applyBirdToImage(firstImage: Bitmap, secondImagePath: Int): Bitmap? {
        val secondImage = BitmapFactory.decodeResource(applicationContext.resources, secondImagePath)

        /*
        calculating the stickers location
        taking the size of the sticker                       secondImage.width
        scaling to size of the first image on screen        ((firstImage!!.width.toFloat()) / xScreen)
        */
        xSticker = (secondImage.width / 2 * (firstImage!!.width.toFloat()) / xScreen).toInt()
        ySticker = (secondImage.height / 2 * (firstImage!!.height.toFloat()) / yScreen).toInt()
        val result = Bitmap.createBitmap(firstImage.width, firstImage.height, firstImage.config)
        val canvas = Canvas(result)
        canvas.drawBitmap(firstImage, 0f, 0f, null)
        canvas.drawBitmap(secondImage, xCord, yCord, null)
        return result
    }

    private fun getPixelSize(){
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        xScreen = displayMetrics.widthPixels
        yScreen = displayMetrics.heightPixels
        xCord = xScreen / 2f
        yCord = yScreen / 2f
    }
    
    private fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkSelfPermission(CameraActivity.PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(CameraActivity.PERMISSION_CAMERA)) {
                Toast.makeText(
                    this,
                    "Kameran käyttöoikeus vaaditaan sovellusta varten.",
                    Toast.LENGTH_LONG)
                    .show()
            }
            requestPermissions(arrayOf(CameraActivity.PERMISSION_CAMERA), CameraActivity.PERMISSIONS_REQUEST)
        }
    }

    //sharing the image to selected media source
    private fun shareImage(){
        if(imageTaken){
            val bmpUri = FileProvider.getUriForFile(this, "org.tensorflow.lite.examples.classification.fileprovider", imageFile!!)
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.putExtra(Intent.EXTRA_STREAM, bmpUri)
            intent.type = "image/jpeg"
            startActivity(Intent.createChooser(intent,"Jaa kuva"))
        }else{
            Toast.makeText(
                this,
                "Ota kuva ensin",
                Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun openButtons(){
        if (buttonsVisible){
            buttonsVisible = false
            captureButton.visibility = View.GONE
            emojiButton.visibility = View.GONE
            shareButton.visibility = View.GONE
            plusButton.setImageResource(R.drawable.ic_keyboard_arrow_right_brown_48dp)
        }else {
            buttonsVisible = true
            captureButton.visibility = View.VISIBLE
            emojiButton.visibility = View.VISIBLE
            shareButton.visibility = View.VISIBLE
            plusButton.setImageResource(R.drawable.ic_keyboard_arrow_left_brown_48dp)
        }
    }

    //applying sticker to the picture
    fun applyImage(img:Int){
        popupWindow.dismiss()
        mCurrentSticker = img
        if(imageTaken){
            Log.d("testing","applying bird")
            saveStickerImage(img)
        }else{
            Toast.makeText(
                this,
                "Ota kuva ensin",
                Toast.LENGTH_SHORT)
                .show()
        }
    }

    //getting image with sticker and saving it
    private fun saveStickerImage(img: Int){
        val image = applyBirdToImage(firstImage!!,img)
        val fileName = "temp_photo"
        val imgPath = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File.createTempFile(fileName, ".jpg", imgPath )

        try {
            // Get the file output stream
            val stream: OutputStream = FileOutputStream(file)

            // Compress the bitmap
            image!!.compress(Bitmap.CompressFormat.JPEG, 100, stream)

            stream.flush()
            stream.close()
        } catch (e: IOException){ // Catch the exception
            e.printStackTrace()
        }
        imageFile = file
        mCurrentPhotoPath = file.absolutePath
        val imageBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath)
        imageView.setImageBitmap(imageBitmap)
    }

    //taking and saving the image
    private fun saveImage(){
        val fileName = "temp_photo"
        val imgPath = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        imageFile = File.createTempFile(fileName, ".jpg", imgPath )
        val photoURI: Uri = FileProvider.getUriForFile(
            this,
            "org.tensorflow.lite.examples.classification.fileprovider",
            imageFile!!
        )
        mCurrentPhotoPath = imageFile!!.absolutePath
        val myIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (myIntent.resolveActivity(packageManager) != null) {
            myIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(myIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun popUp(){
        val inflater: LayoutInflater = layoutInflater
        val view = inflater.inflate(R.layout.popup_insta_camera,null)

        popupWindow = PopupWindow(
            view,
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        popupWindow.elevation = 10.0F
        val slideIn = Slide()
        slideIn.slideEdge = Gravity.TOP
        popupWindow.enterTransition = slideIn

        val slideOut = Slide()
        slideOut.slideEdge = Gravity.END
        popupWindow.exitTransition = slideOut

        val recyclerM = view.findViewById<RecyclerView>(R.id.recyclerMain)
        recyclerM.layoutManager = LinearLayoutManager(this)

        val itemList = mutableListOf<Bird>()
        itemList.add(Bird(R.drawable.bird_temp,"Lintu"))
        itemList.add(Bird(R.drawable.chiken,"Kana"))
        itemList.add(Bird(R.drawable.bird_temp,"Lintu"))
        itemList.add(Bird(R.drawable.chiken,"Kana"))
        recyclerM.adapter = InstaCameraAdapter(itemList,this)

        view.setOnClickListener {
            popupWindow.dismiss()
        }

        // show the popup window on app
        TransitionManager.beginDelayedTransition(insta_camera_layout)
        popupWindow.showAtLocation(
            insta_camera_layout, // Location to display popup window
            Gravity.CENTER, // Exact position of layout to display popup
            0, // X offset
            0 // Y offset
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, recIntent: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath)
            imageView.setImageBitmap(imageBitmap)
            secondImage = firstImage
            firstImage = imageBitmap
            imageTaken = true
        }
    }
}