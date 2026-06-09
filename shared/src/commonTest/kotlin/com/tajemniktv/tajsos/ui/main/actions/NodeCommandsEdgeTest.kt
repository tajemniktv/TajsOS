package com.tajemniktv.tajsos.ui.main.actions

import com.tajemniktv.tajsos.data.AppRepository
import com.tajemniktv.tajsos.data.NodeEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class NodeCommandsEdgeTest {

    private fun createCommands(repository: AppRepository, scope: CoroutineScope): NodeCommands {
        return NodeCommands(
            repository = repository,
            scope = scope,
            currentTodayNodes = { emptyList() },
            currentAllNodes = { emptyList() },
            parseInternalLinks = {},
            setTagOnNode = { _, _, _ -> }
        )
    }

    private suspend fun TestScope.withTestEnv(
        block: suspend (AppRepository, NodeCommands) -> Unit
    ) {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val scope = TestScope(testDispatcher)
        val repo = buildTestRepository()
        val commands = createCommands(repo, scope)
        block(repo, commands)
    }

    @Test
    fun testUpdateMaintenanceTypeIgnoresNonMaintenanceNodes() = runTest {
        withTestEnv { repo, commands ->
            val taskNode = NodeEntity(id = 1L, type = "task", title = "A task")
            repo.insertNode(taskNode)

            commands.updateMaintenanceType(taskNode, "vehicle")
            advanceUntilIdle()

            val updatedNode = repo.getNodeById(1L)
            assertNull(updatedNode?.maintenanceType)
        }
    }

    @Test
    fun testSetMaintenanceOverdueAtIgnoresNonMaintenanceNodes() = runTest {
        withTestEnv { repo, commands ->
            val noteNode = NodeEntity(id = 1L, type = "note", title = "A note")
            repo.insertNode(noteNode)

            commands.setMaintenanceOverdueAt(noteNode, 123456L)
            advanceUntilIdle()

            val updatedNode = repo.getNodeById(1L)
            assertNull(updatedNode?.maintenanceOverdueAt)
        }
    }

    @Test
    fun testSetMaintenanceRecurringIgnoresNonMaintenanceNodes() = runTest {
        withTestEnv { repo, commands ->
            val projectNode = NodeEntity(id = 1L, type = "project", title = "A project")
            repo.insertNode(projectNode)

            commands.setMaintenanceRecurring(projectNode, "1w")
            advanceUntilIdle()

            val updatedNode = repo.getNodeById(1L)
            assertEquals(false, updatedNode?.isRecurring)
            assertNull(updatedNode?.recurringInterval)
            assertNull(updatedNode?.maintenanceInterval)
        }
    }

    @Test
    fun testSetProjectActivePhaseIgnoresNonProjectNodes() = runTest {
        withTestEnv { repo, commands ->
            val recordNode = NodeEntity(id = 1L, type = "record", title = "A record")
            repo.insertNode(recordNode)

            commands.setProjectActivePhase(recordNode, true)
            advanceUntilIdle()

            val updatedNode = repo.getNodeById(1L)
            assertNull(updatedNode?.projectStatus)
        }
    }

    @Test
    fun testSetWorkDateIgnoresNonTaskNodes() = runTest {
        withTestEnv { repo, commands ->
            val noteNode = NodeEntity(id = 1L, type = "note", title = "A note")
            repo.insertNode(noteNode)

            commands.setWorkDate(noteNode, 123456L)
            advanceUntilIdle()

            val updatedNode = repo.getNodeById(1L)
            assertNull(updatedNode?.startAt)
        }
    }

    @Test
    fun testSetTemporaryFocusPeriodCoercesDaysToValidRange() = runTest {
        withTestEnv { repo, commands ->
            val taskNode = NodeEntity(id = 1L, type = "task", title = "A task")
            repo.insertNode(taskNode)

            commands.setTemporaryFocusPeriod(taskNode, -5)
            advanceUntilIdle()

            var updatedNode = repo.getNodeById(1L)
            assertEquals("active", updatedNode?.status)

            commands.setTemporaryFocusPeriod(taskNode, 100)
            advanceUntilIdle()

            updatedNode = repo.getNodeById(1L)
            assertEquals("active", updatedNode?.status)
        }
    }
}
