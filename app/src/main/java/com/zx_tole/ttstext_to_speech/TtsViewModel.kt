package com.zx_tole.ttstext_to_speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.Locale

class TtsViewModel(
    private val repository: TtsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(TtsState())
    val uiState: StateFlow<TtsState> get() = _uiState
    
    private var tts: TextToSpeech? = null
    private var context: Context? = null
    
    init {
        loadHistory()
    }
    
    fun setContext(context: Context) {
        this.context = context
        setupTts()
    }
    
    private fun setupTts() {
        context?.let { ctx ->
            tts = TextToSpeech(ctx) { status ->
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
                        .take(5)
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
                repository.clearHistory()
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
        val history = repository.loadHistory()
        updateState { copy(history = history) }
    }
    
    private fun saveHistory(history: List<String>) {
        repository.saveHistory(history)
    }
    
    private fun updateState(update: TtsState.() -> TtsState) {
        _uiState.value = _uiState.value.update()
    }
    
    override fun onCleared() {
        super.onCleared()
        tts?.setOnUtteranceProgressListener(null)
        tts?.stop()
        tts?.shutdown()
        tts = null
        context = null
    }
}

// Factory for creating ViewModel with repository
class TtsViewModelProviderFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TtsViewModel::class.java)) {
            val repository = TtsRepository(context)
            val viewModel = TtsViewModel(repository)
            viewModel.setContext(context)
            @Suppress("UNCHECKED_CAST")
            return viewModel as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
