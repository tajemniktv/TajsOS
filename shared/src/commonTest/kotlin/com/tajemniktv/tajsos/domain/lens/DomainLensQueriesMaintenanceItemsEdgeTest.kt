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
    fun financeMaintenanceItems_filters_null_maintenanceType() {
        val financeBill = createMaintenanceItem(1, "bill")
        val nullTypeItem = createMaintenanceItem(2, null)

        val snapshot = MaintenanceSnapshot(
            active = listOf(financeBill, nullTypeItem),
            recurring = emptyList(),
            overdue = emptyList()
        )

        val result = DomainLensQueries.financeMaintenanceItems(snapshot)
        assertEquals(1, result.size)
        assertEquals(1L, result.first().node.node.id)
    }

    @Test
    fun financeMaintenanceItems_filters_by_maintenance_type() {
        val financeBill = createMaintenanceItem(1, "bill")
        val financeSubscription = createMaintenanceItem(2, "subscription")
        val financeRenewal = createMaintenanceItem(3, "renewal")
        val healthPrescription = createMaintenanceItem(4, "prescription")
        val unknownType = createMaintenanceItem(5, "unknown_type")

        // Add recurring items
        val financeRecurringBill = createMaintenanceItem(6, "bill")
        val healthRecurringPrescription = createMaintenanceItem(7, "prescription")

        // Add overdue items
        val financeOverdueRenewal = createMaintenanceItem(8, "renewal")
        val unknownType = createMaintenanceItem(5, "unknown_type")
        val nullType = createMaintenanceItem(6, null)

        // They are all active
        val snapshot = MaintenanceSnapshot(
            active = listOf(financeBill, financeSubscription, financeRenewal, healthPrescription, unknownType, nullType),
            recurring = emptyList(),
            overdue = emptyList()
        )

        val activeResult = DomainLensQueries.financeMaintenanceItems(snapshot)
        assertEquals(3, activeResult.size)
        assertEquals(setOf(1L, 2L, 3L), activeResult.map { it.node.node.id }.toSet())

        val recurringResult = DomainLensQueries.financeRecurringItems(snapshot)
        assertEquals(1, recurringResult.size)
        assertEquals(setOf(6L), recurringResult.map { it.node.node.id }.toSet())

        val overdueResult = DomainLensQueries.financeOverdueItems(snapshot)
        assertEquals(2, overdueResult.size)
        assertEquals(setOf(8L, 9L), overdueResult.map { it.node.node.id }.toSet())
    }

    @Test
    fun healthMaintenanceItems_filters_by_maintenance_type() {
        val financeBill = createMaintenanceItem(1, "bill")
        val healthAppointment = createMaintenanceItem(2, "appointment")
        val healthPrescription = createMaintenanceItem(3, "prescription")
        val healthMedRefill = createMaintenanceItem(4, "med_refill")
        val unknownType = createMaintenanceItem(5, "unknown_type")

        // Add recurring items
        val healthRecurringAppointment = createMaintenanceItem(6, "appointment")
        val financeRecurringBill = createMaintenanceItem(7, "bill")
        val unknownRecurringType = createMaintenanceItem(8, "unknown_type")

        // Add overdue items
        val healthOverduePrescription = createMaintenanceItem(9, "prescription")
        val healthOverdueMedRefill = createMaintenanceItem(10, "med_refill")
        val unknownType = createMaintenanceItem(5, "unknown_type")
        val nullType = createMaintenanceItem(6, null)

        // They are all active
        val snapshot = MaintenanceSnapshot(
            active = listOf(financeBill, healthAppointment, healthPrescription, healthMedRefill, unknownType, nullType),
            recurring = emptyList(),
            overdue = emptyList()
        )

        val activeResult = DomainLensQueries.healthMaintenanceItems(snapshot)
        assertEquals(3, activeResult.size)
        assertEquals(setOf(2L, 3L, 4L), activeResult.map { it.node.node.id }.toSet())

        val recurringResult = DomainLensQueries.healthRecurringItems(snapshot)
        assertEquals(1, recurringResult.size)
        assertEquals(setOf(6L), recurringResult.map { it.node.node.id }.toSet())

        val overdueResult = DomainLensQueries.healthOverdueItems(snapshot)
        assertEquals(2, overdueResult.size)
        assertEquals(setOf(9L, 10L), overdueResult.map { it.node.node.id }.toSet())
    }
}
