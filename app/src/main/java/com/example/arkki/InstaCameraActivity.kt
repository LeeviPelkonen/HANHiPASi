package com.example.arkki

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_insta_camera.*
import maes.tech.intentanim.CustomIntent
import org.tensorflow.lite.examples.classification.R
import java.io.File

class InstaCameraActivity : AppCompatActivity() {

    val REQUEST_IMAGE_CAPTURE = 1
    var mCurrentPhotoPath: String = ""
    var imageFile: File? = null
    private lateinit var navigationBar: BottomNavigationView
    private var buttonsVisible = false;

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
        emojiButton.setOnClickListener { applyEmoji() }
        plusButton.setOnClickListener { openButtons() }
        shareButton.setOnClickListener { shareImage() }
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

    fun shareImage(){

    }

    fun openButtons(){
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

    fun applyEmoji(){

    }

    fun saveImage(){
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, recIntent: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath)
            imageView.setImageBitmap(imageBitmap)
        }
    }
}