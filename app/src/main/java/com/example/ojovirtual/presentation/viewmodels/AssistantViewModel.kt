package com.example.ojovirtual.presentation.viewmodels

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ojovirtual.data.repositories.*
import com.example.ojovirtual.domain.usecases.*
import com.example.ojovirtual.utils.SensorHelper
import com.example.ojovirtual.utils.TextToSpeechHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AssistantViewModel(application: Application) : AndroidViewModel(application) {

    // Repositorios
    private val voiceRepository = VoiceRepository(application)
    private val objectDetectionRepository = ObjectDetectionRepository(application)
    private val textRecognitionRepository = TextRecognitionRepository()

    // Use Cases
    private val speechToTextUseCase = SpeechToTextUseCase(voiceRepository)
    private val detectObjectsUseCase = DetectObjectsUseCase(objectDetectionRepository)
    private val ttsHelper = TextToSpeechHelper(application)
    private val textToSpeechUseCase = TextToSpeechUseCase(ttsHelper)

    // Sensor Helper
    val sensorHelper = SensorHelper(application)

    // Estados
    private val _voiceState = MutableStateFlow<VoiceState>(VoiceState.Idle)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private val _detectionState = MutableStateFlow<DetectionState>(DetectionState.Idle)
    val detectionState: StateFlow<DetectionState> = _detectionState.asStateFlow()

    private val _textRecognitionState = MutableStateFlow<TextRecognitionState>(TextRecognitionState.Idle)
    val textRecognitionState: StateFlow<TextRecognitionState> = _textRecognitionState.asStateFlow()

    private val _statusMessage = MutableStateFlow("")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    // ✅ NUEVO: Estado para navegación
    private val _navigationCommand = MutableStateFlow<NavigationCommand?>(null)
    val navigationCommand: StateFlow<NavigationCommand?> = _navigationCommand.asStateFlow()

    // ✅ NUEVO: Estado para cerrar aplicación
    private val _closeAppCommand = MutableStateFlow(false)
    val closeAppCommand: StateFlow<Boolean> = _closeAppCommand.asStateFlow()

    // ✅ NUEVO: Configuración de voz
    private var _speechRate = MutableStateFlow(1.0f)
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private var _speechPitch = MutableStateFlow(1.0f)
    val speechPitch: StateFlow<Float> = _speechPitch.asStateFlow()

    init {
        setupShakeDetection()
        loadVoiceSettings()
    }

    private fun setupShakeDetection() {
        sensorHelper.onShakeDetected = {
            speak("Dispositivo agitado. Función de emergencia.")
            sensorHelper.vibrate(500)
        }
        sensorHelper.startShakeDetection()
    }

    // ✅ NUEVO: Cargar configuración de voz guardada
    private fun loadVoiceSettings() {
        val sharedPreferences = getApplication<Application>().getSharedPreferences("voice_settings", 0)
        _speechRate.value = sharedPreferences.getFloat("speech_rate", 1.0f)
        _speechPitch.value = sharedPreferences.getFloat("speech_pitch", 1.0f)

        // Aplicar configuración al TTS
        textToSpeechUseCase.setSpeechRate(_speechRate.value)
        textToSpeechUseCase.setSpeechPitch(_speechPitch.value)
    }

    // ✅ NUEVO: Guardar configuración de voz
    private fun saveVoiceSettings() {
        val sharedPreferences = getApplication<Application>().getSharedPreferences("voice_settings", 0)
        sharedPreferences.edit().apply {
            putFloat("speech_rate", _speechRate.value)
            putFloat("speech_pitch", _speechPitch.value)
            apply()
        }
    }

    // ✅ NUEVO: Configurar velocidad de voz
    fun setSpeechRate(rate: Float) {
        _speechRate.value = rate
        textToSpeechUseCase.setSpeechRate(rate)
        saveVoiceSettings()
    }

    // ✅ NUEVO: Configurar tono de voz
    fun setSpeechPitch(pitch: Float) {
        _speechPitch.value = pitch
        textToSpeechUseCase.setSpeechPitch(pitch)
        saveVoiceSettings()
    }

    // ✅ NUEVO: Obtener velocidad actual
    fun getSpeechRate(): Float = _speechRate.value

    // ✅ NUEVO: Obtener tono actual
    fun getSpeechPitch(): Float = _speechPitch.value

    // ✅ NUEVO: Restablecer configuración predeterminada
    fun resetVoiceSettings() {
        setSpeechRate(1.0f)
        setSpeechPitch(1.0f)
    }

    // ✅ NUEVO: Limpiar comando de cerrar app
    fun clearCloseAppCommand() {
        _closeAppCommand.value = false
    }

    // ============ VOICE FUNCTIONS ============

    fun startListening() {
        viewModelScope.launch {
            _voiceState.value = VoiceState.Listening
            _statusMessage.value = "Escuchando..."

            speechToTextUseCase().collect { result ->
                when (result) {
                    is VoiceResult.Ready -> {
                        _voiceState.value = VoiceState.Listening
                    }
                    is VoiceResult.Speaking -> {
                        _voiceState.value = VoiceState.Speaking
                    }
                    is VoiceResult.EndSpeaking -> {
                        _statusMessage.value = "Procesando..."
                    }
                    is VoiceResult.Success -> {
                        _voiceState.value = VoiceState.Success(result.text)
                        processVoiceCommand(result.text)
                    }
                    is VoiceResult.Partial -> {
                        _statusMessage.value = "Reconociendo: ${result.text}"
                    }
                    is VoiceResult.Error -> {
                        _voiceState.value = VoiceState.Error(result.message)
                        _statusMessage.value = "Error: ${result.message}"
                        // ✅ Resetear después de error
                        resetStatesAfterDelay()
                    }
                }
            }
        }
    }

    fun stopListening() {
        speechToTextUseCase.stop()
        _voiceState.value = VoiceState.Idle
        _statusMessage.value = ""
    }

    private fun processVoiceCommand(command: String) {
        val lowerCommand = command.lowercase()

        when {
            // ✅ NUEVO: Comando CONFIGURACIÓN
            lowerCommand.contains("configuración") || lowerCommand.contains("configuracion") -> {
                speak("Abriendo configuración de voz")
                _navigationCommand.value = NavigationCommand.NavigateToSettings
                resetStatesAfterDelay()
            }

            // ✅ NUEVO: Comando SALIR/CERRAR
            lowerCommand.contains("salir") || lowerCommand.contains("cerrar") -> {
                speak("Cerrando aplicación")
                // Esperar un momento para que se escuche el mensaje antes de cerrar
                viewModelScope.launch {
                    delay(1500)
                    _closeAppCommand.value = true
                }
                resetStatesAfterDelay()
            }

            // ✅ CORREGIDO: Comandos de cámara PRIMERO (antes de "ayuda" genérico)
            lowerCommand.contains("ayuda a ver") || lowerCommand.contains("ayúdame a ver") -> {
                speak("Activando cámara para ayudarte a ver toca la pantalla para capturar tu entorno")
                _navigationCommand.value = NavigationCommand.NavigateToCamera
                resetStatesAfterDelay()
            }

            lowerCommand.contains("ver") || lowerCommand.contains("cámara") ||
                    lowerCommand.contains("camara") || lowerCommand.contains("detecta") -> {
                speak("Activando modo visión")
                _navigationCommand.value = NavigationCommand.NavigateToCamera
                resetStatesAfterDelay()
            }

            // ✅ LEER texto
            lowerCommand.contains("lee") || lowerCommand.contains("leer") ||
                    lowerCommand.contains("texto") -> {
                speak("Activando lectura de texto")
                _navigationCommand.value = NavigationCommand.NavigateToCamera
                resetStatesAfterDelay()
            }

            // Buscar objeto específico
            lowerCommand.contains("busca") || lowerCommand.contains("encuentra") -> {
                val objectToFind = extractObjectFromCommand(lowerCommand)
                speak("Buscando $objectToFind")
                _navigationCommand.value = NavigationCommand.NavigateToCamera
                resetStatesAfterDelay()
            }

            // Comandos de fecha y hora
            lowerCommand.contains("qué hora") || lowerCommand.contains("que hora") -> {
                val time = sensorHelper.getCurrentTime()
                speak("Son las $time")
                resetStatesAfterDelay()
            }

            lowerCommand.contains("qué fecha") || lowerCommand.contains("que fecha") ||
                    lowerCommand.contains("qué día") || lowerCommand.contains("que dia") -> {
                val date = sensorHelper.getCurrentDate()
                speak("Hoy es $date")
                resetStatesAfterDelay()
            }

            // Batería
            lowerCommand.contains("batería") || lowerCommand.contains("bateria") -> {
                val battery = sensorHelper.getBatteryLevel()
                speak("La batería está al $battery por ciento")
                resetStatesAfterDelay()
            }

            // Día o noche
            lowerCommand.contains("día") && lowerCommand.contains("noche") ||
                    lowerCommand.contains("dia") && lowerCommand.contains("noche") -> {
                val isDayTime = sensorHelper.isDayTime()
                speak(if (isDayTime) "Es de día" else "Es de noche")
                resetStatesAfterDelay()
            }

            // Ayuda genérica (DEBE IR AL FINAL)
            lowerCommand.contains("ayuda") -> {
                speak("Comandos disponibles: Qué hora es, qué fecha es hoy, cuánta batería tengo, es de día o de noche, ayuda a ver, lee esto, configuración, salir")
                resetStatesAfterDelay()
            }

            // Comando no reconocido
            else -> {
                speak("No entendí el comando. Di 'ayuda' para ver los comandos disponibles")
                resetStatesAfterDelay()
            }
        }

        // Volver al estado idle después de procesar
        _voiceState.value = VoiceState.Idle
    }

    private fun extractObjectFromCommand(command: String): String {
        val keywords = listOf("busca", "encuentra", "dónde está", "donde esta")
        var objectName = command

        keywords.forEach { keyword ->
            if (command.contains(keyword)) {
                objectName = command.substringAfter(keyword).trim()
            }
        }

        return objectName.ifEmpty { "objeto" }
    }

    // ============ OBJECT DETECTION FUNCTIONS ============

    fun detectObjects(bitmap: Bitmap) {
        viewModelScope.launch {
            _detectionState.value = DetectionState.Detecting
            _statusMessage.value = "Detectando objetos..."

            try {
                val results = detectObjectsUseCase(bitmap)

                if (results.isEmpty()) {
                    _detectionState.value = DetectionState.NoObjects
                    speak("No se detectan objetos")
                } else {
                    _detectionState.value = DetectionState.Success(results)
                    announceDetections(results)
                }
            } catch (e: Exception) {
                _detectionState.value = DetectionState.Error(e.message ?: "Error en detección")
                speak("Error al detectar objetos")
            }

            // ✅ IMPORTANTE: Resetear después de completar la detección
            resetDetectionStateAfterDelay()
        }
    }

    private fun announceDetections(results: List<DetectionResult>) {
        if (results.isEmpty()) return

        val announcement = buildString {
            append("Detectados ${results.size} objetos. ")
            results.take(3).forEach { detection ->
                append("${detection.label} a ${detection.distance}. ")
            }
        }

        speak(announcement)
    }

    fun detectSingleObject(bitmap: Bitmap, objectName: String) {
        viewModelScope.launch {
            _detectionState.value = DetectionState.Detecting

            try {
                val results = detectObjectsUseCase(bitmap)
                val foundObject = results.find {
                    it.label.contains(objectName, ignoreCase = true)
                }

                if (foundObject != null) {
                    speak("${foundObject.label} encontrado a ${foundObject.distance}")
                    _detectionState.value = DetectionState.Success(listOf(foundObject))
                } else {
                    speak("$objectName no encontrado")
                    _detectionState.value = DetectionState.NoObjects
                }
            } catch (e: Exception) {
                _detectionState.value = DetectionState.Error(e.message ?: "Error")
                speak("Error en la búsqueda")
            }

            // ✅ Resetear después de completar
            resetDetectionStateAfterDelay()
        }
    }

    // ============ TEXT RECOGNITION FUNCTIONS ============

    fun recognizeText(bitmap: Bitmap) {
        viewModelScope.launch {
            _textRecognitionState.value = TextRecognitionState.Recognizing
            _statusMessage.value = "Reconociendo texto..."

            try {
                when (val result = textRecognitionRepository.recognizeText(bitmap)) {
                    is TextRecognitionResult.Success -> {
                        if (result.fullText.isNotBlank()) {
                            _textRecognitionState.value = TextRecognitionState.Success(result.fullText)
                            speak("Texto detectado: ${result.fullText}")
                        } else {
                            _textRecognitionState.value = TextRecognitionState.NoText
                            speak("No se detectó texto")
                        }
                    }
                    is TextRecognitionResult.Error -> {
                        _textRecognitionState.value = TextRecognitionState.Error(result.message)
                        speak("Error al reconocer texto")
                    }
                }
            } catch (e: Exception) {
                _textRecognitionState.value = TextRecognitionState.Error(e.message ?: "Error")
                speak("Error en reconocimiento de texto")
            }

            // ✅ IMPORTANTE: Resetear después de completar el reconocimiento
            resetTextRecognitionStateAfterDelay()
        }
    }

    // ============ TEXT TO SPEECH FUNCTIONS ============

    fun speak(text: String, stopPrevious: Boolean = true) {
        textToSpeechUseCase.speak(text, stopPrevious)
    }

    fun stopSpeaking() {
        textToSpeechUseCase.stop()
    }

    // ✅ NUEVO: Limpiar comando de navegación después de usarlo
    fun clearNavigationCommand() {
        _navigationCommand.value = null
    }

    // ✅ NUEVOS MÉTODOS: Resetear estados después de un tiempo
    private fun resetStatesAfterDelay() {
        viewModelScope.launch {
            delay(3000) // 3 segundos
            _statusMessage.value = ""
            _voiceState.value = VoiceState.Idle
        }
    }

    private fun resetDetectionStateAfterDelay() {
        viewModelScope.launch {
            delay(5000) // 5 segundos para detección (más tiempo para ver resultados)
            _detectionState.value = DetectionState.Idle
            _statusMessage.value = ""
        }
    }

    private fun resetTextRecognitionStateAfterDelay() {
        viewModelScope.launch {
            delay(5000) // 5 segundos para reconocimiento de texto
            _textRecognitionState.value = TextRecognitionState.Idle
            _statusMessage.value = ""
        }
    }

    // ============ CLEANUP ============

    override fun onCleared() {
        super.onCleared()
        speechToTextUseCase.destroy()
        detectObjectsUseCase.close()
        textRecognitionRepository.close()
        textToSpeechUseCase.shutdown()
        sensorHelper.stopShakeDetection()
    }
}

// ============ ESTADOS ============

sealed class VoiceState {
    object Idle : VoiceState()
    object Listening : VoiceState()
    object Speaking : VoiceState()
    data class Success(val text: String) : VoiceState()
    data class Error(val message: String) : VoiceState()
}

sealed class DetectionState {
    object Idle : DetectionState()
    object Detecting : DetectionState()
    data class Success(val results: List<DetectionResult>) : DetectionState()
    object NoObjects : DetectionState()
    data class Error(val message: String) : DetectionState()
}

sealed class TextRecognitionState {
    object Idle : TextRecognitionState()
    object Recognizing : TextRecognitionState()
    data class Success(val text: String) : TextRecognitionState()
    object NoText : TextRecognitionState()
    data class Error(val message: String) : TextRecognitionState()
}

// ✅ NUEVO: Comandos de navegación
sealed class NavigationCommand {
    object NavigateToCamera : NavigationCommand()
    object NavigateToVoice : NavigationCommand()
    object NavigateToSettings : NavigationCommand()
}