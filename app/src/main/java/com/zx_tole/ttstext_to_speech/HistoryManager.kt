package com.zx_tole.ttstext_to_speech

import android.content.Context
import androidx.core.content.edit

class HistoryManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("history_prefs", Context.MODE_PRIVATE)
    private val maxHistorySize = 5

    fun saveHistory(history: List<String>) {
        prefs.edit {
            putInt(HISTORY_SIZE_KEY, history.size)
            history.forEachIndexed { index, phrase ->
                putString("history_phrase_$index", phrase)
            }
        }
    }

    fun loadHistory(): List<String> {
        val size = prefs.getInt(HISTORY_SIZE_KEY, 0)
        return (0 until size).mapNotNull { index ->
            prefs.getString("history_phrase_$index", null)
        }
    }

    fun addToHistory(newText: String) {
        val currentHistory = loadHistory()
        val updatedHistory = (listOf(newText) + currentHistory)
            .distinct()
            .take(maxHistorySize)
        saveHistory(updatedHistory)
    }

    fun clearHistory() {
        prefs.edit {
            val size = prefs.getInt(HISTORY_SIZE_KEY, 0)
            (0 until size).forEach { index ->
                remove("history_phrase_$index")
            }
            remove(HISTORY_SIZE_KEY)
            apply()
        }
    }

    fun isEmpty(): Boolean {
        return loadHistory().isEmpty()
    }

    companion object {
        private const val HISTORY_PREFIX = "history_phrase_"
        private const val HISTORY_SIZE_KEY = "history_size"
    }
}
