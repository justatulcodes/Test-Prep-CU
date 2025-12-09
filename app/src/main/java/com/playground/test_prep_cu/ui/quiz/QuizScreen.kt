package com.playground.test_prep_cu.ui.quiz

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Quiz screen displaying questions and options
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    viewModel: QuizViewModel,
    onQuizCompleted: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Navigate to results when quiz is completed
    LaunchedEffect(uiState) {
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
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Question ${state.questionNumber} of ${state.totalQuestions}",
            style = MaterialTheme.typography.bodySmall
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Question statement
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = state.question.statement,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp)
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
                enabled = state.canGoPrevious
            ) {
                Text("Previous")
            }
            
            // Show Submit or Next/Submit Quiz based on state
            if (!state.isAnswerSubmitted) {
                // Show Submit Answer button
                Button(
                    onClick = onSubmitAnswer,
                    enabled = state.canSubmit
                ) {
                    Text("Submit Answer")
                }
            } else {
                // Show Next or Submit Quiz button
                if (state.isLastQuestion) {
                    Button(
                        onClick = onSubmitQuiz,
                        enabled = state.canGoNext
                    ) {
                        Text("Submit Quiz")
                    }
                } else {
                    Button(
                        onClick = onNext,
                        enabled = state.canGoNext
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
        isSelected && !isAnswerSubmitted -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surface
    }
    
    val borderColor = when {
        isAnswerSubmitted && option.isCorrect -> MaterialTheme.colorScheme.primary
        isAnswerSubmitted && isSelected && !option.isCorrect -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = { if (!isAnswerSubmitted) onSelect() },
                enabled = !isAnswerSubmitted
            ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isAnswerSubmitted && (option.isCorrect || isSelected)) 2.dp else 1.dp,
            color = borderColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
