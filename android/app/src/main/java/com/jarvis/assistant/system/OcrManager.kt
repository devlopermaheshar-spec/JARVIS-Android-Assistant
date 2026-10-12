package com.jarvis.assistant.system

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class OcrManager(private val context: Context) {
    private val tag = "JarvisOcrManager"
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun extractTextFromImage(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val image = InputImage.fromFilePath(context, uri)
            suspendCancellableCoroutine { continuation ->
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val text = visionText.text
                        if (text.isNotBlank()) {
                            continuation.resume(Result.success(text))
                        } else {
                            continuation.resume(Result.success("No readable text detected in this image."))
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e(tag, "ML Kit OCR failed", exception)
                        continuation.resume(Result.failure(exception))
                    }
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to load image for OCR", e)
            Result.failure(e)
        }
    }
}
