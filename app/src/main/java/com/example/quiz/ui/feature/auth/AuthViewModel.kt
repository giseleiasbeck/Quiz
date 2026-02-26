package com.example.quiz.ui.feature.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quiz.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.onFailure
import kotlin.onSuccess
import kotlin.text.isEmpty

sealed class AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    init {
        checkAuthStatus()
    }

    fun checkAuthStatus() {
        if (repository.currentUser != null) {
            _authState.value = AuthState.Authenticated
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun login(email: String, pass: String) {
        if (email.isEmpty() || pass.isEmpty()) {
            _authState.value = AuthState.Error("Email ou senha não podem ser vazios")
            return
        }
        _authState.value = AuthState.Loading

        viewModelScope.launch {
            val result = repository.login(email, pass)
            result.onSuccess {
                _authState.value = AuthState.Authenticated
            }.onFailure { exception ->
                _authState.value = AuthState.Error(exception.message ?: "Erro desconhecido")
            }
        }
    }

    fun signup(email: String, pass: String) {
        if (email.isEmpty() || pass.isEmpty()) {
            _authState.value = AuthState.Error("Email ou senha não podem ser vazios")
            return
        }
        _authState.value = AuthState.Loading

        viewModelScope.launch {
            val result = repository.signup(email, pass)
            result.onSuccess {
                _authState.value = AuthState.Authenticated
            }.onFailure { exception ->
                _authState.value = AuthState.Error(exception.message ?: "Erro desconhecido")
            }
        }
    }

    fun signout() {
        repository.logout()
        _authState.value = AuthState.Unauthenticated
    }
}