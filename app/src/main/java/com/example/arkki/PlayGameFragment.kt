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
import android.widget.ImageView
import android.widget.TextView
import java.lang.Exception
import java.util.*
import kotlin.math.roundToInt
import org.tensorflow.lite.examples.classification.R

class PlayGameFragment : Fragment() {

    var time = 30
    var score = 0


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
        val firstGrid = activity?.findViewById<ImageView>(R.id.game_img_1)
        val grid2 = activity?.findViewById<ImageView>(R.id.game_img_2)
        val grid3 = activity?.findViewById<ImageView>(R.id.game_img_3)
        val grid4 = activity?.findViewById<ImageView>(R.id.game_img_4)
        val grid5 = activity?.findViewById<ImageView>(R.id.game_img_5)
        val grid6 = activity?.findViewById<ImageView>(R.id.game_img_6)
        val grid7 = activity?.findViewById<ImageView>(R.id.game_img_7)
        val grid8 = activity?.findViewById<ImageView>(R.id.game_img_8)
        val grid9 = activity?.findViewById<ImageView>(R.id.game_img_9)
        val grid10 = activity?.findViewById<ImageView>(R.id.game_img_10)
        val grid11 = activity?.findViewById<ImageView>(R.id.game_img_11)
        val grid12 = activity?.findViewById<ImageView>(R.id.game_img_12)
        val grid13 = activity?.findViewById<ImageView>(R.id.game_img_13)
        val grid14 = activity?.findViewById<ImageView>(R.id.game_img_14)
        val grid15 = activity?.findViewById<ImageView>(R.id.game_img_15)
        val grid16 = activity?.findViewById<ImageView>(R.id.game_img_16)
        val allGrids = arrayOf(
            firstGrid,
            grid2,
            grid3,
            grid4,
            grid5,
            grid6,
            grid7,
            grid8,
            grid9,
            grid10,
            grid11,
            grid12,
            grid13,
            grid14,
            grid15,
            grid16
        )

        val timer = activity?.findViewById<TextView>(R.id.timer)
        val scoreText = activity?.findViewById<TextView>(R.id.score)
        scoreText?.text = score.toString()
        val curBird = activity?.findViewById<TextView>(R.id.currentBird)
        val birdVal = mapOf<Int?,Int?>(
            0 to 0,
            1 to 1,
            2 to 2,
            3 to 3
        )

        for (r in allGrids) {
            if(r != null) {
                changePictures(r)
            }
        }


        for (i in allGrids) {
            i?.isClickable
            i?.setOnClickListener {
                giveScore(curBird?.text.toString(), 0)
                curBird?.text = changeCurrentBird()
                for (e in allGrids) {
                    if (e != null) {
                        changePictures(e)
                    }
                }
                scoreText?.text = score.toString()
            }

        }


        // Timer that ticks down as the player plays
        fun performTick(millisUntilFinished: Long) {
            timer?.text = (((millisUntilFinished * 0.001f).roundToInt()).toString())
        }

        val cTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                performTick(millisUntilFinished)
                timer?.text = time.toString()
                time--
            }

            override fun onFinish() {
                timer?.text = getString(R.string.time_up)
                for (i in allGrids) {
                    i?.isClickable = false
                }
                curBird?.text = ""
            }
        }.start()
        performTick(30000)
    }

    //Change pictures shown for the player
    private fun changePictures(img: ImageView): Int {
        val imageArray: TypedArray = resources.obtainTypedArray(R.array.images)
        val imgID = Random().nextInt(imageArray.length())
        try {
            val getRes = imageArray.getResourceId(imgID, 0)
            Log.d("score", imageArray.getResourceId(imgID, 0).toString())
            img.setImageResource(getRes)
        } catch (e: Exception) {
            Log.d("score", e.toString())
        }
        imageArray.recycle()
        return imgID
    }

    // Change the current bird that gives points
    private fun changeCurrentBird(): String {
        val birds = arrayOf("bird1", "bird2", "bird3", "bird4")
        val randomIndex = Random().nextInt(birds.size)
        return birds[randomIndex]
    }

    // Give points if the player presses the right bird
    private fun giveScore(bird: String, birdID: Int) {
        val imageArray = resources.obtainTypedArray(R.array.images)
        val birdNameID = mapOf(
            "bird1" to 2131165272,
            "bird2" to 2131165269,
            "bird3" to 2131165270,
            "bird4" to 2131165271
        )

        if (birdNameID[bird] == imageArray.getResourceId(birdID, 0)) {
            score++
            Log.d("score", "Added one score, score is now: $score")
        }
        imageArray.recycle()
    }
}
