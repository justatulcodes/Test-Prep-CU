package com.playground.test_prep_cu.ui.upload

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.playground.test_prep_cu.data.preferences.RecentFile
import java.text.SimpleDateFormat
import java.util.*

/**
 * File upload/selection screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    viewModel: UploadViewModel,
    onQuizLoaded: () -> Unit,
    onInvigilatorMode: () -> Unit = {}
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when (val state = uiState) {
                is UploadUiState.Initial -> {
                    InitialContent(
                        recentFiles = state.recentFiles,
                        onChooseFile = { filePickerLauncher.launch("application/json") },
                        onRecentFileClick = { file ->
                            viewModel.loadQuizFile(Uri.parse(file.uri), context, file.fileName)
                        },
                        onRemoveFile = { uri ->
                            viewModel.removeRecentFile(uri)
                        }
                    )
                }
                
                is UploadUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                is UploadUiState.Success -> {
                    SuccessContent(
                        state = state,
                        onStartQuiz = onQuizLoaded,
                        onInvigilatorMode = onInvigilatorMode,
                        recentFiles = state.recentFiles,
                        onRecentFileClick = { file ->
                            viewModel.loadQuizFile(Uri.parse(file.uri), context, file.fileName)
                        },
                        onRemoveFile = { uri ->
                            viewModel.removeRecentFile(uri)
                        }
                    )
                }
                
                is UploadUiState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onTryAgain = { viewModel.resetState() },
                        recentFiles = state.recentFiles,
                        onRecentFileClick = { file ->
                            viewModel.loadQuizFile(Uri.parse(file.uri), context, file.fileName)
                        },
                        onRemoveFile = { uri ->
                            viewModel.removeRecentFile(uri)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun InitialContent(
    recentFiles: List<RecentFile>,
    onChooseFile: () -> Unit,
    onRecentFileClick: (RecentFile) -> Unit,
    onRemoveFile: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Upload Quiz JSON",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Select a JSON file containing quiz questions",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onChooseFile,
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
            ) {
                Text("Choose New File")
            }
        }
        
        // Recent files list
        if (recentFiles.isNotEmpty()) {
            HorizontalDivider()
            
            Text(
                text = "Recent Files",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(recentFiles) { file ->
                    RecentFileItem(
                        file = file,
                        onClick = { onRecentFileClick(file) },
                        onRemove = { onRemoveFile(file.uri) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SuccessContent(
    state: UploadUiState.Success,
    onStartQuiz: () -> Unit,
    onInvigilatorMode: () -> Unit,
    recentFiles: List<RecentFile>,
    onRecentFileClick: (RecentFile) -> Unit,
    onRemoveFile: (String) -> Unit
) {
    var isInvigilatorMode by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Success info
        Text(
            text = "Quiz Loaded Successfully!",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        
        Card(modifier = Modifier.fillMaxWidth()) {
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
        
        // Invigilator mode toggle
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isInvigilatorMode) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Invigilator Mode",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Quick search questions & view correct answers",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isInvigilatorMode,
                    onCheckedChange = { isInvigilatorMode = it }
                )
            }
        }

        Button(
            onClick = {
                if (isInvigilatorMode) {
                    onInvigilatorMode()
                } else {
                    onStartQuiz()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
        ) {
            Text(if (isInvigilatorMode) "Open Invigilator View" else "Start Quiz")
        }
        
        // Show other recent files
        if (recentFiles.size > 1) {
            HorizontalDivider()
            Text(
                text = "Other Recent Files",
                style = MaterialTheme.typography.titleMedium
            )
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recentFiles.drop(1)) { file ->
                    RecentFileItem(
                        file = file,
                        onClick = { onRecentFileClick(file) },
                        onRemove = { onRemoveFile(file.uri) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onTryAgain: () -> Unit,
    recentFiles: List<RecentFile>,
    onRecentFileClick: (RecentFile) -> Unit,
    onRemoveFile: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Error",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = message,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onTryAgain,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
            ) {
                Text("Try Again")
            }
        }
        
        // Show recent files as fallback
        if (recentFiles.isNotEmpty()) {
            HorizontalDivider()
            Text(
                text = "Recent Files",
                style = MaterialTheme.typography.titleMedium
            )
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recentFiles) { file ->
                    RecentFileItem(
                        file = file,
                        onClick = { onRecentFileClick(file) },
                        onRemove = { onRemoveFile(file.uri) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentFileItem(
    file: RecentFile,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.fileName,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "${file.questionsCount} questions • ${formatTime(file.lastAccessedTime)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> "${diff / 3600000}h ago"
        diff < 604800000 -> "${diff / 86400000}d ago"
        else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
    }
}
