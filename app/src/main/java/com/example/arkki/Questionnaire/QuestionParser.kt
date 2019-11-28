package com.example.arkki.Questionnaire

import android.util.Log
import com.google.gson.Gson

class QuestionParser(var json: String) {

    fun parseJson()/*: Array<Question>*/ {
        val gson = Gson()
        val q = gson.fromJson(json, Question::class.java)
        Log.d("Test", gson.toString())


    }
}



