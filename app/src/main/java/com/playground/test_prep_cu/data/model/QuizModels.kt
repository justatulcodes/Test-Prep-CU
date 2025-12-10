package com.playground.test_prep_cu.data.model

import com.google.gson.annotations.SerializedName

/**
 * Main quiz data model matching the JSON structure
 */
data class Quiz(
    val id: Int,
    val semesterSubjectChapterId: Int,
    val semesterSubjectChapterName: String,
    val programBatchId: Int,
    val programBatchName: String,
    val assignmentType: String,
    val questionMeta: String,
    val reviewAfterAttempt: Boolean,
    val negativeMarks: Int,
    val isPublished: String,
    val difficultyLevel: String,
    val testType: String,
    val title: String,
    val description: String,
    val totalQuestions: Int,
    val duration: Int, // in minutes
    val filePath: String?,
    val submissionMode: String?,
    val isGraded: String,
    val totalMarks: Int,
    val passingMarks: Int,
    val startDate: Long,
    val endDate: Long,
    val attemptAfterDueDate: String,
    val allowedMultiple: String,
    val showGradesOnly: String?,
    val status: String,
    val isDeleted: String,
    val assignmentFormat: String,
    val questions: List<Question>,
    val assignmentId: Int,
    val maxAttempts: Int,
    val negative: Boolean
)

/**
 * Question data model
 */
data class Question(
    val id: Int,
    val semesterSubjectChapterId: Int,
    val isPractice: String,
    val difficultyLevel: String,
    val statement: String,
    val option1: String,
    val option2: String,
    val option3: String,
    val option4: String,
    val option5: String?,
    val option6: String?,
    val marks: Double,
    val status: String,
    val correctOption: Int // 1-indexed (1, 2, 3, 4, 5, or 6)
) {
    /**
     * Get all non-null, non-empty options
     */
    fun getOptions(): List<QuizOption> {
        return buildList {
            option1.takeIf { it.isNotEmpty() }?.let { add(QuizOption(1, it, correctOption == 1)) }
            option2.takeIf { it.isNotEmpty() }?.let { add(QuizOption(2, it, correctOption == 2)) }
            option3.takeIf { it.isNotEmpty() }?.let { add(QuizOption(3, it, correctOption == 3)) }
            option4.takeIf { it.isNotEmpty() }?.let { add(QuizOption(4, it, correctOption == 4)) }
            option5?.takeIf { it.isNotEmpty() }?.let { add(QuizOption(5, it, correctOption == 5)) }
            option6?.takeIf { it.isNotEmpty() }?.let { add(QuizOption(6, it, correctOption == 6)) }
        }
    }
}

/**
 * Normalized option for easy UI rendering
 */
data class QuizOption(
    val index: Int,
    val text: String,
    val isCorrect: Boolean
)

/**
 * User's answer to a question
 */
data class UserAnswer(
    val questionId: Int,
    val selectedOption: Int, // 1-indexed
    val isCorrect: Boolean,
    val marksEarned: Double
)

/**
 * Quiz attempt state
 */
data class QuizAttempt(
    val quizId: Int,
    val startTime: Long,
    val endTime: Long?,
    val currentQuestionIndex: Int,
    val answers: List<UserAnswer>,
    val isCompleted: Boolean
) {
    fun getScore(totalQuestionsInQuiz: Int = answers.size): QuizScore {
        val correct = answers.count { it.isCorrect }
        val incorrect = answers.count { !it.isCorrect }
        val totalMarks = answers.sumOf { it.marksEarned }
        val attemptedQuestions = answers.size
        val skippedQuestions = totalQuestionsInQuiz - attemptedQuestions

        // Calculate percentage based on attempted questions only for partial attempts
        val percentageOfAttempted = if (attemptedQuestions > 0) (correct.toDouble() / attemptedQuestions * 100) else 0.0
        // Calculate percentage based on total questions for overall score
        val percentageOfTotal = if (totalQuestionsInQuiz > 0) (correct.toDouble() / totalQuestionsInQuiz * 100) else 0.0

        return QuizScore(
            correct = correct,
            incorrect = incorrect,
            totalQuestions = attemptedQuestions,
            totalQuestionsInQuiz = totalQuestionsInQuiz,
            skippedQuestions = skippedQuestions,
            totalMarksEarned = totalMarks,
            percentage = percentageOfAttempted,
            overallPercentage = percentageOfTotal,
            isPartialAttempt = attemptedQuestions < totalQuestionsInQuiz
        )
    }
}

/**
 * Quiz score summary
 */
data class QuizScore(
    val correct: Int,
    val incorrect: Int,
    val totalQuestions: Int, // Questions attempted
    val totalMarksEarned: Double,
    val percentage: Double, // Percentage of attempted questions
    val totalQuestionsInQuiz: Int = totalQuestions, // Total questions in the quiz
    val skippedQuestions: Int = 0,
    val overallPercentage: Double = percentage, // Percentage based on total questions
    val isPartialAttempt: Boolean = false
)
