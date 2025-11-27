package com.example.ojovirtual.presentation.ui

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.ojovirtual.databinding.FragmentCameraBinding
import com.example.ojovirtual.presentation.viewmodels.AssistantViewModel
import com.example.ojovirtual.presentation.viewmodels.DetectionState
import com.example.ojovirtual.presentation.viewmodels.TextRecognitionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AssistantViewModel by activityViewModels()

    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraExecutor = Executors.newSingleThreadExecutor()

        setupCamera()
        setupUI()
        observeViewModel()
    }

    private fun setupCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )

            } catch (e: Exception) {
                Log.e("CameraFragment", "Error al iniciar cámara", e)
                viewModel.speak("Error al iniciar la cámara")
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun setupUI() {
        // CAPTURAR AL TOCAR LA PANTALLA
        binding.cameraPreview.setOnClickListener {
            captureAndDetectObjects()
        }

        binding.fabDetectObjects.setOnClickListener {
            captureAndDetectObjects()
        }

        binding.fabReadText.setOnClickListener {
            captureAndReadText()
        }
    }

    // MÉTODO: Capturar y detectar objetos
    private fun captureAndDetectObjects() {
        binding.detectionText.text = "📸 Capturando..."
        viewModel.sensorHelper.vibrate(100)

        imageCapture?.takePicture(
            cameraExecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val bitmap = imageProxyToBitmap(image)
                    if (bitmap != null) {
                        Log.d("CameraFragment", "Bitmap capturado: ${bitmap.width}x${bitmap.height}")
                        binding.detectionText.text = "🔍 Analizando..."
                        viewModel.detectObjects(bitmap)
                    } else {
                        Log.e("CameraFragment", "Bitmap es null")
                        binding.detectionText.text = "❌ Error al capturar"
                        resetToIdleAfterDelay()
                    }
                    image.close()
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraFragment", "Error al capturar imagen", exception)
                    binding.detectionText.text = "❌ Error al capturar"
                    resetToIdleAfterDelay()
                }
            }
        )
    }

    private fun captureAndReadText() {
        binding.detectionText.text = "📸 Capturando..."
        viewModel.sensorHelper.vibrate(100)

        imageCapture?.takePicture(
            cameraExecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val bitmap = imageProxyToBitmap(image)
                    if (bitmap != null) {
                        binding.detectionText.text = "📖 Leyendo texto..."
                        viewModel.recognizeText(bitmap)
                    } else {
                        Log.e("CameraFragment", "Bitmap es null, no se puede reconocer texto")
                        binding.detectionText.text = "❌ Error al procesar"
                        resetToIdleAfterDelay()
                    }
                    image.close()
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraFragment", "Error al capturar imagen", exception)
                    binding.detectionText.text = "❌ Error al capturar"
                    resetToIdleAfterDelay()
                }
            }
        )
    }

    // MÉTODO: Resetear a estado inicial después de un tiempo
    private fun resetToIdleAfterDelay() {
        viewLifecycleOwner.lifecycleScope.launch {
            delay(3000) // 3 segundos
            if (viewModel.detectionState.value is DetectionState.Idle &&
                viewModel.textRecognitionState.value is TextRecognitionState.Idle) {
                binding.detectionText.text = "👆 Toca la pantalla para capturar"
                binding.distanceText.visibility = View.GONE
            }
        }
    }

    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
        return try {
            when (imageProxy.format) {
                ImageFormat.JPEG -> {
                    convertJPEGToBitmap(imageProxy)
                }
                else -> {
                    Log.w("CameraFragment", "Formato no soportado: ${imageProxy.format}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("CameraFragment", "Error en imageProxyToBitmap: ${e.message}")
            null
        }
    }

    private fun convertJPEGToBitmap(imageProxy: ImageProxy): Bitmap? {
        val buffer = imageProxy.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)

        return try {
            val originalBitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (originalBitmap == null) {
                Log.e("CameraFragment", "BitmapFactory.decodeByteArray retornó null")
                return null
            }

            // Rotar la imagen según la orientación
            val matrix = Matrix()
            matrix.postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())

            Bitmap.createBitmap(
                originalBitmap,
                0,
                0,
                originalBitmap.width,
                originalBitmap.height,
                matrix,
                true
            )
        } catch (e: Exception) {
            Log.e("CameraFragment", "Error en convertJPEGToBitmap: ${e.message}")
            null
        }
    }

    private fun observeViewModel() {
        // Observar estado de detección de objetos
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.detectionState.collect { state ->
                when (state) {
                    is DetectionState.Idle -> {
                        // Resetear si no hay otro estado activo
                        if (viewModel.textRecognitionState.value is TextRecognitionState.Idle) {
                            binding.detectionText.text = "👆 Toca la pantalla para capturar"
                            binding.distanceText.visibility = View.GONE
                        }
                    }

                    is DetectionState.Detecting -> {
                        binding.detectionText.text = "🔍 Analizando imagen..."
                    }

                    is DetectionState.Success -> {
                        if (state.results.isNotEmpty()) {
                            val mainObject = state.results.first()
                            binding.detectionText.text = "✅ ${mainObject.label}"
                            binding.distanceText.text = "📏 ${mainObject.distance}"
                            binding.distanceText.visibility = View.VISIBLE

                            // Resetear después de mostrar resultado
                            resetToIdleAfterDelay()
                        } else {
                            binding.detectionText.text = "❌ No se detectaron objetos"
                            resetToIdleAfterDelay()
                        }
                    }

                    is DetectionState.NoObjects -> {
                        binding.detectionText.text = "❌ No se detectan objetos"
                        binding.distanceText.visibility = View.GONE
                        resetToIdleAfterDelay()
                    }

                    is DetectionState.Error -> {
                        binding.detectionText.text = "❌ Error: ${state.message}"
                        binding.distanceText.visibility = View.GONE
                        resetToIdleAfterDelay()
                    }
                }
            }
        }

        // Observar estado de reconocimiento de texto
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.textRecognitionState.collect { state ->
                when (state) {
                    is TextRecognitionState.Recognizing -> {
                        binding.detectionText.text = "📖 Leyendo texto..."
                    }

                    is TextRecognitionState.Success -> {
                        binding.detectionText.text = "📝 ${state.text.take(80)}..."
                        resetToIdleAfterDelay()
                    }

                    is TextRecognitionState.NoText -> {
                        binding.detectionText.text = "❌ No se detectó texto"
                        resetToIdleAfterDelay()
                    }

                    is TextRecognitionState.Error -> {
                        binding.detectionText.text = "❌ Error: ${state.message}"
                        resetToIdleAfterDelay()
                    }

                    else -> {}
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        _binding = null
    }
}