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
    private var isAnswerSubmitted = false
    
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
        
        // If question was already answered, show it as submitted
        if (previousAnswer != null) {
            selectedOption = previousAnswer.selectedOption
            isAnswerSubmitted = true
        } else {
            selectedOption = null
            isAnswerSubmitted = false
        }
        
        _uiState.value = QuizUiState.QuestionState(
            question = question,
            questionNumber = currentIndex + 1,
            totalQuestions = allQuestions.size,
            selectedOption = selectedOption,
            isAnswerSubmitted = isAnswerSubmitted,
            canGoPrevious = currentIndex > 0,
            canSubmit = selectedOption != null && !isAnswerSubmitted,
            canGoNext = isAnswerSubmitted,
            isLastQuestion = currentIndex == allQuestions.size - 1
        )
    }
    
    /**
     * Select an option for current question
     */
    fun selectOption(optionIndex: Int) {
        if (isAnswerSubmitted) return // Don't allow changing after submit
        
        selectedOption = optionIndex
        
        val currentState = _uiState.value
        if (currentState is QuizUiState.QuestionState) {
            _uiState.value = currentState.copy(
                selectedOption = optionIndex,
                canSubmit = true
            )
        }
    }
    
    /**
     * Submit the current answer and show correct answer
     */
    fun submitAnswer() {
        val currentState = _uiState.value
        if (currentState !is QuizUiState.QuestionState || selectedOption == null || isAnswerSubmitted) return
        
        // Mark as submitted
        isAnswerSubmitted = true
        
        val question = currentState.question
        val isCorrect = selectedOption == question.correctOption
        val marksEarned = if (isCorrect) question.marks else 0.0
        
        // Save answer to repository
        repository.submitAnswer(
            questionId = question.id,
            selectedOption = selectedOption!!,
            isCorrect = isCorrect,
            marksEarned = marksEarned
        )
        
        // Update UI to show answer feedback
        _uiState.value = currentState.copy(
            isAnswerSubmitted = true,
            canSubmit = false,
            canGoNext = true
        )
    }
    
    /**
     * Move to next question
     */
    fun nextQuestion() {
        if (!isAnswerSubmitted) return // Must submit before next
        
        repository.moveToNextQuestion()
        selectedOption = null
        isAnswerSubmitted = false
        
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
        // If current answer not submitted yet, submit it first
        if (!isAnswerSubmitted && selectedOption != null) {
            submitAnswer()
        }
        
        repository.completeQuizAttempt()
        _uiState.value = QuizUiState.Completed
    }
    
    /**
     * Handle back button press
     * Returns true if should show results, false if should go to upload screen
     */
    fun handleBackPress(): Boolean {
        val attempt = repository.getCurrentAttempt()
        
        // If no attempt or no answers, go back to upload
        if (attempt == null || attempt.answers.isEmpty()) {
            repository.reset()
            return false
        }
        
        // If user has attempted questions, complete quiz and show partial results
        repository.completeQuizAttempt()
        _uiState.value = QuizUiState.Completed
        return true
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
        val isAnswerSubmitted: Boolean,
        val canGoPrevious: Boolean,
        val canSubmit: Boolean,
        val canGoNext: Boolean,
        val isLastQuestion: Boolean
    ) : QuizUiState()
    object Completed : QuizUiState()
    data class Error(val message: String) : QuizUiState()
}
