package com.example.quiz.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.quiz.data.local.entity.Question

@Dao
interface QuestionDao {

    @Query("SELECT * FROM questions")
    suspend fun getAllQuestions(): List<Question>

    @Query("SELECT * FROM questions WHERE category = :category")
    suspend fun getAllByCategory(category: String): List<Question>

    @Query("SELECT * FROM questions ORDER BY RANDOM() LIMIT :count")
    suspend fun getRandomQuestions(count: Int): List<Question>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<Question>)

    @Query("SELECT COUNT(*) FROM questions")
    suspend fun getCount(): Int

    @Query("DELETE FROM questions")
    suspend fun deleteAll()

    @Query("SELECT DISTINCT category FROM questions ORDER BY category ASC")
    suspend fun getDistinctCategories(): List<String>
}
