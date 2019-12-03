package com.example.arkki

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_questionnaire_end.*
import org.tensorflow.lite.examples.classification.R

class QuestionnaireEndActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_questionnaire_end)


        val valueAnimator = ValueAnimator.ofFloat(1f, 32.2f).apply {
            duration = 1800
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
        }
        valueAnimator.addUpdateListener {
            val value = it.animatedValue as Float
            starImage.translationY = value
        }
        val maxScore = intent.getIntExtra("MaxScore", 0)
        val score = intent.getIntExtra("Score", 0)

        when (val percentage = (((score).toDouble() / (maxScore).toDouble()) * 100).toInt()) {
            in 0..25 -> {
                congratulationsLabel.text = getString(R.string.kaikkivaarin)
                starImage.setShapeResource(R.drawable.ic_sad_24dp)
            }
            in 26..49 -> {
                congratulationsLabel.text = getString(R.string.pahus, percentage)
                starImage.setShapeResource(R.drawable.ic_sad_24dp)
            }
            in 50..70 -> {
                congratulationsLabel.text = getString(R.string.laheltapiti, percentage)
                starImage.setShapeResource(R.drawable.ic_sad_24dp)
            }
            in 71..99 -> {
                redeemReward.visibility = View.VISIBLE
                congratulationsLabel.text = getString(R.string.jippii, percentage)
                starImage.setShapeResource(R.drawable.ic_star_black_24dp)
            }
            100 -> {
                redeemReward.visibility = View.VISIBLE
                congratulationsLabel.text = getString(R.string.kaikkioikein)
                starImage.setShapeResource(R.drawable.ic_star_black_24dp)
            }
        }
        Handler().postDelayed({
            starImage.performClick()
            starImage.isEnabled = false
            valueAnimator.start()
        }, 350)

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
