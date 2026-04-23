import re

with open('./shared/src/commonMain/kotlin/com/tajemniktv/tajsos/data/PreferencesRepository.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""    private val safeData: Flow<Preferences> =
        dataStore.data.catch { handleReadException(it) }

    private suspend fun kotlinx.coroutines.flow.FlowCollector<Preferences>.handleReadException(e: Throwable) {
        if (e is IOException) {
            emit(emptyPreferences())
        } else {
            throw e
        }
    }""",
"""    private val safeData: Flow<Preferences> =
        dataStore.data.catch { e ->
            when (e) {
                is IOException -> emit(emptyPreferences())
                else -> throw e
            }
        }"""
)

with open('./shared/src/commonMain/kotlin/com/tajemniktv/tajsos/data/PreferencesRepository.kt', 'w') as f:
    f.write(content)
