package com.tajemniktv.tajsos.domain.lens

import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.ui.MaintenanceSnapshot
import com.tajemniktv.tajsos.ui.MaintenanceStatusItem
import kotlin.test.Test
import kotlin.test.assertEquals

class DomainLensQueriesOverdueItemsEdgeTest {
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
    fun financeOverdueItems_filters_by_maintenance_type() {
        val financeBill = createMaintenanceItem(1, "bill")
        val financeSubscription = createMaintenanceItem(2, "subscription")
        val healthPrescription = createMaintenanceItem(3, "prescription")
        val unknownType = createMaintenanceItem(4, "unknown_type")

        // They are all overdue
        val snapshot = MaintenanceSnapshot(
            active = emptyList(),
            recurring = emptyList(),
            overdue = listOf(financeBill, financeSubscription, healthPrescription, unknownType)
        )

        val result = DomainLensQueries.financeOverdueItems(snapshot)
        assertEquals(2, result.size)
        assertEquals(listOf(1L, 2L).sorted(), result.map { it.node.node.id }.sorted())
    }

    @Test
    fun healthOverdueItems_filters_by_maintenance_type() {
        val financeBill = createMaintenanceItem(1, "bill")
        val healthAppointment = createMaintenanceItem(2, "appointment")
        val healthPrescription = createMaintenanceItem(3, "prescription")
        val unknownType = createMaintenanceItem(4, "unknown_type")

        // They are all overdue
        val snapshot = MaintenanceSnapshot(
            active = emptyList(),
            recurring = emptyList(),
            overdue = listOf(financeBill, healthAppointment, healthPrescription, unknownType)
        )

        val result = DomainLensQueries.healthOverdueItems(snapshot)
        assertEquals(2, result.size)
        assertEquals(listOf(2L, 3L).sorted(), result.map { it.node.node.id }.sorted())
    }
}
