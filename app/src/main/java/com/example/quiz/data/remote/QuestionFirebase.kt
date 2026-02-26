package com.example.quiz.data.remote

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class QuestionFirebase(
    val questionText: String = "",       // IGUAL ao JSON
    val optionA: String = "",            // IGUAL ao JSON
    val optionB: String = "",            // IGUAL ao JSON
    val optionC: String = "",            // IGUAL ao JSON
    val optionD: String = "",            // IGUAL ao JSON
    val correctOptionIndex: Int = 0,     // IGUAL ao JSON (é Int)
    val category: String = "Geral"       // IGUAL ao JSON
)
