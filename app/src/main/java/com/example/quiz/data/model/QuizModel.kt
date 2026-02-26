package com.example.quiz.data.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class QuizModel(
    @get:Exclude var id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val time: String = "5",
    val questionList: List<QuestionModel> = emptyList()
)

@IgnoreExtraProperties
data class QuestionModel(
    val question: String = "",
    val options: List<String> = emptyList(),
    val correct: String = ""
)
