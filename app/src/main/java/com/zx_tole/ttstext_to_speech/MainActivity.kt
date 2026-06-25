package com.zx_tole.ttstext_to_speech

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zx_tole.ttstext_to_speech.ui.theme.ПриложениеДляОзвучкиТекстаTTSTexttoSpeechTheme
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ПриложениеДляОзвучкиТекстаTTSTexttoSpeechTheme {
                Surface {
                    TTSApp()
                }
            }
        }
    }
}

@Composable
fun TTSApp() {
    var context = LocalContext.current
    var text by remember { mutableStateOf("") }
    var isInitializing by remember { mutableStateOf(true) }
    var isSpeaking by remember { mutableStateOf(false) }
    
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "TTS Озвучка Текста",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (isInitializing) {
            Text(
                text = "Инициализация голосового движка...",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.secondary
                )
            )
        }

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Введите текст для озвучивания") },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            placeholder = { Text("Введите текст здесь...") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (text.isNotBlank() && !isInitializing) {
                        speakText(text, tts)
                    }
                }
            ),
            singleLine = false,
            minLines = 5,
            maxLines = 10
        )

        Button(
            onClick = { speakText(text, tts) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            enabled = text.isNotBlank() && !isInitializing
        ) {
            Text(
                if (isSpeaking) "Остановить" else "Озвучить",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp
                )
            )
        }

        Button(
            onClick = { tts?.stop() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            enabled = isSpeaking
        ) {
            Text("Остановить воспроизведение")
        }
    }
}

fun speakText(text: String, tts: TextToSpeech?) {
    if (text.isNotBlank() && tts != null) {
        tts.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            null
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TTSAppPreview() {
    ПриложениеДляОзвучкиТекстаTTSTexttoSpeechTheme {
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
