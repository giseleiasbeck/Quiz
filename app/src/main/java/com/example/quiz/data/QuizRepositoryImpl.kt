package com.example.quiz.data

import android.util.Log
import com.example.quiz.data.local.dao.QuestionDao
import com.example.quiz.data.local.dao.QuizResultDao
import com.example.quiz.data.local.entity.Question
import com.example.quiz.data.local.entity.QuizResult
import com.example.quiz.data.remote.QuestionFirebaseSource
import com.example.quiz.data.remote.ResultFirebaseSource
import javax.inject.Inject

class QuizRepositoryImpl @Inject constructor(
    private val questionDao: QuestionDao,
    private val quizResultDao: QuizResultDao,
    private val firebaseSource: QuestionFirebaseSource,
    private val resultFirebaseSource: ResultFirebaseSource
) : QuizRepository {

    companion object {
        private const val TAG = "QuizRepository"
    }

    override suspend fun getRandomQuestions(count: Int): List<Question> {
        return questionDao.getRandomQuestions(count)
    }

    override suspend fun saveQuizResult(result: QuizResult): Long {
        val localId = quizResultDao.insert(result)
        Log.d(TAG, "Resultado salvo localmente (Room ID: $localId)")

        try {
            val cloudResult = resultFirebaseSource.saveResult(
                totalQuestions = result.totalQuestions,
                correctAnswers = result.correctAnswers,
                scorePercentage = result.scorePercentage,
                totalTimeSeconds = result.totalTimeSeconds
            )
            cloudResult.onSuccess {
                Log.d(TAG, "Resultado salvo na nuvem (Firebase)")
            }.onFailure { error ->
                Log.w(TAG, "Falha ao salvar na nuvem: ${error.message}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Erro ao salvar na nuvem: ${e.message}")
        }

        return localId
    }

    override suspend fun getAllResults(): List<QuizResult> {
        return quizResultDao.getAllResults()
    }

    override suspend fun getAverageScore(): Double? {
        return quizResultDao.getAverageScore()
    }

    override suspend fun getBestScore(): Double? {
        return quizResultDao.getBestScore()
    }

    override suspend fun getTotalQuizzes(): Int {
        return quizResultDao.getTotalQuizzes()
    }

    override suspend fun getQuestionCount(): Int {
        return questionDao.getCount()
    }

    override suspend fun getCategories(): List<String> {
        return questionDao.getDistinctCategories()
    }

    override suspend fun syncQuestions(): Boolean {
        val firebaseResult = firebaseSource.fetchAllQuestions()

        firebaseResult.onSuccess { firebaseQuestions ->
            if (firebaseQuestions.isNotEmpty()) {
                Log.d(TAG, "Firebase: ${firebaseQuestions.size} perguntas baixadas")
                questionDao.deleteAll()
                questionDao.insertAll(firebaseQuestions)
                return true
            } else {
                Log.d(TAG, "Firebase: nó 'quizzes' vazio ou inexistente")
            }
        }

        firebaseResult.onFailure { error ->
            Log.w(TAG, "Firebase falhou: ${error.message}")
        }

        if (questionDao.getCount() > 0) {
            Log.d(TAG, "Usando ${questionDao.getCount()} perguntas do cache local")
            return false
        }

        Log.d(TAG, "Sem internet e sem cache -> usando perguntas de fallback")
        populateFallbackQuestions()
        return false
    }

    private suspend fun populateFallbackQuestions() {
        val fallbackQuestions = listOf(
            Question(
                questionText = "Qual é a capital do Brasil?",
                optionA = "São Paulo",
                optionB = "Rio de Janeiro",
                optionC = "Brasília",
                optionD = "Salvador",
                correctOptionIndex = 2,
                category = "Geografia"
            ),
            Question(
                questionText = "Qual planeta é conhecido como 'Planeta Vermelho'?",
                optionA = "Vênus",
                optionB = "Marte",
                optionC = "Júpiter",
                optionD = "Saturno",
                correctOptionIndex = 1,
                category = "Ciências"
            ),
            Question(
                questionText = "Quem pintou a Mona Lisa?",
                optionA = "Michelangelo",
                optionB = "Van Gogh",
                optionC = "Leonardo da Vinci",
                optionD = "Pablo Picasso",
                correctOptionIndex = 2,
                category = "Arte"
            ),
            Question(
                questionText = "Qual é o maior oceano do mundo?",
                optionA = "Atlântico",
                optionB = "Índico",
                optionC = "Ártico",
                optionD = "Pacífico",
                correctOptionIndex = 3,
                category = "Geografia"
            ),
            Question(
                questionText = "Em que ano o Brasil foi descoberto?",
                optionA = "1498",
                optionB = "1500",
                optionC = "1502",
                optionD = "1510",
                correctOptionIndex = 1,
                category = "História"
            ),
            Question(
                questionText = "Qual é a fórmula química da água?",
                optionA = "CO2",
                optionB = "NaCl",
                optionC = "H2O",
                optionD = "O2",
                correctOptionIndex = 2,
                category = "Ciências"
            ),
            Question(
                questionText = "Qual é o maior país do mundo em área territorial?",
                optionA = "China",
                optionB = "Estados Unidos",
                optionC = "Canadá",
                optionD = "Rússia",
                correctOptionIndex = 3,
                category = "Geografia"
            ),
            Question(
                questionText = "Quem escreveu 'Dom Casmurro'?",
                optionA = "José de Alencar",
                optionB = "Machado de Assis",
                optionC = "Clarice Lispector",
                optionD = "Jorge Amado",
                correctOptionIndex = 1,
                category = "Literatura"
            ),
            Question(
                questionText = "Qual é o resultado de 7 × 8?",
                optionA = "54",
                optionB = "56",
                optionC = "58",
                optionD = "64",
                correctOptionIndex = 1,
                category = "Matemática"
            ),
            Question(
                questionText = "Qual linguagem de programação é usada para apps Android nativos?",
                optionA = "Swift",
                optionB = "Python",
                optionC = "Kotlin",
                optionD = "Ruby",
                correctOptionIndex = 2,
                category = "Tecnologia"
            )
        )
        questionDao.insertAll(fallbackQuestions)
    }
}
