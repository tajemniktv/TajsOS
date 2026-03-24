/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.tajemniktv.tajsos.data.createDataStore
import com.tajemniktv.tajsos.data.createDatabase
import com.tajemniktv.tajsos.di.SharedModule
import com.tajemniktv.tajsos.ui.MainViewModel
import kotlinx.coroutines.launch

/**
 *
 */
class MainActivity : FragmentActivity() {
    private lateinit var viewModel: MainViewModel
    private var voiceCaptureResult = mutableStateOf<String?>(null)
    private var pendingIntent = mutableStateOf<Intent?>(null)

    private val speechRecognizerLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                if (!results.isNullOrEmpty()) {
                    voiceCaptureResult.value = results[0]
                }
            }
        }

    /**
     *
     */
    override fun onCreate(
        savedInstanceState: Bundle?,
    ) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        val database = createDatabase(applicationContext)
        val dataStore = createDataStore(applicationContext)
        val sharedModule = SharedModule(database, dataStore)
        viewModel = sharedModule.createViewModel()

        viewModel.setBiometricHardwareAvailable(isBiometricAvailable())
        pendingIntent.value = intent

        setContent {
            val isAuthenticated by viewModel.isAuthenticated.collectAsState()
            val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
            val voiceText by voiceCaptureResult
            val currentPendingIntent by pendingIntent

            LaunchedEffect(isBiometricEnabled, isAuthenticated) {
                if (isBiometricEnabled == true && !isAuthenticated) {
                    showBiometricPrompt(viewModel)
                } else if (isBiometricEnabled == false) {
                    viewModel.setAuthenticated(true)
                }
            }

            LaunchedEffect(isAuthenticated, currentPendingIntent) {
                if (isAuthenticated && currentPendingIntent != null) {
                    handleIntent(currentPendingIntent!!)
                    pendingIntent.value = null
                }
            }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                if (isAuthenticated || isBiometricEnabled == false) {
                    App(
                        viewModel = viewModel,
                        onVoiceCapture = { triggerVoiceCapture() },
                        voiceCaptureResult = voiceText,
                        onVoiceCaptureConsumed = { voiceCaptureResult.value = null },
                    )
                } else if (isBiometricEnabled == true) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("App Locked", style = MaterialTheme.typography.headlineMedium)
                            Button(onClick = { showBiometricPrompt(viewModel) }) {
                                Text("Unlock")
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        pendingIntent.value = intent
    }

    private fun handleIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_SEND) {
            val type = intent.type
            if ("text/plain" == type) {
                intent.getStringExtra(Intent.EXTRA_TEXT)?.let { sharedText ->
                    viewModel.addNode(title = sharedText, type = "note")
                }
            } else if (type?.startsWith("image/") == true) {
                val imageUri =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(Intent.EXTRA_STREAM)
                    }
                imageUri?.let { uri ->
                    lifecycleScope.launch {
                        val nodeId =
                            viewModel.addNodeForResult(title = "Shared Image", type = "resource")
                        viewModel.addAttachment(nodeId, "IMAGE", uri.toString())
                    }
                }
            }
        }
    }

    private fun triggerVoiceCapture() {
        val intent =
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
                )
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
            }
        try {
            speechRecognizerLauncher.launch(intent)
        } catch (e: Exception) {
            // Speech recognizer not available
        }
    }

    private fun showBiometricPrompt(viewModel: MainViewModel) {
        if (!isBiometricAvailable()) {
            viewModel.setAuthenticated(true)
            return
        }

        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt =
            BiometricPrompt(
                this,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        viewModel.setAuthenticated(true)
                    }
                },
            )

        val promptInfo =
            BiometricPrompt.PromptInfo
                .Builder()
                .setTitle("Biometric login")
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL,
                ).build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(this)
        return biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL,
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    //f
    override fun onStop() {
        super.onStop()
        viewModel.lockApp()
    }
}
