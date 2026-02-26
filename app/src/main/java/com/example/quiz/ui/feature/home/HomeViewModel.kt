package com.example.quiz.ui.feature.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quiz.data.QuizRepository
import com.example.quiz.data.model.QuizModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val questionCount: Int = 0,
    val categories: List<String> = emptyList(),
    val totalQuizzes: Int = 0,
    val averageScore: Double = 0.0,
    val bestScore: Double = 0.0,
    val isSynced: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: QuizRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            // 1. Inicia o estado de carregamento preservando dados anteriores se existirem
            _uiState.update { it.copy(isLoading = true) }

            try {
                // 2. Tenta sincronizar com o Firebase
                // O syncQuestions já salva no Room internamente conforme corrigimos
                val isSynced = repository.syncQuestions()

                // 3. Busca os dados atualizados do cache local (Room)
                // Fazemos isso APÓS o sync para garantir que os dados novos apareçam
                val questionCount = repository.getQuestionCount()
                val categories = repository.getCategories()
                val totalQuizzes = repository.getTotalQuizzes()
                val averageScore = repository.getAverageScore() ?: 0.0
                val bestScore = repository.getBestScore() ?: 0.0

                // 4. Atualiza o estado de uma vez só com todos os dados
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        questionCount = questionCount,
                        categories = categories,
                        totalQuizzes = totalQuizzes,
                        averageScore = averageScore,
                        bestScore = bestScore,
                        isSynced = isSynced
                    )
                }

                Log.d("HomeViewModel", "Dados carregados: $questionCount questões encontradas.")

            } catch (e: Exception) {
                Log.e("HomeViewModel", "Erro ao carregar dados", e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
