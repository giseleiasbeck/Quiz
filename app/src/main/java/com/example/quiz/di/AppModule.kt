package com.example.quiz.di

import android.content.Context
import androidx.room.Room
import com.example.quiz.data.AuthRepository
import com.example.quiz.data.AuthRepositoryImpl
import com.example.quiz.data.QuizRepository
import com.example.quiz.data.QuizRepositoryImpl
import com.example.quiz.data.local.AppDatabase
import com.example.quiz.data.local.QuizDatabase
import com.example.quiz.data.local.UserDao
import com.example.quiz.data.local.dao.QuestionDao
import com.example.quiz.data.local.dao.QuizResultDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
 * Combina:
 * - Autenticação (Firebase Auth + Firestore + Room UserDao) — do colega
 * - Quiz (Firebase RTDB + Room QuizDatabase) — nosso código
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ============================================================
    // FIREBASE (Auth + Firestore)
    // ============================================================

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    /** Firestore para perfis de usuário — adicionado pelo colega */
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    // ============================================================
    // ROOM: AppDatabase (perfil do usuário — do colega)
    // ============================================================

    /** Banco de dados do usuário (UserEntity) — adicionado pelo colega */
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"  // Nome diferente do quiz_database para não conflitar!
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideUserDao(appDatabase: AppDatabase): UserDao {
        return appDatabase.userDao()
    }

    // ============================================================
    // AUTH REPOSITORY (atualizado pelo colega: Auth + Firestore + UserDao)
    // ============================================================

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore,
        userDao: UserDao
    ): AuthRepository {
        return AuthRepositoryImpl(auth, firestore, userDao)
    }

    // ============================================================
    // ROOM: QuizDatabase (perguntas + resultados — nosso código)
    // ============================================================

    /**
     * Cria a instância do banco de dados Room para o Quiz.
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

    @Provides
    @Singleton
    fun provideQuestionDao(database: QuizDatabase): QuestionDao {
        return database.questionDao()
    }

    @Provides
    @Singleton
    fun provideQuizResultDao(database: QuizDatabase): QuizResultDao {
        return database.quizResultDao()
    }

    // ============================================================
    // QUIZ REPOSITORY (Firebase RTDB para perguntas E resultados)
    // ============================================================

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