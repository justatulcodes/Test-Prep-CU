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
                        onSubmit = { viewModel.submitQuiz() }
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
    onSubmit: () -> Unit
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
        
        // Options
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            state.question.getOptions().forEach { option ->
                OptionItem(
                    option = option,
                    isSelected = state.selectedOption == option.index,
                    onSelect = { onOptionSelected(option.index) }
                )
            }
        }
        
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
            
            if (state.isLastQuestion) {
                Button(
                    onClick = onSubmit,
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

@Composable
private fun OptionItem(
    option: com.playground.test_prep_cu.data.model.QuizOption,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onSelect
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = option.text,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
