package com.jarvis.assistant.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.assistant.ai.JarvisAiEngine
import com.jarvis.assistant.data.local.ConversationEntity
import com.jarvis.assistant.data.local.JarvisDatabase
import com.jarvis.assistant.data.local.NoteEntity
import com.jarvis.assistant.data.local.ReminderEntity
import com.jarvis.assistant.model.*
import com.jarvis.assistant.service.JarvisAccessibilityService
import com.jarvis.assistant.service.JarvisForegroundService
import com.jarvis.assistant.service.JarvisNotificationListener
import com.jarvis.assistant.system.*
import com.jarvis.assistant.voice.TextToSpeechManager
import com.jarvis.assistant.voice.VoiceRecognizerManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PendingConfirmation(
    val title: String,
    val message: String,
    val isDestructive: Boolean,
    val onConfirm: () -> Unit
)

class JarvisViewModel(application: Application) : AndroidViewModel(application) {
    private val tag = "JarvisViewModel"

    // System Managers
    val deviceManager = DeviceManager(application)
    val appManager = AppManager(application)
    val fileManager = FileManager(application)
    val ocrManager = OcrManager(application)
    val productivityManager = ProductivityManager(application)
    val permissionManager = PermissionManager(application)
    val aiEngine = JarvisAiEngine(application, deviceManager)

    // Voice & TTS
    val voiceRecognizer = VoiceRecognizerManager(application, viewModelScope)
    val tts = TextToSpeechManager(application)

    // Database
    private val db = JarvisDatabase.getInstance(application)

    // UI States
    val isListening: StateFlow<Boolean> = voiceRecognizer.isListening
    val isSpeaking: StateFlow<Boolean> = tts.isSpeaking
    val partialSpeech: StateFlow<String> = voiceRecognizer.partialText
    val audioRmsDb: StateFlow<Float> = voiceRecognizer.audioRmsDb

    private val _systemStatus = MutableStateFlow(deviceManager.getLiveSystemStatus())
    val systemStatus: StateFlow<SystemStatus> = _systemStatus.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: StateFlow<List<AppInfo>> = _installedApps.asStateFlow()

    private val _fileSearchResults = MutableStateFlow<List<FileItem>>(emptyList())
    val fileSearchResults: StateFlow<List<FileItem>> = _fileSearchResults.asStateFlow()

    private val _pendingConfirmation = MutableStateFlow<PendingConfirmation?>(null)
    val pendingConfirmation: StateFlow<PendingConfirmation?> = _pendingConfirmation.asStateFlow()

    private val _activeDocumentContent = MutableStateFlow<String?>(null)
    val activeDocumentContent: StateFlow<String?> = _activeDocumentContent.asStateFlow()

    private val _isContinuousListening = MutableStateFlow(false)
    val isContinuousListening: StateFlow<Boolean> = _isContinuousListening.asStateFlow()

    val notes: Flow<List<NoteEntity>> = productivityManager.allNotes
    val reminders: Flow<List<ReminderEntity>> = productivityManager.allReminders

    init {
        // Collect database conversation history
        viewModelScope.launch {
            db.conversationDao().getAllMessages().collect { entities ->
                _messages.value = entities.map { it.toChatMessage() }
            }
        }

        // Collect recognized voice speech
        viewModelScope.launch {
            voiceRecognizer.recognizedSpeech.collect { spokenText ->
                if (spokenText.isNotBlank()) {
                    handleUserPrompt(spokenText, isVoice = true)
                }
            }
        }

        // Wake word detection
        viewModelScope.launch {
            voiceRecognizer.wakeWordDetected.collect {
                tts.speak("Yes, sir?")
            }
        }

        // Refresh telemetry periodically
        viewModelScope.launch {
            while (true) {
                _systemStatus.value = deviceManager.getLiveSystemStatus()
                kotlinx.coroutines.delay(4000)
            }
        }

        loadApps()
    }

    fun loadApps() {
        viewModelScope.launch {
            _installedApps.value = appManager.getInstalledApps()
        }
    }

    fun toggleContinuousListening() {
        val next = !_isContinuousListening.value
        _isContinuousListening.value = next
        if (next) {
            JarvisForegroundService.startService(getApplication())
            voiceRecognizer.startListening(continuous = true)
            tts.speak("Continuous voice listening activated, sir.")
        } else {
            JarvisForegroundService.stopService(getApplication())
            voiceRecognizer.stopListening()
            tts.speak("Continuous mode deactivated.")
        }
    }

    fun toggleVoiceInput() {
        if (isListening.value) {
            voiceRecognizer.stopListening()
        } else {
            voiceRecognizer.startListening(continuous = _isContinuousListening.value)
        }
    }

    fun handleUserPrompt(text: String, isVoice: Boolean) {
        if (text.isBlank()) return

        val userMessage = ChatMessage(
            text = text,
            sender = MessageSender.USER,
            isVoice = isVoice
        )

        viewModelScope.launch {
            // Save user message to database
            db.conversationDao().insertMessage(ConversationEntity.fromChatMessage(userMessage))

            // AI Reasoning & Action extraction
            val result = aiEngine.processUserMessage(text, isVoice)
            db.conversationDao().insertMessage(ConversationEntity.fromChatMessage(result.replyMessage))

            // Speak assistant response aloud if voice mode or enabled
            tts.speak(result.replyMessage.text)

            // Execute structured action if returned
            result.actionToExecute?.let { action ->
                executeAction(action)
            }
        }
    }

    fun executeAction(action: JarvisAction) {
        viewModelScope.launch {
            when (action) {
                is JarvisAction.LaunchApp -> {
                    val app = appManager.findApp(action.appName)
                    if (app != null) {
                        val ok = appManager.launchApp(app)
                        if (ok) {
                            addSystemNotification("Launched ${app.appName}")
                        } else {
                            addSystemNotification("Unable to open ${action.appName}")
                        }
                    } else if (action.appName.equals("Camera", ignoreCase = true)) {
                        appManager.launchCamera()
                        addSystemNotification("Camera launched")
                    } else {
                        addSystemNotification("App '${action.appName}' not found on device.")
                    }
                }

                is JarvisAction.SearchFiles -> {
                    val files = fileManager.searchFiles(
                        query = action.query,
                        fileCategory = action.fileType,
                        dateFilter = action.filterDate
                    )
                    _fileSearchResults.value = files
                    addSystemNotification("Found ${files.size} matching files.")
                }

                is JarvisAction.OpenFile -> {
                    fileManager.openFile(Uri.parse(action.uri), action.mimeType)
                }

                is JarvisAction.ReadDocument -> {
                    val content = fileManager.readDocumentContent(Uri.parse(action.uri))
                    _activeDocumentContent.value = content
                    val preview = content.take(150)
                    tts.speak("Sir, here is a preview: $preview")
                }

                is JarvisAction.SummarizeDocument -> {
                    val content = fileManager.readDocumentContent(Uri.parse(action.uri))
                    _activeDocumentContent.value = content
                    tts.speak("Summarizing document contents, sir.")
                }

                is JarvisAction.OcrImage -> {
                    val uri = Uri.parse(action.uri)
                    val result = ocrManager.extractTextFromImage(uri)
                    result.onSuccess { text ->
                        _activeDocumentContent.value = text
                        tts.speak("Sir, I've extracted the text from the image.")
                        addSystemNotification("OCR Extracted: ${text.take(60)}...")
                    }.onFailure {
                        tts.speak("Failed to extract text from image, sir.")
                    }
                }

                is JarvisAction.DeleteFile -> {
                    if (action.requiresConfirmation) {
                        _pendingConfirmation.value = PendingConfirmation(
                            title = "Confirm File Deletion",
                            message = "Are you sure you want to permanently delete '${action.fileName}'? This cannot be undone.",
                            isDestructive = true,
                            onConfirm = {
                                viewModelScope.launch {
                                    val deleted = fileManager.deleteFile(Uri.parse(action.uri))
                                    if (deleted) {
                                        tts.speak("File deleted, sir.")
                                        // Refresh search list
                                        _fileSearchResults.value = _fileSearchResults.value.filter { it.uri.toString() != action.uri }
                                    }
                                    _pendingConfirmation.value = null
                                }
                            }
                        )
                    } else {
                        fileManager.deleteFile(Uri.parse(action.uri))
                    }
                }

                is JarvisAction.GetBatteryInfo -> {
                    val bat = deviceManager.getLiveSystemStatus()
                    val rep = "Sir, your battery is at ${bat.batteryPercentage} percent, currently powered by ${bat.chargingSource}. Battery health is ${bat.batteryHealth}."
                    tts.speak(rep)
                    addSystemNotification(rep)
                }

                is JarvisAction.GetStorageInfo -> {
                    val status = deviceManager.getLiveSystemStatus()
                    val rep = "Sir, ${status.formatBytes(status.availableStorageBytes)} is currently available of total ${status.formatBytes(status.totalStorageBytes)}."
                    tts.speak(rep)
                    addSystemNotification(rep)
                }

                is JarvisAction.GetDeviceInfo -> {
                    val status = deviceManager.getLiveSystemStatus()
                    val rep = "Device: ${status.deviceModel}, running ${status.androidVersion}."
                    tts.speak(rep)
                    addSystemNotification(rep)
                }

                is JarvisAction.OpenSettings -> {
                    val (opened, speech) = deviceManager.openSystemSetting(action.settingType)
                    tts.speak(speech)
                }

                is JarvisAction.CreateReminder -> {
                    val (ok, msg) = productivityManager.createReminder(action.title, action.timeString, action.epochTimeMs)
                    tts.speak(msg)
                    addSystemNotification(msg)
                }

                is JarvisAction.CreateNote -> {
                    productivityManager.saveNote(action.title, action.content)
                    tts.speak("Note saved, sir.")
                    addSystemNotification("Saved note: ${action.title}")
                }

                is JarvisAction.SearchNotes -> {
                    val results = productivityManager.searchNotes(action.query)
                    addSystemNotification("Found ${results.size} notes matching '${action.query}'")
                }

                is JarvisAction.AskClarification -> {
                    // Handled in UI
                }

                is JarvisAction.DirectReply -> {
                    tts.speak(action.responseText)
                }
            }
        }
    }

    fun dismissConfirmation() {
        _pendingConfirmation.value = null
    }

    fun clearHistory() {
        viewModelScope.launch {
            db.conversationDao().clearAll()
            aiEngine.clearHistory()
            tts.speak("Conversation memory cleared, sir.")
        }
    }

    private fun addSystemNotification(text: String) {
        val sysMsg = ChatMessage(
            text = text,
            sender = MessageSender.SYSTEM
        )
        viewModelScope.launch {
            db.conversationDao().insertMessage(ConversationEntity.fromChatMessage(sysMsg))
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceRecognizer.destroy()
        tts.shutdown()
    }
}
