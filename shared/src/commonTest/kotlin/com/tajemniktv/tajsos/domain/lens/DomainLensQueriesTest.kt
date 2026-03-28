/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.domain.lens

import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.TagEntity
import com.tajemniktv.tajsos.ui.MaintenanceSnapshot
import com.tajemniktv.tajsos.ui.MaintenanceStatusItem
import kotlin.test.Test
import kotlin.test.assertEquals

class DomainLensQueriesTest {
    @Test
    fun healthQueries_include_actions_knowledge_and_maintenance_without_special_domain_types() {
        val healthTask =
            NodeWithPin(
                node = NodeEntity(id = 1, type = "task", title = "Book doctor appointment", status = "active"),
                pin = null,
                tags = listOf(TagEntity(id = 1, name = "health", normalizedName = "health")),
            )
        val healthRecord =
            NodeWithPin(
                node = NodeEntity(id = 2, type = "record", title = "Symptom log", status = "active"),
                pin = null,
                tags = listOf(TagEntity(id = 2, name = "symptom", normalizedName = "symptom")),
            )
        val unrelatedNote =
            NodeWithPin(
                node = NodeEntity(id = 3, type = "note", title = "Design references", status = "active"),
                pin = null,
                tags = emptyList(),
            )
        val maintenanceItem =
            MaintenanceStatusItem(
                node =
                    NodeWithPin(
                        node =
                            NodeEntity(
                                id = 4,
                                type = "maintenance",
                                title = "Refill prescription",
                                status = "active",
                                maintenanceType = "prescription",
                            ),
                        pin = null,
                    ),
                urgency = "high",
                isRecurring = true,
            )

        val allNodes = listOf(healthTask, healthRecord, unrelatedNote)
        val snapshot = MaintenanceSnapshot(active = listOf(maintenanceItem), overdue = listOf(maintenanceItem))

        assertEquals(listOf(healthTask.node.id), DomainLensQueries.healthActionItems(allNodes).map { it.node.id })
        assertEquals(listOf(healthRecord.node.id), DomainLensQueries.healthKnowledgeItems(allNodes).map { it.node.id })
        assertEquals(listOf(maintenanceItem.node.node.id), DomainLensQueries.healthMaintenanceItems(snapshot).map { it.node.node.id })
        assertEquals(listOf(maintenanceItem.node.node.id), DomainLensQueries.healthOverdueItems(snapshot).map { it.node.node.id })
    }
}
