package com.playground.test_prep_cu.ui.upload

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * File upload/selection screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    viewModel: UploadViewModel,
    onQuizLoaded: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.loadQuizFile(it, context)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Test Prep CU") }
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
                is UploadUiState.Initial -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Upload Quiz JSON",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = "Select a JSON file containing quiz questions",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { filePickerLauncher.launch("application/json") }
                        ) {
                            Text("Choose File")
                        }
                    }
                }
                
                is UploadUiState.Loading -> {
                    CircularProgressIndicator()
                }
                
                is UploadUiState.Success -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Quiz Loaded Successfully!",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Quizzes: ${state.quizzesCount}")
                                Text("Total Questions: ${state.totalQuestions}")
                                Text("Total Marks: ${state.totalMarks}")
                                Text("Duration: ${state.duration} minutes")
                            }
                        }
                        
                        Button(
                            onClick = onQuizLoaded,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Start Quiz")
                        }
                    }
                }
                
                is UploadUiState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Error",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = state.message,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = {
                                viewModel.resetState()
                            }
                        ) {
                            Text("Try Again")
                        }
                    }
                }
            }
        }
    }
}
