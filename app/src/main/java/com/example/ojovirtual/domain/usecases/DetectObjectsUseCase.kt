package com.example.ojovirtual.domain.usecases  // ✅ TU PACKAGE

import android.graphics.Bitmap
import com.example.ojovirtual.data.repositories.DetectionResult         // ✅ TU PACKAGE
import com.example.ojovirtual.data.repositories.ObjectDetectionRepository // ✅ TU PACKAGE

class DetectObjectsUseCase(private val objectDetectionRepository: ObjectDetectionRepository) {

    operator fun invoke(bitmap: Bitmap): List<DetectionResult> {
        return objectDetectionRepository.detectObjects(bitmap)
    }

    fun close() {
        objectDetectionRepository.close()
    }
}