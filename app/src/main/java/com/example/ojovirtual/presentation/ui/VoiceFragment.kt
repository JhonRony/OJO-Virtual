package com.example.ojovirtual.presentation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.ojovirtual.R
import com.example.ojovirtual.databinding.FragmentVoiceBinding
import com.example.ojovirtual.presentation.viewmodels.AssistantViewModel
import com.example.ojovirtual.presentation.viewmodels.VoiceState
import kotlinx.coroutines.launch

class VoiceFragment : Fragment() {

    private var _binding: FragmentVoiceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AssistantViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVoiceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.startVoiceButton.setOnClickListener {
            viewModel.startListening()
            viewModel.sensorHelper.vibrate(100)
        }

        binding.startVoiceButton.setOnLongClickListener {
            viewModel.stopListening()
            true
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.voiceState.collect { state ->
                when (state) {
                    is VoiceState.Idle -> {
                        binding.voiceText.text = "Di AYUDA para escuchar las Funciones"
                        binding.startVoiceButton.text = "PRESIONA PARA HABLAR"
                        binding.startVoiceButton.isEnabled = true
                        binding.voiceAnimation.alpha = 1.0f
                    }

                    is VoiceState.Listening -> {
                        binding.voiceText.text = "Escuchando..."
                        binding.startVoiceButton.text = "ESCUCHANDO..."
                        animateListening()
                    }

                    is VoiceState.Speaking -> {
                        binding.voiceText.text = "Hablando..."
                        animateListening()
                    }

                    is VoiceState.Success -> {
                        binding.voiceText.text = "Dijiste: ${state.text}"
                        binding.startVoiceButton.text = "PRESIONA PARA HABLAR"
                        binding.voiceAnimation.alpha = 1.0f
                    }

                    is VoiceState.Error -> {
                        binding.voiceText.text = "Error: ${state.message}"
                        binding.startVoiceButton.text = "PRESIONA PARA HABLAR"
                        binding.startVoiceButton.isEnabled = true
                        binding.voiceAnimation.alpha = 1.0f
                    }
                }
            }
        }
    }

    private fun animateListening() {
        binding.voiceAnimation.animate()
            .alpha(0.3f)
            .setDuration(500)
            .withEndAction {
                binding.voiceAnimation.animate()
                    .alpha(1.0f)
                    .setDuration(500)
                    .withEndAction {
                        if (viewModel.voiceState.value is VoiceState.Listening ||
                            viewModel.voiceState.value is VoiceState.Speaking) {
                            animateListening()
                        }
                    }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}