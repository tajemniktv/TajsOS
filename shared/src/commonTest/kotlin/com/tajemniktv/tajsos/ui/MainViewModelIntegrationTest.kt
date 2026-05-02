/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui

import com.tajemniktv.tajsos.data.AppRepository
import com.tajemniktv.tajsos.data.FakeAttachmentDao
import com.tajemniktv.tajsos.data.FakeCalendarEventDao
import com.tajemniktv.tajsos.data.FakeCalendarProviderDao
import com.tajemniktv.tajsos.data.FakeDecisionDao
import com.tajemniktv.tajsos.data.FakeEventLogDao
import com.tajemniktv.tajsos.data.FakeFocusSessionDao
import com.tajemniktv.tajsos.data.FakeMedicationDao
import com.tajemniktv.tajsos.data.FakeModeDao
import com.tajemniktv.tajsos.data.FakeNodeDao
import com.tajemniktv.tajsos.data.FakeNodeSnapshotDao
import com.tajemniktv.tajsos.data.FakeProtocolDao
import com.tajemniktv.tajsos.data.FakeRelationDao
import com.tajemniktv.tajsos.data.FakeReviewDao
import com.tajemniktv.tajsos.data.FakeTagDao
import com.tajemniktv.tajsos.data.FakeTemplateDao
import com.tajemniktv.tajsos.data.FakeTrackDao
import com.tajemniktv.tajsos.data.FakeUserDao
import com.tajemniktv.tajsos.data.NodeDao
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.PreferencesRepository
import io.ktor.client.engine.mock.respond
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
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
        val testRepo =
            AppRepository(
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
                medicationDao = FakeMedicationDao(),
            )

        val fakeDataStore = MainViewModelTest.FakeDataStore()
        val testPrefs = PreferencesRepository(fakeDataStore)
        val mockEngine =
            io.ktor.client.engine.mock.MockEngine {
                respond("", io.ktor.http.HttpStatusCode.OK)
            }
        val client = io.ktor.client.HttpClient(mockEngine)
        val calendarManager =
            com.tajemniktv.tajsos.calendar
                .CalendarManager(testRepo, client)

        return MainViewModel(testRepo, testPrefs, calendarManager).also {
            client.close()
        }
    }

    @Test
    fun searchResults_updatesWhenFiltersChange(): TestResult =
        runTest(testDispatcher) {
            val nodeDao = FakeNodeDao()
            nodeDao.insertNode(NodeEntity(type = "task", title = "Task 1", projectId = 1L))
            nodeDao.insertNode(
                NodeEntity(
                    type = "task",
                    title = "Task 2",
                    status = "done",
                    projectId = 1L,
                ),
            )
            nodeDao.insertNode(NodeEntity(type = "note", title = "Note 1", projectId = 2L))
            nodeDao.insertNode(
                NodeEntity(
                    type = "task",
                    title = "Task 3",
                    status = "archived",
                    projectId = 1L,
                ),
            )

            val viewModel = createViewModel(nodeDao)

            // Wait for `searchResults` to produce a result matching the condition.
            // Instead of collecting, we can just `first()` with a filter:

            viewModel.clearSearchFilters() // Default: status=active

            viewModel.updateSearchTypeFilter("task")
            var results =
                viewModel.searchResults.first {
                    it.size == 1 && it[0].node.title == "Task 1"
                }
            assertEquals(1, results.size, "Should filter by type task and status active")
            assertEquals("Task 1", results.first().node.title)

            viewModel.updateSearchStatusFilter("done")
            results =
                viewModel.searchResults.first {
                    it.size == 1 && it[0].node.title == "Task 2"
                }
            assertEquals(1, results.size, "Should filter by status done and type task")
            assertEquals("Task 2", results.first().node.title)

            viewModel.updateSearchStatusFilter("active")
            viewModel.updateSearchTypeFilter(null)
            viewModel.updateSearchProjectFilter(2L)
            results =
                viewModel.searchResults.first {
                    it.size == 1 && it[0].node.title == "Note 1"
                }
            assertEquals(1, results.size, "Should filter by project 2 and status active")
            assertEquals("Note 1", results.first().node.title)

            viewModel.clearSearchFilters()
            results =
                viewModel.searchResults.first {
                    it.size == 2
                }
            assertEquals(2, results.size, "Should return all active nodes after clear")
        }

    @Test
    fun searchResults_updatesWhenQueryChanges(): TestResult =
        runTest(testDispatcher) {
            val nodeDao = FakeNodeDao()
            nodeDao.insertNode(NodeEntity(type = "task", title = "Buy groceries"))
            nodeDao.insertNode(NodeEntity(type = "note", title = "Read a book"))

            val viewModel = createViewModel(nodeDao)

            viewModel.clearSearchFilters()

            var results =
                viewModel.searchResults.first {
                    it.size == 2
                }
            assertEquals(2, results.size, "Should show all active nodes when query is empty")

            viewModel.updateSearchQuery("groceries")
            results =
                viewModel.searchResults.first {
                    it.size == 1 && it[0].node.title == "Buy groceries"
                }
            assertEquals(1, results.size, "Should filter down to matched node")
            assertEquals("Buy groceries", results.first().node.title)
        }

    @Test
    fun updateNodeStatus_done_createsRecurringCopy(): TestResult =
        runTest(testDispatcher) {
            val nodeDao = FakeNodeDao()
            val now =
                kotlin.time.Clock.System
                    .now()
                    .toEpochMilliseconds()

            // A daily recurring task due now
            val recurringNodeId =
                nodeDao.insertNode(
                    NodeEntity(
                        type = "task",
                        title = "Daily Workout",
                        isRecurring = true,
                        recurringInterval = "DAILY",
                        dueAt = now,
                    ),
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
            val newDueAt = newActiveNode.dueAt
            assertTrue(newDueAt != null && newDueAt > now)
            assertEquals(false, newActiveNode.inboxState)
        }

    @Test
    fun archiveNode_updatesStatusToArchived(): TestResult =
        runTest(testDispatcher) {
            val nodeDao = FakeNodeDao()
            val nodeId =
                nodeDao.insertNode(
                    NodeEntity(type = "note", title = "Old Note"),
                )

            val viewModel = createViewModel(nodeDao)
            val targetNode =
                viewModel.allNodes
                    .first { it.isNotEmpty() }
                    .first { it.node.id == nodeId }
                    .node

            viewModel.archiveNode(targetNode)

            val archivedNodes = viewModel.archivedNodes.first { it.isNotEmpty() }
            assertEquals(1, archivedNodes.size)
            assertEquals(nodeId, archivedNodes.first().node.id)
            assertEquals("archived", archivedNodes.first().node.status)
        }

    // ---- splitNote tests ----

    @Test
    fun splitNote_createsOneNodePerSection(): TestResult =
        runTest(testDispatcher) {
            val nodeDao = FakeNodeDao()
            val content =
                "# First Section\nFirst content\n# Second Section\nSecond content\n# Third Section\nThird content"
            val originalId =
                nodeDao.insertNode(
                    NodeEntity(
                        type = "note",
                        title = "Combined Note",
                        content = content,
                    ),
                )

            val viewModel = createViewModel(nodeDao)
            viewModel.splitNote(originalId)

            // 3 new notes + 1 archived original = 4 total, filter active (3)
            val activeNodes =
                viewModel.allNodes.first { list ->
                    list.count { it.node.status == "active" } == 3
                }
            val activeNotes = activeNodes.filter { it.node.status == "active" }
            assertEquals(3, activeNotes.size)
        }

    @Test
    fun splitNote_usesHeaderAsTitle(): TestResult =
        runTest(testDispatcher) {
            val nodeDao = FakeNodeDao()
            val content = "# Alpha\nAlpha body\n# Beta\nBeta body"
            val originalId =
                nodeDao.insertNode(NodeEntity(type = "note", title = "Original", content = content))

            val viewModel = createViewModel(nodeDao)
            viewModel.splitNote(originalId)

            val allNodes =
                viewModel.allNodes.first { list ->
                    list.any { it.node.title == "Alpha" } && list.any { it.node.title == "Beta" }
                }
            val titles = allNodes.map { it.node.title }
            assertTrue(titles.contains("Alpha"), "Should have a note titled 'Alpha'")
            assertTrue(titles.contains("Beta"), "Should have a note titled 'Beta'")
        }

    @Test
    fun splitNote_archivesOriginalNode(): TestResult =
        runTest(testDispatcher) {
            val nodeDao = FakeNodeDao()
            val content = "# Section One\nContent one\n# Section Two\nContent two"
            val originalId =
                nodeDao.insertNode(NodeEntity(type = "note", title = "To Split", content = content))

            val viewModel = createViewModel(nodeDao)
            viewModel.splitNote(originalId)

            val archivedNodes =
                viewModel.archivedNodes.first { it.any { n -> n.node.id == originalId } }
            val archivedOriginal = archivedNodes.find { it.node.id == originalId }
            assertEquals(
                "archived",
                archivedOriginal?.node?.status,
                "Original node should be archived after split",
            )
        }

    @Test
    fun splitNote_doesNotSplitWhenOnlyOneSection(): TestResult =
        runTest(testDispatcher) {
            val nodeDao = FakeNodeDao()
            val content = "# Only Section\nSome content here"
            val originalId =
                nodeDao.insertNode(
                    NodeEntity(
                        type = "note",
                        title = "Single Section",
                        content = content,
                    ),
                )

            val viewModel = createViewModel(nodeDao)
            viewModel.splitNote(originalId)

            // Give the coroutine time to execute
            val allNodes = viewModel.allNodes.first { it.isNotEmpty() }
            assertEquals(1, allNodes.size, "No new nodes should be created for a single section")
            assertEquals(
                "active",
                allNodes.first().node.status,
                "Original node should remain active",
            )
        }

    @Test
    fun splitNote_inheritsProjectAndAreaFromOriginal(): TestResult =
        runTest(testDispatcher) {
            val nodeDao = FakeNodeDao()
            val projectId = 10L
            val areaId = 20L
            val content = "# Part A\nContent A\n# Part B\nContent B"
            val originalId =
                nodeDao.insertNode(
                    NodeEntity(
                        type = "note",
                        title = "Structured Note",
                        content = content,
                        projectId = projectId,
                        areaId = areaId,
                    ),
                )

            val viewModel = createViewModel(nodeDao)
            viewModel.splitNote(originalId)

            val allNodes =
                viewModel.allNodes.first { list ->
                    list.count { it.node.status == "active" } == 2
                }
            val newNotes = allNodes.filter { it.node.status == "active" }
            assertTrue(
                newNotes.all { it.node.projectId == projectId },
                "All new notes should inherit projectId",
            )
            assertTrue(
                newNotes.all { it.node.areaId == areaId },
                "All new notes should inherit areaId",
            )
        }

    @Test
    fun splitNote_doesNotSplitWhenNodeHasNoContent(): TestResult =
        runTest(testDispatcher) {
            val nodeDao = FakeNodeDao()
            val originalId = nodeDao.insertNode(NodeEntity(type = "note", title = "Empty Note"))

            val viewModel = createViewModel(nodeDao)
            viewModel.splitNote(originalId)

            val allNodes = viewModel.allNodes.first { it.isNotEmpty() }
            assertEquals(1, allNodes.size, "No new nodes should be created for empty content")
            assertEquals(
                "active",
                allNodes.first().node.status,
                "Original node should remain unchanged",
            )
        }
}
