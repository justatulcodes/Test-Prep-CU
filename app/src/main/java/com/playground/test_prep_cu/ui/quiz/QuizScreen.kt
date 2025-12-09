package com.playground.test_prep_cu.ui.quiz

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape


/**
 * Quiz screen displaying questions and options
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    viewModel: QuizViewModel,
    onQuizCompleted: () -> Unit,
    onBackToUpload: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Handle back button press
    BackHandler {
        val shouldShowResults = viewModel.handleBackPress()
        if (shouldShowResults) {
            // Navigate to results for partial completion
            onQuizCompleted()
        } else {
            // Navigate back to upload
            onBackToUpload()
        }
    }
    
    // Navigate to results when quiz is completed normally
    LaunchedEffect(key1 = uiState) {
        if (uiState is QuizUiState.Completed) {
            onQuizCompleted()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz") }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is QuizUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                is QuizUiState.QuestionState -> {
                    QuestionContent(
                        state = state,
                        onOptionSelected = { viewModel.selectOption(it) },
                        onPrevious = { viewModel.previousQuestion() },
                        onNext = { viewModel.nextQuestion() },
                        onSubmitAnswer = { viewModel.submitAnswer() },
                        onSubmitQuiz = { viewModel.submitQuiz() }
                    )
                }
                
                is QuizUiState.Error -> {
                    Text(
                        text = state.message,
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                is QuizUiState.Completed -> {
                    // Will navigate away
                }
            }
        }
    }
}

@Composable
private fun QuestionContent(
    state: QuizUiState.QuestionState,
    onOptionSelected: (Int) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSubmitAnswer: () -> Unit,
    onSubmitQuiz: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Progress indicator
        LinearProgressIndicator(
            progress = { state.questionNumber.toFloat() / state.totalQuestions },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outlineVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Question ${state.questionNumber} of ${state.totalQuestions}",
            style = MaterialTheme.typography.bodySmall
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Question statement
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Text(
                text = state.question.statement,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(32.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Options with answer feedback
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            state.question.getOptions().forEach { option ->
                OptionItemWithFeedback(
                    option = option,
                    isSelected = state.selectedOption == option.index,
                    isAnswerSubmitted = state.isAnswerSubmitted,
                    onSelect = { onOptionSelected(option.index) }
                )
            }
        }
        
        // Feedback message after submission
        if (state.isAnswerSubmitted && state.selectedOption != null) {
            Spacer(modifier = Modifier.height(8.dp))
            
            val isCorrect = state.selectedOption == state.question.correctOption
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isCorrect) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.errorContainer
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isCorrect) {
                        "✓ Correct! Well done!"
                    } else {
                        "✗ Incorrect. The correct answer is highlighted in green."
                    },
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isCorrect) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = onPrevious,
                enabled = state.canGoPrevious,
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Previous")
            }
            
            // Show Submit or Next/Submit Quiz based on state
            if (!state.isAnswerSubmitted) {
                // Show Submit Answer button
                Button(
                    onClick = onSubmitAnswer,
                    enabled = state.canSubmit,
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Submit Answer")
                }
            } else {
                // Show Next or Submit Quiz button
                if (state.isLastQuestion) {
                    Button(
                        onClick = onSubmitQuiz,
                        enabled = state.canGoNext,
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Submit Quiz")
                    }
                } else {
                    Button(
                        onClick = onNext,
                        enabled = state.canGoNext,
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Next")
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionItemWithFeedback(
    option: com.playground.test_prep_cu.data.model.QuizOption,
    isSelected: Boolean,
    isAnswerSubmitted: Boolean,
    onSelect: () -> Unit
) {
    // Determine colors based on submission state
    val backgroundColor = when {
        isAnswerSubmitted && option.isCorrect -> MaterialTheme.colorScheme.primaryContainer
        isAnswerSubmitted && isSelected && !option.isCorrect -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surface
    }
    
    val borderColor = when {
        isAnswerSubmitted && option.isCorrect -> MaterialTheme.colorScheme.primary
        isAnswerSubmitted && isSelected && !option.isCorrect -> MaterialTheme.colorScheme.error
        else -> Color.Transparent
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = { if (!isAnswerSubmitted) onSelect() },
                enabled = !isAnswerSubmitted
            ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected && !isAnswerSubmitted) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isAnswerSubmitted && (option.isCorrect || isSelected)) 2.dp else if (isSelected) 4.dp else 2.dp,
            color = if (isSelected && !isAnswerSubmitted) MaterialTheme.colorScheme.primary else borderColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isAnswerSubmitted) {
                // Show radio button before submission
                RadioButton(
                    selected = isSelected,
                    onClick = onSelect,
                    enabled = !isAnswerSubmitted
                )
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                // Show icon after submission
                val icon = when {
                    option.isCorrect -> "✓"
                    isSelected && !option.isCorrect -> "✗"
                    else -> ""
                }
                
                if (icon.isNotEmpty()) {
                    Text(
                        text = icon,
                        style = MaterialTheme.typography.titleLarge,
                        color = if (option.isCorrect) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        modifier = Modifier.width(40.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.width(40.dp))
                }
            }
            
            Text(
                text = option.text,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
