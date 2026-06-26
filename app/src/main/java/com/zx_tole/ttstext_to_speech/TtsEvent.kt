package com.zx_tole.ttstext_to_speech

sealed interface TtsEvent {
    data class TextChange(val text: String) : TtsEvent
    object Speak : TtsEvent
    object Stop : TtsEvent
    object ClearText : TtsEvent
    object ClearHistory : TtsEvent
    data class PlayHistoryItem(val phrase: String) : TtsEvent
}
