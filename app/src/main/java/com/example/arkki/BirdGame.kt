package com.example.arkki

import android.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import org.tensorflow.lite.examples.classification.R

class BirdGame : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bird_game)

        val gameMenu = GameFragment()
        val fManager = supportFragmentManager
        val transaction = fManager?.beginTransaction()
        transaction?.replace(R.id.game_fragments, gameMenu)
        transaction?.commit()

    }
}
