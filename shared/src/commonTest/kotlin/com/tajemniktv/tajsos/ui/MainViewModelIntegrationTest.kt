package com.tajemniktv.tajsos.ui

import com.tajemniktv.tajsos.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.*
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelIntegrationTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(nodeDao: NodeDao = FakeNodeDao()): MainViewModel {
        val testRepo = AppRepository(
            nodeDao = nodeDao,
            focusSessionDao = FakeFocusSessionDao(),
            trackDao = FakeTrackDao(),
            relationDao = FakeRelationDao(),
            tagDao = FakeTagDao(),
            eventLogDao = FakeEventLogDao(),
            attachmentDao = FakeAttachmentDao(),
            templateDao = FakeTemplateDao(),
            nodeSnapshotDao = FakeNodeSnapshotDao(),
            reviewDao = FakeReviewDao(),
            calendarProviderDao = FakeCalendarProviderDao(),
            calendarEventDao = FakeCalendarEventDao(),
            modeDao = FakeModeDao(),
            protocolDao = FakeProtocolDao(),
            decisionDao = FakeDecisionDao(),
            userDao = FakeUserDao(),
            medicationDao = FakeMedicationDao()
        )

        val fakeDataStore = MainViewModelTest.FakeDataStore()
        val testPrefs = PreferencesRepository(fakeDataStore)
        val client = io.ktor.client.HttpClient()
        val calendarManager = com.tajemniktv.tajsos.calendar.CalendarManager(testRepo, client)

        return MainViewModel(testRepo, testPrefs, calendarManager).also {
            client.close()
        }
    }

    @Test
    fun searchResults_updatesWhenFiltersChange() = runTest(testDispatcher) {
        val nodeDao = FakeNodeDao()
        nodeDao.insertNode(NodeEntity(type = "task", title = "Task 1", status = "active", projectId = 1L))
        nodeDao.insertNode(NodeEntity(type = "task", title = "Task 2", status = "done", projectId = 1L))
        nodeDao.insertNode(NodeEntity(type = "note", title = "Note 1", status = "active", projectId = 2L))
        nodeDao.insertNode(NodeEntity(type = "task", title = "Task 3", status = "archived", projectId = 1L))

        val viewModel = createViewModel(nodeDao)

        // Wait for `searchResults` to produce a result matching the condition.
        // Instead of collecting, we can just `first()` with a filter:

        viewModel.clearSearchFilters() // Default: status=active

        viewModel.updateSearchTypeFilter("task")
        var results = viewModel.searchResults.first {
            it.size == 1 && it[0].node.title == "Task 1"
        }
        assertEquals(1, results.size, "Should filter by type task and status active")
        assertEquals("Task 1", results.first().node.title)

        viewModel.updateSearchStatusFilter("done")
        results = viewModel.searchResults.first {
            it.size == 1 && it[0].node.title == "Task 2"
        }
        assertEquals(1, results.size, "Should filter by status done and type task")
        assertEquals("Task 2", results.first().node.title)

        viewModel.updateSearchStatusFilter("active")
        viewModel.updateSearchTypeFilter(null)
        viewModel.updateSearchProjectFilter(2L)
        results = viewModel.searchResults.first {
            it.size == 1 && it[0].node.title == "Note 1"
        }
        assertEquals(1, results.size, "Should filter by project 2 and status active")
        assertEquals("Note 1", results.first().node.title)

        viewModel.clearSearchFilters()
        results = viewModel.searchResults.first {
            it.size == 2
        }
        assertEquals(2, results.size, "Should return all active nodes after clear")
    }

    @Test
    fun searchResults_updatesWhenQueryChanges() = runTest(testDispatcher) {
        val nodeDao = FakeNodeDao()
        nodeDao.insertNode(NodeEntity(type = "task", title = "Buy groceries", status = "active"))
        nodeDao.insertNode(NodeEntity(type = "note", title = "Read a book", status = "active"))

        val viewModel = createViewModel(nodeDao)

        viewModel.clearSearchFilters()

        var results = viewModel.searchResults.first {
            it.size == 2
        }
        assertEquals(2, results.size, "Should show all active nodes when query is empty")

        viewModel.updateSearchQuery("groceries")
        results = viewModel.searchResults.first {
            it.size == 1 && it[0].node.title == "Buy groceries"
        }
        assertEquals(1, results.size, "Should filter down to matched node")
        assertEquals("Buy groceries", results.first().node.title)
    }

    @Test
    fun updateNodeStatus_done_createsRecurringCopy() = runTest(testDispatcher) {
        val nodeDao = FakeNodeDao()
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()

        // A daily recurring task due now
        val recurringNodeId = nodeDao.insertNode(
            NodeEntity(
                type = "task",
                title = "Daily Workout",
                status = "active",
                isRecurring = true,
                recurringInterval = "DAILY",
                dueAt = now
            )
        )

        val viewModel = createViewModel(nodeDao)

        // Ensure nodes are loaded
        val initialNodes = viewModel.allNodes.first { it.isNotEmpty() }
        assertEquals(1, initialNodes.size)

        val targetNode = initialNodes.first { it.node.id == recurringNodeId }.node

        // Complete the task
        viewModel.updateNodeStatus(targetNode, "done")

        // Expect a new active copy and the old one marked as done
        val finalNodes = viewModel.allNodes.first { it.size == 2 }

        val doneNode = finalNodes.first { it.node.id == recurringNodeId }.node
        val newActiveNode = finalNodes.first { it.node.id != recurringNodeId }.node

        assertEquals("done", doneNode.status)
        assertEquals("active", newActiveNode.status)
        assertEquals("Daily Workout", newActiveNode.title)
        assertTrue(newActiveNode.dueAt != null && newActiveNode.dueAt!! > now)
        assertEquals(false, newActiveNode.inboxState)
    }

    @Test
    fun archiveNode_updatesStatusToArchived() = runTest(testDispatcher) {
        val nodeDao = FakeNodeDao()
        val nodeId = nodeDao.insertNode(
            NodeEntity(type = "note", title = "Old Note", status = "active")
        )

        val viewModel = createViewModel(nodeDao)
        val targetNode = viewModel.allNodes.first { it.isNotEmpty() }.first { it.node.id == nodeId }.node

        viewModel.archiveNode(targetNode)

        val archivedNodes = viewModel.archivedNodes.first { it.isNotEmpty() }
        assertEquals(1, archivedNodes.size)
        assertEquals(nodeId, archivedNodes.first().node.id)
        assertEquals("archived", archivedNodes.first().node.status)
    }
}
