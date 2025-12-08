package com.playground.test_prep_cu.ui.review

import androidx.lifecycle.ViewModel
import com.playground.test_prep_cu.data.model.Question
import com.playground.test_prep_cu.data.model.UserAnswer
import com.playground.test_prep_cu.data.repository.QuizRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for review screen
 */
class ReviewViewModel(private val repository: QuizRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow<ReviewUiState>(ReviewUiState.Loading)
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()
    
    private var currentFilter = ReviewFilter.ALL
    
    init {
        loadReviewData()
    }
    
    private fun loadReviewData() {
        val attempt = repository.getCurrentAttempt()
        val allQuestions = repository.getAllQuestions()
        
        if (attempt == null) {
            _uiState.value = ReviewUiState.Error("No quiz attempt found")
            return
        }
        
        val reviewItems = allQuestions.mapNotNull { question ->
            val answer = attempt.answers.find { it.questionId == question.id }
            answer?.let {
                ReviewItem(
                    question = question,
                    userAnswer = it
                )
            }
        }
        
        updateFilteredItems(reviewItems)
    }
    
    private fun updateFilteredItems(allItems: List<ReviewItem>) {
        val filtered = when (currentFilter) {
            ReviewFilter.ALL -> allItems
            ReviewFilter.CORRECT -> allItems.filter { it.userAnswer.isCorrect }
            ReviewFilter.INCORRECT -> allItems.filter { !it.userAnswer.isCorrect }
        }
        
        _uiState.value = ReviewUiState.Success(
            items = filtered,
            currentFilter = currentFilter,
            totalQuestions = allItems.size,
            correctCount = allItems.count { it.userAnswer.isCorrect },
            incorrectCount = allItems.count { !it.userAnswer.isCorrect }
        )
    }
    
    fun setFilter(filter: ReviewFilter) {
        currentFilter = filter
        loadReviewData()
    }
}

/**
 * Review item combining question and user answer
 */
data class ReviewItem(
    val question: Question,
    val userAnswer: UserAnswer
)

/**
 * Filter options for review
 */
enum class ReviewFilter {
    ALL,
    CORRECT,
    INCORRECT
}

/**
 * UI state for review screen
 */
sealed class ReviewUiState {
    object Loading : ReviewUiState()
    data class Success(
        val items: List<ReviewItem>,
        val currentFilter: ReviewFilter,
        val totalQuestions: Int,
        val correctCount: Int,
        val incorrectCount: Int
    ) : ReviewUiState()
    data class Error(val message: String) : ReviewUiState()
}
