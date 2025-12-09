package com.playground.test_prep_cu.ui.upload

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.playground.test_prep_cu.data.preferences.QuizPreferences
import com.playground.test_prep_cu.data.preferences.RecentFile
import com.playground.test_prep_cu.data.repository.QuizRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for file upload/selection screen
 */
class UploadViewModel(
    private val repository: QuizRepository,
    private val preferences: QuizPreferences
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<UploadUiState>(UploadUiState.Initial(emptyList()))
    val uiState: StateFlow<UploadUiState> = _uiState.asStateFlow()
    
    init {
        loadRecentFiles()
    }
    
    private fun loadRecentFiles() {
        val recentFiles = preferences.getRecentFiles()
        _uiState.value = UploadUiState.Initial(recentFiles)
    }
    
    /**
     * Load quiz from file URI
     */
    fun loadQuizFile(uri: Uri, context: Context, fileName: String = "") {
        viewModelScope.launch {
            _uiState.value = UploadUiState.Loading
            
            val result = repository.loadQuizzesFromUri(uri)
            
            _uiState.value = if (result.isSuccess) {
                val quizzes = result.getOrNull() ?: emptyList()
                val metadata = repository.getQuizMetadata()
                
                // Save to recent files
                val name = fileName.ifEmpty { uri.lastPathSegment ?: "Quiz File" }
                preferences.saveRecentFile(uri, name, metadata.totalQuestions)
                
                // Reload recent files
                val recentFiles = preferences.getRecentFiles()
                
                UploadUiState.Success(
                    quizzesCount = quizzes.size,
                    totalQuestions = metadata.totalQuestions,
                    totalMarks = metadata.totalMarks,
                    duration = metadata.duration,
                    recentFiles = recentFiles
                )
            } else {
                val recentFiles = preferences.getRecentFiles()
                UploadUiState.Error(
                    message = result.exceptionOrNull()?.message ?: "Failed to load quiz",
                    recentFiles = recentFiles
                )
            }
        }
    }
    
    fun removeRecentFile(uri: String) {
        preferences.removeRecentFile(uri)
        loadRecentFiles()
    }
    
    fun resetState() {
        loadRecentFiles()
    }
}

/**
 * UI state for upload screen
 */
sealed class UploadUiState {
    data class Initial(val recentFiles: List<RecentFile>) : UploadUiState()
    object Loading : UploadUiState()
    data class Success(
        val quizzesCount: Int,
        val totalQuestions: Int,
        val totalMarks: Int,
        val duration: Int,
        val recentFiles: List<RecentFile>
    ) : UploadUiState()
    data class Error(
        val message: String,
        val recentFiles: List<RecentFile>
    ) : UploadUiState()
}
