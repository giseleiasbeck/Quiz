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

// Estados de Autenticação
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


    /* GEMINI PRO - START
     Prompt:
     I'm using Hilt and LiveData in my AuthViewModel to manage my app's login. I created a sealed class AuthState with the states Authenticated, Unauthenticated, Loading, and Error.
     I already have the login and registration methods working, but I have a question about logout:
     I already have a repository.logout() that calls Firebase, but I noticed that when I click the logout button, the screen doesn't change! I stay on the main screen, and the app only realizes that I've logged out if I close and reopen it.
     How do I make the signout() method of my ViewModel notify the UI that the user is no longer authenticated right after calling the repository? Also, can you show me how to structure this ViewModel by injecting the repository using Hilt and handling these states?
     .*/
    fun signout() {
        repository.logout()
        // REQUIRED: Notify the UI that we are now logged out.
        // Without this, the LoginScreen thinks we are still logged in and redirects.
        _authState.value = AuthState.Unauthenticated
    }

    /* GEMINI PRO - END */
}