package com.jarvis.assistant.model

import android.net.Uri

data class FileItem(
    val id: Long,
    val name: String,
    val path: String,
    val uri: Uri,
    val sizeBytes: Long,
    val mimeType: String,
    val dateModified: Long,
    val isDirectory: Boolean = false
) {
    val formattedSize: String
        get() {
            if (sizeBytes <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            val digitGroups = (Math.log10(sizeBytes.toDouble()) / Math.log10(1024.0)).toInt()
            val size = sizeBytes / Math.pow(1024.0, digitGroups.toDouble())
            return String.format(java.util.Locale.US, "%.1f %s", size, units[digitGroups])
        }

    val extension: String
        get() = name.substringAfterLast('.', "").uppercase()

    val formattedDate: String
        get() {
            val sdf = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
            return sdf.format(java.util.Date(dateModified * if (dateModified < 100000000000L) 1000L else 1L))
        }
}
