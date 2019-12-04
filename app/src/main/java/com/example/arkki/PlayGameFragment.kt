package com.example.arkki

import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import java.lang.Exception
import java.util.*
import kotlin.math.roundToInt
import com.hanhipasi.luontoarkki.R

class PlayGameFragment : Fragment() {

    var time = 30
    var score = 0
    val gridList = arrayListOf<ImageView>()
    var birdToLookFor = ""
    lateinit var countDownTime: CountDownTimer


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_play_game, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Here are all ImageViews in the existing grid
        val wholeGrid = activity?.findViewById<GridLayout>(R.id.imageGrid)
        val timer = activity?.findViewById<TextView>(R.id.timer)
        val scoreText = activity?.findViewById<TextView>(R.id.score)
        scoreText?.text = score.toString()
        val curBird = activity?.findViewById<TextView>(R.id.currentBird)

        for (index in 0 until (wholeGrid)!!.childCount)
        {
            val nextChild = (wholeGrid).getChildAt(index) as ImageView
            gridList.add(nextChild)

        }
        Log.d("xdlsd", gridList.toString())
        curBird?.text = changeCurrentBird()

        for (r in gridList) {
            changePictures(r)
        }

        for (i in gridList) {
            i.isClickable
            i.setOnClickListener {
                giveScore(birdToLookFor, i)
                curBird?.text = changeCurrentBird()
                scoreText?.text = "Score: " + score.toString()
                for (e in gridList) {
                    changePictures(e)
                }
                Log.d("xdlsd", i.tag.toString())
            }

        }


        // Timer that ticks down as the player plays
        countDownTime = createTimer(timer, curBird).start()


        performTick(30000, timer)
    }

    //Change pictures shown for the player
    private fun changePictures(img: ImageView) {
        val imageArray: TypedArray = resources.obtainTypedArray(R.array.images)
        val imgID = Random().nextInt(imageArray.length())
        try {
            val getRes = imageArray.getResourceId(imgID, 0)
            img.setImageResource(getRes)
            Log.d("xdlsd", "Now all images changed")

                img.tag = imgID

        } catch (e: Exception) {
            Log.e("pictureError", e.toString())
        }
        imageArray.recycle()
    }

    // Change the current bird that gives points
    private fun changeCurrentBird(): String {
        val birds = arrayOf("Punasotka", "Ristisorsa", "Nokikana", "Lapasorsa")
        val randomIndex = Random().nextInt(birds.size)
        birdToLookFor = birds[randomIndex]
        Log.d("xdlsd", "CurrentBird to look for changed")
        return birds[randomIndex]
    }

    // Give points if the player presses the right bird
    private fun giveScore(clickedBird: String, clickedImageTag: ImageView) {
        val birdNameID = mapOf(
            "Punasotka" to 0,
            "Ristisorsa" to 1,
            "Nokikana" to 2,
            "Lapasorsa" to 3
        )

        if (birdNameID[clickedBird] === clickedImageTag.tag) {
            score++
            Log.d("score", "Bird was: $clickedBird, and clickedImage was ${clickedImageTag.tag} score is now: $score")
        }
    }

    private fun performTick(millisUntilFinished: Long, timer: TextView?) {
        timer?.text = (((millisUntilFinished * 0.001f).roundToInt()).toString())
    }

    private fun createTimer(timer: TextView?, curBird: TextView?): CountDownTimer {
        val cTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                performTick(millisUntilFinished, timer)
                timer?.text = "Time left: " + time.toString()
                time--
            }

            override fun onFinish() {
                timer?.text = getString(R.string.time_up)
                timer?.textSize = 60.0F
                for (i in gridList) {
                    i.isClickable = false
                }
                curBird?.text = ""
            }
        }
        return cTimer
    }

    override fun onStop() {
        super.onStop()
        if (countDownTime != null) {
            countDownTime.cancel()
        }
    }
}
