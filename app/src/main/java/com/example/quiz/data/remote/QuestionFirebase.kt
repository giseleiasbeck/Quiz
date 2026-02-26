package com.example.quiz.data.remote

data class QuestionFirebase(
    var questionText: String = "",
    var optionA: String = "",
    var optionB: String = "",
    var optionC: String = "",
    var optionD: String = "",
    var correctOptionIndex: Int = 0,
    var category: String = "Geral"
)
