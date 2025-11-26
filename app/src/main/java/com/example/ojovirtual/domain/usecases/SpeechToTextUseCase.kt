package com.example.ojovirtual.domain.usecases  // ✅ TU PACKAGE

import com.example.ojovirtual.data.repositories.VoiceRepository  // ✅ TU PACKAGE
import com.example.ojovirtual.data.repositories.VoiceResult      // ✅ TU PACKAGE
import kotlinx.coroutines.flow.Flow

class SpeechToTextUseCase(private val voiceRepository: VoiceRepository) {

    operator fun invoke(): Flow<VoiceResult> {
        return voiceRepository.startListening()
    }

    fun stop() {
        voiceRepository.stopListening()
    }

    fun destroy() {
        voiceRepository.destroy()
    }
}