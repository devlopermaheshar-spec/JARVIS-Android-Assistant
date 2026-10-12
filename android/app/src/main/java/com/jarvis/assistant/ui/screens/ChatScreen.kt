package com.jarvis.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
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
import com.jarvis.assistant.model.ChatMessage
import com.jarvis.assistant.model.MessageSender
import com.jarvis.assistant.ui.theme.*
import com.jarvis.assistant.ui.viewmodel.JarvisViewModel
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.messages.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StarkBg)
            .padding(12.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "COMMUNICATION LOGS",
                color = StarkCyan,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            IconButton(onClick = { viewModel.clearHistory() }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Clear History",
                    tint = StarkTextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { message ->
                ChatMessageItem(
                    message = message,
                    onOptionSelected = { option ->
                        viewModel.handleUserPrompt(option, isVoice = false)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input Field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(StarkSurface)
                .border(1.dp, StarkBorder, RoundedCornerShape(24.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text(
                        text = "Type or speak to JARVIS...",
                        color = StarkTextMuted,
                        fontSize = 14.sp
                    )
                },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = StarkTextPrimary,
                    unfocusedTextColor = StarkTextPrimary,
                    cursorColor = StarkCyan,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )

            IconButton(
                onClick = { viewModel.toggleVoiceInput() }
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Voice",
                    tint = if (isListening) StarkGreen else StarkCyan
                )
            }

            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.handleUserPrompt(inputText, isVoice = false)
                        inputText = ""
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = StarkCyan
                )
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    onOptionSelected: (String) -> Unit
) {
    val isUser = message.sender == MessageSender.USER
    val isSystem = message.sender == MessageSender.SYSTEM

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = when {
            isUser -> Alignment.End
            isSystem -> Alignment.CenterHorizontally
            else -> Alignment.Start
        }
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 14.dp,
                        topEnd = 14.dp,
                        bottomStart = if (isUser) 14.dp else 2.dp,
                        bottomEnd = if (isUser) 2.dp else 14.dp
                    )
                )
                .background(
                    when {
                        isUser -> StarkCard
                        isSystem -> StarkSurface.copy(alpha = 0.6f)
                        else -> StarkSurface
                    }
                )
                .border(
                    1.dp,
                    when {
                        isUser -> StarkCyan.copy(alpha = 0.4f)
                        isSystem -> StarkBorder
                        else -> StarkGold.copy(alpha = 0.4f)
                    },
                    RoundedCornerShape(14.dp)
                )
                .padding(12.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = when (message.sender) {
                            MessageSender.USER -> "USER"
                            MessageSender.JARVIS -> "J.A.R.V.I.S."
                            MessageSender.SYSTEM -> "SYSTEM PROTOCOL"
                        },
                        color = when (message.sender) {
                            MessageSender.USER -> StarkCyan
                            MessageSender.JARVIS -> StarkGold
                            MessageSender.SYSTEM -> StarkTextMuted
                        },
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )

                    if (message.isVoice) {
                        Text(
                            text = "VOICE",
                            color = StarkGreen,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = message.text,
                    color = StarkTextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                // Follow-up clarification options if available
                if (!message.clarificationOptions.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        message.clarificationOptions.forEach { opt ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(StarkCard)
                                    .border(1.dp, StarkCyan.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                    .clickable { onOptionSelected(opt) }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = opt,
                                    color = StarkCyan,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
