import re

with open('androidApp/src/main/kotlin/com/tajemniktv/tajsos/MainActivity.kt', 'r') as f:
    content = f.read()

# Replace the bulky LaunchedEffect with a method call
launched_effect = """
            val currentPendingIntent by pendingIntent

            LaunchedEffect(isAuthenticated, currentPendingIntent) {
                if (isAuthenticated && currentPendingIntent != null) {
                    processPendingIntent(currentPendingIntent!!)
                }
            }
"""

content = re.sub(
    r'            val currentPendingIntent by pendingIntent.*?\n                    pendingIntent.value = null\n                \}\n            \}',
    launched_effect.strip(),
    content,
    flags=re.DOTALL
)

# Add the new methods
new_methods = """
    private suspend fun processPendingIntent(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SEND -> handleActionSend(intent)
            Intent.ACTION_SEND_MULTIPLE -> handleActionSendMultiple(intent)
        }
        intent.action = null
        pendingIntent.value = null
    }

    private suspend fun handleActionSend(intent: Intent) {
        val type = intent.type ?: return
        if ("text/plain" == type) {
            intent.getStringExtra(Intent.EXTRA_TEXT)?.let { sharedText ->
                viewModel.addNode(title = sharedText, type = "note")
            }
        } else if (type.startsWith("image/")) {
            val imageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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

    private suspend fun handleActionSendMultiple(intent: Intent) {
        val type = intent.type ?: return
        if (type.startsWith("image/")) {
            val imageUris = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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
"""

content = re.sub(
    r'    private fun handleIntent\(intent: Intent\) \{',
    new_methods + '\n    private fun handleIntent(intent: Intent) {',
    content
)

with open('androidApp/src/main/kotlin/com/tajemniktv/tajsos/MainActivity.kt', 'w') as f:
    f.write(content)
