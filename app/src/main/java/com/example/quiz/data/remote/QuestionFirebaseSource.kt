package com.example.quiz.data.remote

import com.example.quiz.data.local.entity.Question
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class QuestionFirebaseSource @Inject constructor() {

    private val database = FirebaseDatabase.getInstance("https://quiz-app-d7112-default-rtdb.firebaseio.com")
    private val questionsRef = database.getReference("quizzes")

    suspend fun fetchAllQuestions(): Result<List<Question>> {
        return try {
            val snapshot = questionsRef.get().await()

            val questions = snapshot.children.mapNotNull { child ->
                val firebaseQuestion = child.getValue(QuestionFirebase::class.java)
                firebaseQuestion?.toLocalQuestion()
            }

            Result.success(questions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun QuestionFirebase.toLocalQuestion(): Question {
        return Question(
            id = 0,
            questionText = this.questionText,
            optionA = this.optionA,
            optionB = this.optionB,
            optionC = this.optionC,
            optionD = this.optionD,
            correctOptionIndex = this.correctOptionIndex,
            category = this.category
        )
    }
}
