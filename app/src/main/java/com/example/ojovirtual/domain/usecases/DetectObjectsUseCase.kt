package com.example.ojovirtual.domain.usecases

import android.graphics.Bitmap
import com.example.ojovirtual.data.repositories.DetectionResult
import com.example.ojovirtual.data.repositories.ObjectDetectionRepository

class DetectObjectsUseCase(private val objectDetectionRepository: ObjectDetectionRepository) {

    operator fun invoke(bitmap: Bitmap): List<DetectionResult> {
        return objectDetectionRepository.detectObjects(bitmap)
    }

    fun close() {
        objectDetectionRepository.close()
    }
}