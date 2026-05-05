package com.tajemniktv.tajsos.domain.lens

import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.ui.MaintenanceSnapshot
import com.tajemniktv.tajsos.ui.MaintenanceStatusItem
import kotlin.test.Test
import kotlin.test.assertEquals

class DomainLensQueriesMaintenanceItemsEdgeTest {
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
    fun financeMaintenanceItems_filters_by_maintenance_type() {
        val financeBill = createMaintenanceItem(1, "bill")
        val financeSubscription = createMaintenanceItem(2, "subscription")
        val financeRenewal = createMaintenanceItem(3, "renewal")
        val healthPrescription = createMaintenanceItem(4, "prescription")
        val unknownType = createMaintenanceItem(5, "unknown_type")

        // They are all active
        val snapshot = MaintenanceSnapshot(
            active = listOf(financeBill, financeSubscription, financeRenewal, healthPrescription, unknownType),
            recurring = emptyList(),
            overdue = emptyList()
        )

        val result = DomainLensQueries.financeMaintenanceItems(snapshot)
        assertEquals(3, result.size)
        assertEquals(setOf(1L, 2L, 3L), result.map { it.node.node.id }.toSet())
    }

    @Test
    fun healthMaintenanceItems_filters_by_maintenance_type() {
        val financeBill = createMaintenanceItem(1, "bill")
        val healthAppointment = createMaintenanceItem(2, "appointment")
        val healthPrescription = createMaintenanceItem(3, "prescription")
        val healthMedRefill = createMaintenanceItem(4, "med_refill")
        val unknownType = createMaintenanceItem(5, "unknown_type")

        // They are all active
        val snapshot = MaintenanceSnapshot(
            active = listOf(financeBill, healthAppointment, healthPrescription, healthMedRefill, unknownType),
            recurring = emptyList(),
            overdue = emptyList()
        )

        val result = DomainLensQueries.healthMaintenanceItems(snapshot)
        assertEquals(3, result.size)
        assertEquals(setOf(2L, 3L, 4L), result.map { it.node.node.id }.toSet())
    }
}
