package com.example.arkki

import android.animation.ValueAnimator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.core.os.postDelayed
import com.example.arkki.Questionnaire.Question
import com.example.arkki.Questionnaire.QuizPictureFragment
import com.example.arkki.Questionnaire.Type
import com.example.arkki.Questionnaire.WordFragment
import kotlinx.android.synthetic.main.activity_questionnaire.*
import org.tensorflow.lite.examples.classification.R

interface OnFragmentInteractionListener {
    // TODO: Update argument type and name
    fun onFragmentInteraction(correct: Boolean)
}

class QuestionnaireActivity : AppCompatActivity(), OnFragmentInteractionListener {

    private val list = mutableListOf<Question>()
    private var score = 0
    private var questionNumber: Int = 1
    private var questionNumberInternal: Int = 0

    override fun onFragmentInteraction(correct: Boolean) {
        Log.d("Correct answer", correct.toString())
        if (correct) {
            score++
            Log.d("Score", score.toString())

        }
        if (questionNumber < list.size) {
            questionNumber++
        }
        questionNumberInternal++
        questionNumberLabel.text = getString(R.string.QuestionNumber, questionNumber, list.size)
        val progress = ((questionNumberInternal.toDouble() / list.size.toDouble()) * 100).toInt()
        val animator = ValueAnimator.ofInt(progressBar.progress, progress)

        animator.apply {
            duration = 1000
            addUpdateListener {
                progressBar.progress = it.animatedValue as Int
            }
            start()
        }
        nextQuestion()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_questionnaire)


        val q = Question(Type.TEXT, "#234234", "Kuinka monta kissaeläintä museossa on esillä?", mutableListOf("3", "5", "4", "2"), "no", "4")
        val a = Question(Type.IMAGE, "#234234", "Mikä lintu esiintyy kuvassa?", mutableListOf("Kurki", "Tiainen", "Punarinta", "Haukka"), "no", "Haukka")
        val b = Question(Type.TEXT, "#234234", "Kuinka monta hammasta löytyy karhun suusta?", mutableListOf("24", "14", "16", "32"), "no", "32")
        val d = Question(Type.IMAGE, "#234234", "Mikä nisäkäs löytyy kuvasta?", mutableListOf("Ilves", "Tiikeri", "Karhu", "Susi"), "no", "Ilves")
        list.add(q)
        list.add(a)
        list.add(b)
        list.add(d)
        questionNumberLabel.text = getString(R.string.QuestionNumber, questionNumber, list.size)

        nextQuestion()

    }

//    private fun setupAnimators() {
//        valueAnimator = ValueAnimator.ofFloat(scoreText.textSize, (scoreText.textSize * 1.4).toFloat()).apply {
//            duration = 300
//            repeatCount = 1
//            repeatMode = ValueAnimator.REVERSE
//            addUpdateListener {
//                val value = it.animatedValue as Float
//                scoreText.textSize = value
//            }
//        }
//    }

    private fun nextQuestion() {
        try {
            val fm = supportFragmentManager
            val transaction = fm.beginTransaction()
            if (list[questionNumberInternal].type == Type.TEXT) {
                transaction.replace(R.id.fragmentHolder, WordFragment(list[questionNumberInternal]))
                    .commit()
            } else {
                transaction.replace(R.id.fragmentHolder, QuizPictureFragment(list[questionNumberInternal]))
                    .commit()
            }
        } catch (e: IndexOutOfBoundsException) {
            showScore()
        }

    }

    private fun showScore() {
            Handler().postDelayed( {
                finish()
                val i = Intent(this, QuestionnaireEndActivity::class.java)
                i.putExtra("MaxScore", list.size)
                i.putExtra("Score", score)
                startActivity(i)
            }, 500)
    }
}
