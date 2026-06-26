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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.zx_tole.ttstext_to_speech.ui.theme.TTSAppTheme

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
fun TTSApp(
    viewModel: TtsViewModel = viewModel(
        factory = TtsViewModelProviderFactory(LocalContext.current)
    )
) {
    val uiState by viewModel.uiState.collectAsState()

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

        if (uiState.isInitializing) {
            Text(
                text = stringResource(R.string.initialize),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.secondary
                )
            )
        }

        OutlinedTextField(
            value = uiState.text,
            onValueChange = { viewModel.onEvent(TtsEvent.TextChange(it)) },
            label = { Text(stringResource(R.string.enter_text)) },
            modifier = Modifier
                .fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.placeholder_text)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    viewModel.onEvent(TtsEvent.Speak)
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
                onClick = { viewModel.onEvent(TtsEvent.Speak) },
                modifier = Modifier
                    .weight(1f),
                enabled = uiState.text.isNotBlank() && 
                    !uiState.isInitializing && 
                    !uiState.isSpeaking
            ) {
                Text(stringResource(R.string.speak))
            }

            Button(
                onClick = { viewModel.onEvent(TtsEvent.ClearText) },
                modifier = Modifier
                    .weight(1f),
                enabled = uiState.text.isNotEmpty()
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
                onClick = { viewModel.onEvent(TtsEvent.Stop) },
                modifier = Modifier
                    .weight(1f),
                enabled = true
            ) {
                Text(stringResource(R.string.stop))
            }

            Button(
                onClick = { viewModel.onEvent(TtsEvent.ClearHistory) },
                modifier = Modifier
                    .weight(1f),
                enabled = uiState.history.isNotEmpty()
            ) {
                Text(stringResource(R.string.clear_history))
            }
        }

        // Playback history
        if (uiState.history.isNotEmpty()) {
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
                items(uiState.history) { phrase ->
                    Card(
                        onClick = {
                            viewModel.onEvent(TtsEvent.PlayHistoryItem(phrase))
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
