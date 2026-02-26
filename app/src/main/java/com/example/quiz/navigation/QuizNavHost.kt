package com.example.quiz.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.quiz.ui.feature.auth.AuthViewModel
import com.example.quiz.ui.feature.history.HistoryScreen
import com.example.quiz.ui.feature.history.HistoryViewModel
import com.example.quiz.ui.feature.home.HomeScreen
import com.example.quiz.ui.feature.home.HomeViewModel
import com.example.quiz.ui.feature.login.LoginScreen
import com.example.quiz.ui.feature.quiz.QuizResultScreen
import com.example.quiz.ui.feature.quiz.QuizScreen
import com.example.quiz.ui.feature.quiz.QuizViewModel
import kotlinx.serialization.Serializable

// =============================================================
// ROTAS — Cada objeto/classe é uma "tela" do app.
//
// Fluxo completo:
//   Login → Home (dashboard) → Quiz → Resultado
//                ↓
//              Histórico
// =============================================================

@Serializable
object LoginRoute

/** Tela Home/Dashboard — HUB central do app */
@Serializable
object HomeRoute

/** Tela do Quiz — onde o usuário responde as perguntas */
@Serializable
object QuizRoute

/** Tela de Resultado — mostra desempenho ao final do quiz */
@Serializable
data class QuizResultRoute(
    val totalQuestions: Int,
    val correctAnswers: Int,
    val scorePercentage: Double,
    val totalTimeSeconds: Long
)

/** Tela de Histórico — lista todos os quizzes feitos */
@Serializable
object HistoryRoute

// =============================================================
// NAVIGATION HOST
// =============================================================

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()

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
                    // TODO: Implementar quando a rota de Cadastro for reativada
                }
            )
        }

        // --- HOME (Dashboard) ---
        composable<HomeRoute> {
            val homeViewModel: HomeViewModel = hiltViewModel()

            HomeScreen(
                viewModel = homeViewModel,
                onStartQuiz = {
                    navController.navigate(QuizRoute)
                },
                onViewHistory = {
                    navController.navigate(HistoryRoute)
                },
                onLogout = {
                    authViewModel.signout()
                    navController.navigate(LoginRoute) {
                        popUpTo(HomeRoute) { inclusive = true }
                    }
                }
            )
        }

        // --- QUIZ (execução das perguntas) ---
        composable<QuizRoute> {
            val quizViewModel: QuizViewModel = hiltViewModel()

            QuizScreen(
                viewModel = quizViewModel,
                onQuizFinished = { total, correct, percentage, time ->
                    navController.navigate(
                        QuizResultRoute(
                            totalQuestions = total,
                            correctAnswers = correct,
                            scorePercentage = percentage,
                            totalTimeSeconds = time
                        )
                    ) {
                        popUpTo(QuizRoute) { inclusive = true }
                    }
                }
            )
        }

        // --- RESULTADO DO QUIZ ---
        composable<QuizResultRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<QuizResultRoute>()

            QuizResultScreen(
                totalQuestions = route.totalQuestions,
                correctAnswers = route.correctAnswers,
                scorePercentage = route.scorePercentage,
                totalTimeSeconds = route.totalTimeSeconds,
                onPlayAgain = {
                    // Novo quiz direto
                    navController.navigate(QuizRoute) {
                        popUpTo(HomeRoute)
                    }
                },
                onGoHome = {
                    // Volta para o Dashboard
                    navController.navigate(HomeRoute) {
                        popUpTo(HomeRoute) { inclusive = true }
                    }
                }
            )
        }

        // --- HISTÓRICO ---
        composable<HistoryRoute> {
            val historyViewModel: HistoryViewModel = hiltViewModel()

            HistoryScreen(
                viewModel = historyViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}