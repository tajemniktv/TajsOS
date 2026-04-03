/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.dashboard

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Regression tests for the batchable-tasks selection logic inside renderSuggestionsBlock.
 *
 * Before the fix, the block called `.first()` on `batchableTasks.values`, which returned an
 * empty list when the first map entry held an empty collection – this then caused a crash on
 * the subsequent `.first().node.areaId` call. The fix replaced the chain with
 * `.firstOrNull { it.isNotEmpty() } ?: return@Column` so that entries with empty task lists
 * are skipped entirely.
 *
 * Because [renderSuggestionsBlock] is an `@Composable` internal function, these tests exercise
 * the collection-selection predicate in isolation as a specification for the expected behaviour.
 */
class DashboardSuggestionsBlockTest {

    // Helper mirroring the fixed logic in renderSuggestionsBlock:
    //   batchableTasks.values.firstOrNull { it.isNotEmpty() }
    private fun <T> firstNonEmptyBatch(batches: Map<*, List<T>>): List<T>? =
        batches.values.firstOrNull { it.isNotEmpty() }

    // ------------------------------------------------------------------
    // Regression: all-empty values should yield null, not an empty list
    // ------------------------------------------------------------------

    @Test
    fun firstNonEmptyBatch_allEmptyLists_returnsNull() {
        val batches = mapOf("areaA" to emptyList<Any>(), "areaB" to emptyList())
        assertNull(firstNonEmptyBatch(batches), "All-empty batchableTasks should yield null so the block is skipped")
    }

    @Test
    fun firstNonEmptyBatch_emptyMap_returnsNull() {
        val batches = emptyMap<String, List<Any>>()
        assertNull(firstNonEmptyBatch(batches))
    }

    // ------------------------------------------------------------------
    // Happy path: first non-empty batch is selected
    // ------------------------------------------------------------------

    @Test
    fun firstNonEmptyBatch_singleNonEmptyList_returnsIt() {
        val items = listOf("task1", "task2")
        val batches = mapOf("areaA" to items)
        assertEquals(items, firstNonEmptyBatch(batches))
    }

    @Test
    fun firstNonEmptyBatch_firstEmptyThenNonEmpty_skipsEmpty() {
        val nonEmpty = listOf("task1")
        val batches = linkedMapOf("areaA" to emptyList<String>(), "areaB" to nonEmpty)
        assertEquals(nonEmpty, firstNonEmptyBatch(batches), "Should skip the empty entry and return the first non-empty one")
    }

    @Test
    fun firstNonEmptyBatch_multipleNonEmpty_returnsFirst() {
        val first = listOf("taskA")
        val second = listOf("taskB", "taskC")
        val batches = linkedMapOf("area1" to first, "area2" to second)
        assertEquals(first, firstNonEmptyBatch(batches))
    }

    @Test
    fun firstNonEmptyBatch_mixedEmptyAndNonEmpty_returnsFirstNonEmpty() {
        val nonEmpty = listOf("x", "y", "z")
        val batches = linkedMapOf(
            "alpha" to emptyList<String>(),
            "beta" to emptyList(),
            "gamma" to nonEmpty,
            "delta" to listOf("w"),
        )
        assertEquals(nonEmpty, firstNonEmptyBatch(batches))
    }

    // ------------------------------------------------------------------
    // Safe area-name lookup (mirrors the fixed firstOrNull()?.node?.areaId logic)
    // ------------------------------------------------------------------

    private data class FakeNode(val areaId: Long?)
    private data class FakeNodeWithPin(val node: FakeNode)

    @Test
    fun areaIdLookup_nonEmptyBatch_returnsFirstAreaId() {
        val batch = listOf(FakeNodeWithPin(FakeNode(areaId = 42L)), FakeNodeWithPin(FakeNode(areaId = 7L)))
        val areaId = batch.firstOrNull()?.node?.areaId
        assertEquals(42L, areaId)
    }

    @Test
    fun areaIdLookup_emptyBatch_returnsNull() {
        val batch = emptyList<FakeNodeWithPin>()
        val areaId = batch.firstOrNull()?.node?.areaId
        assertNull(areaId, "An empty batch should produce a null areaId, not throw")
    }

    @Test
    fun areaIdLookup_batchWithNullAreaId_returnsNull() {
        val batch = listOf(FakeNodeWithPin(FakeNode(areaId = null)))
        val areaId = batch.firstOrNull()?.node?.areaId
        assertNull(areaId)
    }

    @Test
    fun areaName_fallsBackToGeneral_whenAreaNotFound() {
        val batch = listOf(FakeNodeWithPin(FakeNode(areaId = 99L)))
        val areas = listOf<Pair<Long, String>>() // no areas registered
        val areaName = areas.find { it.first == batch.firstOrNull()?.node?.areaId }?.second ?: "GENERAL"
        assertEquals("GENERAL", areaName)
    }

    @Test
    fun areaName_usesAreaTitle_whenAreaFound() {
        val batch = listOf(FakeNodeWithPin(FakeNode(areaId = 5L)))
        val areas = listOf(5L to "WORK", 6L to "HOME")
        val areaId = batch.firstOrNull()?.node?.areaId
        val areaName = areas.find { it.first == areaId }?.second ?: "GENERAL"
        assertEquals("WORK", areaName)
    }
}