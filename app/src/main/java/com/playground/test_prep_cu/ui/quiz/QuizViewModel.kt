package com.playground.test_prep_cu.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.playground.test_prep_cu.data.model.Question
import com.playground.test_prep_cu.data.model.QuizAttempt
import com.playground.test_prep_cu.data.repository.QuizRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for quiz screen
 */
class QuizViewModel(private val repository: QuizRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Loading)
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()
    
    private var allQuestions: List<Question> = emptyList()
    private var selectedOption: Int? = null
    
    init {
        loadQuiz()
    }
    
    private fun loadQuiz() {
        viewModelScope.launch {
            allQuestions = repository.getAllQuestions()
            
            if (allQuestions.isEmpty()) {
                _uiState.value = QuizUiState.Error("No questions found")
                return@launch
            }
            
            // Start quiz attempt
            repository.startCombinedQuizAttempt()
            
            updateCurrentQuestion()
        }
    }
    
    private fun updateCurrentQuestion() {
        val attempt = repository.getCurrentAttempt() ?: return
        val currentIndex = attempt.currentQuestionIndex
        
        if (currentIndex >= allQuestions.size) {
            // Quiz completed
            _uiState.value = QuizUiState.Completed
            return
        }
        
        val question = allQuestions[currentIndex]
        val previousAnswer = attempt.answers.find { it.questionId == question.id }
        selectedOption = previousAnswer?.selectedOption
        
        _uiState.value = QuizUiState.QuestionState(
            question = question,
            questionNumber = currentIndex + 1,
            totalQuestions = allQuestions.size,
            selectedOption = selectedOption,
            canGoPrevious = currentIndex > 0,
            canGoNext = selectedOption != null,
            isLastQuestion = currentIndex == allQuestions.size - 1
        )
    }
    
    /**
     * Select an option for current question
     */
    fun selectOption(optionIndex: Int) {
        selectedOption = optionIndex
        
        val currentState = _uiState.value
        if (currentState is QuizUiState.QuestionState) {
            _uiState.value = currentState.copy(
                selectedOption = optionIndex,
                canGoNext = true
            )
        }
    }
    
    /**
     * Submit current answer and move to next question
     */
    fun nextQuestion() {
        val currentState = _uiState.value
        if (currentState !is QuizUiState.QuestionState || selectedOption == null) return
        
        // Submit answer
        val question = currentState.question
        val isCorrect = selectedOption == question.correctOption
        val marksEarned = if (isCorrect) question.marks else 0.0
        
        repository.submitAnswer(
            questionId = question.id,
            selectedOption = selectedOption!!,
            isCorrect = isCorrect,
            marksEarned = marksEarned
        )
        
        // Move to next question
        repository.moveToNextQuestion()
        selectedOption = null
        
        updateCurrentQuestion()
    }
    
    /**
     * Go to previous question
     */
    fun previousQuestion() {
        repository.moveToPreviousQuestion()
        updateCurrentQuestion()
    }
    
    /**
     * Submit the entire quiz
     */
    fun submitQuiz() {
        // Submit current answer if any
        val currentState = _uiState.value
        if (currentState is QuizUiState.QuestionState && selectedOption != null) {
            val question = currentState.question
            val isCorrect = selectedOption == question.correctOption
            val marksEarned = if (isCorrect) question.marks else 0.0
            
            repository.submitAnswer(
                questionId = question.id,
                selectedOption = selectedOption!!,
                isCorrect = isCorrect,
                marksEarned = marksEarned
            )
        }
        
        repository.completeQuizAttempt()
        _uiState.value = QuizUiState.Completed
    }
}

/**
 * UI state for quiz screen
 */
sealed class QuizUiState {
    object Loading : QuizUiState()
    data class QuestionState(
        val question: Question,
        val questionNumber: Int,
        val totalQuestions: Int,
        val selectedOption: Int?,
        val canGoPrevious: Boolean,
        val canGoNext: Boolean,
        val isLastQuestion: Boolean
    ) : QuizUiState()
    object Completed : QuizUiState()
    data class Error(val message: String) : QuizUiState()
}
