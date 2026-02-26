package com.example.quiz.data

import com.example.quiz.data.model.QuizModel
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuizRepository @Inject constructor() {

    private val database = FirebaseDatabase.getInstance().reference

    suspend fun getQuizzes(): Result<List<QuizModel>> {
        return try {
            val snapshot = database.get().await()
            val quizList = mutableListOf<QuizModel>()

            if (snapshot.exists()) {
                for (child in snapshot.children) {
                    val quiz = child.getValue(QuizModel::class.java)
                    if (quiz != null) {
                        quizList.add(quiz.copy(id = child.key ?: ""))
                    }
                }
            }
            Result.success(quizList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
