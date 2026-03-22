package com.tajemniktv.tajsos.ui

import com.tajemniktv.tajsos.data.*
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import app.cash.turbine.test
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val repository = mockk<AppRepository>()
    private val preferencesRepository = mockk<PreferencesRepository>()

    private val allNodesFlow = MutableStateFlow<List<NodeWithPin>>(emptyList())
    private val isBiometricEnabledFlow = MutableStateFlow<Boolean?>(null)

    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        every { repository.getAllNodes() } returns allNodesFlow
        every { repository.getTodayNodes() } returns MutableStateFlow(emptyList())
        every { repository.getAllTrackEntries() } returns MutableStateFlow(emptyList())
        every { repository.getNodesByType("project") } returns MutableStateFlow(emptyList())
        every { repository.getNodesByType("area") } returns MutableStateFlow(emptyList())
        every { repository.getActiveSession() } returns MutableStateFlow(null)
        every { repository.getAllSessions() } returns MutableStateFlow(emptyList())
        every { repository.getAllTags() } returns MutableStateFlow(emptyList())

        every { preferencesRepository.isBiometricEnabled } returns isBiometricEnabledFlow
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `searchResults returns empty list when query is empty`() = runTest {
        val viewModel = MainViewModel(repository, preferencesRepository)
        allNodesFlow.value = listOf(createNodeWithPin(1, "Task 1", "Content 1"))

        viewModel.updateSearchQuery("")

        viewModel.searchResults.test {
            assertEquals(emptyList(), awaitItem())
        }
    }

    @Test
    fun `searchResults returns empty list when query is blank`() = runTest {
        val viewModel = MainViewModel(repository, preferencesRepository)
        allNodesFlow.value = listOf(createNodeWithPin(1, "Task 1", "Content 1"))

        viewModel.updateSearchQuery("   ")

        viewModel.searchResults.test {
            assertEquals(emptyList(), awaitItem())
        }
    }

    @Test
    fun `searchResults filters by title`() = runTest {
        val viewModel = MainViewModel(repository, preferencesRepository)
        val node1 = createNodeWithPin(1, "Apple", "Content 1")
        val node2 = createNodeWithPin(2, "Banana", "Content 2")
        allNodesFlow.value = listOf(node1, node2)

        viewModel.updateSearchQuery("app")

        viewModel.searchResults.test {
            assertEquals(listOf(node1), awaitItem())
        }
    }

    @Test
    fun `searchResults filters by content`() = runTest {
        val viewModel = MainViewModel(repository, preferencesRepository)
        val node1 = createNodeWithPin(1, "Title 1", "Green Apple")
        val node2 = createNodeWithPin(2, "Title 2", "Yellow Banana")
        allNodesFlow.value = listOf(node1, node2)

        viewModel.updateSearchQuery("yellow")

        viewModel.searchResults.test {
            assertEquals(listOf(node2), awaitItem())
        }
    }

    @Test
    fun `searchResults is case insensitive`() = runTest {
        val viewModel = MainViewModel(repository, preferencesRepository)
        val node1 = createNodeWithPin(1, "APPLE", "Content 1")
        val node2 = createNodeWithPin(2, "banana", "CONTENT 2")
        allNodesFlow.value = listOf(node1, node2)

        viewModel.updateSearchQuery("apple")
        viewModel.searchResults.test {
            assertEquals(listOf(node1), awaitItem())
        }

        viewModel.updateSearchQuery("CONTENT")
        viewModel.searchResults.test {
            assertEquals(listOf(node2), awaitItem())
        }
    }

    @Test
    fun `searchResults updates when allNodes changes`() = runTest {
        val viewModel = MainViewModel(repository, preferencesRepository)
        val node1 = createNodeWithPin(1, "Apple", "Content 1")
        allNodesFlow.value = listOf(node1)

        viewModel.updateSearchQuery("apple")

        viewModel.searchResults.test {
            assertEquals(listOf(node1), awaitItem())

            val node2 = createNodeWithPin(2, "Pineapple", "Content 2")
            allNodesFlow.value = listOf(node1, node2)

            assertEquals(listOf(node1, node2), awaitItem())
        }
    }

    private fun createNodeWithPin(id: Long, title: String, content: String): NodeWithPin {
        return NodeWithPin(
            node = NodeEntity(
                id = id,
                title = title,
                content = content,
                type = "task"
            ),
            pin = null
        )
    }
}
