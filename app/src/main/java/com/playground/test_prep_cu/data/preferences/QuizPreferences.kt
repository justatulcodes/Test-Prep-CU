package com.playground.test_prep_cu.data.preferences

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Manages persistent storage of quiz files
 */
class QuizPreferences(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "quiz_prefs",
        Context.MODE_PRIVATE
    )
    
    private val gson = Gson()
    
    companion object {
        private const val KEY_RECENT_FILES = "recent_files"
        private const val MAX_RECENT_FILES = 10
    }
    
    /**
     * Save a recently used quiz file
     */
    fun saveRecentFile(uri: Uri, fileName: String, questionsCount: Int) {
        val recentFiles = getRecentFiles().toMutableList()
        
        // Remove if already exists (to update to top)
        recentFiles.removeAll { it.uri == uri.toString() }
        
        // Add to beginning
        recentFiles.add(0, RecentFile(
            uri = uri.toString(),
            fileName = fileName,
            questionsCount = questionsCount,
            lastAccessedTime = System.currentTimeMillis()
        ))
        
        // Keep only max recent files
        if (recentFiles.size > MAX_RECENT_FILES) {
            recentFiles.subList(MAX_RECENT_FILES, recentFiles.size).clear()
        }
        
        // Save to preferences
        val json = gson.toJson(recentFiles)
        prefs.edit().putString(KEY_RECENT_FILES, json).apply()
    }
    
    /**
     * Get list of recent files
     */
    fun getRecentFiles(): List<RecentFile> {
        val json = prefs.getString(KEY_RECENT_FILES, null) ?: return emptyList()
        
        return try {
            val type = object : TypeToken<List<RecentFile>>() {}.type
            gson.fromJson<List<RecentFile>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Remove a recent file
     */
    fun removeRecentFile(uri: String) {
        val recentFiles = getRecentFiles().toMutableList()
        recentFiles.removeAll { it.uri == uri }
        
        val json = gson.toJson(recentFiles)
        prefs.edit().putString(KEY_RECENT_FILES, json).apply()
    }
    
    /**
     * Clear all recent files
     */
    fun clearRecentFiles() {
        prefs.edit().remove(KEY_RECENT_FILES).apply()
    }
}

/**
 * Data class for recent file information
 */
data class RecentFile(
    val uri: String,
    val fileName: String,
    val questionsCount: Int,
    val lastAccessedTime: Long
)
