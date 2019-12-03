package com.example.arkki

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import maes.tech.intentanim.CustomIntent.customType
import org.tensorflow.lite.examples.classification.R

class MainActivity : AppCompatActivity() {

    private lateinit var navigationBar: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigationBar = findViewById(R.id.bottomNavigationView)
        navigationBar.setOnNavigationItemSelectedListener {
            when (it.toString()) {
                "Koti" -> {
                    Log.d("dbg", "koti")
                }
                "Linnut" -> {
                    val intent = Intent(this, ClassifierActivity::class.java)
                    startActivity(intent)
                    customType(this, "bottom-to-up")
                }
                "Peli" -> {
                    Log.d("dbg", "peli")
                }
                "Trivia" -> {
                    Log.d("dbg", "trivia")
                    val intent = Intent(this, QuestionnaireActivity::class.java)
                    startActivity(intent)
                    customType(this, "bottom-to-up")
                }
                "Kamera" -> {
                    Log.d("dbg", "kamera123")
                    val intent = Intent(this, InstaCameraActivity::class.java)
                    startActivity(intent)
                    customType(this, "bottom-to-up")
                }
            }

            Log.d("dbg", "$it")

            return@setOnNavigationItemSelectedListener true
        }
    }

    override fun onResume() {
        super.onResume()
        navigationBar.selectedItemId = R.id.action_home
    }

}
