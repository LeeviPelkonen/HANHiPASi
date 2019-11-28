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
import com.example.arkki.R
import kotlinx.android.synthetic.main.fragment_quiz_picture.view.*


class QuizPictureFragment(private val question: Question) : Fragment() {
    private var listener: OnFragmentInteractionListener? = null
    private lateinit var inAnimation: Animation
    private lateinit var outAnimation: Animation
    private var buttonList = mutableListOf<LinearLayout>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_quiz_picture, container, false)
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
        setupAnimator(view)

        return view
    }

    private fun setupClickListeners(v: View) {
        v.answerA.setOnClickListener {
            outAnimation(v)
            Handler().postDelayed(500) {
                onButtonPressed(question.correctAnswer(v.textA.text.toString()))
            }
        }
        v.answerB.setOnClickListener {
            outAnimation(v)
            Handler().postDelayed(500) {
                onButtonPressed(question.correctAnswer(v.textB.text.toString()))
            }
        }
        v.answerC.setOnClickListener {
            outAnimation(v)
            Handler().postDelayed(500) {
                onButtonPressed(question.correctAnswer(v.textC.text.toString()))
            }
        }
        v.answerD.setOnClickListener {
            outAnimation(v)
            Handler().postDelayed(500) {
                onButtonPressed(question.correctAnswer(v.textD.text.toString()))
            }
        }
    }

    private fun setupAnimator(v: View) {
        inAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_left_to_right)
        buttonList.forEach {
            it.visibility = View.VISIBLE
            it.startAnimation(inAnimation)
        }
        v.quizPicture.startAnimation(inAnimation)
    }

    private fun outAnimation(v: View) {
        outAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_away_right)
        buttonList.forEach {
            it.startAnimation(outAnimation)
        }
        v.quizPicture.startAnimation(outAnimation)
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
