package com.example.quiz.di

import android.content.Context
import androidx.room.Room
import com.example.quiz.data.AuthRepository
import com.example.quiz.data.AuthRepositoryImpl
import com.example.quiz.data.QuizRepository
import com.example.quiz.data.QuizRepositoryImpl
import com.example.quiz.data.local.QuizDatabase
import com.example.quiz.data.local.dao.QuestionDao
import com.example.quiz.data.local.dao.QuizResultDao
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo do Hilt (Injeção de Dependências).
 *
 * Esse é o "cardápio" que ensina o Hilt a criar as dependências.
 * Quando alguma classe pede um QuizRepository, por exemplo,
 * o Hilt olha aqui e sabe como criá-lo.
 *
 * @Module → marca como módulo Hilt
 * @InstallIn(SingletonComponent) → as instâncias vivem durante toda a vida do app
 * @Singleton → cria apenas UMA instância (não recria a cada uso)
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ============================================================
    // FIREBASE AUTH (já existia)
    // ============================================================

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth
    ): AuthRepository {
        return AuthRepositoryImpl(auth)
    }

    // ============================================================
    // ROOM DATABASE (novo)
    // ============================================================

    /**
     * Cria a instância do banco de dados Room.
     *
     * - @ApplicationContext context → o Hilt fornece o contexto do app
     * - Room.databaseBuilder() → cria o banco com o nome "quiz_database"
     * - .fallbackToDestructiveMigration() → se mudarmos a versão do schema,
     *   o Room apaga e recria o banco em vez de crashar.
     *   ⚠️ Em produção, usaríamos Migrations para preservar os dados.
     *   Para desenvolvimento, destructive é mais prático.
     */
    @Provides
    @Singleton
    fun provideQuizDatabase(
        @ApplicationContext context: Context
    ): QuizDatabase {
        return Room.databaseBuilder(
            context,
            QuizDatabase::class.java,
            "quiz_database"
        ).fallbackToDestructiveMigration().build()
    }

    /**
     * Extrai o QuestionDao do banco.
     * Assim o Hilt sabe injetar o DAO diretamente onde for preciso.
     */
    @Provides
    @Singleton
    fun provideQuestionDao(database: QuizDatabase): QuestionDao {
        return database.questionDao()
    }

    /**
     * Extrai o QuizResultDao do banco.
     */
    @Provides
    @Singleton
    fun provideQuizResultDao(database: QuizDatabase): QuizResultDao {
        return database.quizResultDao()
    }

    /**
     * Ensina o Hilt: quando alguém pedir QuizRepository (interface),
     * entregue um QuizRepositoryImpl (implementação real).
     * Inclui fontes do Firebase para perguntas E resultados.
     */
    @Provides
    @Singleton
    fun provideQuizRepository(
        questionDao: QuestionDao,
        quizResultDao: QuizResultDao,
        firebaseSource: com.example.quiz.data.remote.QuestionFirebaseSource,
        resultFirebaseSource: com.example.quiz.data.remote.ResultFirebaseSource
    ): QuizRepository {
        return QuizRepositoryImpl(questionDao, quizResultDao, firebaseSource, resultFirebaseSource)
    }
}