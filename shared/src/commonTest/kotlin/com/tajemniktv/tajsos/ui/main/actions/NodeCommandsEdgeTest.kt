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

    private fun testEdgeCase(
        nodeType: String,
        action: NodeCommands.(NodeEntity) -> Unit,
        verify: (NodeEntity?) -> Unit
    ) = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val scope = TestScope(testDispatcher)
        val repo = buildTestRepository()
        val commands = createCommands(repo, scope)

        val node = NodeEntity(id = 1L, type = nodeType, title = "Test Node")
        repo.insertNode(node)

        commands.action(node)
        advanceUntilIdle()

        verify(repo.getNodeById(1L))
    }

    @Test
    fun testUpdateMaintenanceTypeIgnoresNonMaintenanceNodes() = testEdgeCase(
        nodeType = "task",
        action = { updateMaintenanceType(it, "vehicle") },
        verify = { assertNull(it?.maintenanceType) }
    )

    @Test
    fun testSetMaintenanceOverdueAtIgnoresNonMaintenanceNodes() = testEdgeCase(
        nodeType = "note",
        action = { setMaintenanceOverdueAt(it, 123456L) },
        verify = { assertNull(it?.maintenanceOverdueAt) }
    )

    @Test
    fun testSetMaintenanceRecurringIgnoresNonMaintenanceNodes() = testEdgeCase(
        nodeType = "project",
        action = { setMaintenanceRecurring(it, "1w") },
        verify = {
            assertEquals(false, it?.isRecurring)
            assertNull(it?.recurringInterval)
            assertNull(it?.maintenanceInterval)
        }
    )

    @Test
    fun testSetProjectActivePhaseIgnoresNonProjectNodes() = testEdgeCase(
        nodeType = "record",
        action = { setProjectActivePhase(it, true) },
        verify = { assertNull(it?.projectStatus) }
    )

    @Test
    fun testSetWorkDateIgnoresNonTaskNodes() = testEdgeCase(
        nodeType = "note",
        action = { setWorkDate(it, 123456L) },
        verify = { assertNull(it?.startAt) }
    )

    @Test
    fun testSetTemporaryFocusPeriodCoercesNegativeDays() = testEdgeCase(
        nodeType = "task",
        action = { setTemporaryFocusPeriod(it, -5) },
        verify = { assertEquals("active", it?.status) }
    )

    @Test
    fun testSetTemporaryFocusPeriodCoercesLargeDays() = testEdgeCase(
        nodeType = "task",
        action = { setTemporaryFocusPeriod(it, 100) },
        verify = { assertEquals("active", it?.status) }
    )
}
