package com.example.quiz.ui.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quiz.data.QuizRepository
import com.example.quiz.data.local.entity.QuizResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val isLoading: Boolean = true,
    val results: List<QuizResult> = emptyList(),
    val totalQuizzes: Int = 0,
    val averageScore: Double = 0.0,
    val bestScore: Double = 0.0
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: QuizRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            try {
                val results = repository.getAllResults()
                val totalQuizzes = repository.getTotalQuizzes()
                val averageScore = repository.getAverageScore() ?: 0.0
                val bestScore = repository.getBestScore() ?: 0.0

                _uiState.value = HistoryUiState(
                    isLoading = false,
                    results = results,
                    totalQuizzes = totalQuizzes,
                    averageScore = averageScore,
                    bestScore = bestScore
                )
            } catch (e: Exception) {
                _uiState.value = HistoryUiState(isLoading = false)
            }
        }
    }
}
