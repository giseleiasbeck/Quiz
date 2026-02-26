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

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideUserDao(appDatabase: AppDatabase): UserDao {
        return appDatabase.userDao()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore,
        userDao: UserDao
    ): AuthRepository {
        return AuthRepositoryImpl(auth, firestore, userDao)
    }

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