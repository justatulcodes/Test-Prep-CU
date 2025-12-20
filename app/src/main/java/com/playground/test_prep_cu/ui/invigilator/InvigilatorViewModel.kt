package com.playground.test_prep_cu.ui.invigilator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.playground.test_prep_cu.data.model.Question
import com.playground.test_prep_cu.data.repository.QuizRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for invigilator mode screen
 */
class InvigilatorViewModel(
    private val repository: QuizRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<InvigilatorUiState>(InvigilatorUiState.Loading)
    val uiState: StateFlow<InvigilatorUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var allQuestions: List<Question> = emptyList()

    init {
        loadQuestions()
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            try {
                allQuestions = repository.getAllQuestions()
                if (allQuestions.isEmpty()) {
                    _uiState.value = InvigilatorUiState.Error("No questions loaded")
                } else {
                    _uiState.value = InvigilatorUiState.Success(
                        questions = allQuestions,
                        filteredQuestions = allQuestions,
                        searchQuery = ""
                    )
                }
            } catch (e: Exception) {
                _uiState.value = InvigilatorUiState.Error(e.message ?: "Failed to load questions")
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        filterQuestions(query)
    }

    private fun filterQuestions(query: String) {
        val currentState = _uiState.value
        if (currentState is InvigilatorUiState.Success) {
            val filtered = if (query.isBlank()) {
                allQuestions
            } else {
                allQuestions.filter { question ->
                    question.statement.contains(query, ignoreCase = true)
                }
            }
            _uiState.value = currentState.copy(
                filteredQuestions = filtered,
                searchQuery = query
            )
        }
    }
}

/**
 * UI state for invigilator screen
 */
sealed class InvigilatorUiState {
    object Loading : InvigilatorUiState()

    data class Success(
        val questions: List<Question>,
        val filteredQuestions: List<Question>,
        val searchQuery: String
    ) : InvigilatorUiState()

    data class Error(val message: String) : InvigilatorUiState()
}

