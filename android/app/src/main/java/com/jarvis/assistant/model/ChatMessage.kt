package com.jarvis.assistant.model

import java.util.UUID

enum class MessageSender {
    USER,
    JARVIS,
    SYSTEM
}

enum class ActionExecutionStatus {
    PENDING,
    IN_PROGRESS,
    CONFIRMATION_REQUIRED,
    SUCCESS,
    FAILED,
    CANCELLED
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val sender: MessageSender,
    val timestamp: Long = System.currentTimeMillis(),
    val isVoice: Boolean = false,
    val actionType: String? = null,
    val actionPayload: String? = null,
    val actionStatus: ActionExecutionStatus? = null,
    val clarificationOptions: List<String>? = null
)
