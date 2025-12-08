package com.playground.test_prep_cu.ui.results

import androidx.lifecycle.ViewModel
import com.playground.test_prep_cu.data.model.QuizScore
import com.playground.test_prep_cu.data.repository.QuizMetadata
import com.playground.test_prep_cu.data.repository.QuizRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for results screen
 */
class ResultsViewModel(private val repository: QuizRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow<ResultsUiState>(ResultsUiState.Loading)
    val uiState: StateFlow<ResultsUiState> = _uiState.asStateFlow()
    
    init {
        loadResults()
    }
    
    private fun loadResults() {
        val attempt = repository.getCurrentAttempt()
        val metadata = repository.getQuizMetadata()
        
        if (attempt == null || !attempt.isCompleted) {
            _uiState.value = ResultsUiState.Error("No completed quiz found")
            return
        }
        
        val score = attempt.getScore()
        val timeTaken = if (attempt.endTime != null) {
            (attempt.endTime - attempt.startTime) / 1000 // seconds
        } else 0L
        
        _uiState.value = ResultsUiState.Success(
            score = score,
            timeTaken = timeTaken,
            passingMarks = 0, // Will calculate based on total
            totalMarks = metadata.totalMarks,
            reviewAllowed = metadata.reviewAfterAttempt
        )
    }
    
    fun resetQuiz() {
        repository.reset()
    }
}

/**
 * UI state for results screen
 */
sealed class ResultsUiState {
    object Loading : ResultsUiState()
    data class Success(
        val score: QuizScore,
        val timeTaken: Long, // in seconds
        val passingMarks: Int,
        val totalMarks: Int,
        val reviewAllowed: Boolean
    ) : ResultsUiState()
    data class Error(val message: String) : ResultsUiState()
}
