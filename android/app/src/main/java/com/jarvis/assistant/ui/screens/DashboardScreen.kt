package com.jarvis.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarvis.assistant.ui.components.ArcReactorOrb
import com.jarvis.assistant.ui.components.AudioVisualizer
import com.jarvis.assistant.ui.components.SystemHudBanner
import com.jarvis.assistant.ui.theme.*
import com.jarvis.assistant.ui.viewmodel.JarvisViewModel

@Composable
fun DashboardScreen(
    viewModel: JarvisViewModel,
    onNavigateToChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isListening by viewModel.isListening.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val partialText by viewModel.partialSpeech.collectAsState()
    val audioRmsDb by viewModel.audioRmsDb.collectAsState()
    val systemStatus by viewModel.systemStatus.collectAsState()
    val isContinuous by viewModel.isContinuousListening.collectAsState()
    val messages by viewModel.messages.collectAsState()

    val lastJarvisMessage = messages.lastOrNull { it.sender == com.jarvis.assistant.model.MessageSender.JARVIS }

    val quickCommands = listOf(
        "Meri battery kitni hai?" to "Battery?",
        "YouTube kholo" to "YouTube",
        "Mere downloads mein PDF dhundo" to "PDFs in Downloads",
        "Kal ke documents dikhao" to "Kal ke Docs",
        "Camera kholo" to "Camera",
        "Wi-Fi settings kholo" to "Wi-Fi Settings"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StarkBg)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Top HUD Telemetry Banner
        SystemHudBanner(
            systemStatus = systemStatus,
            isListening = isListening
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Continuous Mode Switch & Status Label
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(StarkSurface)
                .border(1.dp, StarkBorder, RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isContinuous) "CONTINUOUS PROTOCOL: ACTIVE" else "TAP OR SAY 'HEY JARVIS'",
                color = if (isContinuous) StarkGreen else StarkTextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = isContinuous,
                onCheckedChange = { viewModel.toggleContinuousListening() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = StarkBg,
                    checkedTrackColor = StarkGreen,
                    uncheckedThumbColor = StarkTextMuted,
                    uncheckedTrackColor = StarkCard
                ),
                modifier = Modifier.height(24.dp)
            )
        }

        Spacer(modifier = Modifier.weight(0.5f))

        // 3. Central Arc Reactor Orb
        ArcReactorOrb(
            isListening = isListening,
            isSpeaking = isSpeaking,
            audioRmsDb = audioRmsDb,
            size = 230.dp,
            onClick = { viewModel.toggleVoiceInput() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Live Audio Waveform Visualizer
        AudioVisualizer(
            isListening = isListening,
            isSpeaking = isSpeaking,
            audioRmsDb = audioRmsDb,
            height = 36.dp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 5. Speech Transcription / Response Readout Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(StarkSurface.copy(alpha = 0.9f))
                .border(1.dp, if (isListening) StarkGreen else StarkBorder, RoundedCornerShape(12.dp))
                .clickable { onNavigateToChat() }
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            val displayText = when {
                isListening && partialText.isNotBlank() -> "\"$partialText\""
                isListening -> "Listening for Hindi / Hinglish / English commands..."
                isSpeaking && lastJarvisMessage != null -> lastJarvisMessage.text
                lastJarvisMessage != null -> lastJarvisMessage.text
                else -> "Greetings Sir. Awaiting your instruction."
            }

            Text(
                text = displayText,
                color = if (isListening) StarkGreen else StarkTextPrimary,
                fontSize = 14.sp,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                maxLines = 3
            )
        }

        Spacer(modifier = Modifier.weight(0.5f))

        // 6. Quick Command Chips (Hindi & English)
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "VOICE DIRECTIVES",
                color = StarkTextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(quickCommands) { (command, label) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(StarkCard)
                            .border(1.dp, StarkBorder, RoundedCornerShape(8.dp))
                            .clickable { viewModel.handleUserPrompt(command, isVoice = false) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = label,
                            color = StarkCyan,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 7. Large Microphone Activation Button
        IconButton(
            onClick = { viewModel.toggleVoiceInput() },
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(
                    if (isListening) StarkGreen else StarkCyan
                )
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = "Microphone",
                tint = StarkBg,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
