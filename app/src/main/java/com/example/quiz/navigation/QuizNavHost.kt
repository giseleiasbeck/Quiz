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
import com.example.quiz.ui.feature.singup.SignupScreen
import kotlinx.serialization.Serializable

@Serializable
object LoginRoute

@Serializable
object SignupRoute

@Serializable
object HomeRoute

@Serializable
object QuizRoute

@Serializable
data class QuizResultRoute(
    val totalQuestions: Int,
    val correctAnswers: Int,
    val scorePercentage: Double,
    val totalTimeSeconds: Long
)

@Serializable
object HistoryRoute

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = LoginRoute) {

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

        composable<SignupRoute> {
            SignupScreen(
                viewModel = authViewModel,
                navigateToHome = {
                    navController.navigate(LoginRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                },
                navigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

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

        composable<QuizResultRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<QuizResultRoute>()

            QuizResultScreen(
                totalQuestions = route.totalQuestions,
                correctAnswers = route.correctAnswers,
                scorePercentage = route.scorePercentage,
                totalTimeSeconds = route.totalTimeSeconds,
                onPlayAgain = {
                    navController.navigate(QuizRoute) {
                        popUpTo(HomeRoute)
                    }
                },
                onGoHome = {
                    navController.navigate(HomeRoute) {
                        popUpTo(HomeRoute) { inclusive = true }
                    }
                }
            )
        }

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