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
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.arkki.CameraActivity
import com.example.arkki.MainActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_insta_camera.*
import maes.tech.intentanim.CustomIntent
import org.jetbrains.anko.image
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.toast
import org.tensorflow.lite.examples.classification.R
import java.io.*


class InstaCameraActivity : AppCompatActivity() {

    val REQUEST_IMAGE_CAPTURE = 1
    var mCurrentSticker: Int = 0
    var mCurrentPhotoPath: String = ""
    var imageFile: File? = null
    private lateinit var navigationBar: BottomNavigationView
    private var buttonsVisible = false
    var firstImage : Bitmap? = null
    var secondImage: Bitmap? = null
    var imageTaken = false
    var xCord = 0f
    var yCord = 0f
    //var xVal = 1f
    //var yVal = 1f

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insta_camera)

        navigationBar = findViewById(R.id.bottomNavigationView)
        navigationBar.setOnNavigationItemSelectedListener {
            when (it.toString()) {
                "Koti" -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    CustomIntent.customType(this, "up-to-bottom")
                }
                "Linnut" -> {
                    Log.d("dbg", "linnut")
                }
                "Peli" -> {
                    Log.d("dbg", "peli")
                }
                "Trivia" -> {
                    Log.d("dbg", "trivia")
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

        //moving the sticker
        stickerImageView.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    xCord = stickerImageView.x - event.rawX
                    yCord = stickerImageView.y - event.rawY
                    Log.d("testing", "--------------------------------")
                    Log.d("testing", "x = $xCord")
                    Log.d("testing", "y = $yCord")
                    return@setOnTouchListener true
                }
                MotionEvent.ACTION_UP -> {
                    Log.d("testing","Action up")
                    stickerImageView.animate().x(event.rawX + xCord).y(event.rawY + yCord)
                    return@setOnTouchListener true
                }

            }
            v?.onTouchEvent(event) ?: true
        }
    }

    //draws the bitmaps on top of each other
    private fun applyBirdToImage(firstImage: Bitmap, secondImagePath: Int): Bitmap? {
        val secondImage = BitmapFactory.decodeResource(applicationContext.resources, secondImagePath)
        var xVal = secondImage.width / firstImage.width.toFloat() //try with screen width
        var yVal = secondImage.height / firstImage.height.toFloat()
        yVal = 0.15f
        val result = Bitmap.createBitmap(firstImage.width, firstImage.height, firstImage.config)
        val canvas = Canvas(result)
        canvas.drawBitmap(firstImage, 0f, 0f, null)
        canvas.drawBitmap(secondImage, (stickerImageView.x / xVal +(secondImage.width * xVal)), (stickerImageView.y / xVal +(secondImage.height * xVal)), null)
        Log.d("testing", "x = " + -stickerImageView.x + "  width = " + secondImage.width + "  xVal = " + stickerImageView.measuredWidth)
        Log.d("testing", "y = " + -stickerImageView.y + "  height = " + secondImage.height + "  XVal = " + xVal)
        return result
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
                    "Camera permission is required for this demo",
                    Toast.LENGTH_LONG)
                    .show()
            }
            requestPermissions(arrayOf(CameraActivity.PERMISSION_CAMERA), CameraActivity.PERMISSIONS_REQUEST)
        }
    }

    private fun shareImage(){
        if(imageFile != null){
            if(mCurrentSticker != 0){
                val image = applyBirdToImage(firstImage!!,mCurrentSticker)
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
            val bmpUri = FileProvider.getUriForFile(this, "org.tensorflow.lite.examples.classification.fileprovider", imageFile!!)
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.putExtra(Intent.EXTRA_STREAM, bmpUri)
            intent.type = "image/jpeg"

            startActivity(Intent.createChooser(intent,"Jaa kuva"))
        }else{
            Log.d("testing","imagefile is null!")
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

    fun applyImage(img:Int){
        mCurrentSticker = img
        stickerImageView.setImageResource(img)
    }

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
        // Initialize a new layout inflater instance
        val inflater: LayoutInflater = layoutInflater

        // Inflate a custom view using layout inflater
        val view = inflater.inflate(R.layout.popup_insta_camera,null)

        // Initialize a new instance of popup window
        val popupWindow = PopupWindow(
            view, // Custom view to show in popup window
            LinearLayout.LayoutParams.MATCH_PARENT, // Width of popup window
            LinearLayout.LayoutParams.WRAP_CONTENT // Window height
        )

        // Set an elevation for the popup window
        popupWindow.elevation = 10.0F
        // Create a new slide animation for popup window enter transition
        val slideIn = Slide()
        slideIn.slideEdge = Gravity.TOP
        popupWindow.enterTransition = slideIn

        // Slide animation for popup window exit transition
        val slideOut = Slide()
        slideOut.slideEdge = Gravity.END
        popupWindow.exitTransition = slideOut


        // Get the widgets reference from custom view
        val recyclerM = view.findViewById<RecyclerView>(R.id.recyclerMain)

        recyclerM.layoutManager = LinearLayoutManager(this)

        val itemList = mutableListOf<Bird>()
        itemList.add(Bird(R.drawable.bird_temp,"Lintu"))
        itemList.add(Bird(R.drawable.bird_temp,"Lintu2"))
        itemList.add(Bird(R.drawable.bird_temp,"Lintu3"))
        itemList.add(Bird(R.drawable.bird_temp,"Lintu4"))
        recyclerM.adapter = InstaCameraAdapter(itemList,this)

        view.setOnClickListener {
            popupWindow.dismiss()
        }
        recyclerM.setOnClickListener {
            popupWindow.dismiss()
        }


        // Finally, show the popup window on app
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