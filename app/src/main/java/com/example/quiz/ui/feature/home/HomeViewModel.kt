package com.example.quiz.ui.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quiz.data.QuizRepository
import com.example.quiz.data.model.QuizModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class HomeState {
    object Loading : HomeState()
    data class Success(val quizList: List<QuizModel>, val userEmail: String) : HomeState()
    data class Error(val message: String) : HomeState()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _homeState = MutableStateFlow<HomeState>(HomeState.Loading)
    val homeState: StateFlow<HomeState> = _homeState

    init {
        loadQuizzes()
    }

    fun loadQuizzes() {
        _homeState.value = HomeState.Loading
        viewModelScope.launch {
            val result = quizRepository.getQuizzes()
            result.onSuccess { list ->
                _homeState.value = HomeState.Success(
                    quizList = list,
                    userEmail = auth.currentUser?.email ?: ""
                )
            }.onFailure { e ->
                _homeState.value = HomeState.Error(e.message ?: "Erro ao carregar quizzes")
            }
        }
    }

    fun getCurrentUserEmail(): String = auth.currentUser?.email ?: ""
}
