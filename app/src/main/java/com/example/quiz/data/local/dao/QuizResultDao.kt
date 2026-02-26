package com.example.quiz.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.quiz.data.local.entity.QuizResult

@Dao
interface QuizResultDao {

    @Insert
    suspend fun insert(result: QuizResult): Long

    @Query("SELECT * FROM quiz_results ORDER BY dateTimestamp DESC")
    suspend fun getAllResults(): List<QuizResult>

    @Query("SELECT AVG(scorePercentage) FROM quiz_results")
    suspend fun getAverageScore(): Double?

    @Query("SELECT MAX(scorePercentage) FROM quiz_results")
    suspend fun getBestScore(): Double?

    @Query("SELECT COUNT(*) FROM quiz_results")
    suspend fun getTotalQuizzes(): Int
}
