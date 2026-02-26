package com.example.quiz.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity do Room que representa UMA pergunta do quiz.
 *
 * - @Entity(tableName = "questions") → cria uma tabela chamada "questions" no SQLite.
 * - @PrimaryKey(autoGenerate = true) → o Room gera o ID automaticamente (1, 2, 3...).
 *
 * Por que guardar as alternativas como campos separados (optionA, optionB...)?
 * → É a forma mais simples e direta. Poderíamos usar um TypeConverter para
 *   salvar uma lista, mas para 4 alternativas fixas, campos separados são
 *   mais legíveis e fáceis de consultar via SQL.
 *
 * O campo `correctOptionIndex` guarda QUAL alternativa é a certa (0=A, 1=B, 2=C, 3=D).
 * Assim, na hora de corrigir, basta comparar o índice clicado com esse valor.
 */
@Entity(tableName = "questions")
data class Question(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val questionText: String,    // Texto da pergunta, ex: "Qual a capital do Brasil?"

    val optionA: String,         // Alternativa A
    val optionB: String,         // Alternativa B
    val optionC: String,         // Alternativa C
    val optionD: String,         // Alternativa D

    val correctOptionIndex: Int, // Índice da resposta certa (0=A, 1=B, 2=C, 3=D)

    val category: String = "Geral" // Categoria da pergunta (ex: "Geografia", "Ciências")
) {
    /**
     * Função auxiliar que retorna todas as alternativas como uma lista.
     * Facilita na hora de exibir na tela — basta iterar a lista.
     */
    fun getOptions(): List<String> = listOf(optionA, optionB, optionC, optionD)
}
