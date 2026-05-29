package com.tajemniktv.tajsos.domain.lens

import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.ui.MaintenanceSnapshot
import com.tajemniktv.tajsos.ui.MaintenanceStatusItem
import kotlin.test.Test
import kotlin.test.assertEquals

class DomainLensQueriesActiveMaintenanceItemsEdgeTest {
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
            isRecurring = false
        )
    }

    @Test
    fun financeMaintenanceItems_filters_by_maintenance_type_from_active_only() {
        val financeBill = createMaintenanceItem(1, "bill")
        val financeSubscription = createMaintenanceItem(2, "subscription")
        val healthPrescription = createMaintenanceItem(3, "prescription")
        val unknownType = createMaintenanceItem(4, "unknown_type")

        // Place matching types in overdue and recurring, but they shouldn't be included if they aren't in 'active'
        val overdueFinance = createMaintenanceItem(5, "bill")
        val recurringFinance = createMaintenanceItem(6, "subscription")

        val snapshot = MaintenanceSnapshot(
            active = listOf(financeBill, financeSubscription, healthPrescription, unknownType),
            recurring = listOf(recurringFinance),
            overdue = listOf(overdueFinance)
        )

        val result = DomainLensQueries.financeMaintenanceItems(snapshot)
        assertEquals(2, result.size)
        assertEquals(setOf(1L, 2L), result.map { it.node.node.id }.toSet())
    }

    @Test
    fun healthMaintenanceItems_filters_by_maintenance_type_from_active_only() {
        val financeBill = createMaintenanceItem(1, "bill")
        val healthAppointment = createMaintenanceItem(2, "appointment")
        val healthPrescription = createMaintenanceItem(3, "prescription")
        val unknownType = createMaintenanceItem(4, "unknown_type")

        // Place matching types in overdue and recurring, but they shouldn't be included if they aren't in 'active'
        val overdueHealth = createMaintenanceItem(5, "appointment")
        val recurringHealth = createMaintenanceItem(6, "prescription")

        val snapshot = MaintenanceSnapshot(
            active = listOf(financeBill, healthAppointment, healthPrescription, unknownType),
            recurring = listOf(recurringHealth),
            overdue = listOf(overdueHealth)
        )

        val result = DomainLensQueries.healthMaintenanceItems(snapshot)
        assertEquals(2, result.size)
        assertEquals(setOf(2L, 3L), result.map { it.node.node.id }.toSet())
    }
}
