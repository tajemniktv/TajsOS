/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */
package com.tajemniktv.tajos.ui.main.calculators

import com.tajemniktv.tajos.data.NodeEntity
import com.tajemniktv.tajos.data.NodeWithPin
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class TimeArchitectureSnapshotTest {
    private fun millis(value: String) = Instant.parse(value).toEpochMilliseconds()
    private fun task(id: Long, dueAt: Long?, status: String = "active") =
        NodeWithPin(NodeEntity(id = id, title = "Task $id", type = "task", status = status, dueAt = dueAt), null)

    @Test
    fun rollingHorizonsAreInclusiveAndTodayUsesExplicitPayloadNotDueDate() {
        val now = millis("2026-12-31T12:00:00Z")
        val day = 86400000L
        val nodes = listOf(
            task(1, now - 1), task(2, now), task(3, now + 7 * day),
            task(4, now + 7 * day + 1), task(5, now + 30 * day),
            task(6, now + 30 * day + 1), task(7, now + 120 * day),
            task(8, now + 120 * day + 1), task(9, null), task(10, now, "done"),
        )
        val result = calculateTimeArchitectureSnapshot(nodes, listOf(nodes[8].node, nodes[9].node), emptyList(), now, TimeZone.UTC)
        assertEquals(listOf(9L), result.todayLayer.map { it.node.id })
        assertEquals(listOf(2L, 3L), result.weekLayer.map { it.node.id })
        assertEquals(listOf(2L, 3L, 4L, 5L), result.monthLayer.map { it.node.id })
        assertEquals(listOf(2L, 3L, 4L, 5L, 6L, 7L), result.semesterLayer.map { it.node.id })
        assertEquals("2027-01-01", result.monthlyResetDate)
    }

    @Test
    fun weeklyMapUsesLocalDatesAcrossBothDstTransitions() {
        val zone = TimeZone.of("Europe/Warsaw")
        for ((start, times) in listOf(
            "2026-03-28T12:00:00Z" to listOf("2026-03-28T23:00:00Z", "2026-03-29T00:30:00Z", "2026-03-29T01:30:00Z"),
            "2026-10-24T12:00:00Z" to listOf("2026-10-24T22:00:00Z", "2026-10-25T00:30:00Z", "2026-10-25T01:30:00Z"),
        )) {
            val nodes = times.mapIndexed { index, time -> task(index.toLong() + 1, millis(time)) }
            val result = calculateTimeArchitectureSnapshot(nodes, emptyList(), emptyList(), millis(start), zone)
            assertEquals(mapOf("SUNDAY" to 3), result.weeklyMap)
        }
    }

    @Test
    fun nextResetIsAlwaysFirstOfNextLocalMonthIncludingLeapAndYearBoundaries() {
        val zone = TimeZone.of("Europe/Warsaw")
        for ((instant, expected) in listOf(
            "2024-01-31T12:00:00Z" to "2024-02-01",
            "2024-02-29T12:00:00Z" to "2024-03-01",
            "2026-10-31T22:59:59.999Z" to "2026-11-01",
            "2026-10-31T23:00:00Z" to "2026-12-01",
            "2026-12-31T23:00:00Z" to "2027-02-01",
        )) assertEquals(expected, nextMonthlyResetDate(Instant.parse(instant), zone), instant)
    }
}
