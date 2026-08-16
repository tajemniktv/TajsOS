package com.tajemniktv.tajsos.ui.main.calculators

import com.tajemniktv.tajsos.data.*
import com.tajemniktv.tajsos.ui.DashboardUIState
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MainStateAssemblersDashboardEdgeTest {

    private fun createTestNodeWithPin(
        node: NodeEntity
    ): NodeWithPin {
        return NodeWithPin(
            node = node,
            pin = null,
            tags = emptyList()
        )
    }

    private fun defaultNode(id: Long, type: String = "task", status: String = "active") = NodeEntity(
        id = id,
        title = "Test Node $id",
        content = "",
        type = type,
        status = status,
        inboxState = false,
        reminderAt = null,
        updatedAt = 0L,
        createdAt = 0L
    )

    private fun createRepo(): AppRepository {
        return AppRepository(
            nodeDao = FakeNodeDao(),
            focusSessionDao = FakeFocusSessionDao(),
            trackDao = FakeTrackDao(),
            relationDao = FakeRelationDao(),
            tagDao = FakeTagDao(),
            eventLogDao = FakeEventLogDao(),
            attachmentDao = FakeAttachmentDao(),
            templateDao = FakeTemplateDao(),
            protocolDao = FakeProtocolDao(),
            userDao = FakeUserDao(),
            modeDao = FakeModeDao(),
            medicationDao = FakeMedicationDao(),
            decisionDao = FakeDecisionDao(),
            nodeSnapshotDao = FakeNodeSnapshotDao(),
            reviewDao = FakeReviewDao(),
            calendarProviderDao = FakeCalendarProviderDao(),
            calendarEventDao = FakeCalendarEventDao()
        )
    }

    private suspend fun assembleState(
        repo: AppRepository,
        nodes: List<NodeWithPin>,
        mode: ModeEntity? = null
    ): DashboardUIState {
        val modesList = mode?.let { listOf(it) } ?: emptyList()
        return buildDashboardUIState(
            repository = repo,
            nodes = nodes,
            modesList = modesList,
            activeId = mode?.id,
            areasList = emptyList(),
            packs = PackRegistry(emptySet(), emptySet())
        )
    }

    @Test
    fun `buildDashboardUIState identifies vaults and incubator correctly`() = runTest {
        val repo = createRepo()

        val readLaterNode = createTestNodeWithPin(defaultNode(1, "note").copy(noteType = "read_later"))
        val quoteNode = createTestNodeWithPin(defaultNode(2, "note").copy(noteType = "quote"))
        val ideaNode = createTestNodeWithPin(defaultNode(3, "idea").copy(projectId = null)) // No project
        val ideaWithProject = createTestNodeWithPin(defaultNode(4, "idea").copy(projectId = 100L)) // Excluded from incubator
        val inactiveReadLater = createTestNodeWithPin(defaultNode(5, "note", "archived").copy(noteType = "read_later"))

        val nodes = listOf(readLaterNode, quoteNode, ideaNode, ideaWithProject, inactiveReadLater)
        val state = assembleState(repo, nodes)

        assertEquals(1, state.readLaterVault.size)
        assertEquals(1L, state.readLaterVault[0].node.id)

        assertEquals(1, state.quoteVault.size)
        assertEquals(2L, state.quoteVault[0].node.id)

        assertEquals(1, state.ideaIncubator.size)
        assertEquals(3L, state.ideaIncubator[0].node.id)
    }

    @Test
    fun `buildDashboardUIState surfaces forgotten wisdom correctly`() = runTest {
        val repo = createRepo()
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
        val staleTime = now - (31L * 24 * 60 * 60 * 1000L) // 31 days ago

        val evergreenNote = createTestNodeWithPin(defaultNode(1, "note").copy(noteType = "evergreen", updatedAt = now)) // Included despite recent update
        val staleIdea = createTestNodeWithPin(defaultNode(2, "idea").copy(updatedAt = staleTime)) // Included due to stale update
        val recentNote = createTestNodeWithPin(defaultNode(3, "note").copy(updatedAt = now)) // Excluded (not evergreen, not stale)
        val inactiveEvergreen = createTestNodeWithPin(defaultNode(4, "note", "archived").copy(noteType = "evergreen")) // Excluded (not active)

        val nodes = listOf(evergreenNote, staleIdea, recentNote, inactiveEvergreen)

        // forgottenWisdom uses firstOrNull after shuffling, so we just check if it finds ONE of the valid ones
        val state = assembleState(repo, nodes)

        assertNotNull(state.forgottenWisdom)
        val id = state.forgottenWisdom!!.node.id
        assertTrue(id == 1L || id == 2L, "Expected ID to be 1 or 2, was $id")
    }

    @Test
    fun `buildDashboardUIState surfaces deserves attention correctly`() = runTest {
        val repo = createRepo()
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
        val staleTime = now - (8L * 24 * 60 * 60 * 1000L) // 8 days ago

        val neglectedTask = createTestNodeWithPin(defaultNode(1, "task").copy(isPinned = false, dueAt = null, updatedAt = staleTime))
        val pinnedNeglectedTask = createTestNodeWithPin(defaultNode(2, "task").copy(isPinned = true, dueAt = null, updatedAt = staleTime)) // Excluded (pinned)
        val dueNeglectedTask = createTestNodeWithPin(defaultNode(3, "task").copy(isPinned = false, dueAt = now + 10000L, updatedAt = staleTime)) // Excluded (has due date)
        val recentTask = createTestNodeWithPin(defaultNode(4, "task").copy(isPinned = false, dueAt = null, updatedAt = now)) // Excluded (updated recently)
        val inactiveNeglectedTask = createTestNodeWithPin(defaultNode(5, "task", "archived").copy(isPinned = false, dueAt = null, updatedAt = staleTime)) // Excluded (inactive)

        val nodes = listOf(neglectedTask, pinnedNeglectedTask, dueNeglectedTask, recentTask, inactiveNeglectedTask)
        val state = assembleState(repo, nodes)

        assertEquals(1, state.deservesAttention.size)
        assertEquals(1L, state.deservesAttention[0].node.id)
    }
}
