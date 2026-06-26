package com.zx_tole.ttstext_to_speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.Locale

class TtsViewModel(private val context: Context) : ViewModel() {
    private val prefs = context.getSharedPreferences("history_prefs", Context.MODE_PRIVATE)
    private val maxHistorySize = 5
    private val historyManager = HistoryManager(context)
    
    private val _uiState = MutableStateFlow(TtsState())
    val uiState: StateFlow<TtsState> get() = _uiState
    
    private val _events = MutableStateFlow<(TtsEvent) -> Unit> { }
    val events = _events.asSharedFlow()
    
    private var tts: TextToSpeech? = null
    
    init {
        setupTts()
        loadHistory()
    }
    
    private fun setupTts() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.let {
                    it.language = Locale.getDefault()
                }
                updateState { copy(isInitializing = false) }
            } else {
                updateState { copy(isInitializing = false) }
            }
        }
        
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                updateState { copy(isSpeaking = true) }
            }

            override fun onDone(utteranceId: String?) {
                updateState { copy(isSpeaking = false) }
            }

            override fun onError(utteranceId: String?) {
                updateState { copy(isSpeaking = false) }
            }
        })
    }
    
    fun onEvent(event: TtsEvent) {
        when (event) {
            is TtsEvent.TextChange -> {
                updateState { copy(text = event.text) }
            }
            
            is TtsEvent.Speak -> {
                val currentState = _uiState.value
                if (currentState.text.isNotBlank() && tts != null && !currentState.isInitializing) {
                    tts?.speak(
                        currentState.text,
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        null
                    )
                    val newHistory = listOf(currentState.text) + currentState.history
                        .filter { it != currentState.text }
                        .take(maxHistorySize - 1)
                    updateState { copy(text = "", history = newHistory) }
                    saveHistory(newHistory)
                }
            }
            
            is TtsEvent.Stop -> {
                tts?.stop()
            }
            
            is TtsEvent.ClearText -> {
                updateState { copy(text = "") }
            }
            
            is TtsEvent.ClearHistory -> {
                historyManager.clearHistory()
                updateState { copy(history = emptyList()) }
            }
            
            is TtsEvent.PlayHistoryItem -> {
                val currentState = _uiState.value
                if (tts != null && !currentState.isSpeaking) {
                    tts?.speak(event.phrase, TextToSpeech.QUEUE_FLUSH, null, null)
                }
            }
        }
    }
    
    private fun loadHistory() {
        val history = historyManager.loadHistory()
        updateState { copy(history = history) }
    }
    
    private fun saveHistory(history: List<String>) {
        prefs.edit {
            putInt("history_size", history.size)
            history.forEachIndexed { index, phrase ->
                putString("history_phrase_$index", phrase)
            }
            apply()
        }
    }
    
    private fun updateState(update: TtsState.() -> TtsState) {
        _uiState.value = _uiState.value.update()
    }
    
    override fun onCleared() {
        super.onCleared()
        tts?.setOnUtteranceProgressListener(null)
        tts?.stop()
        tts?.shutdown()
    }
}
