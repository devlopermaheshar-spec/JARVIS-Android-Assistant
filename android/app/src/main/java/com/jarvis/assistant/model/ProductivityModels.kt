package com.jarvis.assistant.model

data class Note(
    val id: Long = 0,
    val title: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class ReminderItem(
    val id: Long = 0,
    val title: String,
    val timeDescription: String,
    val triggerTimeMs: Long,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
