package com.example.ojovirtual.domain.usecases

import com.example.ojovirtual.data.repositories.VoiceRepository
import com.example.ojovirtual.data.repositories.VoiceResult
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