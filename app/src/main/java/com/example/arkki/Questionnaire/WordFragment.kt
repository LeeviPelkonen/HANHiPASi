package com.example.arkki.Questionnaire

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.core.os.postDelayed
import androidx.fragment.app.Fragment
import com.example.arkki.OnFragmentInteractionListener
import kotlinx.android.synthetic.main.fragment_word.view.*
import org.tensorflow.lite.examples.classification.R


class WordFragment(private val question: Question) : Fragment() {
    private var listener: OnFragmentInteractionListener? = null
    private lateinit var inAnimation: Animation
    private lateinit var outAnimation: Animation
    private var buttonList = mutableListOf<LinearLayout>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_word, container, false)
        // Initialize texts
        view.questionTitle.text = question.question
        view.textA.text = question.answers[0]
        view.textB.text = question.answers[1]
        view.textC.text = question.answers[2]
        view.textD.text = question.answers[3]
        buttonList.add(view.answerA)
        buttonList.add(view.answerB)
        buttonList.add(view.answerC)
        buttonList.add(view.answerD)

        setupClickListeners(view)
        setupAnimator()
        //outAnimation(view)

        return view
    }

    private fun setupClickListeners(v: View) {
        v.answerA.setOnClickListener {
            outAnimation()
            Handler().postDelayed({
                onButtonPressed(question.correctAnswer(v.textA.text.toString()))
            }, 500)
        }
        v.answerB.setOnClickListener {
            outAnimation()
            Handler().postDelayed({
                onButtonPressed(question.correctAnswer(v.textB.text.toString()))
            }, 500)
        }
        v.answerC.setOnClickListener {
            outAnimation()
            Handler().postDelayed({
                onButtonPressed(question.correctAnswer(v.textC.text.toString()))
            }, 500)
        }
        v.answerD.setOnClickListener {
            outAnimation()

            Handler().postDelayed({
                onButtonPressed(question.correctAnswer(v.textD.text.toString()))
            }, 500)
        }
    }

    private fun setupAnimator() {
        inAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_left_to_right)
        buttonList.forEach {
            it.visibility = View.VISIBLE
            it.startAnimation(inAnimation)
        }
    }

    private fun outAnimation() {
        outAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_away_right)
        buttonList.forEach {
            it.startAnimation(outAnimation)
        }
    }

    private fun onButtonPressed(correct: Boolean) {
        listener?.onFragmentInteraction(correct)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        }

    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }



}
