package com.example.quiz.ui.feature.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quiz.data.QuizRepository
import com.example.quiz.data.local.entity.Question
import com.example.quiz.data.local.entity.QuizResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class QuizUiState {
    object Loading : QuizUiState()

    data class Playing(
        val currentQuestion: Question,
        val currentIndex: Int,
        val totalQuestions: Int,
        val selectedOptionIndex: Int? = null,
        val isAnswered: Boolean = false,
        val correctAnswers: Int = 0,
        val remainingSeconds: Int = 0,
        val totalTimeSeconds: Long = 0
    ) : QuizUiState()

    data class Finished(
        val totalQuestions: Int,
        val correctAnswers: Int,
        val scorePercentage: Double,
        val totalTimeSeconds: Long,
        val timedOut: Boolean = false
    ) : QuizUiState()

    data class Error(val message: String) : QuizUiState()
}

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val repository: QuizRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Loading)
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private var questions: List<Question> = emptyList()
    private var currentIndex = 0
    private var correctAnswers = 0
    private var startTimeMillis = 0L

    private var timerJob: Job? = null
    private var remainingSeconds = 0

    companion object {
        const val QUESTIONS_PER_QUIZ = 5
        const val QUIZ_TIME_SECONDS = 60
    }

    fun startQuiz() {
        viewModelScope.launch {
            _uiState.value = QuizUiState.Loading

            try {
                repository.syncQuestions()

                questions = repository.getRandomQuestions(QUESTIONS_PER_QUIZ)

                if (questions.isEmpty()) {
                    _uiState.value = QuizUiState.Error("Nenhuma pergunta encontrada!")
                    return@launch
                }

                currentIndex = 0
                correctAnswers = 0
                startTimeMillis = System.currentTimeMillis()
                remainingSeconds = QUIZ_TIME_SECONDS

                _uiState.value = QuizUiState.Playing(
                    currentQuestion = questions[0],
                    currentIndex = 0,
                    totalQuestions = questions.size,
                    correctAnswers = 0,
                    remainingSeconds = remainingSeconds
                )

                startTimer()
            } catch (e: Exception) {
                _uiState.value = QuizUiState.Error(
                    "Erro ao carregar perguntas: ${e.message}"
                )
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()

        timerJob = viewModelScope.launch {
            while (remainingSeconds > 0) {
                delay(1000L)
                remainingSeconds--

                val currentState = _uiState.value
                if (currentState is QuizUiState.Playing) {
                    _uiState.value = currentState.copy(
                        remainingSeconds = remainingSeconds,
                        totalTimeSeconds = (System.currentTimeMillis() - startTimeMillis) / 1000
                    )
                }
            }

            val currentState = _uiState.value
            if (currentState is QuizUiState.Playing) {
                finishQuiz(timedOut = true)
            }
        }
    }

    fun selectAnswer(optionIndex: Int) {
        val currentState = _uiState.value
        if (currentState !is QuizUiState.Playing || currentState.isAnswered) return

        val isCorrect = optionIndex == questions[currentIndex].correctOptionIndex
        if (isCorrect) correctAnswers++

        _uiState.value = currentState.copy(
            selectedOptionIndex = optionIndex,
            isAnswered = true,
            correctAnswers = correctAnswers
        )
    }

    fun nextQuestion() {
        val currentState = _uiState.value
        if (currentState !is QuizUiState.Playing || !currentState.isAnswered) return

        currentIndex++

        if (currentIndex < questions.size) {
            _uiState.value = QuizUiState.Playing(
                currentQuestion = questions[currentIndex],
                currentIndex = currentIndex,
                totalQuestions = questions.size,
                correctAnswers = correctAnswers,
                remainingSeconds = remainingSeconds,
                totalTimeSeconds = (System.currentTimeMillis() - startTimeMillis) / 1000
            )
        } else {
            finishQuiz(timedOut = false)
        }
    }

    private fun finishQuiz(timedOut: Boolean = false) {
        timerJob?.cancel()

        val totalTime = (System.currentTimeMillis() - startTimeMillis) / 1000
        val percentage = (correctAnswers.toDouble() / questions.size) * 100.0

        val result = QuizResult(
            totalQuestions = questions.size,
            correctAnswers = correctAnswers,
            scorePercentage = percentage,
            totalTimeSeconds = totalTime
        )

        viewModelScope.launch {
            try {
                repository.saveQuizResult(result)
            } catch (_: Exception) { }
        }

        _uiState.value = QuizUiState.Finished(
            totalQuestions = questions.size,
            correctAnswers = correctAnswers,
            scorePercentage = percentage,
            totalTimeSeconds = totalTime,
            timedOut = timedOut
        )
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
