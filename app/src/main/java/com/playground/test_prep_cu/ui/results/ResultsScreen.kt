package com.playground.test_prep_cu.ui.results

import androidx.activity.compose.BackHandler
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
    onNewQuiz: () -> Unit,
    onBackToUpload: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Handle back button press to go to upload screen
    BackHandler {
        viewModel.resetQuiz()
        onBackToUpload()
    }
    
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
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Your Score",
                    style = MaterialTheme.typography.titleMedium
                )
                
                // Circular Progress
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.size(150.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                        strokeWidth = 12.dp,
                    )
                    CircularProgressIndicator(
                        progress = { state.score.percentage.toFloat() / 100f },
                        modifier = Modifier.size(150.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 12.dp,
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${String.format("%.0f", state.score.percentage)}%",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onSurface 
                        )
                    }
                }

                Text(
                    text = "${state.score.correct} / ${state.score.totalQuestions} Questions Correct",
                    style = MaterialTheme.typography.bodyLarge
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
                MetricRow("Questions Attempted", state.score.totalQuestions.toString())
                MetricRow("Correct Answers", state.score.correct.toString())
                MetricRow("Incorrect Answers", state.score.incorrect.toString())
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                MetricRow("Pass Percentage", "60%") // Typical passing grade
                MetricRow("Your Percentage", String.format("%.1f%%", state.score.percentage))
                val passed = state.score.percentage >= 60
                MetricRow(
                    "Status", 
                    if (passed) "✓ Passed" else "✗ Failed"
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                MetricRow("Total Marks Earned", String.format("%.1f / %d", state.score.totalMarksEarned, state.totalMarks))
                MetricRow("Time Taken", formatTime(state.timeTaken))
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Action Buttons
        if (state.reviewAllowed) {
            Button(
                onClick = onReviewAnswers,
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
            ) {
                Text("Review Answers")
            }
        }
        
        OutlinedButton(
            onClick = onNewQuiz,
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
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
