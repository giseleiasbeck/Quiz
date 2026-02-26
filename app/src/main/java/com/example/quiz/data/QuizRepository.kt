package com.example.quiz.data

import com.example.quiz.data.local.entity.Question
import com.example.quiz.data.local.entity.QuizResult

/**
 * Interface do repositório de Quiz.
 *
 * Agora com método de sincronização com Firebase.
 * O ViewModel só conhece essa interface — não sabe se os dados
 * vêm do Room, do Firebase, ou de Marte. Isso é abstração.
 */
interface QuizRepository {

    /** Busca N perguntas aleatórias para montar um quiz */
    suspend fun getRandomQuestions(count: Int): List<Question>

    /** Salva o resultado de um quiz finalizado. Retorna o ID gerado */
    suspend fun saveQuizResult(result: QuizResult): Long

    /** Busca todo o histórico de quizzes feitos */
    suspend fun getAllResults(): List<QuizResult>

    /** Retorna a média de acerto de todos os quizzes */
    suspend fun getAverageScore(): Double?

    /** Retorna a melhor pontuação já obtida */
    suspend fun getBestScore(): Double?

    /** Retorna quantos quizzes o usuário já completou */
    suspend fun getTotalQuizzes(): Int

    /** Conta quantas perguntas existem no banco local */
    suspend fun getQuestionCount(): Int

    /**
     * Sincroniza as perguntas: busca do Firebase → salva no Room.
     * Se o Firebase falhar (sem internet), usa os dados locais.
     * Retorna true se sincronizou com sucesso, false se usou cache.
     */
    suspend fun syncQuestions(): Boolean

    /** Retorna todas as categorias distintas das perguntas no banco */
    suspend fun getCategories(): List<String>
}
