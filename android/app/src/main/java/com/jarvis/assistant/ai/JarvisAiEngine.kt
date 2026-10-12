package com.jarvis.assistant.ai

import android.content.Context
import android.util.Log
import com.jarvis.assistant.data.remote.ActionPayloadDto
import com.jarvis.assistant.data.remote.ChatRequest
import com.jarvis.assistant.data.remote.HistoryItem
import com.jarvis.assistant.data.remote.JarvisApiService
import com.jarvis.assistant.model.ActionExecutionStatus
import com.jarvis.assistant.model.ChatMessage
import com.jarvis.assistant.model.JarvisAction
import com.jarvis.assistant.model.MessageSender
import com.jarvis.assistant.system.DeviceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AiResponseResult(
    val replyMessage: ChatMessage,
    val actionToExecute: JarvisAction?
)

class JarvisAiEngine(
    private val context: Context,
    private val deviceManager: DeviceManager
) {
    private val tag = "JarvisAiEngine"
    private val messageHistory = mutableListOf<HistoryItem>()

    var backendBaseUrl: String = "http://10.0.2.2:4000" // default Android emulator localhost or custom LAN IP
    var languageMode: String = "hinglish" // "hindi", "hinglish", "english"

    suspend fun processUserMessage(
        userText: String,
        isVoice: Boolean
    ): AiResponseResult = withContext(Dispatchers.IO) {
        // 1. First check offline fast parser for instant system actions
        val localParse = HinglishCommandParser.tryParse(userText)
        if (localParse != null) {
            val replyMsg = ChatMessage(
                text = localParse.initialReply,
                sender = MessageSender.JARVIS,
                isVoice = isVoice,
                actionType = localParse.action::class.java.simpleName,
                actionStatus = ActionExecutionStatus.IN_PROGRESS
            )
            appendHistory("user", userText)
            appendHistory("assistant", localParse.initialReply)
            return@withContext AiResponseResult(replyMsg, localParse.action)
        }

        // 2. Query secure backend API
        try {
            val apiService = JarvisApiService.create(backendBaseUrl)
            val liveSystem = deviceManager.getLiveSystemStatus()
            val systemContextMap = mapOf(
                "battery" to "${liveSystem.batteryPercentage}% (Charging: ${liveSystem.isCharging})",
                "storage_available" to liveSystem.formatBytes(liveSystem.availableStorageBytes),
                "device" to liveSystem.deviceModel,
                "os" to liveSystem.androidVersion
            )

            val request = ChatRequest(
                message = userText,
                history = messageHistory.takeLast(10),
                language = languageMode,
                systemContext = systemContextMap
            )

            val response = apiService.sendMessage(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val action = mapDtoToAction(body.action)

                val replyMsg = ChatMessage(
                    text = body.reply,
                    sender = MessageSender.JARVIS,
                    isVoice = isVoice,
                    actionType = body.action?.action,
                    actionStatus = if (action != null) ActionExecutionStatus.IN_PROGRESS else null,
                    clarificationOptions = body.action?.options
                )

                appendHistory("user", userText)
                appendHistory("assistant", body.reply)

                return@withContext AiResponseResult(replyMsg, action)
            } else {
                Log.w(tag, "Backend returned error code: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to connect to backend", e)
        }

        // 3. Fallback graceful AI reply when backend is offline
        val fallbackText = generateOfflineFallbackReply(userText)
        val fallbackMsg = ChatMessage(
            text = fallbackText,
            sender = MessageSender.JARVIS,
            isVoice = isVoice
        )
        appendHistory("user", userText)
        appendHistory("assistant", fallbackText)
        AiResponseResult(fallbackMsg, null)
    }

    private fun mapDtoToAction(dto: ActionPayloadDto?): JarvisAction? {
        dto ?: return null
        return when (dto.action.lowercase()) {
            "launch_app" -> dto.appName?.let { JarvisAction.LaunchApp(it) }
            "search_files" -> JarvisAction.SearchFiles(
                query = dto.query ?: "",
                fileType = dto.fileType,
                filterDate = dto.dateFilter
            )
            "battery_status", "get_battery" -> JarvisAction.GetBatteryInfo
            "storage_status", "get_storage" -> JarvisAction.GetStorageInfo
            "device_info", "get_device_info" -> JarvisAction.GetDeviceInfo
            "open_settings" -> dto.settingName?.let { JarvisAction.OpenSettings(it) }
            "create_reminder" -> JarvisAction.CreateReminder(
                title = dto.title ?: "Reminder",
                timeString = dto.timeString ?: "30 minutes"
            )
            "create_note" -> JarvisAction.CreateNote(
                title = dto.title ?: "Note",
                content = dto.content ?: ""
            )
            "ask_clarification" -> JarvisAction.AskClarification(
                question = dto.question ?: "Could you please clarify, sir?",
                options = dto.options ?: emptyList()
            )
            else -> null
        }
    }

    private fun generateOfflineFallbackReply(userText: String): String {
        val lower = userText.lowercase()
        return when {
            lower.contains("hello") || lower.contains("hi") || lower.contains("kaise ho") ->
                "Greetings, sir. All core local systems are fully operational. How may I be of assistance?"
            lower.contains("who are you") || lower.contains("tum kaun ho") ->
                "I am J.A.R.V.I.S., your personal AI assistant. I manage your device, files, applications, and productivity."
            else ->
                "Sir, I am currently operating on standalone local mode. I can execute commands like 'Battery kitni hai', 'YouTube kholo', 'Downloads mein PDF dhundo', or 'Kal ke documents dikhao'."
        }
    }

    private fun appendHistory(role: String, content: String) {
        messageHistory.add(HistoryItem(role, content))
        if (messageHistory.size > 20) {
            messageHistory.removeAt(0)
        }
    }

    fun clearHistory() {
        messageHistory.clear()
    }
}
