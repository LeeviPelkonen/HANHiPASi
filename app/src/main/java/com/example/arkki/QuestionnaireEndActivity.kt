package com.example.arkki


/*
ISC License

Copyright (c) 2017 Dion Segijn

Permission to use, copy, modify, and/or distribute this software for any
purpose with or without fee is hereby granted, provided that the above
copyright notice and this permission notice appear in all copies.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_questionnaire_end.*
import maes.tech.intentanim.CustomIntent
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
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
                viewKonfetti.build()
                        .addColors(Color.YELLOW, Color.BLUE, Color.RED)
                        .setDirection(0.0, 359.0)
                        .setSpeed(1f, 5f)
                        .setFadeOutEnabled(true)
                        .setTimeToLive(2000L)
                        .addShapes(Shape.RECT, Shape.CIRCLE)
                        .addSizes(Size(12))
                        .setPosition((viewKonfetti.width / 2) - 50f, (viewKonfetti.width / 2) + 80f, -50f, -50f)
                        .streamFor(300, 5000L)
            }
            100 -> {
                redeemReward.visibility = View.VISIBLE
                congratulationsLabel.text = getString(R.string.kaikkioikein)
                starImage.setShapeResource(R.drawable.ic_star_black_24dp)
                viewKonfetti.build()
                        .addColors(Color.YELLOW, Color.BLUE, Color.RED)
                        .setDirection(0.0, 359.0)
                        .setSpeed(1f, 5f)
                        .setFadeOutEnabled(true)
                        .setTimeToLive(2000L)
                        .addShapes(Shape.RECT, Shape.CIRCLE)
                        .addSizes(Size(12))
                        .setPosition((viewKonfetti.width / 2) - 50f, (viewKonfetti.width / 2) + 80f, -50f, -50f)
                        .streamFor(300, 5000L)
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

        quizBackButton.setOnClickListener {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            CustomIntent.customType(this, "up-to-bottom")
        }

    }

    override fun onBackPressed() {
        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
    }
}
