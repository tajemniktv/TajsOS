/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos

import android.content.Intent
import android.net.Uri
import android.os.BadParcelableException
import android.os.Build
import android.os.Bundle
import android.os.ParcelFormatException
import android.os.Parcelable
import android.content.ActivityNotFoundException
import android.speech.RecognizerIntent
import android.util.Log
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.KeyPermanentlyInvalidatedException
import java.security.GeneralSecurityException
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
import androidx.core.content.IntentCompat
import androidx.fragment.app.FragmentActivity
import com.google.firebase.FirebaseApp
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import com.tajemniktv.tajsos.data.createDataStore
import com.tajemniktv.tajsos.data.createDatabase
import com.tajemniktv.tajsos.di.SharedModule
import com.tajemniktv.tajsos.ui.MainViewModel

private const val DEFAULT_LOG_TAG = "MainActivity"

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
    companion object {
        private const val TAG = "MainActivity"
        const val BIOMETRIC_KEY_ALIAS = "tajsos_biometric_key"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val CIPHER_TRANSFORMATION =
            "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_GCM}/${KeyProperties.ENCRYPTION_PADDING_NONE}"
    }

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
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_SECURE,
            android.view.WindowManager.LayoutParams.FLAG_SECURE
        )
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        FirebaseApp.initializeApp(this)

        val database = createDatabase(applicationContext)
        val dataStore = createDataStore(applicationContext)
        val sharedModule = SharedModule(database, dataStore)
        viewModel =
            sharedModule.createViewModel(
                nextStepFallbackLabel = getString(R.string.next_step),
                untitledFallbackLabel = getString(R.string.untitled),
            )

        viewModel.setBiometricHardwareAvailable(isBiometricAvailable())
        handleIntent(intent)

        setupMainUi()
    }

    /**
     * Extracts the Compose setup logic from onCreate to satisfy CodeScene's Complex Method rule.
     */
    private fun setupMainUi() {
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
                        onPickAvatar = { try {
                            avatarPickerLauncher.launch("image/*")
                        } catch (e: android.content.ActivityNotFoundException) {
                            Log.e(TAG, "No image picker available", e)
                        } },
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
                            Text(
                                getString(R.string.auth_app_locked),
                                style = MaterialTheme.typography.headlineMedium,
                            )
                            Button(onClick = { showBiometricPrompt(viewModel) }) {
                                Text(getString(R.string.auth_unlock))
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
        setIntent(intent)
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
            val imageUri = intent.getSafeParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            imageUri?.let { uri ->
                val nodeId =
                    viewModel.addNodeForResult(
                        title = getString(R.string.intent_shared_image),
                        type = "resource",
                    )
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
            val imageUris = intent.getSafeParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)
            imageUris?.let { uris ->
                val nodeId =
                    viewModel.addNodeForResult(
                        title = getString(R.string.intent_shared_images),
                        type = "resource",
                    )
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
                putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_speak_now))
            }
        try {
            speechRecognizerLauncher.launch(intent)
        } catch (e: android.content.ActivityNotFoundException) {
            Log.w(TAG, "Speech recognizer activity not found", e)
        }
    }

    /**
     * Retrieves the existing Keystore-backed AES key or creates a new one.
     *
     * On API 30+ the key is explicitly bound to strong biometric authentication using
     * [KeyProperties.AUTH_BIOMETRIC_STRONG] via [KeyGenParameterSpec.Builder.setUserAuthenticationParameters].
     * On earlier API levels [KeyGenParameterSpec.Builder.setUserAuthenticationRequired] is used.
     * The key is invalidated whenever new biometric enrolment occurs.
     */
    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(BIOMETRIC_KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec =
            KeyGenParameterSpec
                .Builder(
                    BIOMETRIC_KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setInvalidatedByBiometricEnrollment(true)
                .apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        // Require auth on every key use (timeout = 0 means no time window).
                        setUserAuthenticationParameters(0, KeyProperties.AUTH_BIOMETRIC_STRONG)
                    } else {
                        // On API < 30, -1 is the documented constant for per-use authentication.
                        setUserAuthenticationRequired(true)
                        @Suppress("DEPRECATION")
                        setUserAuthenticationValidityDurationSeconds(-1)
                    }
                }.build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    /** Returns a new [Cipher] instance configured for [CIPHER_TRANSFORMATION]. */
    private fun getCipher(): Cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)

    /**
     * Initialises a [Cipher] in [Cipher.ENCRYPT_MODE] using the Keystore-backed key.
     *
     * If the existing key has been permanently invalidated (e.g. after a biometric enrolment
     * change) it is deleted and a fresh key is provisioned before retrying.
     *
     * @return An initialised [Cipher], or `null` if initialisation fails.
     */
    private fun initCipher(): Cipher? {
        return try {
            getCipher().apply {
                init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
            }
        } catch (e: KeyPermanentlyInvalidatedException) {
            Log.w(TAG, "Biometric key invalidated, regenerating key")
            try {
                val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
                keyStore.deleteEntry(BIOMETRIC_KEY_ALIAS)
                getCipher().apply {
                    init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
                }
            } catch (regenerationException: GeneralSecurityException) {
                Log.e(TAG, "Failed to reinitialize cipher after key regeneration: ${regenerationException.javaClass.simpleName}")
                null
            }
        } catch (e: GeneralSecurityException) {
            Log.e(TAG, "Failed to initialize biometric cipher: ${e.javaClass.simpleName}")
            null
        }
    }

    /**
     * Displays the system biometric prompt for user authentication.
     *
     * Biometric unlock is cryptographically bound to the Android Keystore through a
     * [BiometricPrompt.CryptoObject]. Authentication succeeds only when the hardware
     * biometric sensor approves the operation **and** the Keystore-backed cipher can
     * complete successfully.
     *
     * @param viewModel The ViewModel to update upon successful or failed authentication.
     */
    private fun showBiometricPrompt(viewModel: MainViewModel) {
        if (!isBiometricAvailable()) {
            return
        }

        val cipher = initCipher() ?: return

        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt =
            BiometricPrompt(
                this,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        val authCipher = result.cryptoObject?.cipher ?: return
                        try {
                            authCipher.doFinal("auth".toByteArray())
                            viewModel.setAuthenticated(true)
                        } catch (e: Exception) {
                            Log.e(TAG, "Biometric crypto operation failed: ${e.javaClass.simpleName}")
                        }
                    }

                    override fun onAuthenticationError(
                        errorCode: Int,
                        errString: CharSequence,
                    ) {
                        super.onAuthenticationError(errorCode, errString)
                        Log.e(TAG, "Biometric authentication error [$errorCode]: $errString")
                        // User-initiated dismissal is not a security failure — the lock screen
                        // stays visible and the user can retry via the Unlock button.
                        if (!isBiometricUserCancellation(errorCode)) {
                            viewModel.setAuthenticated(false)
                        }
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Log.w(TAG, "Biometric authentication attempt failed")
                    }
                },
            )

        val promptInfo =
            BiometricPrompt.PromptInfo
                .Builder()
                .setTitle(getString(R.string.auth_biometric_title))
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .setNegativeButtonText(getString(R.string.auth_biometric_cancel))
                .build()

        biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
    }

    /**
     * Checks if strong biometric authentication hardware is available and enrolled on the device.
     *
     * Only [BiometricManager.Authenticators.BIOMETRIC_STRONG] is checked because the biometric
     * prompt uses a [BiometricPrompt.CryptoObject] which is incompatible with device-credential
     * fallback.
     *
     * @return `true` if strong biometrics can be used for authentication.
     */
    private fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(this)
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
            BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Returns `true` for [BiometricPrompt] error codes that represent deliberate user
     * cancellation (e.g. pressing Cancel or the back button) rather than a terminal
     * hardware or security failure.
     *
     * Use this to distinguish errors where calling [MainViewModel.setAuthenticated]
     * is unnecessary because the user intends to stay on the lock screen and retry later.
     */
    private fun isBiometricUserCancellation(errorCode: Int): Boolean =
        errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
            errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON

    /**
     * Called when the activity is no longer visible.
     * Locks the application to ensure security on the next launch.
     */
    override fun onStop() {
        super.onStop()
        viewModel.lockApp()
    }

    /**
     * Safely extracts a Parcelable extra from an Intent, handling OS version differences
     * and catching potential unparcelling exceptions (e.g., BadParcelableException).
     */
    internal inline fun <reified T : android.os.Parcelable> Intent.getSafeParcelableExtra(name: String): T? =
        try {
            androidx.core.content.IntentCompat.getParcelableExtra(this, name, T::class.java)
        } catch (e: BadParcelableException) {
            Log.e(TAG, "Failed to read parcelable extra: $name: ${e.javaClass.simpleName}")
            null
        } catch (e: ParcelFormatException) {
            Log.e(TAG, "Failed to read parcelable extra (bad parcel format): $name: ${e.javaClass.simpleName}")
            null
        }

    /**
     * Safely extracts a Parcelable ArrayList extra from an Intent.
     */
    internal inline fun <reified T : android.os.Parcelable> Intent.getSafeParcelableArrayListExtra(name: String): java.util.ArrayList<T>? =
        try {
            androidx.core.content.IntentCompat.getParcelableArrayListExtra(this, name, T::class.java)
        } catch (e: BadParcelableException) {
            Log.e(TAG, "Failed to read parcelable array list extra: $name: ${e.javaClass.simpleName}")
            null
        } catch (e: ParcelFormatException) {
            Log.e(TAG, "Failed to read parcelable array list extra (bad parcel format): $name: ${e.javaClass.simpleName}")
            null
        }
}

/**
 * Safely extracts a Parcelable extra from an Intent, handling OS version differences
 * and catching potential unparcelling exceptions (e.g., BadParcelableException).
 */
private fun <T : Parcelable> Intent.getSafeParcelableExtra(
    name: String,
    clazz: Class<T>,
    logTag: String = DEFAULT_LOG_TAG,
): T? =
    try {
        IntentCompat.getParcelableExtra(this, name, clazz)
    } catch (e: BadParcelableException) {
        Log.e(logTag, "Failed to read parcelable extra: $name: ${e.javaClass.simpleName}")
        null
    } catch (e: ParcelFormatException) {
        Log.e(logTag, "Failed to read parcelable extra (bad parcel format): $name: ${e.javaClass.simpleName}")
        null
    }

/**
 * Safely extracts a Parcelable ArrayList extra from an Intent.
 */
private fun <T : Parcelable> Intent.getSafeParcelableArrayListExtra(
    name: String,
    clazz: Class<T>,
    logTag: String = DEFAULT_LOG_TAG,
): ArrayList<T>? =
    try {
        IntentCompat.getParcelableArrayListExtra(this, name, clazz)
    } catch (e: BadParcelableException) {
        Log.e(logTag, "Failed to read parcelable array list extra: $name: ${e.javaClass.simpleName}")
        null
    } catch (e: ParcelFormatException) {
        Log.e(logTag, "Failed to read parcelable array list extra (bad parcel format): $name: ${e.javaClass.simpleName}")
        null
    }
