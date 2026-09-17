package com.tajemniktv.tajos.domain.lens

import com.tajemniktv.tajos.data.NodeEntity
import com.tajemniktv.tajos.data.NodeWithPin
import com.tajemniktv.tajos.ui.MaintenanceSnapshot
import com.tajemniktv.tajos.ui.MaintenanceStatusItem
import kotlin.test.Test
import kotlin.test.assertEquals

class DomainLensQueriesRecurringItemsEdgeTest {
    private fun createMaintenanceItem(
        id: Long,
        maintenanceType: String?,
        title: String = "Test Item"
    ): MaintenanceStatusItem {
        return MaintenanceStatusItem(
            node = NodeWithPin(
                node = NodeEntity(
                    id = id,
                    title = title,
                    type = "maintenance",
                    maintenanceType = maintenanceType,
                    status = "active"
                ),
                pin = null,
                tags = emptyList()
            ),
            urgency = "high",
            isRecurring = true
        )
    }

    @Test
    fun financeRecurringItems_filters_by_maintenance_type() {
        val financeBill = createMaintenanceItem(1, "bill")
        val financeSubscription = createMaintenanceItem(2, "subscription")
        val healthPrescription = createMaintenanceItem(3, "prescription")
        val unknownType = createMaintenanceItem(4, "unknown_type")

        // They are all recurring
        val snapshot = MaintenanceSnapshot(
            active = emptyList(),
            recurring = listOf(financeBill, financeSubscription, healthPrescription, unknownType),
            overdue = emptyList()
        )

        val result = DomainLensQueries.financeRecurringItems(snapshot)
        assertEquals(2, result.size)
        assertEquals(setOf(1L, 2L), result.map { it.node.node.id }.toSet())
    }

    @Test
    fun healthRecurringItems_filters_by_maintenance_type() {
        val financeBill = createMaintenanceItem(1, "bill")
        val healthAppointment = createMaintenanceItem(2, "appointment")
        val healthPrescription = createMaintenanceItem(3, "prescription")
        val unknownType = createMaintenanceItem(4, "unknown_type")

        // They are all recurring
        val snapshot = MaintenanceSnapshot(
            active = emptyList(),
            recurring = listOf(financeBill, healthAppointment, healthPrescription, unknownType),
            overdue = emptyList()
        )

        val result = DomainLensQueries.healthRecurringItems(snapshot)
        assertEquals(2, result.size)
        assertEquals(setOf(2L, 3L), result.map { it.node.node.id }.toSet())
    }
}
