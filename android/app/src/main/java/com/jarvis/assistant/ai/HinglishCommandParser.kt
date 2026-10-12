package com.jarvis.assistant.ai

import com.jarvis.assistant.model.JarvisAction
import java.util.Locale

object HinglishCommandParser {

    data class ParseResult(
        val action: JarvisAction,
        val initialReply: String
    )

    fun tryParse(rawInput: String): ParseResult? {
        val lower = rawInput.lowercase(Locale.ROOT).trim()

        // 1. Battery inquiries ("Meri battery kitni hai?", "Battery check karo", "How much battery is left?")
        if (lower.contains("battery") || lower.contains("charging") || lower.contains("charge hai")) {
            return ParseResult(
                action = JarvisAction.GetBatteryInfo,
                initialReply = "Checking system power diagnostics, sir."
            )
        }

        // 2. Yesterday's documents ("Kal ke documents dikhao", "Show yesterday's documents")
        if ((lower.contains("kal") || lower.contains("yesterday")) &&
            (lower.contains("document") || lower.contains("file") || lower.contains("pdf"))
        ) {
            return ParseResult(
                action = JarvisAction.SearchFiles(query = "", fileType = "DOCUMENT", filterDate = "YESTERDAY"),
                initialReply = "Retrieving documents modified yesterday, sir."
            )
        }

        // 3. Downloads PDF search ("Mere downloads mein PDF dhundo", "Find PDFs in downloads")
        if ((lower.contains("download") || lower.contains("downloads")) &&
            (lower.contains("pdf") || lower.contains("document"))
        ) {
            return ParseResult(
                action = JarvisAction.SearchFiles(query = "pdf", fileType = "PDF"),
                initialReply = "Scanning your Downloads directory for PDF files, sir."
            )
        }

        // General PDF search
        if (lower.contains("pdf") && (lower.contains("dhundo") || lower.contains("khojo") || lower.contains("find") || lower.contains("search"))) {
            return ParseResult(
                action = JarvisAction.SearchFiles(query = "pdf", fileType = "PDF"),
                initialReply = "Locating all PDF documents on your device, sir."
            )
        }

        // 4. Camera launch ("Camera kholo", "Open camera", "Photo khicho", "Camera on karo")
        if (lower.contains("camera") || lower.contains("photo khicho") || lower.contains("selfie") || lower.contains("tasveer")) {
            return ParseResult(
                action = JarvisAction.LaunchApp(appName = "Camera"),
                initialReply = "Initiating camera sensors right away, sir."
            )
        }

        // 5. App launch ("YouTube kholo", "WhatsApp kholo", "Chrome open karo", "Open Instagram")
        val appLaunchRegex = Regex(
            "^(open|launch|kholo|chalao|start)?\\s*([a-zA-Z0-9]+)\\s*(kholo|chalao|open|launch|app|khol do)?$",
            RegexOption.IGNORE_CASE
        )
        val match = appLaunchRegex.find(lower)
        if (match != null) {
            val potentialApp = match.groupValues[2].trim()
            val commonAppNames = listOf(
                "youtube", "whatsapp", "chrome", "instagram", "telegram",
                "spotify", "gmail", "maps", "calculator", "settings", "gallery", "clock", "calendar"
            )
            val matchedApp = commonAppNames.firstOrNull { potentialApp.equals(it, ignoreCase = true) }
            if (matchedApp != null) {
                return ParseResult(
                    action = JarvisAction.LaunchApp(appName = matchedApp.replaceFirstChar { it.uppercase() }),
                    initialReply = "Launching ${matchedApp.replaceFirstChar { it.uppercase() }}, sir."
                )
            }
        }

        // Explicit "kholo" or "open" followed by app name
        if (lower.contains("kholo") || lower.contains("open ") || lower.contains("chalao")) {
            for (app in listOf("youtube", "whatsapp", "chrome", "instagram", "telegram", "spotify", "gmail", "maps", "calculator", "settings", "gallery")) {
                if (lower.contains(app)) {
                    return ParseResult(
                        action = JarvisAction.LaunchApp(appName = app.replaceFirstChar { it.uppercase() }),
                        initialReply = "Opening ${app.replaceFirstChar { it.uppercase() }}, sir."
                    )
                }
            }
        }

        // 6. Reminder creation ("Mujhe reminder de do", "Reminder lagao", "Set reminder")
        if (lower.contains("reminder") || lower.contains("yaad dilana") || lower.contains("yaad dilao")) {
            val cleanTitle = lower
                .replace(Regex("^(mujhe|mera)?\\s*(reminder|yaad dilana|yaad dilao)?\\s*(de do|lagao|set karo)?"), "")
                .trim()

            if (cleanTitle.length > 3) {
                return ParseResult(
                    action = JarvisAction.CreateReminder(title = cleanTitle, timeString = "30 minutes"),
                    initialReply = "Setting a reminder for: '$cleanTitle', sir."
                )
            } else {
                return ParseResult(
                    action = JarvisAction.AskClarification(
                        question = "What would you like to be reminded about, and when, sir?",
                        options = listOf("Meeting in 30 mins", "Call in 1 hour", "Review notes tonight")
                    ),
                    initialReply = "Please specify the reminder details, sir."
                )
            }
        }

        // 7. Storage status ("Storage kitna khali hai?", "Storage check karo", "Available space")
        if (lower.contains("storage") || lower.contains("memory kitni") || lower.contains("khali space") || lower.contains("free space")) {
            return ParseResult(
                action = JarvisAction.GetStorageInfo,
                initialReply = "Analyzing internal and external storage metrics, sir."
            )
        }

        // 8. Device diagnostics ("Phone information", "Device model kya hai?", "System specs")
        if (lower.contains("device") || lower.contains("specs") || lower.contains("model kya") || lower.contains("android version") || lower.contains("diagnostic")) {
            return ParseResult(
                action = JarvisAction.GetDeviceInfo,
                initialReply = "Compiling full hardware and OS diagnostic telemetries, sir."
            )
        }

        // 9. System settings ("Wi-Fi settings kholo", "Bluetooth open karo")
        if (lower.contains("wifi") || lower.contains("wi-fi")) {
            return ParseResult(
                action = JarvisAction.OpenSettings("wifi"),
                initialReply = "Accessing Wi-Fi interface settings, sir."
            )
        }
        if (lower.contains("bluetooth")) {
            return ParseResult(
                action = JarvisAction.OpenSettings("bluetooth"),
                initialReply = "Accessing Bluetooth configuration panel, sir."
            )
        }
        if (lower.contains("location") || lower.contains("gps")) {
            return ParseResult(
                action = JarvisAction.OpenSettings("location"),
                initialReply = "Accessing Location and GPS settings, sir."
            )
        }

        // 10. Notes ("Note banao", "Write note", "Save note")
        if (lower.startsWith("note ") || lower.contains("note likho") || lower.contains("note banao")) {
            val content = lower.replace(Regex("^(note likho|note banao|save note|write note)\\s*"), "").trim()
            return ParseResult(
                action = JarvisAction.CreateNote(title = "Voice Note", content = content.ifEmpty { "Quick note created via Jarvis" }),
                initialReply = "Saving note to internal memory, sir."
            )
        }

        return null
    }
}
