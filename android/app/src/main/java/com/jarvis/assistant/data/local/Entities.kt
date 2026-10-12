package com.jarvis.assistant.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jarvis.assistant.model.ActionExecutionStatus
import com.jarvis.assistant.model.ChatMessage
import com.jarvis.assistant.model.MessageSender

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey
    val id: String,
    val text: String,
    val sender: String, // USER, JARVIS, SYSTEM
    val timestamp: Long,
    val isVoice: Boolean,
    val actionType: String?,
    val actionPayload: String?,
    val actionStatus: String?
) {
    fun toChatMessage(): ChatMessage {
        return ChatMessage(
            id = id,
            text = text,
            sender = try { MessageSender.valueOf(sender) } catch (e: Exception) { MessageSender.SYSTEM },
            timestamp = timestamp,
            isVoice = isVoice,
            actionType = actionType,
            actionPayload = actionPayload,
            actionStatus = actionStatus?.let {
                try { ActionExecutionStatus.valueOf(it) } catch (e: Exception) { null }
            }
        )
    }

    companion object {
        fun fromChatMessage(msg: ChatMessage): ConversationEntity {
            return ConversationEntity(
                id = msg.id,
                text = msg.text,
                sender = msg.sender.name,
                timestamp = msg.timestamp,
                isVoice = msg.isVoice,
                actionType = msg.actionType,
                actionPayload = msg.actionPayload,
                actionStatus = msg.actionStatus?.name
            )
        }
    }
}

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val timeDescription: String,
    val triggerTimeMs: Long,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
