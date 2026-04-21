import re

with open('androidApp/src/main/kotlin/com/tajemniktv/tajsos/MainActivity.kt', 'r') as f:
    content = f.read()

# find the onCreate method
match = re.search(r'override fun onCreate\(savedInstanceState: Bundle\?\).*?setContent \{', content, re.DOTALL)
if not match:
    print("Could not find onCreate")
    exit(1)

# we will extract setContent to a new method called setupMainUi()
new_content = content.replace('''        setContent {
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
        }''', '''        setupMainUi()
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
        }''')

with open('androidApp/src/main/kotlin/com/tajemniktv/tajsos/MainActivity.kt', 'w') as f:
    f.write(new_content)

print("Done")
