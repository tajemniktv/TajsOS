/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.firebase.FirebaseApp
import com.tajemniktv.tajsos.data.createDataStore
import com.tajemniktv.tajsos.data.createDatabase
import com.tajemniktv.tajsos.di.SharedModule
import com.tajemniktv.tajsos.ui.MainViewModel

/**
 * Main entry point for the Android application.
 *
 * This activity manages:
 * - App initialization and dependency injection setup.
 * - Biometric authentication and app locking logic.
 * - Intent handling for shared content (text and images).
 * - Voice capture and image picking through activity result launchers.
 * - Main UI hosting using Jetpack Compose.
 */
class MainActivity : FragmentActivity() {
    /** The main view model for app-level orchestration. */
    private lateinit var viewModel: MainViewModel

    /** State holder for speech-to-text results. */
    private var voiceCaptureResult = mutableStateOf<String?>(null)

    /** State holder for the picked avatar image URI. */
    private var avatarPickResult = mutableStateOf<String?>(null)

    /** Stores an incoming intent that needs processing after authentication. */
    private var pendingIntent = mutableStateOf<Intent?>(null)

    /** Launcher for speech recognition activity. */
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

    /** Launcher for the system image picker. */
    private val avatarPickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            avatarPickResult.value = uri?.toString()
        }

    /**
     * Initializes the activity, sets up edge-to-edge UI, and establishes DI.
     * Triggers biometric authentication if enabled.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in [onSaveInstanceState].
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        FirebaseApp.initializeApp(this)

        val database = createDatabase(applicationContext)
        val dataStore = createDataStore(applicationContext)
        val sharedModule = SharedModule(database, dataStore)
        viewModel = sharedModule.createViewModel()

        viewModel.setBiometricHardwareAvailable(isBiometricAvailable())
        handleIntent(intent)

        setContent {
            val isAuthenticated by viewModel.isAuthenticated.collectAsState()
            val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
            val voiceText by voiceCaptureResult
            val avatarRef by avatarPickResult

            LaunchedEffect(isBiometricEnabled, isAuthenticated) {
                if (isBiometricEnabled == true && !isAuthenticated) {
                    showBiometricPrompt(viewModel)
                } else if (isBiometricEnabled == false) {
                    viewModel.setAuthenticated(true)
                }
            }

            val currentPendingIntent by pendingIntent

            LaunchedEffect(isAuthenticated, currentPendingIntent) {
                val intent = currentPendingIntent
                if (isAuthenticated && intent != null) {
                    processPendingIntent(intent)
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
                        onVoiceCaptureConsume = { voiceCaptureResult.value = null },
                        onPickAvatar = { avatarPickerLauncher.launch("image/*") },
                        avatarPickResult = avatarRef,
                        onAvatarPickConsume = { avatarPickResult.value = null },
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

    /**
     * Called when the activity is already running and receives a new intent.
     * Used for handling shared content while the app is in the foreground.
     *
     * @param intent The new intent that was started for the activity.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    /**
     * Processes an incoming intent that was previously captured and pending authentication.
     * Handles single and multiple shared items.
     *
     * @param intent The intent to process.
     */
    private suspend fun processPendingIntent(intent: Intent) {
        when (intent.action)
        {
            Intent.ACTION_SEND -> handleActionSend(intent)
            Intent.ACTION_SEND_MULTIPLE -> handleActionSendMultiple(intent)
        }
        intent.action = null
        pendingIntent.value = null
    }

    /**
     * Handles a single shared item (plain text or an image).
     *
     * @param intent The intent containing the shared content.
     */
    private suspend fun handleActionSend(intent: Intent) {
        val type = intent.type ?: return
        if ("text/plain" == type) {
            intent.getStringExtra(Intent.EXTRA_TEXT)?.let { sharedText ->
                viewModel.addNode(title = sharedText, type = "note")
            }
        } else if (type.startsWith("image/")) {
            val imageUri =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(Intent.EXTRA_STREAM)
                }
            imageUri?.let { uri ->
                val nodeId = viewModel.addNodeForResult(title = "Shared Image", type = "resource")
                viewModel.addAttachment(nodeId, "IMAGE", uri.toString())
            }
        }
    }

    /**
     * Handles multiple shared items (currently supports multiple images).
     *
     * @param intent The intent containing multiple URIs.
     */
    private suspend fun handleActionSendMultiple(intent: Intent) {
        val type = intent.type ?: return
        if (type.startsWith("image/")) {
            val imageUris =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
                }
            imageUris?.let { uris ->
                val nodeId = viewModel.addNodeForResult(title = "Shared Images", type = "resource")
                uris.forEach { uri ->
                    viewModel.addAttachment(nodeId, "IMAGE", uri.toString())
                }
            }
        }
    }

    /**
     * Analyzes an intent to see if it should be queued for processing.
     *
     * @param intent The incoming intent to check.
     */
    private fun handleIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_SEND || intent.action == Intent.ACTION_SEND_MULTIPLE) {
            pendingIntent.value = intent
        }
    }

    /**
     * Launches the system speech recognition overlay.
     */
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

    /**
     * Displays the system biometric prompt for user authentication.
     * Automatically authenticates if hardware is unavailable.
     *
     * @param viewModel The ViewModel to update upon successful authentication.
     */
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

    /**
     * Checks if biometric authentication hardware is available and configured on the device.
     *
     * @return True if biometrics can be used for authentication.
     */
    private fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(this)
        return biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL,
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Called when the activity is no longer visible.
     * Locks the application to ensure security on the next launch.
     */
    override fun onStop() {
        super.onStop()
        viewModel.lockApp()
    }
}
