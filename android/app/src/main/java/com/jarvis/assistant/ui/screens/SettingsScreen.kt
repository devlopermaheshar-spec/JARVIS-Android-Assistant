package com.jarvis.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarvis.assistant.ui.theme.*
import com.jarvis.assistant.ui.viewmodel.JarvisViewModel

@Composable
fun SettingsScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    var backendUrl by remember { mutableStateOf(viewModel.aiEngine.backendBaseUrl) }
    var selectedLanguage by remember { mutableStateOf(viewModel.aiEngine.languageMode) }
    var ttsPitch by remember { mutableFloatStateOf(viewModel.tts.pitch) }
    var ttsRate by remember { mutableFloatStateOf(viewModel.tts.speechRate) }
    var saveStatus by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StarkBg)
            .padding(12.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Settings, contentDescription = null, tint = StarkCyan, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "SYSTEM CONFIGURATION & PROTOCOLS",
                color = StarkCyan,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        // 1. Backend Server Setup
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(StarkSurface)
                .border(1.dp, StarkBorder, RoundedCornerShape(8.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "SECURE AI BACKEND SERVER",
                    color = StarkGold,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "API keys are stored on your local or remote server to prevent secret exposure in the APK binary.",
                    color = StarkTextMuted,
                    fontSize = 11.sp
                )

                OutlinedTextField(
                    value = backendUrl,
                    onValueChange = { backendUrl = it },
                    label = { Text("Backend Server URL", color = StarkTextSecondary, fontSize = 12.sp) },
                    placeholder = { Text("http://192.168.1.100:4000 or http://10.0.2.2:4000") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = StarkTextPrimary,
                        unfocusedTextColor = StarkTextPrimary,
                        focusedBorderColor = StarkCyan,
                        unfocusedBorderColor = StarkBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        viewModel.aiEngine.backendBaseUrl = backendUrl
                        saveStatus = "Backend URL saved successfully."
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StarkCyan, contentColor = StarkBg),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SAVE ENDPOINT", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }

                if (saveStatus.isNotBlank()) {
                    Text(saveStatus, color = StarkGreen, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // 2. Language Selection (Hindi, Hinglish, English)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(StarkSurface)
                .border(1.dp, StarkBorder, RoundedCornerShape(8.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "PRIMARY LINGUISTIC MODE",
                    color = StarkCyan,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                val modes = listOf("hinglish" to "Hinglish (Conversational Mix)", "hindi" to "Hindi (हिंदी)", "english" to "English (UK/US)")
                modes.forEach { (mode, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = selectedLanguage == mode,
                            onClick = {
                                selectedLanguage = mode
                                viewModel.aiEngine.languageMode = mode
                                viewModel.voiceRecognizer.preferredLanguageTag = when (mode) {
                                    "hindi" -> "hi-IN"
                                    "hinglish" -> "en-IN"
                                    else -> "en-US"
                                }
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = StarkCyan, unselectedColor = StarkTextMuted)
                        )
                        Text(label, color = StarkTextPrimary, fontSize = 13.sp)
                    }
                }
            }
        }

        // 3. Speech Synthesizer Controls
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(StarkSurface)
                .border(1.dp, StarkBorder, RoundedCornerShape(8.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "JARVIS VOCAL SYNTHESIS",
                    color = StarkCyan,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                Text("Pitch: ${String.format(java.util.Locale.US, "%.2f", ttsPitch)}", color = StarkTextSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                Slider(
                    value = ttsPitch,
                    onValueChange = {
                        ttsPitch = it
                        viewModel.tts.pitch = it
                    },
                    valueRange = 0.5f..1.5f,
                    colors = SliderDefaults.colors(thumbColor = StarkCyan, activeTrackColor = StarkCyan)
                )

                Text("Speech Rate: ${String.format(java.util.Locale.US, "%.2f", ttsRate)}", color = StarkTextSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                Slider(
                    value = ttsRate,
                    onValueChange = {
                        ttsRate = it
                        viewModel.tts.speechRate = it
                    },
                    valueRange = 0.5f..2.0f,
                    colors = SliderDefaults.colors(thumbColor = StarkCyan, activeTrackColor = StarkCyan)
                )

                Button(
                    onClick = {
                        viewModel.tts.speak("Greetings Sir. All core diagnostics indicate normal operational parameters.")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StarkCard),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = StarkCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("TEST SYNTHESIS", color = StarkCyan, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
            }
        }

        // 4. Memory & Reset
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(StarkSurface)
                .border(1.dp, StarkBorder, RoundedCornerShape(8.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "DATA PURGE & PRIVACY PROTOCOL",
                    color = StarkRed,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Wipes all local conversation transcripts, cached memory, and reset states.",
                    color = StarkTextMuted,
                    fontSize = 11.sp
                )

                Button(
                    onClick = { viewModel.clearHistory() },
                    colors = ButtonDefaults.buttonColors(containerColor = StarkRed.copy(alpha = 0.2f), contentColor = StarkRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StarkRed),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PURGE MEMORY", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
            }
        }
    }
}
