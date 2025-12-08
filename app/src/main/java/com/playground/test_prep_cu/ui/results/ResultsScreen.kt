package com.playground.test_prep_cu.ui.results

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Results screen showing quiz score and performance
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    viewModel: ResultsViewModel,
    onReviewAnswers: () -> Unit,
    onNewQuiz: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz Results") }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is ResultsUiState.Loading -> {
                    CircularProgressIndicator()
                }
                
                is ResultsUiState.Success -> {
                    ResultsContent(
                        state = state,
                        onReviewAnswers = onReviewAnswers,
                        onNewQuiz = {
                            viewModel.resetQuiz()
                            onNewQuiz()
                        }
                    )
                }
                
                is ResultsUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultsContent(
    state: ResultsUiState.Success,
    onReviewAnswers: () -> Unit,
    onNewQuiz: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Text(
            text = "Quiz Completed!",
            style = MaterialTheme.typography.headlineMedium
        )
        
        // Score Card
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Your Score",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Text(
                    text = "${state.score.correct}/${state.score.totalQuestions}",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = "${String.format("%.1f", state.score.percentage)}%",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
        
        // Performance Metrics
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricRow("Correct Answers", state.score.correct.toString())
                MetricRow("Incorrect Answers", state.score.incorrect.toString())
                MetricRow("Total Marks Earned", String.format("%.1f", state.score.totalMarksEarned))
                MetricRow("Total Marks", state.totalMarks.toString())
                MetricRow("Time Taken", formatTime(state.timeTaken))
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Action Buttons
        if (state.reviewAllowed) {
            Button(
                onClick = onReviewAnswers,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Review Answers")
            }
        }
        
        OutlinedButton(
            onClick = onNewQuiz,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start New Quiz")
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

private fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", minutes, secs)
}
