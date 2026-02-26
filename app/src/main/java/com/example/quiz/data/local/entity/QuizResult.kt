package com.example.quiz.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity do Room que salva o RESULTADO de uma sessão de quiz.
 *
 * Toda vez que o usuário termina um quiz, criamos um QuizResult com:
 * - totalQuestions: quantas perguntas tinha no quiz
 * - correctAnswers: quantas ele acertou
 * - scorePercentage: a porcentagem de acerto (ex: 75.0 = 75%)
 * - totalTimeSeconds: quanto tempo ele demorou em segundos
 * - dateTimestamp: quando ele fez o quiz (em milissegundos desde 1970 — "epoch")
 *
 * Por que usar Long para a data (timestamp) em vez de Date?
 * → O Room armazena dados em SQLite, que não entende objetos Java como Date.
 *   Usar Long (milissegundos) é o padrão mais prático — e podemos converter
 *   para Date na hora de exibir usando SimpleDateFormat ou java.time.
 */
@Entity(tableName = "quiz_results")
data class QuizResult(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val totalQuestions: Int,       // Total de perguntas no quiz
    val correctAnswers: Int,       // Quantas o usuário acertou
    val scorePercentage: Double,   // Porcentagem de acerto (0.0 a 100.0)
    val totalTimeSeconds: Long,    // Tempo total gasto em segundos
    val dateTimestamp: Long = System.currentTimeMillis() // Data/hora automática
)
