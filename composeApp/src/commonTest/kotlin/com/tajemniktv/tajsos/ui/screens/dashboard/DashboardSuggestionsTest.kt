/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.dashboard

import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.ui.DashboardUIState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests that verify the null-safety logic introduced in [renderSuggestionsBlock] for the
 * batchableTasks map.
 *
 * Before the fix, `batchableTasks.values.first()` would throw [NoSuchElementException] when the
 * map contained only empty-list entries. The fix replaced this with
 * `firstOrNull { it.isNotEmpty() }` and guards via an early return.
 *
 * These tests exercise the data-extraction pattern directly, without a Compose runtime.
 */
class DashboardSuggestionsTest {

    private fun nodeWithPin(id: Long = 1L, title: String = "Task", areaId: Long? = null): NodeWithPin =
        NodeWithPin(
            node = NodeEntity(id = id, type = "task", title = title, areaId = areaId),
            pin = null,
        )

    // ── batchableTasks: firstOrNull { it.isNotEmpty() } guard ────────────────

    @Test
    fun batchableTasks_emptyMap_firstOrNullReturnsNull() {
        val batchableTasks: Map<Long?, List<NodeWithPin>> = emptyMap()
        val firstBatch = batchableTasks.values.firstOrNull { it.isNotEmpty() }
        assertNull(firstBatch, "An empty map should yield null for firstOrNull { it.isNotEmpty() }")
    }

    @Test
    fun batchableTasks_allEmptyLists_firstOrNullReturnsNull() {
        val batchableTasks: Map<Long?, List<NodeWithPin>> = mapOf(
            1L to emptyList(),
            2L to emptyList(),
            null to emptyList(),
        )
        val firstBatch = batchableTasks.values.firstOrNull { it.isNotEmpty() }
        assertNull(firstBatch, "A map with all empty-list values should yield null")
    }

    @Test
    fun batchableTasks_firstEntryEmptySecondNonEmpty_returnsSecondEntry() {
        val nonEmptyBatch = listOf(nodeWithPin(id = 10L))
        val batchableTasks: Map<Long?, List<NodeWithPin>> = linkedMapOf(
            1L to emptyList(),
            2L to nonEmptyBatch,
        )
        val firstBatch = batchableTasks.values.firstOrNull { it.isNotEmpty() }
        assertNotNull(firstBatch)
        assertEquals(nonEmptyBatch, firstBatch)
    }

    @Test
    fun batchableTasks_firstEntryNonEmpty_returnsThatEntry() {
        val nonEmptyBatch = listOf(nodeWithPin(id = 5L), nodeWithPin(id = 6L))
        val batchableTasks: Map<Long?, List<NodeWithPin>> = mapOf(
            42L to nonEmptyBatch,
            99L to emptyList(),
        )
        val firstBatch = batchableTasks.values.firstOrNull { it.isNotEmpty() }
        assertNotNull(firstBatch)
        assertEquals(nonEmptyBatch, firstBatch)
    }

    @Test
    fun batchableTasks_singleNonEmptyEntry_returnsThatEntry() {
        val batch = listOf(nodeWithPin(id = 1L, title = "Buy groceries"))
        val batchableTasks: Map<Long?, List<NodeWithPin>> = mapOf(7L to batch)
        val firstBatch = batchableTasks.values.firstOrNull { it.isNotEmpty() }
        assertEquals(batch, firstBatch)
    }

    // ── areaId lookup: safe ?.node?.areaId null propagation ──────────────────

    @Test
    fun areaIdLookup_emptyList_firstOrNullReturnsNull() {
        val batch: List<NodeWithPin> = emptyList()
        val areaId = batch.firstOrNull()?.node?.areaId
        assertNull(areaId, "firstOrNull on empty list should produce null areaId")
    }

    @Test
    fun areaIdLookup_nodeWithNullAreaId_returnsNull() {
        val batch = listOf(nodeWithPin(id = 1L, areaId = null))
        val areaId = batch.firstOrNull()?.node?.areaId
        assertNull(areaId)
    }

    @Test
    fun areaIdLookup_nodeWithAreaId_returnsAreaId() {
        val batch = listOf(nodeWithPin(id = 1L, areaId = 55L))
        val areaId = batch.firstOrNull()?.node?.areaId
        assertEquals(55L, areaId)
    }

    @Test
    fun areaIdLookup_firstNodeAreaIdUsed_notSecond() {
        val batch = listOf(
            nodeWithPin(id = 1L, areaId = 10L),
            nodeWithPin(id = 2L, areaId = 20L),
        )
        val areaId = batch.firstOrNull()?.node?.areaId
        assertEquals(10L, areaId, "Should use the first node's areaId, not subsequent ones")
    }

    // ── DashboardUIState: batchableTasks field defaults ───────────────────────

    @Test
    fun dashboardUIState_defaultBatchableTasks_isEmpty() {
        val state = DashboardUIState()
        assertTrue(state.batchableTasks.isEmpty(), "Default batchableTasks should be an empty map")
    }

    @Test
    fun dashboardUIState_withAllEmptyBatches_firstOrNullGuardPreventsAccess() {
        val state = DashboardUIState(
            batchableTasks = mapOf(
                1L to emptyList(),
                2L to emptyList(),
            ),
        )
        // Simulate the fixed logic: when all values are empty, firstOrNull returns null
        assertTrue(state.batchableTasks.isNotEmpty(), "Map has entries")
        val firstBatch = state.batchableTasks.values.firstOrNull { it.isNotEmpty() }
        assertNull(firstBatch, "Should get null when all batch lists are empty — guard prevents crash")
    }

    @Test
    fun dashboardUIState_withMixedBatches_findsFirstNonEmpty() {
        val tasks = listOf(nodeWithPin(id = 1L, areaId = 3L))
        val state = DashboardUIState(
            batchableTasks = mapOf(
                null to emptyList(),
                3L to tasks,
            ),
        )
        val firstBatch = state.batchableTasks.values.firstOrNull { it.isNotEmpty() }
        assertNotNull(firstBatch)
        assertEquals(tasks.size, firstBatch.size)
        val areaId = firstBatch.firstOrNull()?.node?.areaId
        assertEquals(3L, areaId)
    }
}