package com.jarvis.assistant.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class VoiceRecognizerManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val tag = "JarvisVoiceRecognizer"

    private var speechRecognizer: SpeechRecognizer? = null
    private var isContinuousListening = false

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText.asStateFlow()

    private val _audioRmsDb = MutableStateFlow(0f)
    val audioRmsDb: StateFlow<Float> = _audioRmsDb.asStateFlow()

    private val _recognizedSpeech = MutableSharedFlow<String>()
    val recognizedSpeech: SharedFlow<String> = _recognizedSpeech.asSharedFlow()

    private val _wakeWordDetected = MutableSharedFlow<Unit>()
    val wakeWordDetected: SharedFlow<Unit> = _wakeWordDetected.asSharedFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()

    var preferredLanguageTag: String = "en-IN" // "hi-IN", "en-IN", "en-US"

    init {
        initRecognizer()
    }

    private fun initRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            scope.launch {
                _errorMessage.emit("Speech recognition service is not available on this device.")
            }
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(createListener())
        }
    }

    private fun createListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            _isListening.value = true
        }

        override fun onBeginningOfSpeech() {
            _isListening.value = true
        }

        override fun onRmsChanged(rmsdB: Float) {
            // Normalize RMS dB typically in range -2 to 10
            val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
            _audioRmsDb.value = normalized
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            _audioRmsDb.value = 0f
        }

        override fun onError(error: Int) {
            _isListening.value = false
            _audioRmsDb.value = 0f
            val message = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Missing RECORD_AUDIO permission"
                SpeechRecognizer.ERROR_NETWORK -> "Network error"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                SpeechRecognizer.ERROR_SERVER -> "Recognition server error"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input detected"
                else -> "Recognition error: $error"
            }
            Log.d(tag, "SpeechRecognizer error: $message ($error)")

            // If continuous mode is enabled and it's a transient timeout or no-match, restart safely
            if (isContinuousListening && (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)) {
                restartListening()
            } else if (error != SpeechRecognizer.ERROR_NO_MATCH && error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                scope.launch { _errorMessage.emit(message) }
            }
        }

        override fun onResults(results: Bundle?) {
            _isListening.value = false
            _audioRmsDb.value = 0f
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                val bestMatch = matches[0].trim()
                _partialText.value = bestMatch
                checkWakeWordAndEmit(bestMatch)
            }

            if (isContinuousListening) {
                restartListening()
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                val partial = matches[0]
                _partialText.value = partial
                if (containsWakeWord(partial)) {
                    scope.launch { _wakeWordDetected.emit(Unit) }
                }
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    private fun containsWakeWord(text: String): Boolean {
        val lower = text.lowercase(Locale.ROOT)
        return lower.contains("jarvis") ||
                lower.contains("जार्विस") ||
                lower.contains("hey jarvis") ||
                lower.contains("ok jarvis")
    }

    private fun checkWakeWordAndEmit(spokenText: String) {
        val lower = spokenText.lowercase(Locale.ROOT)
        if (containsWakeWord(lower)) {
            scope.launch { _wakeWordDetected.emit(Unit) }
        }

        // Clean out wake word prefix if spoken as "Jarvis, open YouTube"
        val cleanedText = spokenText
            .replace(Regex("^(hey|ok|hello)?\\s*jarvis[,\\s]*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("^(अरे|सुनो)?\\s*जार्विस[,\\s]*", RegexOption.IGNORE_CASE), "")
            .trim()

        val textToProcess = if (cleanedText.isNotEmpty()) cleanedText else spokenText

        scope.launch {
            _recognizedSpeech.emit(textToProcess)
        }
    }

    fun startListening(continuous: Boolean = false) {
        isContinuousListening = continuous
        if (speechRecognizer == null) {
            initRecognizer()
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, preferredLanguageTag)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, preferredLanguageTag)
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        }

        try {
            speechRecognizer?.startListening(intent)
            _isListening.value = true
        } catch (e: Exception) {
            Log.e(tag, "Failed to start listening", e)
            _isListening.value = false
        }
    }

    fun stopListening() {
        isContinuousListening = false
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.e(tag, "Failed to stop listening", e)
        } finally {
            _isListening.value = false
            _audioRmsDb.value = 0f
        }
    }

    private fun restartListening() {
        if (!isContinuousListening) return
        scope.launch(Dispatchers.Main) {
            try {
                speechRecognizer?.cancel()
                kotlinx.coroutines.delay(250)
                startListening(continuous = true)
            } catch (e: Exception) {
                Log.e(tag, "Failed to restart speech recognizer", e)
            }
        }
    }

    fun destroy() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            Log.e(tag, "Failed to destroy speech recognizer", e)
        }
    }
}
