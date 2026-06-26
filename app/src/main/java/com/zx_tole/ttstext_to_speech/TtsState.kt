package com.zx_tole.ttstext_to_speech

data class TtsState(
    val text: String = "",
    val history: List<String> = emptyList(),
    val isInitializing: Boolean = true,
    val isSpeaking: Boolean = false
)
