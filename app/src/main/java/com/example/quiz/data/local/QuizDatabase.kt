package com.example.quiz.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.quiz.data.local.dao.QuestionDao
import com.example.quiz.data.local.dao.QuizResultDao
import com.example.quiz.data.local.entity.Question
import com.example.quiz.data.local.entity.QuizResult

/**
 * Classe principal do banco de dados Room.
 *
 * @Database → diz ao Room:
 *   - entities: quais tabelas existem no banco (Question e QuizResult)
 *   - version: versão do schema. Se mudarmos a estrutura das tabelas,
 *              precisamos incrementar a version e criar uma Migration.
 *   - exportSchema: false → não exporta o schema em JSON (simplifica o projeto)
 *
 * É abstrata porque o Room gera a implementação real em tempo de compilação.
 * Nós só declaramos quais DAOs existem, e o Room cria a classe concreta
 * (QuizDatabase_Impl) automaticamente.
 *
 * A instância real será criada no AppModule via Hilt (Injeção de Dependências).
 */
@Database(
    entities = [Question::class, QuizResult::class],
    version = 1,
    exportSchema = false
)
abstract class QuizDatabase : RoomDatabase() {

    /**
     * Retorna o DAO de perguntas.
     * O Room implementa esse método automaticamente.
     */
    abstract fun questionDao(): QuestionDao

    /**
     * Retorna o DAO de resultados.
     * O Room implementa esse método automaticamente.
     */
    abstract fun quizResultDao(): QuizResultDao
}
