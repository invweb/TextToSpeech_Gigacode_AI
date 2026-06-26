package com.zx_tole.ttstext_to_speech

import android.content.Context
import androidx.core.content.edit

class TtsRepository(context: Context) {
    private val prefs = context.getSharedPreferences("history_prefs", Context.MODE_PRIVATE)
    private val maxHistorySize = 5

    fun saveHistory(history: List<String>) {
        prefs.edit {
            putInt("history_size", history.size)
            history.forEachIndexed { index, phrase ->
                putString("history_phrase_$index", phrase)
            }
            apply()
        }
    }

    fun loadHistory(): List<String> {
        val size = prefs.getInt("history_size", 0)
        return (0 until size).mapNotNull { index ->
            prefs.getString("history_phrase_$index", null)
        }
    }

    fun clearHistory() {
        prefs.edit {
            val size = prefs.getInt("history_size", 0)
            (0 until size).forEach { index ->
                remove("history_phrase_$index")
            }
            remove("history_size")
            apply()
        }
    }

    fun addToHistory(newText: String): List<String> {
        val currentHistory = loadHistory()
        val updatedHistory = (listOf(newText) + currentHistory)
            .distinct()
            .take(maxHistorySize)
        saveHistory(updatedHistory)
        return updatedHistory
    }

    fun isEmpty(): Boolean {
        return loadHistory().isEmpty()
    }
}
