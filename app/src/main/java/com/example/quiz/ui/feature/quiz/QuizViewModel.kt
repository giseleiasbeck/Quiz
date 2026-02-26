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

/**
 * Estados possíveis da tela de quiz.
 *
 * Usamos sealed class (mesma ideia do AuthState que já existe no projeto).
 * Cada estado define O QUE a tela deve exibir.
 *
 * Por que StateFlow em vez de LiveData (como no AuthViewModel)?
 * → StateFlow é mais moderno e recomendado pelo Google para Compose.
 *   LiveData funciona bem, mas StateFlow integra melhor com coroutines
 *   e o lifecycle do Compose. Ambas abordagens são válidas.
 */
sealed class QuizUiState {
    /** Estado inicial — carregando perguntas do banco */
    object Loading : QuizUiState()

    /** Quiz em andamento — tem uma pergunta sendo exibida */
    data class Playing(
        val currentQuestion: Question,       // Pergunta atual
        val currentIndex: Int,               // Índice da pergunta (0, 1, 2...)
        val totalQuestions: Int,             // Total de perguntas no quiz
        val selectedOptionIndex: Int? = null, // Qual opção o usuário clicou (null = nenhuma)
        val isAnswered: Boolean = false,     // Se já respondeu essa pergunta
        val correctAnswers: Int = 0,         // Acertos acumulados até agora
        val remainingSeconds: Int = 0,       // Tempo restante do TIMER (countdown)
        val totalTimeSeconds: Long = 0       // Tempo total desde o início do quiz
    ) : QuizUiState()

    /** Quiz acabou — mostra o resultado */
    data class Finished(
        val totalQuestions: Int,
        val correctAnswers: Int,
        val scorePercentage: Double,
        val totalTimeSeconds: Long,
        val timedOut: Boolean = false        // Se o quiz acabou por tempo esgotado
    ) : QuizUiState()

    /** Erro ao carregar perguntas */
    data class Error(val message: String) : QuizUiState()
}

/**
 * ViewModel que controla toda a lógica do quiz.
 *
 * NOVO: Agora com TIMER de countdown!
 * O timer conta segundos regressivamente. Quando chega a 0,
 * o quiz é finalizado automaticamente (tempo esgotado).
 *
 * O timer usa coroutines com delay(1000) em vez de CountDownTimer do Android.
 * Por quê? → Coroutines são mais simples, canceláveis, e se integram
 * melhor com o viewModelScope (cancelam automaticamente quando o VM é destruído).
 */
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

    // --- TIMER ---
    private var timerJob: Job? = null  // Job da coroutine do timer (cancelável)
    private var remainingSeconds = 0   // Segundos restantes

    companion object {
        const val QUESTIONS_PER_QUIZ = 5
        /**
         * Tempo total do quiz em segundos.
         * 60 segundos (1 minuto) = ~12 segundos por pergunta com 5 perguntas.
         * Esse valor pode ser mudado facilmente.
         */
        const val QUIZ_TIME_SECONDS = 60
    }

    /**
     * Inicia o quiz: sincroniza perguntas, inicia timer.
     */
    fun startQuiz() {
        viewModelScope.launch {
            _uiState.value = QuizUiState.Loading

            try {
                // 1. Sincroniza perguntas (Firebase → Room, ou usa cache local)
                repository.syncQuestions()

                // 2. Busca N perguntas aleatórias
                questions = repository.getRandomQuestions(QUESTIONS_PER_QUIZ)

                if (questions.isEmpty()) {
                    _uiState.value = QuizUiState.Error("Nenhuma pergunta encontrada!")
                    return@launch
                }

                // 3. Inicializa os contadores
                currentIndex = 0
                correctAnswers = 0
                startTimeMillis = System.currentTimeMillis()
                remainingSeconds = QUIZ_TIME_SECONDS

                // 4. Exibe a primeira pergunta
                _uiState.value = QuizUiState.Playing(
                    currentQuestion = questions[0],
                    currentIndex = 0,
                    totalQuestions = questions.size,
                    correctAnswers = 0,
                    remainingSeconds = remainingSeconds
                )

                // 5. Inicia o timer countdown
                startTimer()
            } catch (e: Exception) {
                _uiState.value = QuizUiState.Error(
                    "Erro ao carregar perguntas: ${e.message}"
                )
            }
        }
    }

    /**
     * Timer de countdown usando coroutines.
     *
     * Como funciona:
     * 1. Cria um Job (coroutine cancelável)
     * 2. A cada 1 segundo (delay(1000)):
     *    - Decrementa remainingSeconds
     *    - Atualiza o estado da UI (barra de tempo atualiza)
     *    - Se chegar a 0 → finaliza o quiz por timeout
     *
     * Por que Job separado?
     * → Podemos cancelar o timer se o quiz terminar antes do tempo.
     *   Sem isso, o timer continuaria rodando em background.
     *
     * O timerJob?.cancel() no início garante que só um timer roda por vez.
     */
    private fun startTimer() {
        timerJob?.cancel() // Cancela timer anterior (se existir)

        timerJob = viewModelScope.launch {
            while (remainingSeconds > 0) {
                delay(1000L) // Espera 1 segundo
                remainingSeconds--

                // Atualiza o estado APENAS se o quiz ainda está em andamento
                val currentState = _uiState.value
                if (currentState is QuizUiState.Playing) {
                    _uiState.value = currentState.copy(
                        remainingSeconds = remainingSeconds,
                        totalTimeSeconds = (System.currentTimeMillis() - startTimeMillis) / 1000
                    )
                }
            }

            // Tempo esgotou! Finaliza automaticamente
            val currentState = _uiState.value
            if (currentState is QuizUiState.Playing) {
                finishQuiz(timedOut = true)
            }
        }
    }

    /**
     * Chamado quando o usuário clica em uma alternativa.
     */
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

    /**
     * Avança para a próxima pergunta OU finaliza o quiz.
     */
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

    /**
     * Finaliza o quiz, calcula a pontuação e salva no Room + Firebase.
     *
     * @param timedOut true se o quiz acabou porque o tempo esgotou
     */
    private fun finishQuiz(timedOut: Boolean = false) {
        // Para o timer
        timerJob?.cancel()

        val totalTime = (System.currentTimeMillis() - startTimeMillis) / 1000
        val percentage = (correctAnswers.toDouble() / questions.size) * 100.0

        val result = QuizResult(
            totalQuestions = questions.size,
            correctAnswers = correctAnswers,
            scorePercentage = percentage,
            totalTimeSeconds = totalTime
        )

        // Salva no Room + Firebase
        viewModelScope.launch {
            try {
                repository.saveQuizResult(result)
            } catch (e: Exception) {
                // Falha ao salvar não impede mostrar resultado
            }
        }

        _uiState.value = QuizUiState.Finished(
            totalQuestions = questions.size,
            correctAnswers = correctAnswers,
            scorePercentage = percentage,
            totalTimeSeconds = totalTime,
            timedOut = timedOut
        )
    }

    /**
     * Cancela o timer quando o ViewModel é destruído.
     * Importante para evitar leaks de coroutines.
     */
    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
