package com.playground.test_prep_cu.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.playground.test_prep_cu.data.preferences.QuizPreferences
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
 * Main navigation host
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    repository: QuizRepository,
    preferences: QuizPreferences
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Upload.route
    ) {
        composable(Screen.Upload.route) {
            val viewModel: UploadViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return UploadViewModel(repository, preferences) as T
                    }
                }
            )
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
            val viewModel: QuizViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return QuizViewModel(repository) as T
                    }
                }
            )
            QuizScreen(
                viewModel = viewModel,
                onQuizCompleted = {
                    navController.navigate(Screen.Results.route) {
                        popUpTo(Screen.Quiz.route) { inclusive = true }
                    }
                },
                onBackToUpload = {
                    navController.navigate(Screen.Upload.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Results.route) {
            val viewModel: ResultsViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return ResultsViewModel(repository) as T
                    }
                }
            )
            ResultsScreen(
                viewModel = viewModel,
                onReviewAnswers = {
                    navController.navigate(Screen.Review.route)
                },
                onNewQuiz = {
                    navController.navigate(Screen.Upload.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBackToUpload = {
                    navController.navigate(Screen.Upload.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Review.route) {
            val viewModel: ReviewViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return ReviewViewModel(repository) as T
                    }
                }
            )
            ReviewScreen(
                viewModel = viewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
