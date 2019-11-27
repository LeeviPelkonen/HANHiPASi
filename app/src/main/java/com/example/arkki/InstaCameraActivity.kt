package com.example.arkki

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_insta_camera.*
import org.tensorflow.lite.examples.classification.R
import java.io.File

class InstaCameraActivity : AppCompatActivity() {

    val REQUEST_IMAGE_CAPTURE = 1
    var mCurrentPhotoPath: String = ""
    var imageFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        captureButton.setOnClickListener { saveImage() }
        emojiButton.setOnClickListener { applyEmoji() }
    }

    fun applyEmoji(){

    }

    fun saveImage(){
        val fileName = "temp_photo"
        val imgPath = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        imageFile = File.createTempFile(fileName, ".jpg", imgPath )
        val photoURI: Uri = FileProvider.getUriForFile(
            this,
            "com.example.arkki.fileprovider",
            imageFile!!
        )
        mCurrentPhotoPath = imageFile!!.absolutePath
        val myIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (myIntent.resolveActivity(packageManager) != null) {
            myIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(myIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, recIntent: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath)
            imageView.setImageBitmap(imageBitmap)
        }
    }
}