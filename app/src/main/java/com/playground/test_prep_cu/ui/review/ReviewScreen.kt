package com.playground.test_prep_cu.ui.review

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Review screen showing all answers with correct/incorrect indicators
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    viewModel: ReviewViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review Answers") }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is ReviewUiState.Loading -> {
                    CircularProgressIndicator()
                }
                
                is ReviewUiState.Success -> {
                    ReviewContent(
                        state = state,
                        onFilterChange = { viewModel.setFilter(it) }
                    )
                }
                
                is ReviewUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewContent(
    state: ReviewUiState.Success,
    onFilterChange: (ReviewFilter) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = state.currentFilter == ReviewFilter.ALL,
                onClick = { onFilterChange(ReviewFilter.ALL) },
                label = { Text("All (${state.totalQuestions})") }
            )
            FilterChip(
                selected = state.currentFilter == ReviewFilter.CORRECT,
                onClick = { onFilterChange(ReviewFilter.CORRECT) },
                label = { Text("Correct (${state.correctCount})") }
            )
            FilterChip(
                selected = state.currentFilter == ReviewFilter.INCORRECT,
                onClick = { onFilterChange(ReviewFilter.INCORRECT) },
                label = { Text("Incorrect (${state.incorrectCount})") }
            )
        }
        
        // Review items list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.items) { item ->
                ReviewItemCard(item)
            }
        }
    }
}

@Composable
private fun ReviewItemCard(item: ReviewItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (item.userAnswer.isCorrect) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Question
            Text(
                text = item.question.statement,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            
            // Options
            item.question.getOptions().forEach { option ->
                val isUserAnswer = option.index == item.userAnswer.selectedOption
                val isCorrect = option.isCorrect
                
                val backgroundColor = when {
                    isCorrect -> MaterialTheme.colorScheme.primary
                    isUserAnswer && !isCorrect -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.surface
                }
                
                val textColor = when {
                    isCorrect || (isUserAnswer && !isCorrect) -> MaterialTheme.colorScheme.onPrimary
                    else -> MaterialTheme.colorScheme.onSurface
                }
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = backgroundColor,
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = option.text,
                            color = textColor
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        if (isUserAnswer && !isCorrect) {
                            Text(
                                text = "✗",
                                color = textColor,
                                fontWeight = FontWeight.Bold
                            )
                        } else if (isCorrect) {
                            Text(
                                text = "✓",
                                color = textColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            // Marks info
            Text(
                text = "Marks: ${item.userAnswer.marksEarned} / ${item.question.marks}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
