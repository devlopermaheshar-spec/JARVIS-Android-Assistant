package com.jarvis.assistant.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID

class TextToSpeechManager(private val context: Context) : TextToSpeech.OnInitListener {
    private val tag = "JarvisTTS"

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    var pitch: Float = 0.88f // Jarvis's refined lower pitch
    var speechRate: Float = 1.0f

    private val hindiLocale = Locale("hi", "IN")
    private val englishInLocale = Locale("en", "IN")
    private val englishUkLocale = Locale.UK

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.let { engine ->
                // Check if English (UK or IN) is available
                val result = engine.setLanguage(englishInLocale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    engine.setLanguage(Locale.US)
                }
                engine.setPitch(pitch)
                engine.setSpeechRate(speechRate)

                engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }
                })
            }
            Log.d(tag, "TextToSpeech successfully initialized.")
        } else {
            Log.e(tag, "Failed to initialize TextToSpeech ($status)")
        }
    }

    fun speak(text: String, queueMode: Int = TextToSpeech.QUEUE_FLUSH) {
        if (!isInitialized || tts == null) {
            Log.w(tag, "TTS is not ready yet.")
            return
        }

        val containsHindiScript = text.any { it in '\u0900'..'\u097F' }
        if (containsHindiScript) {
            val res = tts?.setLanguage(hindiLocale)
            if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(englishInLocale)
            }
        } else {
            val res = tts?.setLanguage(englishUkLocale)
            if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(englishInLocale)
            }
        }

        tts?.setPitch(pitch)
        tts?.setSpeechRate(speechRate)

        val utteranceId = UUID.randomUUID().toString()
        tts?.speak(text, queueMode, null, utteranceId)
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.e(tag, "Error stopping TTS", e)
        } finally {
            _isSpeaking.value = false
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
        } catch (e: Exception) {
            Log.e(tag, "Error shutting down TTS", e)
        }
    }
}
