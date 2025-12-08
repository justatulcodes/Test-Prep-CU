package com.playground.test_prep_cu.data.parser

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.playground.test_prep_cu.data.model.Quiz

/**
 * Parses quiz JSON data
 */
class QuizJsonParser {
    
    private val gson = Gson()
    
    /**
     * Parse JSON string containing a single quiz, array of quizzes, or multiple concatenated quiz objects
     * @return List of Quiz objects or empty list if parsing fails
     */
    fun parseQuizJson(jsonString: String): Result<List<Quiz>> {
        return try {
            // Try different parsing strategies
            val quizzes = tryParseAsArray(jsonString) 
                ?: tryParseAsSingle(jsonString)
                ?: tryParseAsMultipleObjects(jsonString)
            
            if (quizzes != null && quizzes.isNotEmpty()) {
                Result.success(quizzes)
            } else {
                Result.failure(IllegalArgumentException("No valid quiz data found"))
            }
        } catch (e: JsonSyntaxException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun tryParseAsArray(jsonString: String): List<Quiz>? {
        return try {
            val listType = object : TypeToken<List<Quiz>>() {}.type
            gson.fromJson<List<Quiz>>(jsonString, listType)
        } catch (e: Exception) {
            null
        }
    }
    
    private fun tryParseAsSingle(jsonString: String): List<Quiz>? {
        return try {
            val quiz = gson.fromJson(jsonString, Quiz::class.java)
            listOf(quiz)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Parse multiple JSON objects concatenated together (not in an array)
     * Handles format like: {...}{...}{...}
     */
    private fun tryParseAsMultipleObjects(jsonString: String): List<Quiz>? {
        return try {
            val quizzes = mutableListOf<Quiz>()
            val trimmed = jsonString.trim()
            
            // Split by "}{" pattern and restore the braces
            val objectStrings = mutableListOf<String>()
            var currentObject = StringBuilder()
            var braceCount = 0
            
            for (char in trimmed) {
                when (char) {
                    '{' -> {
                        braceCount++
                        currentObject.append(char)
                    }
                    '}' -> {
                        braceCount--
                        currentObject.append(char)
                        // When braces are balanced, we have a complete object
                        if (braceCount == 0 && currentObject.isNotEmpty()) {
                            objectStrings.add(currentObject.toString())
                            currentObject = StringBuilder()
                        }
                    }
                    else -> currentObject.append(char)
                }
            }
            
            // Parse each object separately
            for (objectString in objectStrings) {
                try {
                    val quiz = gson.fromJson(objectString.trim(), Quiz::class.java)
                    if (quiz != null) {
                        quizzes.add(quiz)
                    }
                } catch (e: Exception) {
                    // Skip invalid objects
                    continue
                }
            }
            
            if (quizzes.isNotEmpty()) quizzes else null
        } catch (e: Exception) {
            null
        }
    }
}

