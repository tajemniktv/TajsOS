package com.tajemniktv.tajsos.ui.main.calculators

import com.tajemniktv.tajsos.data.AppRepository
import com.tajemniktv.tajsos.data.ModeEntity
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.PackRegistry
import com.tajemniktv.tajsos.data.FakeNodeDao
import com.tajemniktv.tajsos.data.FakeModeDao
import com.tajemniktv.tajsos.data.FakeUserDao
import com.tajemniktv.tajsos.data.FakeTagDao
import com.tajemniktv.tajsos.data.FakeFocusSessionDao
import com.tajemniktv.tajsos.data.FakeTrackDao
import com.tajemniktv.tajsos.data.FakeRelationDao
import com.tajemniktv.tajsos.data.FakeEventLogDao
import com.tajemniktv.tajsos.data.FakeAttachmentDao
import com.tajemniktv.tajsos.data.FakeTemplateDao
import com.tajemniktv.tajsos.data.FakeProtocolDao
import com.tajemniktv.tajsos.data.FakeMedicationDao
import com.tajemniktv.tajsos.data.FakeDecisionDao
import com.tajemniktv.tajsos.data.FakeNodeSnapshotDao
import com.tajemniktv.tajsos.data.FakeReviewDao
import com.tajemniktv.tajsos.data.FakeCalendarProviderDao
import com.tajemniktv.tajsos.data.FakeCalendarEventDao
import com.tajemniktv.tajsos.data.ModeAreaFilterEntity
import com.tajemniktv.tajsos.data.ModeTypeFilterEntity
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertNull
import kotlin.time.Clock

class ConfigurableFakeModeDao(
    private val areaFilters: List<ModeAreaFilterEntity> = emptyList(),
    private val typeFilters: List<ModeTypeFilterEntity> = emptyList()
) : com.tajemniktv.tajsos.data.ModeDao by FakeModeDao() {
    override fun getAreaFiltersForMode(modeId: Long): Flow<List<ModeAreaFilterEntity>> = flowOf(areaFilters)
    override fun getTypeFiltersForMode(modeId: Long): Flow<List<ModeTypeFilterEntity>> = flowOf(typeFilters)
}

class MainStateAssemblersDashboardTest {

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
        updatedAt = 0L
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

    @Test
    fun `buildDashboardUIState basic state count`() = runTest {
        val repo = createRepo()
        val nodeActiveTask1 = createTestNodeWithPin(defaultNode(1, "task", "active"))
        val nodeActiveTask2 = createTestNodeWithPin(defaultNode(2, "task", "active"))
        val nodeDoneTask = createTestNodeWithPin(defaultNode(3, "task", "done"))
        val nodeNote = createTestNodeWithPin(defaultNode(4, "note", "active"))

        val mode = ModeEntity(id = 1L, key = "FOCUS", name = "Focus", icon = "focus")

        val nodes = listOf(nodeActiveTask1, nodeActiveTask2, nodeDoneTask, nodeNote)
        val state = buildDashboardUIState(
            repository = repo,
            nodes = nodes,
            modesList = listOf(mode),
            activeId = 1L,
            areasList = emptyList(),
            packs = PackRegistry(emptySet(), emptySet())
        )

        assertEquals(2, state.tasksCount)
        assertEquals(1, state.notesCount)
    }

    @Test
    fun `buildDashboardUIState handles recovery mode task filtering correctly`() = runTest {
        val repo = createRepo()
        val highEnergyTask = createTestNodeWithPin(defaultNode(1, "task", "active").copy(energyLevel = 3))
        val lowEnergyTask = createTestNodeWithPin(defaultNode(2, "task", "active").copy(energyLevel = 1, friction = "easy"))
        val nodeNote = createTestNodeWithPin(defaultNode(3, "note", "active"))

        val mode = ModeEntity(id = 1L, key = "RECOVERY", name = "Recovery", icon = "recovery")

        val nodes = listOf(highEnergyTask, lowEnergyTask, nodeNote)
        val state = buildDashboardUIState(
            repository = repo,
            nodes = nodes,
            modesList = listOf(mode),
            activeId = 1L,
            areasList = emptyList(),
            packs = PackRegistry(emptySet(), emptySet())
        )

        assertEquals(1, state.tasksCount)
        assertTrue(state.lowEnergyTasks.size == 1)
        assertEquals(2L, state.lowEnergyTasks.first().node.id)
    }

    @Test
    fun `buildDashboardUIState capacity warnings trigger correctly`() = runTest {
        val repo = createRepo()
        val nodes = (1..51).map {
            createTestNodeWithPin(defaultNode(it.toLong(), "task", "active"))
        }

        val state = buildDashboardUIState(
            repository = repo,
            nodes = nodes,
            modesList = emptyList(),
            activeId = null,
            areasList = emptyList(),
            packs = PackRegistry(emptySet(), emptySet())
        )

        assertNotNull(state.capacityWarning)
        assertTrue(state.capacityWarning!!.contains("SYSTEM OVERLOADED"))
    }

    @Test
    fun `buildDashboardUIState fragmentation warnings trigger correctly`() = runTest {
        val repo = createRepo()
        val nodes = (1..9).map {
            createTestNodeWithPin(defaultNode(it.toLong(), "task", "active").copy(projectId = it.toLong()))
        }

        val state = buildDashboardUIState(
            repository = repo,
            nodes = nodes,
            modesList = emptyList(),
            activeId = null,
            areasList = emptyList(),
            packs = PackRegistry(emptySet(), emptySet())
        )

        assertNotNull(state.capacityWarning)
        assertTrue(state.capacityWarning!!.contains("ATTENTION FRAGMENTED"))
    }

    @Test
    fun `buildDashboardUIState open loops calculations`() = runTest {
        val repo = createRepo()
        val now = Clock.System.now().toEpochMilliseconds()

        val nodes = (1..12).map {
            createTestNodeWithPin(defaultNode(it.toLong(), "open_loop", "active").copy(createdAt = now - 10000))
        }

        val state = buildDashboardUIState(
            repository = repo,
            nodes = nodes,
            modesList = emptyList(),
            activeId = null,
            areasList = emptyList(),
            packs = PackRegistry(emptySet(), emptySet())
        )

        assertEquals(12, state.openLoops.size)
        assertNotNull(state.openLoopsOverloadWarning)
        assertTrue(state.openLoopsOverloadWarning!!.contains("OPEN LOOPS OVERLOAD"))
    }

    @Test
    fun `buildDashboardUIState stale tasks calculation`() = runTest {
        val repo = createRepo()
        val now = Clock.System.now().toEpochMilliseconds()

        val staleTime = now - (30 * 24 * 60 * 60 * 1000L)
        val activeRecentTask = createTestNodeWithPin(defaultNode(1, "task", "active").copy(updatedAt = now))
        val activeStaleTask = createTestNodeWithPin(defaultNode(2, "task", "active").copy(dueAt = staleTime, updatedAt = staleTime, createdAt = staleTime - 1000L))
        val activeRecentNote = createTestNodeWithPin(defaultNode(3, "note", "active").copy(updatedAt = staleTime))

        val nodes = listOf(activeRecentTask, activeStaleTask, activeRecentNote)

        val state = buildDashboardUIState(
            repository = repo,
            nodes = nodes,
            modesList = emptyList(),
            activeId = null,
            areasList = emptyList(),
            packs = PackRegistry(emptySet(), emptySet())
        )

        assertEquals(1, state.staleTasksCount)
    }

    @Test
    fun `buildDashboardUIState context clustering`() = runTest {
        val repo = createRepo()

        val homeTask = createTestNodeWithPin(defaultNode(1, "task", "active").copy(locationContext = "at_home"))
        val workTask = createTestNodeWithPin(defaultNode(2, "task", "active").copy(locationContext = "at_work"))
        val homeTask2 = createTestNodeWithPin(defaultNode(3, "task", "active").copy(locationContext = "at_home"))
        val nullContextTask = createTestNodeWithPin(defaultNode(4, "task", "active").copy(locationContext = null))

        val nodes = listOf(homeTask, workTask, homeTask2, nullContextTask)

        val state = buildDashboardUIState(
            repository = repo,
            nodes = nodes,
            modesList = emptyList(),
            activeId = null,
            areasList = emptyList(),
            packs = PackRegistry(emptySet(), emptySet())
        )

        assertEquals(3, state.contextClusteredTasks.size) // at_home, at_work, general (null defaults to general)
        assertEquals(2, state.contextClusteredTasks["at_home"]?.size)
        assertEquals(1, state.contextClusteredTasks["at_work"]?.size)
        assertEquals(1, state.contextClusteredTasks["general"]?.size)
    }

    @Test
    fun `buildDashboardUIState handles area and type filters correctly`() = runTest {
        val repo = AppRepository(
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
            modeDao = ConfigurableFakeModeDao(
                areaFilters = listOf(
                    ModeAreaFilterEntity(modeId = 1L, areaId = 10L, include = true),
                    ModeAreaFilterEntity(modeId = 1L, areaId = 20L, include = false)
                ),
                typeFilters = listOf(
                    ModeTypeFilterEntity(modeId = 1L, nodeType = "task", include = true),
                    ModeTypeFilterEntity(modeId = 1L, nodeType = "note", include = false)
                )
            ),
            medicationDao = FakeMedicationDao(),
            decisionDao = FakeDecisionDao(),
            nodeSnapshotDao = FakeNodeSnapshotDao(),
            reviewDao = FakeReviewDao(),
            calendarProviderDao = FakeCalendarProviderDao(),
            calendarEventDao = FakeCalendarEventDao()
        )

        val mode = ModeEntity(id = 1L, key = "WORK", name = "Work", icon = "work")

        val includedAreaTask = createTestNodeWithPin(defaultNode(1, "task", "active").copy(areaId = 10L))
        val excludedAreaTask = createTestNodeWithPin(defaultNode(2, "task", "active").copy(areaId = 20L))
        val otherAreaTask = createTestNodeWithPin(defaultNode(3, "task", "active").copy(areaId = 30L)) // Will be excluded because include list is not empty
        val includedAreaNote = createTestNodeWithPin(defaultNode(4, "note", "active").copy(areaId = 10L)) // Will be excluded by type filter
        val includedAreaIdea = createTestNodeWithPin(defaultNode(5, "idea", "active").copy(areaId = 10L)) // Will be excluded by type filter
        val areaItem = createTestNodeWithPin(defaultNode(6, "area", "active").copy(areaId = 10L)) // area items should be kept

        val nodes = listOf(includedAreaTask, excludedAreaTask, otherAreaTask, includedAreaNote, includedAreaIdea, areaItem)

        val state = buildDashboardUIState(
            repository = repo,
            nodes = nodes,
            modesList = listOf(mode),
            activeId = 1L,
            areasList = emptyList(),
            packs = PackRegistry(emptySet(), emptySet())
        )

        // Only includedAreaTask should remain as an active task
        assertEquals(1, state.tasksCount)
    }

    @Test
    fun `buildDashboardUIState suggested context generation`() = runTest {
        val repo = createRepo()

        val contextNode = createTestNodeWithPin(defaultNode(1, "task", "active").copy(
            locationContext = "at_home", // Matches 22..23, 0..5 priority key
            energyContext = "commute_friendly", // Matches 6..9, 16..18 priority key
            deviceContext = "on_campus" // Matches else priority key
        ))

        val nodes = listOf(contextNode)

        val state = buildDashboardUIState(
            repository = repo,
            nodes = nodes,
            modesList = emptyList(),
            activeId = null,
            areasList = emptyList(),
            packs = PackRegistry(emptySet(), emptySet())
        )

        assertNotNull(state.suggestedContextKey)
        assertTrue(state.suggestedContextTasks.isNotEmpty())
    }

    @Test
    fun `buildDashboardUIState foundational notes filtering`() = runTest {
        val repo = createRepo()
        val note = defaultNode(1, "note", "active")
        val noteWithPin = NodeWithPin(
            node = note,
            pin = null,
            tags = listOf(com.tajemniktv.tajsos.data.TagEntity(id = 1L, name = "foundational", normalizedName = "foundational", color = null))
        )

        val nodes = listOf(noteWithPin)

        val state = buildDashboardUIState(
            repository = repo,
            nodes = nodes,
            modesList = emptyList(),
            activeId = null,
            areasList = emptyList(),
            packs = PackRegistry(emptySet(), emptySet())
        )

        assertTrue(state.foundationalNotes.isNotEmpty())
    }
}
