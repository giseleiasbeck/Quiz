package com.example.quiz.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.quiz.data.local.entity.QuizResult

/**
 * DAO para a tabela "quiz_results".
 * Gerencia o histórico de resultados dos quizzes feitos pelo usuário.
 *
 * As queries de estatísticas (média, melhor resultado) são feitas
 * diretamente no SQL por performance — é mais eficiente que buscar
 * todos os resultados e calcular no Kotlin.
 */
@Dao
interface QuizResultDao {

    /**
     * Insere UM resultado no banco.
     * Chamado quando o usuário termina um quiz.
     * Retorna o ID gerado automaticamente.
     */
    @Insert
    suspend fun insert(result: QuizResult): Long

    /**
     * Busca todos os resultados ordenados do mais recente para o mais antigo.
     * ORDER BY dateTimestamp DESC → o mais novo primeiro.
     * Usado na tela de histórico.
     */
    @Query("SELECT * FROM quiz_results ORDER BY dateTimestamp DESC")
    suspend fun getAllResults(): List<QuizResult>

    /**
     * Retorna a média de acerto de todos os quizzes.
     * AVG() do SQL calcula a média diretamente no banco.
     * Retorna null se não houver resultados (por isso Double?).
     */
    @Query("SELECT AVG(scorePercentage) FROM quiz_results")
    suspend fun getAverageScore(): Double?

    /**
     * Retorna a MELHOR pontuação já obtida.
     * MAX() pega o maior valor. Também pode ser null.
     */
    @Query("SELECT MAX(scorePercentage) FROM quiz_results")
    suspend fun getBestScore(): Double?

    /**
     * Conta quantos quizzes o usuário já completou.
     * Usado nas estatísticas da tela de histórico.
     */
    @Query("SELECT COUNT(*) FROM quiz_results")
    suspend fun getTotalQuizzes(): Int
}
