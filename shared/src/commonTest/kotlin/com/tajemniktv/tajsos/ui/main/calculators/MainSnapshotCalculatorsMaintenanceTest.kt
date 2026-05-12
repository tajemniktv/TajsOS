/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.main.calculators

import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock

@Suppress("TestMethodWithoutAssertion")
class MainSnapshotCalculatorsMaintenanceTest {

    private fun defaultMaintenanceNode(id: Long): NodeEntity {
        return NodeEntity(
            id = id,
            title = "Test Maintenance Node $id",
            type = "maintenance",
            status = "active"
        )
    }

    private fun NodeEntity.toPin(): NodeWithPin {
        return NodeWithPin(node = this, pin = null, tags = emptyList())
    }

    @Test
    fun calculateMaintenanceSnapshot_excludesNonMaintenanceOrInactive() {
        val now = Clock.System.now().toEpochMilliseconds()
        val inactiveMaintenance = defaultMaintenanceNode(1L).copy(status = "done").toPin()
        val activeTask = defaultMaintenanceNode(2L).copy(type = "task", title = "").toPin()

        val snapshot = calculateMaintenanceSnapshot(listOf(inactiveMaintenance, activeTask))

        assertTrue(snapshot.active.isEmpty())
        assertTrue(snapshot.recurring.isEmpty())
        assertTrue(snapshot.overdue.isEmpty())
        assertTrue(snapshot.byType.isEmpty())
        assertTrue(snapshot.byArea.isEmpty())
    }

    @Test
    fun calculateMaintenanceSnapshot_calculatesUrgencyAndDebtCorrectly() {
        val now = Clock.System.now().toEpochMilliseconds()
        val dayMs = 24 * 60 * 60 * 1000L

        // critical: past due
        val criticalOverdue = defaultMaintenanceNode(1L).copy(dueAt = now - dayMs).toPin()

        // critical: due within 24h
        val criticalSoon = defaultMaintenanceNode(2L).copy(dueAt = now + (dayMs / 2)).toPin()

        // high: specific type and due within 3 days
        val highPrescription = defaultMaintenanceNode(3L).copy(maintenanceType = "prescription", dueAt = now + 2 * dayMs).toPin()

        // high: due within 3 days (not special type)
        val highNormal = defaultMaintenanceNode(4L).copy(dueAt = now + 2 * dayMs).toPin()

        // medium: due within 7 days
        val mediumNormal = defaultMaintenanceNode(5L).copy(dueAt = now + 5 * dayMs).toPin()

        // low: due after 7 days
        val lowNormal = defaultMaintenanceNode(6L).copy(dueAt = now + 10 * dayMs).toPin()

        // no due date
        val noDue = defaultMaintenanceNode(7L).toPin()

        val snapshot = calculateMaintenanceSnapshot(listOf(
            criticalOverdue, criticalSoon, highPrescription, highNormal, mediumNormal, lowNormal, noDue
        ))

        assertEquals(7, snapshot.active.size)

        // Test sorted descending by urgency severity
        assertEquals("critical", snapshot.active[0].urgency)
        assertEquals("critical", snapshot.active[1].urgency)
        assertEquals("high", snapshot.active[2].urgency)
        assertEquals("high", snapshot.active[3].urgency)
        assertEquals("medium", snapshot.active[4].urgency)
        assertEquals("low", snapshot.active[5].urgency)
        assertEquals("low", snapshot.active[6].urgency)

        // adminDebtMeter = (activeItems.size * 4) + (overdue.size * 12) + (critical.size * 18)
        // active: 7 -> 28
        // overdue: 1 (overdueDays > 0) + 1 (urgency = critical but not overdueDays > 0 because dueAt >= now) -> 2 items -> 24
        // critical: 2 -> 36
        // Total = 28 + 24 + 36 = 88
        val expectedDebt = (7 * 4) + (2 * 12) + (2 * 18)

        assertEquals(2, snapshot.overdue.size)
        assertTrue(snapshot.adminDebtMeter in 0..100) // 88 is in range
        assertEquals(88, snapshot.adminDebtMeter)

        // Check overdue warning
        assertEquals("ADMIN DEBT HIGH // REDUCE RISK ITEMS", snapshot.overdueWarning)
    }

    @Test
    fun calculateMaintenanceSnapshot_computesRecurring() {
        val recurringNode1 = defaultMaintenanceNode(1L).copy(isRecurring = true).toPin()
        val recurringNode2 = defaultMaintenanceNode(2L).copy(maintenanceInterval = "weekly").toPin()
        val normalNode = defaultMaintenanceNode(3L).toPin()

        val snapshot = calculateMaintenanceSnapshot(listOf(recurringNode1, recurringNode2, normalNode))

        assertEquals(2, snapshot.recurring.size)
        assertEquals(3, snapshot.active.size)
    }

    @Test
    fun calculateMaintenanceSnapshot_groupsByTypeAndArea() {
        val node1 = defaultMaintenanceNode(1L).copy(maintenanceType = "bill", areaId = 10L).toPin()
        val node2 = defaultMaintenanceNode(2L).copy(maintenanceType = "bill", areaId = 10L).toPin()
        val node3 = defaultMaintenanceNode(3L).copy(maintenanceType = "form", areaId = 20L).toPin()
        val node4 = defaultMaintenanceNode(4L).copy(maintenanceType = null, areaId = null).toPin()

        val snapshot = calculateMaintenanceSnapshot(listOf(node1, node2, node3, node4))

        assertEquals(2, snapshot.byType["bill"]?.size)
        assertEquals(1, snapshot.byType["form"]?.size)
        assertEquals(1, snapshot.byType["manual"]?.size)

        assertEquals(2, snapshot.byArea[10L]?.size)
        assertEquals(1, snapshot.byArea[20L]?.size)
        assertEquals(1, snapshot.byArea[null]?.size)
    }

    @Test
    fun maintenanceUrgency_computesCorrectly() {
        val now = Clock.System.now().toEpochMilliseconds()
        val dayMs = 24 * 60 * 60 * 1000L

        assertEquals("critical", maintenanceUrgency(NodeEntity(id = 1, title = "t", type = "maintenance", dueAt = now - dayMs), now))
        assertEquals("critical", maintenanceUrgency(NodeEntity(id = 1, title = "t", type = "maintenance", maintenanceOverdueAt = now - dayMs), now))
        assertEquals("critical", maintenanceUrgency(NodeEntity(id = 1, title = "t", type = "maintenance", dueAt = now + (dayMs / 2)), now))
        assertEquals("high", maintenanceUrgency(NodeEntity(id = 1, title = "t", type = "maintenance", maintenanceType = "prescription", dueAt = now + 2 * dayMs), now))
        assertEquals("high", maintenanceUrgency(NodeEntity(id = 1, title = "t", type = "maintenance", dueAt = now + 2 * dayMs), now))
        assertEquals("medium", maintenanceUrgency(NodeEntity(id = 1, title = "t", type = "maintenance", dueAt = now + 5 * dayMs), now))
        assertEquals("low", maintenanceUrgency(NodeEntity(id = 1, title = "t", type = "maintenance", dueAt = now + 10 * dayMs), now))
        assertEquals("low", maintenanceUrgency(NodeEntity(id = 1, title = "t", type = "maintenance"), now))
    }
}
