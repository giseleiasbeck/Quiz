package com.example.quiz.data

import com.example.quiz.data.local.entity.Question
import com.example.quiz.data.local.entity.QuizResult

interface QuizRepository {
    suspend fun getRandomQuestions(count: Int): List<Question>
    suspend fun saveQuizResult(result: QuizResult): Long
    suspend fun getAllResults(): List<QuizResult>
    suspend fun getAverageScore(): Double?
    suspend fun getBestScore(): Double?
    suspend fun getTotalQuizzes(): Int
    suspend fun getQuestionCount(): Int
    suspend fun syncQuestions(): Boolean
    suspend fun getCategories(): List<String>
}
