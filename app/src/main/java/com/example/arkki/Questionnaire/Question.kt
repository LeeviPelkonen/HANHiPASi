package com.example.arkki.Questionnaire

enum class Type {
    IMAGE,
    TEXT
}

data class Question (val type: Type,
                val color: String,
                val question: String,
                val answers: List<String>,
                val picture: String,
                val correctAnswer: String) {

    fun correctAnswer(answer: String): Boolean {
        return answer == correctAnswer
    }
}