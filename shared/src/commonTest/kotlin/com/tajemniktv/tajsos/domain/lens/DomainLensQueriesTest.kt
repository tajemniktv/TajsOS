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
    private fun createTestNode(
        id: Long,
        type: String,
        title: String,
        dueAt: Long? = null,
        updatedAt: Long = 0L,
        noteType: String? = null,
        maintenanceType: String? = null,
        tags: List<String> = emptyList(),
    ): NodeWithPin {
        return NodeWithPin(
            node =
                NodeEntity(
                    id = id,
                    type = type,
                    title = title,
                    dueAt = dueAt,
                    updatedAt = updatedAt,
                    noteType = noteType,
                    maintenanceType = maintenanceType,
                ),
            pin = null,
            tags = tags.mapIndexed { index, tag -> TagEntity(id = index.toLong(), name = tag, normalizedName = tag.lowercase()) },
        )
    }

    @Test
    fun financeQueries_include_actions_knowledge_deadlines_and_maintenance_without_resource_types() {
        val financeTask = createTestNode(id = 10, type = "task", title = "Pay rent", dueAt = 2_000L, tags = listOf("finance"))
        val financeNote = createTestNode(id = 11, type = "note", title = "Insurance policy reference", noteType = "reference", updatedAt = 2_000L, tags = listOf("insurance"))
        val financeDeadline = createTestNode(id = 12, type = "note", title = "Tax filing deadline", dueAt = 1_000L, updatedAt = 1_000L)
        val unrelatedRecord = createTestNode(id = 13, type = "record", title = "Therapy reflection")

        val maintenanceItem =
            MaintenanceStatusItem(
                node = createTestNode(id = 14, type = "maintenance", title = "Renew bank card", maintenanceType = "renewal"),
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
        val healthTask = createTestNode(id = 1, type = "task", title = "Book doctor appointment", tags = listOf("health"))
        val healthRecord = createTestNode(id = 2, type = "record", title = "Symptom log", updatedAt = 2_000L, tags = listOf("symptom"))
        val unrelatedNote = createTestNode(id = 3, type = "note", title = "Design references")

        val maintenanceItem =
            MaintenanceStatusItem(
                node = createTestNode(id = 4, type = "maintenance", title = "Refill prescription", maintenanceType = "prescription"),
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
