package com.example.ojovirtual.presentation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.ojovirtual.databinding.FragmentSettingsBinding
import com.example.ojovirtual.presentation.viewmodels.AssistantViewModel

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AssistantViewModel by activityViewModels()

    // Valores por defecto
    private val defaultSpeechRate = 1.0f
    private val defaultSpeechPitch = 1.0f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        loadCurrentSettings()
    }

    private fun setupUI() {
        // Configurar SeekBar de velocidad
        binding.speechRateSeekbar.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                val rate = progress / 100.0f
                updateSpeechRateText(rate)
            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {
                val rate = seekBar?.progress?.div(100.0f) ?: defaultSpeechRate
                viewModel.setSpeechRate(rate)
            }
        })

        // Configurar SeekBar de tono
        binding.speechPitchSeekbar.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                val pitch = progress / 100.0f
                updateSpeechPitchText(pitch)
            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {
                val pitch = seekBar?.progress?.div(100.0f) ?: defaultSpeechPitch
                viewModel.setSpeechPitch(pitch)
            }
        })

        // Botón probar voz
        binding.testVoiceButton.setOnClickListener {
            testVoiceSettings()
        }

        // Botón restablecer
        binding.resetButton.setOnClickListener {
            resetToDefaults()
        }
    }

    private fun loadCurrentSettings() {
        // Cargar configuración actual del ViewModel
        val currentRate = viewModel.getSpeechRate()
        val currentPitch = viewModel.getSpeechPitch()

        binding.speechRateSeekbar.progress = (currentRate * 100).toInt()
        binding.speechPitchSeekbar.progress = (currentPitch * 100).toInt()

        updateSpeechRateText(currentRate)
        updateSpeechPitchText(currentPitch)
    }

    private fun updateSpeechRateText(rate: Float) {
        val rateText = when {
            rate < 0.7f -> "Lenta"
            rate > 1.3f -> "Rápida"
            else -> "Normal"
        }
        binding.speechRateValue.text = "Velocidad: $rateText (${String.format("%.1f", rate)})"
    }

    private fun updateSpeechPitchText(pitch: Float) {
        val pitchText = when {
            pitch < 0.7f -> "Grave"
            pitch > 1.3f -> "Agudo"
            else -> "Normal"
        }
        binding.speechPitchValue.text = "Tono: $pitchText (${String.format("%.1f", pitch)})"
    }

    private fun testVoiceSettings() {
        binding.testText.visibility = View.VISIBLE
        viewModel.speak("Hola, esta es una prueba de la configuración de voz.")
    }

    private fun resetToDefaults() {
        binding.speechRateSeekbar.progress = (defaultSpeechRate * 100).toInt()
        binding.speechPitchSeekbar.progress = (defaultSpeechPitch * 100).toInt()

        viewModel.setSpeechRate(defaultSpeechRate)
        viewModel.setSpeechPitch(defaultSpeechPitch)

        updateSpeechRateText(defaultSpeechRate)
        updateSpeechPitchText(defaultSpeechPitch)

        viewModel.speak("Configuración restablecida a valores predeterminados")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}