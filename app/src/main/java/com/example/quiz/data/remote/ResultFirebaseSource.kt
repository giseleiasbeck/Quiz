package com.example.quiz.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ResultFirebaseSource @Inject constructor() {

    private val database = FirebaseDatabase.getInstance("https://quiz-app-d7112-default-rtdb.firebaseio.com")
    private val resultsRef = database.getReference("results")
    private val auth = FirebaseAuth.getInstance()

    data class ResultData(
        val totalQuestions: Int = 0,
        val correctAnswers: Int = 0,
        val scorePercentage: Double = 0.0,
        val totalTimeSeconds: Long = 0,
        val dateTimestamp: Long = System.currentTimeMillis()
    )

    suspend fun saveResult(
        totalQuestions: Int,
        correctAnswers: Int,
        scorePercentage: Double,
        totalTimeSeconds: Long
    ): Result<Boolean> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("Usuário não logado"))

            val resultData = ResultData(
                totalQuestions = totalQuestions,
                correctAnswers = correctAnswers,
                scorePercentage = scorePercentage,
                totalTimeSeconds = totalTimeSeconds
            )

            resultsRef
                .child(userId)
                .push()
                .setValue(resultData)
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserResults(): Result<List<ResultData>> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("Usuário não logado"))

            val snapshot = resultsRef
                .child(userId)
                .orderByChild("dateTimestamp")
                .get()
                .await()

            val results = snapshot.children.mapNotNull { child ->
                child.getValue(ResultData::class.java)
            }.reversed()

            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
