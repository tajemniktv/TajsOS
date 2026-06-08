package com.tajemniktv.tajsos.ui.main.actions

import com.tajemniktv.tajsos.data.NodeEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertNull
import kotlin.test.assertFalse

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class NodeCommandsEdgeTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val scope = TestScope(dispatcher)

    @Test
    fun testConvertOpenLoop() = runTest {
        val repo = buildTestRepository()

        val sourceNodeId = repo.insertNode(
            NodeEntity(
                type = "open_loop",
                title = "Need to decide on car",
                content = "Details about the car",
                openLoopType = "choice"
            )
        )

        val commands = NodeCommands(
            repository = repo,
            scope = scope,
            currentTodayNodes = { emptyList() },
            currentAllNodes = { emptyList() },
            parseInternalLinks = {},
            setTagOnNode = { _, _, _ -> }
        )

        commands.convertOpenLoopToDecision(sourceNodeId)

        dispatcher.scheduler.advanceUntilIdle()

        val updatedSource = repo.getNodeById(sourceNodeId)
        assertNotNull(updatedSource)
        assertEquals("done", updatedSource.status)
        assertEquals("Converted to DECISION", updatedSource.completionNote)
        assertFalse(updatedSource.inboxState)

        val relations = repo.getRelationsForNode(sourceNodeId).first()
        assertEquals(1, relations.size)
        val relation = relations.first()
        assertEquals("DERIVED_FROM", relation.relationType)

        val createdId = relation.toNodeId
        val createdNode = repo.getNodeById(createdId)
        assertNotNull(createdNode)
        assertEquals("decision", createdNode.type)
        assertEquals("Decision: Need to decide on car", createdNode.title)
        assertTrue(createdNode.content.contains("Details about the car"))
        assertTrue(createdNode.content.contains("Converted from open loop (choice)."))
        assertEquals("pending", createdNode.decisionStatus)
        assertEquals("major", createdNode.decisionCategory)
    }

    @Test
    fun testSetTemporaryFocusPeriodCoercion() = runTest {
        val repo = buildTestRepository()
        val id = repo.insertNode(NodeEntity(type = "task", title = "A task", startAt = null, dueAt = null))
        val node = repo.getNodeById(id)!!

        val commands = NodeCommands(
            repository = repo,
            scope = scope,
            currentTodayNodes = { emptyList() },
            currentAllNodes = { emptyList() },
            parseInternalLinks = {},
            setTagOnNode = { _, _, _ -> }
        )

        // Using coercion tests implicitly (40 should coerce to 30)
        commands.setTemporaryFocusPeriod(node, 40)

        dispatcher.scheduler.advanceUntilIdle()

        val updatedNode = repo.getNodeById(id)!!
        assertNotNull(updatedNode.startAt)
        assertNotNull(updatedNode.dueAt)
        assertEquals("active", updatedNode.status)

        // Difference should be 30 days
        val diffMs = updatedNode.dueAt!! - updatedNode.startAt!!
        assertTrue(diffMs > 29L * 24 * 60 * 60 * 1000)
    }

    @Test
    fun testSetProjectActivePhase() = runTest {
        val repo = buildTestRepository()
        val projectId = repo.insertNode(NodeEntity(type = "project", title = "P", projectStatus = "active"))
        val taskId = repo.insertNode(NodeEntity(type = "task", title = "T", projectStatus = null))

        val commands = NodeCommands(
            repository = repo,
            scope = scope,
            currentTodayNodes = { emptyList() },
            currentAllNodes = { emptyList() },
            parseInternalLinks = {},
            setTagOnNode = { _, _, _ -> }
        )

        commands.setProjectActivePhase(repo.getNodeById(projectId)!!, false)
        commands.setProjectActivePhase(repo.getNodeById(taskId)!!, false)

        dispatcher.scheduler.advanceUntilIdle()

        val p = repo.getNodeById(projectId)!!
        assertEquals("on_hold", p.projectStatus)

        val t = repo.getNodeById(taskId)!!
        assertNull(t.projectStatus)
    }
}
