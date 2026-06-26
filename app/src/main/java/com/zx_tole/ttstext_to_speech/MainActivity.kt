package com.zx_tole.ttstext_to_speech

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.zx_tole.ttstext_to_speech.ui.theme.TTSAppTheme
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TTSAppTheme {
                Surface {
                    TTSApp()
                }
            }
        }
    }
}

@Composable
fun TTSApp() {
    val context = LocalContext.current
    var text by remember { mutableStateOf("") }
    var isInitializing by remember { mutableStateOf(true) }
    var isSpeaking by remember { mutableStateOf(false) }
    
    val historyManager = remember { HistoryManager(context) }
    var history by remember { mutableStateOf(emptyList<String>()) }
    
    // Load history after initialization
    LaunchedEffect(Unit) {
        history = historyManager.loadHistory()
    }
    
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    
    LaunchedEffect(context) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.let {
                    it.language = Locale.getDefault()
                    isInitializing = false
                }
            } else {
                isInitializing = false
            }
        }
    }

    DisposableEffect(tts) {
        val listener = object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                isSpeaking = true
            }

            override fun onDone(utteranceId: String?) {
                isSpeaking = false
            }

            override fun onError(utteranceId: String?) {
                isSpeaking = false
            }
        }

        tts?.setOnUtteranceProgressListener(listener)

        onDispose {
            tts?.setOnUtteranceProgressListener(null)
            tts?.stop()
            tts?.shutdown()
        }
    }

    fun speakText(text: String) {
        if (text.isNotBlank() && tts != null) {
            tts?.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                null
            )
        }
    }

    fun addToHistory(newText: String) {
        historyManager.addToHistory(newText)
        history = historyManager.loadHistory()
    }

    fun clearText() {
        text = ""
    }

    fun clearHistory() {
        historyManager.clearHistory()
        history = historyManager.loadHistory()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)
        )

        if (isInitializing) {
            Text(
                text = stringResource(R.string.initialize),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.secondary
                )
            )
        }

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text(stringResource(R.string.enter_text)) },
            modifier = Modifier
                .fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.placeholder_text)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (text.isNotBlank() && !isInitializing) {
                        speakText(text)
                        addToHistory(text)
                    }
                }
            ),
            singleLine = false,
            minLines = 3,
            maxLines = 4
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { 
                    speakText(text)
                    addToHistory(text)
                },
                modifier = Modifier
                    .weight(1f),
                enabled = text.isNotBlank() && !isInitializing && !isSpeaking
            ) {
                Text(stringResource(R.string.speak))
            }

            Button(
                onClick = { clearText() },
                modifier = Modifier
                    .weight(1f),
                enabled = text.isNotEmpty()
            ) {
                Text(stringResource(R.string.clear_text))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { 
                    if (tts != null) {
                        tts?.stop()
                    }
                },
                modifier = Modifier
                    .weight(1f),
                enabled = true
            ) {
                Text(stringResource(R.string.stop))
            }

            Button(
                onClick = { clearHistory() },
                modifier = Modifier
                    .weight(1f),
                enabled = history.isNotEmpty()
            ) {
                Text(stringResource(R.string.clear_history))
            }
        }

        // Playback history
        if (history.isNotEmpty()) {
            Text(
                text = stringResource(R.string.history),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(history) { phrase ->
                    Card(
                        onClick = {
                            if (tts != null && !isSpeaking) {
                                speakText(phrase)
                            }
                        },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E0E0))
                    ) {
                        Text(
                            text = phrase,
                            modifier = Modifier.padding(16.dp),
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TTSAppPreview() {
    TTSAppTheme {
        Surface {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("TTS Preview")
            }
        }
    }
}
