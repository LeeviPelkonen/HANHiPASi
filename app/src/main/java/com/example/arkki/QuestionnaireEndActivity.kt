package com.example.arkki

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_questionnaire_end.*
import org.tensorflow.lite.examples.classification.R

class QuestionnaireEndActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_questionnaire_end)

        val maxScore = intent.getIntExtra("MaxScore", 0)
        val score = intent.getIntExtra("Score",0)
        val percentage = (((score).toDouble() / (maxScore).toDouble()) * 100).toInt()

        if (percentage < 50 ) {
            congratulationsLabel.text = getString(R.string.pahus, percentage)
        } else {
            redeemReward.visibility = View.VISIBLE
            congratulationsLabel.text = getString(R.string.jippii, percentage)
        }

        redeemReward.setOnClickListener {

        }

        playAgainQuiz.setOnClickListener {
            val i = Intent(this, QuestionnaireActivity::class.java)
            startActivity(i)
        }

    }

    override fun onBackPressed() {
        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
    }
}
