package com.example.quiz.navigation

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.quiz.data.model.QuizModel
import com.example.quiz.ui.feature.auth.AuthViewModel
import com.example.quiz.ui.feature.home.HomeScreen
import com.example.quiz.ui.feature.login.LoginScreen
import com.example.quiz.ui.feature.quiz.QuizScreen
import com.example.quiz.ui.feature.singup.SignupScreen
import kotlinx.serialization.Serializable

// --- ROTAS ---
@Serializable
object LoginRoute

@Serializable
object SignupRoute

@Serializable
object HomeRoute

@Serializable
object QuizRoute

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()

    // Compartilha o quiz selecionado entre Home e Quiz
    var selectedQuiz by remember { mutableStateOf<QuizModel?>(null) }

    NavHost(navController = navController, startDestination = LoginRoute) {

        // --- LOGIN ---
        composable<LoginRoute> {
            LoginScreen(
                viewModel = authViewModel,
                navigateToHome = {
                    navController.navigate(HomeRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                },
                navigateToSignup = {
                    navController.navigate(SignupRoute)
                }
            )
        }

        // --- CADASTRO ---
        composable<SignupRoute> {
            SignupScreen(
                viewModel = authViewModel,
                navigateToHome = {
                    navController.navigate(HomeRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                },
                navigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // --- HOME / DASHBOARD ---
        composable<HomeRoute> {
            HomeScreen(
                onQuizSelected = { quiz ->
                    selectedQuiz = quiz
                    navController.navigate(QuizRoute)
                },
                onSignOut = {
                    authViewModel.signout()
                    navController.navigate(LoginRoute) {
                        popUpTo(HomeRoute) { inclusive = true }
                    }
                }
            )
        }

        // --- QUIZ ---
        composable<QuizRoute> {
            val quiz = selectedQuiz
            if (quiz != null) {
                QuizScreen(
                    quiz = quiz,
                    onFinish = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}