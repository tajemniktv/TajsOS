package com.tajemniktv.tajsos

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Icon
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.tajemniktv.tajsos.data.createDatabase
import com.tajemniktv.tajsos.data.createDataStore
import com.tajemniktv.tajsos.di.SharedModule
import com.tajemniktv.tajsos.ui.MainViewModel

class MainActivity : FragmentActivity() {
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = createDatabase(applicationContext)
        val dataStore = createDataStore(applicationContext)
        val sharedModule = SharedModule(database, dataStore)
        viewModel = sharedModule.createViewModel()

        viewModel.setBiometricHardwareAvailable(isBiometricAvailable())

        setContent {
            val isAuthenticated by viewModel.isAuthenticated.collectAsState()
            val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()

            LaunchedEffect(isBiometricEnabled, isAuthenticated) {
                if (isBiometricEnabled == true && !isAuthenticated) {
                    showBiometricPrompt(viewModel)
                } else if (isBiometricEnabled == false) {
                    viewModel.setAuthenticated(true)
                }
            }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                if (isAuthenticated || isBiometricEnabled == false) {
                    App(viewModel)
                } else if (isBiometricEnabled == true) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = painterResource(id = R.drawable.lock_24px),
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary
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

    private fun showBiometricPrompt(viewModel: MainViewModel) {
        if (!isBiometricAvailable()) {
            viewModel.setAuthenticated(true)
            return
        }

        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(
            this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    viewModel.setAuthenticated(true)
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(this)
        return biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    override fun onStop() {
        super.onStop()
        viewModel.lockApp()
    }
}
