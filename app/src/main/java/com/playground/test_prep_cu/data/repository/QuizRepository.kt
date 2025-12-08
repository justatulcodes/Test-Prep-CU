package com.playground.test_prep_cu.data.repository

import android.content.Context
import android.net.Uri
import com.playground.test_prep_cu.data.model.Quiz
import com.playground.test_prep_cu.data.model.QuizAttempt
import com.playground.test_prep_cu.data.model.UserAnswer
import com.playground.test_prep_cu.data.parser.QuizJsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Repository for managing quiz data and user attempts
 */
class QuizRepository(private val context: Context) {
    
    private val parser = QuizJsonParser()
    
    // In-memory storage (can be replaced with Room database later)
    private var currentQuizzes: List<Quiz> = emptyList()
    private var currentAttempt: QuizAttempt? = null
    
    /**
     * Load quizzes from a file URI
     */
    suspend fun loadQuizzesFromUri(uri: Uri): Result<List<Quiz>> = withContext(Dispatchers.IO) {
        try {
            val jsonString = readJsonFromUri(uri)
            val result = parser.parseQuizJson(jsonString)
            
            if (result.isSuccess) {
                currentQuizzes = result.getOrNull() ?: emptyList()
            }
            
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Read JSON content from URI
     */
    private fun readJsonFromUri(uri: Uri): String {
        val stringBuilder = StringBuilder()
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    stringBuilder.append(line)
                }
            }
        }
        return stringBuilder.toString()
    }
    
    /**
     * Get all loaded quizzes
     */
    fun getQuizzes(): List<Quiz> = currentQuizzes
    
    /**
     * Get all questions from all loaded quizzes
     */
    fun getAllQuestions(): List<com.playground.test_prep_cu.data.model.Question> {
        return currentQuizzes.flatMap { it.questions }
    }
    
    /**
     * Start a new quiz attempt
     */
    fun startQuizAttempt(quizId: Int): QuizAttempt {
        val attempt = QuizAttempt(
            quizId = quizId,
            startTime = System.currentTimeMillis(),
            endTime = null,
            currentQuestionIndex = 0,
            answers = emptyList(),
            isCompleted = false
        )
        currentAttempt = attempt
        return attempt
    }
    
    /**
     * Start attempt for all questions combined
     */
    fun startCombinedQuizAttempt(): QuizAttempt {
        val attempt = QuizAttempt(
            quizId = -1, // -1 indicates combined quiz
            startTime = System.currentTimeMillis(),
            endTime = null,
            currentQuestionIndex = 0,
            answers = emptyList(),
            isCompleted = false
        )
        currentAttempt = attempt
        return attempt
    }
    
    /**
     * Get current quiz attempt
     */
    fun getCurrentAttempt(): QuizAttempt? = currentAttempt
    
    /**
     * Submit an answer for the current question
     */
    fun submitAnswer(
        questionId: Int,
        selectedOption: Int,
        isCorrect: Boolean,
        marksEarned: Double
    ) {
        currentAttempt?.let { attempt ->
            val answer = UserAnswer(
                questionId = questionId,
                selectedOption = selectedOption,
                isCorrect = isCorrect,
                marksEarned = marksEarned
            )
            
            // Remove existing answer for this question if any
            val updatedAnswers = attempt.answers.filter { it.questionId != questionId } + answer
            
            currentAttempt = attempt.copy(answers = updatedAnswers)
        }
    }
    
    /**
     * Move to next question
     */
    fun moveToNextQuestion() {
        currentAttempt?.let { attempt ->
            currentAttempt = attempt.copy(
                currentQuestionIndex = attempt.currentQuestionIndex + 1
            )
        }
    }
    
    /**
     * Move to previous question
     */
    fun moveToPreviousQuestion() {
        currentAttempt?.let { attempt ->
            if (attempt.currentQuestionIndex > 0) {
                currentAttempt = attempt.copy(
                    currentQuestionIndex = attempt.currentQuestionIndex - 1
                )
            }
        }
    }
    
    /**
     * Complete the quiz attempt
     */
    fun completeQuizAttempt(): QuizAttempt? {
        currentAttempt?.let { attempt ->
            val completed = attempt.copy(
                endTime = System.currentTimeMillis(),
                isCompleted = true
            )
            currentAttempt = completed
            return completed
        }
        return null
    }
    
    /**
     * Reset quiz data
     */
    fun reset() {
        currentQuizzes = emptyList()
        currentAttempt = null
    }
    
    /**
     * Get quiz metadata
     */
    fun getQuizMetadata(): QuizMetadata {
        val totalQuestions = getAllQuestions().size
        val totalMarks = currentQuizzes.sumOf { it.totalMarks }
        val duration = currentQuizzes.sumOf { it.duration }
        val reviewAfterAttempt = currentQuizzes.any { it.reviewAfterAttempt }
        
        return QuizMetadata(
            totalQuestions = totalQuestions,
            totalMarks = totalMarks,
            duration = duration,
            reviewAfterAttempt = reviewAfterAttempt
        )
    }
}

/**
 * Quiz metadata summary
 */
data class QuizMetadata(
    val totalQuestions: Int,
    val totalMarks: Int,
    val duration: Int,
    val reviewAfterAttempt: Boolean
)
