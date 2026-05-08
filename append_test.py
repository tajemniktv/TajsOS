import sys

file_path = "shared/src/commonTest/kotlin/com/tajemniktv/tajsos/data/PreferencesRepositoryTest.kt"

with open(file_path, "r") as f:
    content = f.read()

# Add imports
content = content.replace(
    "import kotlinx.coroutines.flow.MutableStateFlow",
    "import kotlinx.coroutines.flow.MutableStateFlow\nimport kotlinx.coroutines.flow.flow\nimport okio.IOException"
)

test_code = """
    private class FaultyDataStore : DataStore<Preferences> {
        override val data: Flow<Preferences> = flow {
            throw IOException("Corrupted file")
        }

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
            return emptyPreferences()
        }
    }

    @Test
    fun catchIoException_emitsEmptyPreferences() = runTest {
        val dataStore = FaultyDataStore()
        val repository = PreferencesRepository(dataStore)

        repository.sidebarMode.test {
            assertEquals(SidebarMode.EXPANDED, awaitItem())
            awaitComplete()
        }
    }
"""

content = content.replace("class PreferencesRepositoryTest {", "class PreferencesRepositoryTest {\n" + test_code)

with open(file_path, "w") as f:
    f.write(content)
