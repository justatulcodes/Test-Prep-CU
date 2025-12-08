package com.playground.test_prep_cu.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.playground.test_prep_cu.data.repository.QuizRepository
import com.playground.test_prep_cu.ui.quiz.QuizScreen
import com.playground.test_prep_cu.ui.quiz.QuizViewModel
import com.playground.test_prep_cu.ui.results.ResultsScreen
import com.playground.test_prep_cu.ui.results.ResultsViewModel
import com.playground.test_prep_cu.ui.review.ReviewScreen
import com.playground.test_prep_cu.ui.review.ReviewViewModel
import com.playground.test_prep_cu.ui.upload.UploadScreen
import com.playground.test_prep_cu.ui.upload.UploadViewModel

/**
 * Main navigation host for the app
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    repository: QuizRepository
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Upload.route
    ) {
        composable(Screen.Upload.route) {
            val viewModel = UploadViewModel(repository)
            UploadScreen(
                viewModel = viewModel,
                onQuizLoaded = {
                    navController.navigate(Screen.Quiz.route) {
                        popUpTo(Screen.Upload.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Quiz.route) {
            val viewModel = QuizViewModel(repository)
            QuizScreen(
                viewModel = viewModel,
                onQuizCompleted = {
                    navController.navigate(Screen.Results.route) {
                        popUpTo(Screen.Quiz.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Results.route) {
            val viewModel = ResultsViewModel(repository)
            ResultsScreen(
                viewModel = viewModel,
                onReviewAnswers = {
                    navController.navigate(Screen.Review.route)
                },
                onNewQuiz = {
                    navController.navigate(Screen.Upload.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Review.route) {
            val viewModel = ReviewViewModel(repository)
            ReviewScreen(
                viewModel = viewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
