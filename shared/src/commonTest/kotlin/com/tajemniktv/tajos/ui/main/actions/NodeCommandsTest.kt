package com.tajemniktv.tajos.ui.main.actions

import com.tajemniktv.tajos.data.AppRepository
import com.tajemniktv.tajos.data.NodeEntity
import com.tajemniktv.tajos.data.RelationEntity
import com.tajemniktv.tajos.data.TaskState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus

/** Exercises persisted command outcomes, including snapshot recovery and relation preservation. */
@OptIn(ExperimentalCoroutinesApi::class)
class NodeCommandsTest {

    private fun setupTestCommands(
        scope: TestScope,
        repo: AppRepository,
        allNodes: List<NodeEntity> = emptyList(),
        todayNodes: List<NodeEntity> = emptyList()
    ): NodeCommands {
        return NodeCommands(
            repository = repo,
            scope = scope,
            currentTodayNodes = { todayNodes },
            currentAllNodes = { allNodes },
            parseInternalLinks = { _ -> },
            setTagOnNode = { _, _, _ -> }
        )
    }

    @Test
    fun `archiveNode sets status to archived and updates timestamp`() = runTest {
        val repo = buildTestRepository()
        val commands = setupTestCommands(this, repo)

        val node = NodeEntity(id = 1, type = "task", title = "Task 1", status = "active", updatedAt = 0)
        repo.insertNode(node)

        commands.archiveNode(node)
        testScheduler.advanceUntilIdle()

        val updatedNode = repo.getNodeById(1)
        assertNotNull(updatedNode)
        assertEquals("archived", updatedNode.status)
        assertTrue(updatedNode.updatedAt > 0)
    }

    @Test
    fun `updateNodeStatus to done sets completedAt and creates recurring node if applicable`() = runTest {
        val repo = buildTestRepository()
        val commands = setupTestCommands(this, repo)

        val now = Clock.System.now().toEpochMilliseconds()
        val node = NodeEntity(
            id = 1,
            type = "task",
            title = "Task 1",
            status = "active",
            isRecurring = true,
            recurringInterval = "1w",
            dueAt = now
        )
        repo.insertNode(node)

        commands.updateNodeStatus(node, "done")
        testScheduler.advanceUntilIdle()

        val updatedNode = repo.getNodeById(1)
        assertNotNull(updatedNode)
        assertEquals("done", updatedNode.status)
        assertNotNull(updatedNode.completedAt)

        val nodes = repo.getAllNodes().first()
        assertEquals(2, nodes.size)

        val newNode = nodes.first { it.node.id != 1L }.node
        assertEquals("Task 1", newNode.title)
        assertEquals("active", newNode.status)
        assertTrue(newNode.dueAt != null && newNode.dueAt!! > now)
    }

    @Test
    fun `sweepStaleTasks updates stale tasks to someday and increments postponeCount`() = runTest {
        val repo = buildTestRepository()
        val now = Clock.System.now()
        val staleTime = (now - 5.days).toEpochMilliseconds()

        val staleTask = NodeEntity(id = 1, title = "test", type = "task", dueAt = staleTime, status = "active")
        val activeTask = NodeEntity(id = 2, title = "test2", type = "task", dueAt = now.toEpochMilliseconds(), status = "active")

        repo.insertNode(staleTask)
        repo.insertNode(activeTask)

        val commands = setupTestCommands(this, repo, allNodes = listOf(staleTask, activeTask))

        commands.sweepStaleTasks(cutoffDays = 3)
        testScheduler.advanceUntilIdle()

        val updatedStaleTask = repo.getNodeById(1)
        assertNotNull(updatedStaleTask)
        assertEquals(TaskState.SOMEDAY.storageKey, updatedStaleTask.status)
        assertEquals(1, updatedStaleTask.postponeCount)

        val updatedActiveTask = repo.getNodeById(2)
        assertNotNull(updatedActiveTask)
        assertEquals("active", updatedActiveTask.status)
        assertEquals(0, updatedActiveTask.postponeCount)
    }

    @Test
    fun `extractNextStep sets nextSmallestStep correctly`() = runTest {
        val repo = buildTestRepository()
        val commands = setupTestCommands(this, repo)

        val node = NodeEntity(
            id = 1,
            type = "task",
            title = "Task 1",
            content = "- Step 1\n- Step 2",
            nextSmallestStep = ""
        )
        repo.insertNode(node)

        commands.extractNextStep(1)
        testScheduler.advanceUntilIdle()

        val updatedNode = repo.getNodeById(1)
        assertNotNull(updatedNode)
        assertEquals("Step 1", updatedNode.nextSmallestStep)
    }

    @Test
    fun `splitIntoSubtasks creates new child nodes and updates parent`() = runTest {
        val repo = buildTestRepository()
        val commands = setupTestCommands(this, repo)

        val node = NodeEntity(
            id = 1,
            type = "project",
            title = "Project 1",
            content = "- Subtask A\n- Subtask B\nSome note"
        )
        repo.insertNode(node)

        commands.splitIntoSubtasks(1)
        testScheduler.advanceUntilIdle()

        val updatedNode = repo.getNodeById(1)
        assertNotNull(updatedNode)
        assertTrue(updatedNode.content.contains("// SPLIT INTO SUBTASKS"))

        val nodes = repo.getAllNodes().first()
        assertEquals(3, nodes.size)

        val subtasks = nodes.map { it.node }.filter { it.type == "task" }
        assertEquals(2, subtasks.size)
        assertTrue(subtasks.any { it.title == "Subtask A" })
        assertTrue(subtasks.any { it.title == "Subtask B" })
    }

    @Test
    fun `createSnapshot and restoreSnapshot`() = runTest {
        val repo = buildTestRepository()
        val commands = setupTestCommands(this, repo)

        val node = NodeEntity(id = 1, type = "note", title = "Original Title", content = "Original Content")
        repo.insertNode(node)

        commands.createSnapshot(1)
        testScheduler.advanceUntilIdle()

        repo.updateNode(node.copy(title = "New Title", content = "New Content"))

        val snapshot = repo.getSnapshotsForNode(1).first().single()
        assertEquals("Original Title", snapshot.title)
        assertEquals("Original Content", snapshot.content)
        commands.restoreSnapshot(snapshot)
        testScheduler.advanceUntilIdle()

        val restoredNode = repo.getNodeById(1)
        assertNotNull(restoredNode)
        assertEquals("Original Title", restoredNode.title)
        assertEquals("Original Content", restoredNode.content)
    }

    @Test
    fun `mergeNodes updates content and reassigns relationships`() = runTest {
        val repo = buildTestRepository()
        val commands = setupTestCommands(this, repo)

        val primary = NodeEntity(id = 1, type = "task", title = "Primary", content = "Primary Content")
        val other = NodeEntity(id = 2, type = "task", title = "Other", content = "Other Content")
        repo.insertNode(primary)
        repo.insertNode(other)
        repo.insertNode(NodeEntity(id = 3, type = "note", title = "Related note"))
        repo.insertNode(NodeEntity(id = 4, type = "note", title = "Incoming note"))

        repo.insertRelation(RelationEntity(id = 1, fromNodeId = 2, toNodeId = 3, relationType = "RELATED"))
        repo.insertRelation(RelationEntity(id = 2, fromNodeId = 4, toNodeId = 2, relationType = "RELATED"))

        commands.mergeNodes(1, listOf(2))
        testScheduler.advanceUntilIdle()

        val updatedPrimary = repo.getNodeById(1)
        assertNotNull(updatedPrimary)
        assertTrue(updatedPrimary.content.contains("Primary Content"))
        assertTrue(updatedPrimary.content.contains("MERGED FROM Other"))
        assertTrue(updatedPrimary.content.contains("Other Content"))

        val archivedOther = repo.getNodeById(2)
        assertNotNull(archivedOther)
        assertEquals("archived", archivedOther.status)

        val relations = repo.getRelationsForNode(1).first()
        assertTrue(relations.any { it.toNodeId == 3L && it.relationType == "RELATED" })
        assertTrue(relations.any { it.fromNodeId == 4L && it.toNodeId == 1L && it.relationType == "RELATED" })
    }

    @Test
    fun `temporary focus clamps both boundaries in local calendar days`() = runTest {
        val repo = buildTestRepository()
        val commands = setupTestCommands(this, repo)
        for ((requestedDays, expectedDays) in listOf(-5 to 1, 40 to 30)) {
            val id = repo.insertNode(NodeEntity(type = "task", title = "Focus", status = "someday"))
            commands.setTemporaryFocusPeriod(assertNotNull(repo.getNodeById(id)), requestedDays)
            testScheduler.advanceUntilIdle()
            val updated = assertNotNull(repo.getNodeById(id))
            val start = Instant.fromEpochMilliseconds(assertNotNull(updated.startAt))
            assertEquals(
                start.plus(expectedDays, DateTimeUnit.DAY, TimeZone.currentSystemDefault()).toEpochMilliseconds(),
                updated.dueAt,
            )
            assertEquals(TaskState.ACTIVE.storageKey, updated.status)
        }
    }

    @Test
    fun `open loop conversion preserves source context and links the new decision`() = runTest {
        val repo = buildTestRepository()
        val sourceId = repo.insertNode(NodeEntity(type = "open_loop", title = "Choose", content = "Context", projectId = 10, areaId = 20, inboxState = true))
        val commands = setupTestCommands(this, repo)
        commands.convertOpenLoopToDecision(sourceId)
        testScheduler.advanceUntilIdle()

        val source = assertNotNull(repo.getNodeById(sourceId))
        assertEquals(TaskState.DONE.storageKey, source.status)
        assertFalse(source.inboxState)
        val relation = repo.getRelationsForNode(sourceId).first().single { it.relationType == "DERIVED_FROM" }
        assertEquals(sourceId, relation.fromNodeId)
        assertEquals("DERIVED_FROM", relation.relationType)
        val decision = assertNotNull(repo.getNodeById(relation.toNodeId))
        assertTrue(decision.content.contains(source.content))
        assertEquals(source.projectId, decision.projectId)
        assertEquals(source.areaId, decision.areaId)
        assertEquals("pending", decision.decisionStatus)
    }
}
