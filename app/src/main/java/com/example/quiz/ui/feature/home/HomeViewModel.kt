package com.example.quiz.ui.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quiz.data.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
            _uiState.value = HomeUiState(isLoading = true)

            try {
                val synced = repository.syncQuestions()

                val questionCount = repository.getQuestionCount()
                val categories = repository.getCategories()
                val totalQuizzes = repository.getTotalQuizzes()
                val averageScore = repository.getAverageScore() ?: 0.0
                val bestScore = repository.getBestScore() ?: 0.0

                _uiState.value = HomeUiState(
                    isLoading = false,
                    questionCount = questionCount,
                    categories = categories,
                    totalQuizzes = totalQuizzes,
                    averageScore = averageScore,
                    bestScore = bestScore,
                    isSynced = synced
                )
            } catch (e: Exception) {
                _uiState.value = HomeUiState(isLoading = false)
            }
        }
    }
}
