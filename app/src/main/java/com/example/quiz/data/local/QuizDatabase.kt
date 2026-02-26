package com.example.quiz.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.quiz.data.local.dao.QuestionDao
import com.example.quiz.data.local.dao.QuizResultDao
import com.example.quiz.data.local.entity.Question
import com.example.quiz.data.local.entity.QuizResult

@Database(
    entities = [Question::class, QuizResult::class],
    version = 1,
    exportSchema = false
)
abstract class QuizDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun quizResultDao(): QuizResultDao
}
