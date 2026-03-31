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
    fun financeQueries_include_actions_knowledge_deadlines_and_maintenance_without_resource_types() {
        val financeTask =
            NodeWithPin(
                node =
                    NodeEntity(
                        id = 10,
                        type = "task",
                        title = "Pay rent",
                        dueAt = 2_000L,
                    ),
                pin = null,
                tags = listOf(TagEntity(id = 10, name = "finance", normalizedName = "finance")),
            )
        val financeNote =
            NodeWithPin(
                node =
                    NodeEntity(
                        id = 11,
                        type = "note",
                        title = "Insurance policy reference",
                        noteType = "reference",
                    ),
                pin = null,
                tags = listOf(TagEntity(id = 11, name = "insurance", normalizedName = "insurance")),
            )
        val financeDeadline =
            NodeWithPin(
                node =
                    NodeEntity(
                        id = 12,
                        type = "note",
                        title = "Tax filing deadline",
                        dueAt = 1_000L,
                    ),
                pin = null,
                tags = emptyList(),
            )
        val unrelatedRecord =
            NodeWithPin(
                node = NodeEntity(id = 13, type = "record", title = "Therapy reflection"),
                pin = null,
                tags = emptyList(),
            )
        val maintenanceItem =
            MaintenanceStatusItem(
                node =
                    NodeWithPin(
                        node =
                            NodeEntity(
                                id = 14,
                                type = "maintenance",
                                title = "Renew bank card",
                                maintenanceType = "renewal",
                            ),
                        pin = null,
                    ),
                urgency = "medium",
                isRecurring = true,
            )

        val allNodes = listOf(financeTask, financeNote, financeDeadline, unrelatedRecord)
        val snapshot =
            MaintenanceSnapshot(
                active = listOf(maintenanceItem),
                recurring = listOf(maintenanceItem),
                overdue = listOf(maintenanceItem),
            )

        assertEquals(listOf(financeTask.node.id), DomainLensQueries.financeActionItems(allNodes).map { it.node.id })
        assertEquals(listOf(financeNote.node.id, financeDeadline.node.id), DomainLensQueries.financeKnowledgeItems(allNodes).map { it.node.id })
        assertEquals(listOf(financeDeadline.node.id, financeTask.node.id), DomainLensQueries.financeDeadlineItems(allNodes).map { it.node.id })
        assertEquals(listOf(maintenanceItem.node.node.id), DomainLensQueries.financeMaintenanceItems(snapshot).map { it.node.node.id })
        assertEquals(listOf(maintenanceItem.node.node.id), DomainLensQueries.financeRecurringItems(snapshot).map { it.node.node.id })
        assertEquals(listOf(maintenanceItem.node.node.id), DomainLensQueries.financeOverdueItems(snapshot).map { it.node.node.id })
    }

    @Test
    fun healthQueries_include_actions_knowledge_and_maintenance_without_special_domain_types() {
        val healthTask =
            NodeWithPin(
                node = NodeEntity(id = 1, type = "task", title = "Book doctor appointment"),
                pin = null,
                tags = listOf(TagEntity(id = 1, name = "health", normalizedName = "health")),
            )
        val healthRecord =
            NodeWithPin(
                node = NodeEntity(id = 2, type = "record", title = "Symptom log"),
                pin = null,
                tags = listOf(TagEntity(id = 2, name = "symptom", normalizedName = "symptom")),
            )
        val unrelatedNote =
            NodeWithPin(
                node = NodeEntity(id = 3, type = "note", title = "Design references"),
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
