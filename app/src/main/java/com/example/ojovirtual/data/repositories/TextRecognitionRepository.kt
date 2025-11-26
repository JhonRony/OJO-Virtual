package com.example.ojovirtual.data.repositories

import android.graphics.Bitmap
import android.graphics.Rect
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class TextRecognitionRepository {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun recognizeText(bitmap: Bitmap): TextRecognitionResult {
        return try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val result = recognizer.process(inputImage).awaitCoroutine()

            val fullText = result.text
            val blocks = result.textBlocks.map { block ->
                TextBlock(
                    text = block.text,
                    boundingBox = block.boundingBox,
                    // ✅ confidence fue removido de la API moderna de ML Kit
                    // Usamos un valor por defecto o calculamos basado en líneas
                    confidence = calculateConfidence(block)
                )
            }

            TextRecognitionResult.Success(fullText, blocks)
        } catch (e: Exception) {
            TextRecognitionResult.Error(e.message ?: "Error al reconocer texto")
        }
    }

    // ✅ FUNCIÓN PARA CALCULAR CONFIANZA (ML Kit removió confidence directo)
    private fun calculateConfidence(textBlock: Text.TextBlock): Float {
        // Estrategia: usar la confianza de las líneas o valor por defecto
        return if (textBlock.lines.isNotEmpty()) {
            // Promedio de confianza de las líneas (si están disponibles)
            val linesWithConfidence = textBlock.lines.filter {
                it.confidence != null
            }
            if (linesWithConfidence.isNotEmpty()) {
                linesWithConfidence.map { it.confidence!! }.average().toFloat()
            } else {
                0.8f // Valor por defecto si no hay confianza disponible
            }
        } else {
            0.8f // Valor por defecto
        }
    }

    private suspend fun <T> Task<T>.awaitCoroutine(): T {
        return suspendCancellableCoroutine { continuation ->
            addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    continuation.resume(task.result)
                } else {
                    continuation.resumeWithException(task.exception ?: Exception("Unknown error"))
                }
            }
        }
    }

    fun close() {
        recognizer.close()
    }
}

sealed class TextRecognitionResult {
    data class Success(val fullText: String, val blocks: List<TextBlock>) : TextRecognitionResult()
    data class Error(val message: String) : TextRecognitionResult()
}

data class TextBlock(
    val text: String,
    val boundingBox: Rect?,
    val confidence: Float
)