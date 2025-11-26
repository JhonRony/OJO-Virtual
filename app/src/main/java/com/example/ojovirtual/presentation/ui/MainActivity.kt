package com.example.ojovirtual.presentation.ui  // ✅ TU PACKAGE

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ojovirtual.R  // ✅ TU PACKAGE
import com.example.ojovirtual.databinding.ActivityMainBinding  // ✅ TU PACKAGE
import com.example.ojovirtual.presentation.viewmodels.AssistantViewModel  // ✅ TU PACKAGE
import com.example.ojovirtual.presentation.viewmodels.NavigationCommand
import com.example.ojovirtual.utils.PermissionsManager  // ✅ TU PACKAGE
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: AssistantViewModel by viewModels()
    private lateinit var permissionsManager: PermissionsManager

    private var currentFragment: String = "voice"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionsManager = PermissionsManager(this)

        setupUI()
        checkPermissions()
        observeViewModel()

        // Iniciar con fragmento de voz
        if (savedInstanceState == null) {
            showVoiceFragment()
        }
    }

    private fun setupUI() {
        // Botón FAB de voz
        binding.fabVoice.setOnClickListener {
            if (permissionsManager.checkAudioPermission()) {
                viewModel.startListening()
                viewModel.sensorHelper.vibrate(100)
            } else {
                Toast.makeText(this, R.string.permission_microphone, Toast.LENGTH_SHORT).show()
            }
        }

        // ✅ ACTUALIZADO: Navegación inferior con configuración
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_voice -> {
                    showVoiceFragment()
                    true
                }
                R.id.navigation_camera -> {
                    if (permissionsManager.checkCameraPermission()) {
                        showCameraFragment()
                        true
                    } else {
                        Toast.makeText(this, R.string.permission_camera, Toast.LENGTH_SHORT).show()
                        false
                    }
                }
                R.id.navigation_settings -> {
                    showSettingsFragment()
                    true
                }
                else -> false
            }
        }
    }

    private fun showVoiceFragment() {
        if (currentFragment != "voice") {
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, VoiceFragment())
                .commit()
            currentFragment = "voice"
            // ✅ Actualizar navegación inferior
            binding.bottomNavigation.selectedItemId = R.id.navigation_voice
        }
    }

    private fun showCameraFragment() {
        if (currentFragment != "camera") {
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, CameraFragment())
                .commit()
            currentFragment = "camera"
            // ✅ Actualizar navegación inferior
            binding.bottomNavigation.selectedItemId = R.id.navigation_camera
        }
    }

    // ✅ NUEVO: Mostrar fragmento de configuración
    private fun showSettingsFragment() {
        if (currentFragment != "settings") {
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, SettingsFragment())
                .commit()
            currentFragment = "settings"
            // ✅ Actualizar navegación inferior
            binding.bottomNavigation.selectedItemId = R.id.navigation_settings
        }
    }

    private fun checkPermissions() {
        if (!permissionsManager.checkPermissions()) {
            permissionsManager.requestPermissions(this)
        } else {
            // Dar bienvenida
            viewModel.speak(getString(R.string.welcome_message))
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PermissionsManager.ALL_PERMISSIONS_CODE) {
            if (permissionsManager.checkPermissions()) {
                viewModel.speak(getString(R.string.welcome_message))
            } else {
                Toast.makeText(
                    this,
                    R.string.permissions_denied,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // ✅ ACTUALIZADO: Manejar el botón back con configuración
    override fun onBackPressed() {
        when (currentFragment) {
            "camera", "settings" -> {
                // Si está en la cámara o configuración, regresar al micrófono
                showVoiceFragment()
                viewModel.speak("Volviendo al asistente de voz")
            }
            "voice" -> {
                // Si está en el micrófono, cerrar la app (comportamiento normal)
                super.onBackPressed()
            }
            else -> {
                super.onBackPressed()
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.statusMessage.collect { message ->
                if (message.isNotEmpty()) {
                    binding.statusText.text = message
                    binding.statusText.visibility = View.VISIBLE
                } else {
                    binding.statusText.visibility = View.GONE
                }
            }
        }

        // ✅ NUEVO: Observar comandos de navegación
        lifecycleScope.launch {
            viewModel.navigationCommand.collect { command ->
                when (command) {
                    is NavigationCommand.NavigateToCamera -> {
                        if (permissionsManager.checkCameraPermission()) {
                            showCameraFragment()
                        } else {
                            Toast.makeText(this@MainActivity, R.string.permission_camera, Toast.LENGTH_SHORT).show()
                        }
                        // ✅ Limpiar el comando después de usarlo
                        viewModel.clearNavigationCommand()
                    }
                    is NavigationCommand.NavigateToVoice -> {
                        showVoiceFragment()
                        viewModel.clearNavigationCommand()
                    }
                    is NavigationCommand.NavigateToSettings -> {
                        showSettingsFragment()
                        viewModel.clearNavigationCommand()
                    }
                    null -> {
                        // No hacer nada
                    }
                }
            }
        }

        // ✅ NUEVO: Observar comando para cerrar aplicación
        lifecycleScope.launch {
            viewModel.closeAppCommand.collect { shouldClose ->
                if (shouldClose) {
                    // Limpiar el comando
                    viewModel.clearCloseAppCommand()
                    // Cerrar la aplicación
                    finishAffinity()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopSpeaking()
    }
}