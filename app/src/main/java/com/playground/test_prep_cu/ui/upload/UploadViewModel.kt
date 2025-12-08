package com.playground.test_prep_cu.ui.upload

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.playground.test_prep_cu.data.repository.QuizRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for file upload/selection screen
 */
class UploadViewModel(private val repository: QuizRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow<UploadUiState>(UploadUiState.Initial)
    val uiState: StateFlow<UploadUiState> = _uiState.asStateFlow()
    
    /**
     * Load quiz from file URI
     */
    fun loadQuizFile(uri: Uri, context: Context) {
        viewModelScope.launch {
            _uiState.value = UploadUiState.Loading
            
            val result = repository.loadQuizzesFromUri(uri)
            
            _uiState.value = if (result.isSuccess) {
                val quizzes = result.getOrNull() ?: emptyList()
                val metadata = repository.getQuizMetadata()
                UploadUiState.Success(
                    quizzesCount = quizzes.size,
                    totalQuestions = metadata.totalQuestions,
                    totalMarks = metadata.totalMarks,
                    duration = metadata.duration
                )
            } else {
                UploadUiState.Error(result.exceptionOrNull()?.message ?: "Failed to load quiz")
            }
        }
    }
    
    fun resetState() {
        _uiState.value = UploadUiState.Initial
    }
}

/**
 * UI state for upload screen
 */
sealed class UploadUiState {
    object Initial : UploadUiState()
    object Loading : UploadUiState()
    data class Success(
        val quizzesCount: Int,
        val totalQuestions: Int,
        val totalMarks: Int,
        val duration: Int
    ) : UploadUiState()
    data class Error(val message: String) : UploadUiState()
}
