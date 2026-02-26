package com.example.quiz.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.quiz.data.local.entity.Question

/**
 * DAO = Data Access Object.
 * É uma interface que define COMO acessar os dados na tabela "questions".
 * O Room gera a implementação real automaticamente em tempo de compilação.
 *
 * Por que interface e não classe?
 * → O Room usa anotações (@Query, @Insert) para gerar o código SQL
 *   por baixo dos panos. Nós só precisamos declarar O QUE queremos,
 *   e o Room implementa o COMO.
 *
 * Todas as funções são `suspend` (exceto consultas Flow) porque operações
 * de banco devem rodar fora da thread principal (Room exige isso).
 */
@Dao
interface QuestionDao {

    /**
     * Busca TODAS as perguntas do banco.
     * Usado para carregar as questões quando o quiz começa.
     */
    @Query("SELECT * FROM questions")
    suspend fun getAllQuestions(): List<Question>

    /**
     * Busca perguntas filtradas por categoria.
     * Ex: getAllByCategory("Geografia") → só perguntas de geografia.
     */
    @Query("SELECT * FROM questions WHERE category = :category")
    suspend fun getAllByCategory(category: String): List<Question>

    /**
     * Busca uma quantidade limitada de perguntas aleatórias.
     * ORDER BY RANDOM() embaralha no banco, e LIMIT :count pega só N.
     * Perfeito para gerar quizzes com N perguntas aleatórias.
     */
    @Query("SELECT * FROM questions ORDER BY RANDOM() LIMIT :count")
    suspend fun getRandomQuestions(count: Int): List<Question>

    /**
     * Insere uma lista de perguntas no banco.
     * OnConflictStrategy.REPLACE → se já existir uma pergunta com o mesmo ID,
     * ela será substituída (atualizada). Útil para sincronização com Firebase.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<Question>)

    /**
     * Conta quantas perguntas existem no banco.
     * Útil para saber se já fizemos o download inicial das questões.
     */
    @Query("SELECT COUNT(*) FROM questions")
    suspend fun getCount(): Int

    /**
     * Apaga TODAS as perguntas do banco.
     * Usado na sincronização: antes de salvar as perguntas novas do Firebase,
     * limpamos as antigas para evitar duplicatas.
     */
    @Query("DELETE FROM questions")
    suspend fun deleteAll()

    /**
     * Retorna todas as categorias distintas (sem repetição).
     * Usado na tela Home para listar as categorias de quiz disponíveis.
     */
    @Query("SELECT DISTINCT category FROM questions ORDER BY category ASC")
    suspend fun getDistinctCategories(): List<String>
}
