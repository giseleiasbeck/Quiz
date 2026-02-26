package com.example.quiz.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_results")
data class QuizResult(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val scorePercentage: Double,
    val totalTimeSeconds: Long,
    val dateTimestamp: Long = System.currentTimeMillis()
)
