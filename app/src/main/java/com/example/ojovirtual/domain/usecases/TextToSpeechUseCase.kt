package com.example.ojovirtual.domain.usecases  // ✅ TU PACKAGE

import com.example.ojovirtual.utils.TextToSpeechHelper  // ✅ TU PACKAGE
import kotlinx.coroutines.flow.StateFlow

class TextToSpeechUseCase(private val ttsHelper: TextToSpeechHelper) {

    val isReady: StateFlow<Boolean> = ttsHelper.isReady

    fun speak(text: String, stopPrevious: Boolean = false) {
        if (stopPrevious) {
            ttsHelper.stop()
        }
        ttsHelper.speak(text)
    }

    fun stop() {
        ttsHelper.stop()
    }

    fun shutdown() {
        ttsHelper.shutdown()
    }

    fun isSpeaking(): Boolean {
        return ttsHelper.isSpeaking()
    }

    // ✅ NUEVO: Configurar velocidad de voz
    fun setSpeechRate(rate: Float) {
        ttsHelper.setSpeechRate(rate)
    }

    // ✅ NUEVO: Configurar tono de voz
    fun setSpeechPitch(pitch: Float) {
        ttsHelper.setSpeechPitch(pitch)
    }

    // ✅ NUEVO: Obtener velocidad actual
    fun getSpeechRate(): Float = ttsHelper.getSpeechRate()

    // ✅ NUEVO: Obtener tono actual
    fun getSpeechPitch(): Float = ttsHelper.getSpeechPitch()
}