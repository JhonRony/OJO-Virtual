package com.example.ojovirtual.data.repositories

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.detector.Detection
import org.tensorflow.lite.task.vision.detector.ObjectDetector

class ObjectDetectionRepository(private val context: Context) {

    private var objectDetector: ObjectDetector? = null

    init {
        setupObjectDetector()
    }

    private fun setupObjectDetector() {
        try {
            val options = ObjectDetector.ObjectDetectorOptions.builder()
                .setMaxResults(5)
                .setScoreThreshold(0.4f)
                .build()

            objectDetector = ObjectDetector.createFromFileAndOptions(
                context,
                "efficientdet-lite0.tflite",
                options
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun detectObjects(bitmap: Bitmap): List<DetectionResult> {
        if (objectDetector == null) {
            return emptyList()
        }

        try {
            val tensorImage = TensorImage.fromBitmap(bitmap)
            val results = objectDetector?.detect(tensorImage)

            return results?.map { detection ->
                DetectionResult(
                    label = translateLabel(detection.categories[0].label),
                    confidence = detection.categories[0].score,
                    boundingBox = detection.boundingBox,
                    distance = calculateDistance(detection.boundingBox, bitmap.width, bitmap.height)
                )
            } ?: emptyList()

        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

    private fun translateLabel(label: String): String {
        return when (label.lowercase()) {
            // Personas
            "person" -> "persona"
            "man" -> "hombre"
            "woman" -> "mujer"
            "child" -> "niño"

            // Muebles
            "chair" -> "silla"
            "table" -> "mesa"
            "desk" -> "escritorio"
            "bed" -> "cama"
            "couch" -> "sofá"
            "sofa" -> "sofá"
            "cabinet" -> "armario"
            "shelf" -> "estante"

            // Electrónicos
            "tv" -> "televisor"
            "television" -> "televisor"
            "monitor" -> "monitor"
            "laptop" -> "portátil"
            "computer" -> "computadora"
            "keyboard" -> "teclado"
            "mouse" -> "ratón"
            "phone" -> "teléfono"
            "cell phone" -> "teléfono móvil"

            // Cocina
            "bottle" -> "botella"
            "cup" -> "taza"
            "glass" -> "vaso"
            "bowl" -> "bol"
            "plate" -> "plato"
            "fork" -> "tenedor"
            "knife" -> "cuchillo"
            "spoon" -> "cuchara"

            // Alimentos
            "apple" -> "manzana"
            "banana" -> "plátano"
            "orange" -> "naranja"
            "sandwich" -> "sándwich"
            "pizza" -> "pizza"
            "hot dog" -> "perro caliente"

            // Ropa
            "hat" -> "sombrero"
            "cap" -> "gorra"
            "shirt" -> "camisa"
            "pants" -> "pantalones"
            "shoe" -> "zapato"
            "sneaker" -> "tenis"

            // Animales
            "dog" -> "perro"
            "cat" -> "gato"
            "bird" -> "pájaro"
            "horse" -> "caballo"
            "cow" -> "vaca"

            // Vehículos
            "car" -> "coche"
            "truck" -> "camión"
            "bus" -> "autobús"
            "motorcycle" -> "motocicleta"
            "bicycle" -> "bicicleta"

            // Casa
            "door" -> "puerta"
            "window" -> "ventana"
            "clock" -> "reloj"
            "vase" -> "florero"
            "book" -> "libro"
            "backpack" -> "mochila"
            "umbrella" -> "paraguas"

            // Deportes
            "sports ball" -> "pelota"
            "ball" -> "pelota"
            "tennis racket" -> "raqueta de tenis"
            "baseball bat" -> "bate de béisbol"

            // Baño
            "toilet" -> "inodoro"
            "sink" -> "lavabo"
            "toothbrush" -> "cepillo de dientes"

            // Oficina
            "scissors" -> "tijeras"
            "book" -> "libro"
            "newspaper" -> "periódico"

            // Si no está en la lista, devolver el original pero en español amigable
            else -> {
                // Convertir "traffic_light" a "semáforo" o mantener el original
                if (label.contains("_")) {
                    label.replace("_", " ").lowercase()
                } else {
                    label
                }
            }
        }
    }

    private fun calculateDistance(boundingBox: RectF, imageWidth: Int, imageHeight: Int): String {
        val boxArea = (boundingBox.width() * boundingBox.height())
        val imageArea = (imageWidth * imageHeight).toFloat()
        val areaRatio = boxArea / imageArea

        return when {
            areaRatio > 0.4 -> "menos de 1 metro"
            areaRatio > 0.2 -> "1 a 2 metros"
            areaRatio > 0.1 -> "2 a 3 metros"
            areaRatio > 0.05 -> "3 a 5 metros"
            else -> "más de 5 metros"
        }
    }

    fun close() {
        objectDetector?.close()
        objectDetector = null
    }
}

data class DetectionResult(
    val label: String,
    val confidence: Float,
    val boundingBox: RectF,
    val distance: String
)