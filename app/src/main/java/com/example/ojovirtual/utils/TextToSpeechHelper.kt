package com.example.ojovirtual.utils

import android.content.Context
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*

class TextToSpeechHelper(private val context: Context) {

    private var tts: TextToSpeech? = null
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady

    // Variables para controlar velocidad y tono
    private var speechRate = 1.0f
    private var speechPitch = 1.0f

    init {
        initTTS()
    }

    private fun initTTS() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale("es", "ES"))
                if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Intenta con español genérico
                    tts?.setLanguage(Locale("es"))
                }

                // Aplicar configuración guardada
                tts?.setPitch(speechPitch)
                tts?.setSpeechRate(speechRate)

                _isReady.value = true
            }
        }

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {}
            override fun onError(utteranceId: String?) {}
        })
    }

    fun speak(text: String, priority: Int = TextToSpeech.QUEUE_ADD) {
        if (_isReady.value) {
            // Asegurar que la configuración se aplique antes de hablar
            tts?.setSpeechRate(speechRate)
            tts?.setPitch(speechPitch)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                tts?.speak(text, priority, null, UUID.randomUUID().toString())
            } else {
                @Suppress("DEPRECATION")
                tts?.speak(text, priority, null)
            }
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        _isReady.value = false
    }

    fun isSpeaking(): Boolean {
        return tts?.isSpeaking ?: false
    }

    // Configurar velocidad de voz
    fun setSpeechRate(rate: Float) {
        speechRate = rate
        if (_isReady.value) {
            tts?.setSpeechRate(rate)
        }
    }

    // Configurar tono de voz
    fun setSpeechPitch(pitch: Float) {
        speechPitch = pitch
        if (_isReady.value) {
            tts?.setPitch(pitch)
        }
    }

    // Obtener velocidad actual
    fun getSpeechRate(): Float = speechRate

    // Obtener tono actual
    fun getSpeechPitch(): Float = speechPitch
}