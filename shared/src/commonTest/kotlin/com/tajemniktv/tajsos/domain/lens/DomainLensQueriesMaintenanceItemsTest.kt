package com.tajemniktv.tajsos.domain.lens

import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.TagEntity
import com.tajemniktv.tajsos.ui.MaintenanceSnapshot
import com.tajemniktv.tajsos.ui.MaintenanceStatusItem
import kotlin.test.Test
import kotlin.test.assertEquals

class DomainLensQueriesMaintenanceItemsTest {

    private fun createMaintenanceItem(
        id: Long,
        maintenanceType: String?,
        title: String = "Test Item",
        tags: List<String> = emptyList()
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
                tags = tags.mapIndexed { index, tag -> TagEntity(id = index.toLong(), name = tag, normalizedName = tag.lowercase()) }
            ),
            urgency = "high",
            isRecurring = true
        )
    }

    @Test
    fun financeMaintenanceItems_filters_by_maintenance_type() {
        val financeBill = createMaintenanceItem(1, "bill", tags = listOf("finance"))
        val healthAppointment = createMaintenanceItem(2, "appointment", tags = listOf("health"))

        val snapshot = MaintenanceSnapshot(
            active = listOf(financeBill, healthAppointment),
            recurring = emptyList(),
            overdue = emptyList()
        )

        val result = DomainLensQueries.financeMaintenanceItems(snapshot)
        assertEquals(1, result.size)
        assertEquals(1L, result.first().node.node.id)
    }

    @Test
    fun healthMaintenanceItems_filters_by_maintenance_type() {
        val financeBill = createMaintenanceItem(1, "bill", tags = listOf("finance"))
        val healthAppointment = createMaintenanceItem(2, "appointment", tags = listOf("health"))

        val snapshot = MaintenanceSnapshot(
            active = listOf(financeBill, healthAppointment),
            recurring = emptyList(),
            overdue = emptyList()
        )

        val result = DomainLensQueries.healthMaintenanceItems(snapshot)
        assertEquals(1, result.size)
        assertEquals(2L, result.first().node.node.id)
    }
}
