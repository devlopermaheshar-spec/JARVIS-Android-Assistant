package com.jarvis.assistant.model

sealed interface JarvisAction {
    val description: String

    data class LaunchApp(
        val appName: String,
        val packageName: String? = null
    ) : JarvisAction {
        override val description: String = "Opening $appName"
    }

    data class SearchFiles(
        val query: String,
        val fileType: String? = null, // "PDF", "DOCX", "TXT", "IMAGE", "AUDIO", "VIDEO"
        val filterDate: String? = null // "YESTERDAY", "TODAY", "WEEK"
    ) : JarvisAction {
        override val description: String = "Searching files: '$query'"
    }

    data class OpenFile(
        val uri: String,
        val fileName: String,
        val mimeType: String
    ) : JarvisAction {
        override val description: String = "Opening file: $fileName"
    }

    data class ReadDocument(
        val uri: String,
        val fileName: String
    ) : JarvisAction {
        override val description: String = "Reading document: $fileName"
    }

    data class SummarizeDocument(
        val uri: String,
        val fileName: String
    ) : JarvisAction {
        override val description: String = "Summarizing document: $fileName"
    }

    data class OcrImage(
        val uri: String,
        val fileName: String
    ) : JarvisAction {
        override val description: String = "Extracting text from image: $fileName"
    }

    data class DeleteFile(
        val uri: String,
        val fileName: String,
        val requiresConfirmation: Boolean = true
    ) : JarvisAction {
        override val description: String = "Delete file: $fileName"
    }

    object GetBatteryInfo : JarvisAction {
        override val description: String = "Checking battery status"
    }

    object GetStorageInfo : JarvisAction {
        override val description: String = "Checking device storage"
    }

    object GetDeviceInfo : JarvisAction {
        override val description: String = "Fetching device diagnostics"
    }

    data class OpenSettings(
        val settingType: String // "wifi", "bluetooth", "location", "sound", "display", "notifications"
    ) : JarvisAction {
        override val description: String = "Opening $settingType settings"
    }

    data class CreateReminder(
        val title: String,
        val timeString: String,
        val epochTimeMs: Long? = null
    ) : JarvisAction {
        override val description: String = "Setting reminder: '$title' for $timeString"
    }

    data class CreateNote(
        val title: String,
        val content: String
    ) : JarvisAction {
        override val description: String = "Saving note: '$title'"
    }

    data class SearchNotes(
        val query: String
    ) : JarvisAction {
        override val description: String = "Searching notes: '$query'"
    }

    data class AskClarification(
        val question: String,
        val options: List<String>
    ) : JarvisAction {
        override val description: String = "Clarification needed: $question"
    }

    data class DirectReply(
        val responseText: String
    ) : JarvisAction {
        override val description: String = "Responding to query"
    }
}
