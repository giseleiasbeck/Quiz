package com.example.quiz.data.model

data class QuizModel(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val time: String = "5",
    val questionList: List<QuestionModel> = emptyList()
)

data class QuestionModel(
    val question: String = "",
    val options: List<String> = emptyList(),
    val correct: String = ""
)
