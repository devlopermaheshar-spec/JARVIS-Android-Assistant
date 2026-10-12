package com.jarvis.assistant.system

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import com.jarvis.assistant.model.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.Calendar
import java.util.Locale

class FileManager(private val context: Context) {
    private val tag = "JarvisFileManager"

    suspend fun searchFiles(
        query: String = "",
        fileCategory: String? = null, // "PDF", "DOCUMENT", "IMAGE", "AUDIO", "VIDEO", null
        dateFilter: String? = null // "YESTERDAY", "TODAY", "WEEK", null
    ): List<FileItem> = withContext(Dispatchers.IO) {
        val results = mutableListOf<FileItem>()

        try {
            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.DATE_MODIFIED
            )

            val selectionParts = mutableListOf<String>()
            val selectionArgs = mutableListOf<String>()

            // 1. Text Search Query on DISPLAY_NAME
            if (query.isNotBlank()) {
                selectionParts.add("${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE ?")
                selectionArgs.add("%$query%")
            }

            // 2. File Category filter
            when (fileCategory?.uppercase(Locale.ROOT)) {
                "PDF" -> {
                    selectionParts.add("(${MediaStore.Files.FileColumns.MIME_TYPE} = ? OR ${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE ?)")
                    selectionArgs.add("application/pdf")
                    selectionArgs.add("%.pdf")
                }
                "DOCUMENT" -> {
                    selectionParts.add("(${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ? OR ${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE ? OR ${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE ? OR ${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE ? OR ${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE ?)")
                    selectionArgs.add("%document%")
                    selectionArgs.add("%.pdf")
                    selectionArgs.add("%.docx")
                    selectionArgs.add("%.txt")
                    selectionArgs.add("%.xlsx")
                }
                "IMAGE" -> {
                    selectionParts.add("${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ?")
                    selectionArgs.add("image/%")
                }
                "AUDIO" -> {
                    selectionParts.add("${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ?")
                    selectionArgs.add("audio/%")
                }
                "VIDEO" -> {
                    selectionParts.add("${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ?")
                    selectionArgs.add("video/%")
                }
            }

            // 3. Date Filter: "Kal ke documents" (yesterday), "Aaj" (today), etc.
            val nowCal = Calendar.currentOrStartOfDay()
            when (dateFilter?.uppercase(Locale.ROOT)) {
                "TODAY" -> {
                    val startOfTodaySeconds = nowCal.timeInMillis / 1000L
                    selectionParts.add("${MediaStore.Files.FileColumns.DATE_MODIFIED} >= ?")
                    selectionArgs.add(startOfTodaySeconds.toString())
                }
                "YESTERDAY" -> {
                    val yesterdayStartCal = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_YEAR, -1)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val yesterdayEndCal = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_YEAR, -1)
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }
                    val startSec = yesterdayStartCal.timeInMillis / 1000L
                    val endSec = yesterdayEndCal.timeInMillis / 1000L
                    selectionParts.add("${MediaStore.Files.FileColumns.DATE_MODIFIED} BETWEEN ? AND ?")
                    selectionArgs.add(startSec.toString())
                    selectionArgs.add(endSec.toString())
                }
                "WEEK" -> {
                    val weekAgoCal = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_YEAR, -7)
                    }
                    val startSec = weekAgoCal.timeInMillis / 1000L
                    selectionParts.add("${MediaStore.Files.FileColumns.DATE_MODIFIED} >= ?")
                    selectionArgs.add(startSec.toString())
                }
            }

            val selection = if (selectionParts.isNotEmpty()) selectionParts.joinToString(" AND ") else null
            val args = if (selectionArgs.isNotEmpty()) selectionArgs.toTypedArray() else null

            val collectionUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Files.getContentUri("external")
            }

            val sortOrder = "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC LIMIT 50"

            context.contentResolver.query(
                collectionUri,
                projection,
                selection,
                args,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val dataCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val mimeCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val name = cursor.getString(nameCol) ?: "Unknown"
                    val path = if (dataCol != -1) cursor.getString(dataCol) ?: "" else ""
                    val size = cursor.getLong(sizeCol)
                    val mime = if (mimeCol != -1) cursor.getString(mimeCol) ?: "application/octet-stream" else "application/octet-stream"
                    val date = cursor.getLong(dateCol)
                    val contentUri = ContentUris.withAppendedId(collectionUri, id)

                    results.add(
                        FileItem(
                            id = id,
                            name = name,
                            path = path,
                            uri = contentUri,
                            sizeBytes = size,
                            mimeType = mime,
                            dateModified = date
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to query MediaStore", e)
        }

        // Fallback: also inspect standard Downloads folder if MediaStore is empty or for direct file access
        if (results.isEmpty() && query.isNotBlank()) {
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            searchDirectoryRecursive(downloadDir, query, fileCategory, results, maxDepth = 2)
        }

        results
    }

    private fun searchDirectoryRecursive(
        dir: File?,
        query: String,
        category: String?,
        results: MutableList<FileItem>,
        maxDepth: Int
    ) {
        if (dir == null || !dir.exists() || !dir.canRead() || maxDepth < 0) return
        val files = dir.listFiles() ?: return

        for (file in files) {
            if (file.isDirectory) {
                searchDirectoryRecursive(file, query, category, results, maxDepth - 1)
            } else {
                val matchesQuery = query.isEmpty() || file.name.contains(query, ignoreCase = true)
                val matchesCat = when (category?.uppercase(Locale.ROOT)) {
                    "PDF" -> file.name.endsWith(".pdf", ignoreCase = true)
                    "DOCUMENT" -> file.name.endsWith(".pdf", ignoreCase = true) ||
                            file.name.endsWith(".docx", ignoreCase = true) ||
                            file.name.endsWith(".txt", ignoreCase = true)
                    "IMAGE" -> file.name.endsWith(".jpg", ignoreCase = true) ||
                            file.name.endsWith(".png", ignoreCase = true)
                    else -> true
                }
                if (matchesQuery && matchesCat) {
                    val uri = try {
                        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                    } catch (e: Exception) {
                        Uri.fromFile(file)
                    }
                    results.add(
                        FileItem(
                            id = file.hashCode().toLong(),
                            name = file.name,
                            path = file.absolutePath,
                            uri = uri,
                            sizeBytes = file.length(),
                            mimeType = "application/octet-stream",
                            dateModified = file.lastModified() / 1000L
                        )
                    )
                    if (results.size >= 30) return
                }
            }
        }
    }

    suspend fun readDocumentContent(uri: Uri, maxChars: Int = 10000): String = withContext(Dispatchers.IO) {
        val stringBuilder = StringBuilder()
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line: String? = reader.readLine()
                    var charsRead = 0
                    while (line != null && charsRead < maxChars) {
                        stringBuilder.append(line).append("\n")
                        charsRead += line.length
                        line = reader.readLine()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to read document at $uri", e)
            return@withContext "Unable to extract raw text directly: ${e.localizedMessage}"
        }
        stringBuilder.toString()
    }

    fun openFile(uri: Uri, mimeType: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, if (mimeType.isNotBlank()) mimeType else "*/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(tag, "Failed to open file: $uri", e)
            false
        }
    }

    suspend fun deleteFile(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val rows = context.contentResolver.delete(uri, null, null)
            rows > 0
        } catch (e: Exception) {
            Log.e(tag, "Failed to delete file at $uri", e)
            false
        }
    }
}

private fun Calendar.Companion.currentOrStartOfDay(): Calendar {
    return getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
}
