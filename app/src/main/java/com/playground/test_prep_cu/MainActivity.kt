package com.playground.test_prep_cu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.playground.test_prep_cu.data.repository.QuizRepository
import com.playground.test_prep_cu.navigation.AppNavHost
import com.playground.test_prep_cu.ui.theme.TestPrepCUTheme

class MainActivity : ComponentActivity() {
    
    private lateinit var repository: QuizRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize repository
        repository = QuizRepository(applicationContext)
        
        enableEdgeToEdge()
        setContent {
            TestPrepCUTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavHost(
                        navController = navController,
                        repository = repository
                    )
                }
            }
        }
    }
}