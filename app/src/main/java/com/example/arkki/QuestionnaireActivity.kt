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
import maes.tech.intentanim.CustomIntent
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



        list.add(Question(Type.TEXT, "#234234", "Harmaahaikara pyydystää pääasiassa:", mutableListOf("Matoja ja toukkia", "Kaloja ja sammakoita", "Pikkulintuja", "Leijonia"), "no", "Kaloja ja sammakoita"))
        list.add(Question(Type.IMAGE, "#234234", "Mikä lintu esiintyy kuvassa?", mutableListOf("Kurki", "Ristisorsa", "Punasotka", "Punarinta"), "punasotka", "Punasotka"))

        list.add(Question(Type.TEXT, "#234234", "Kaulushaikaran huuto muistuttaa eniten:", mutableListOf("Kiljumista", "Torven soittoa", "Pulloon puhaltamista", "Ulvontaa"), "no", "Pulloon puhaltamista"))
        list.add(Question(Type.IMAGE, "#234234", "Mikä lintu esiintyy kuvassa?", mutableListOf("Lapasorsa", "Punasotka", "Ristisorsa", "Taivaanvuohi"), "ristisorsa", "Ristisorsa"))

        list.add(Question(Type.TEXT, "#234234", "Mikä on Pohjois-Pohjanmaan maakuntalintu?", mutableListOf("Kurki", "Ruokki", "Ruskosuohaukka", "Lapasorsa"), "no", "Kurki"))
        list.add(Question(Type.IMAGE, "#234234", "Mikä lintu esiintyy kuvassa?", mutableListOf("Ruisrääkkä", "Ruskosuohaukka", "Kurki", "Harmaahaikara"), "ruskosuohaukka", "Ruskosuohaukka"))

        list.add(Question(Type.TEXT, "#234234", "Missä maastossa nokikana viihtyy?", mutableListOf("Vesistöissä", "Puiden latvoissa", "Heinikossa", "Hiekkarannoilla"), "no", "Vesistöissä"))
        list.add(Question(Type.IMAGE, "#234234", "Mikä lintu esiintyy kuvassa?", mutableListOf("Taivaanvuohi", "Ruokki", "Nokikana", "Kaulushaikara"), "taivaanvuohi", "Taivaanvuohi"))

        questionNumberLabel.text = getString(R.string.QuestionNumber, questionNumber, list.size)

        nextQuestion()

    }

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
