package com.example.arkki.Questionnaire

/*
The MIT License (MIT)

Copyright (c) 2016 Chad Song

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.example.arkki.OnFragmentInteractionListener
import kotlinx.android.synthetic.main.fragment_quiz_picture.view.*
import com.hanhipasi.luontoarkki.R


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
        val src = context?.resources?.getIdentifier(question.picture, "drawable", context?.packageName)
        view.quizPicture.setImageResource(src!!)

        setupClickListeners(view)
        setupAnimator(view)

        return view
    }

    private fun setupClickListeners(v: View) {
        v.answerA.setOnClickListener {
            outAnimation(v)
            Handler().postDelayed( {
                onButtonPressed(question.correctAnswer(v.textA.text.toString()))
            }, 500)
        }
        v.answerB.setOnClickListener {
            outAnimation(v)
            Handler().postDelayed( {
                onButtonPressed(question.correctAnswer(v.textB.text.toString()))
            }, 500)
        }
        v.answerC.setOnClickListener {
            outAnimation(v)
            Handler().postDelayed( {
                onButtonPressed(question.correctAnswer(v.textC.text.toString()))
            }, 500)
        }
        v.answerD.setOnClickListener {
            outAnimation(v)
            Handler().postDelayed( {
                onButtonPressed(question.correctAnswer(v.textD.text.toString()))
            }, 500)
        }
    }

    private fun setupAnimator(v: View) {
        inAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_left_to_right)
        buttonList.forEach {
            it.visibility = View.VISIBLE
            it.startAnimation(inAnimation)
        }
        v.quizPicture.startAnimation(inAnimation)
        v.questionTitle.startAnimation(inAnimation)
    }

    private fun outAnimation(v: View) {
        outAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_away_right)
        buttonList.forEach {
            it.startAnimation(outAnimation)
        }
        v.quizPicture.startAnimation(outAnimation)
        v.questionTitle.startAnimation(outAnimation)
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
