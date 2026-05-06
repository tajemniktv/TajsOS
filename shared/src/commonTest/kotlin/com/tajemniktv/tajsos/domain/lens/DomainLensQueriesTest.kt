/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.domain.lens

import com.tajemniktv.tajsos.data.ItemKind
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.NoteKind
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
        content: String? = null,
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
                    content = content,
                ),
            pin = null,
            tags = tags.mapIndexed { index, tag -> TagEntity(id = index.toLong(), name = tag, normalizedName = tag.lowercase()) },
        )
    }

    @Test
    fun financeQueries_include_actions_knowledge_deadlines_and_maintenance_without_resource_types() {
        val financeTask = createTestNode(id = 10, type = ItemKind.TASK.storageKey, title = "Pay rent", dueAt = 2_000L, tags = listOf("finance"))
        val financeNote = createTestNode(id = 11, type = ItemKind.NOTE.storageKey, title = "Insurance policy reference", noteType = NoteKind.REFERENCE.storageKey, updatedAt = 2_000L, tags = listOf("insurance"))
        val financeDeadline = createTestNode(id = 12, type = ItemKind.NOTE.storageKey, title = "Tax filing deadline", dueAt = 1_000L, updatedAt = 1_000L)
        val unrelatedRecord = createTestNode(id = 13, type = ItemKind.RECORD.storageKey, title = "Therapy reflection")

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
        val healthTask = createTestNode(id = 1, type = ItemKind.TASK.storageKey, title = "Book doctor appointment", tags = listOf("health"))
        val healthRecord = createTestNode(id = 2, type = ItemKind.RECORD.storageKey, title = "Symptom log", updatedAt = 2_000L, tags = listOf("symptom"))
        val unrelatedNote = createTestNode(id = 3, type = ItemKind.NOTE.storageKey, title = "Design references")

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

    @Test
    fun financeQueries_heuristics_edge_cases() {
        // financeTitleKeywords includes "budget", "paycheck"
        val titleMatch = createTestNode(id = 1, type = ItemKind.TASK.storageKey, title = "Update monthly budget")
        val contentMatch = createTestNode(id = 2, type = ItemKind.TASK.storageKey, title = "Task", updatedAt = 1_000L, content = "Got my paycheck today")
        val refNoteTitleMatch = createTestNode(id = 3, type = ItemKind.NOTE.storageKey, title = "budget", noteType = NoteKind.REFERENCE.storageKey, updatedAt = 1_000L)
        val refNoteTagMatch = createTestNode(id = 4, type = ItemKind.NOTE.storageKey, title = "Ref", noteType = NoteKind.REFERENCE.storageKey, tags = listOf("finance"), updatedAt = 1_000L)
        val nonMatchingRefNote = createTestNode(id = 5, type = ItemKind.NOTE.storageKey, title = "General Info", noteType = NoteKind.REFERENCE.storageKey)
        val unrelatedTask = createTestNode(id = 6, type = ItemKind.TASK.storageKey, title = "Walk the dog")

        val allNodes = listOf(titleMatch, contentMatch, refNoteTitleMatch, refNoteTagMatch, nonMatchingRefNote, unrelatedTask)

        // Active tasks matching finance heuristics
        val actionItems = DomainLensQueries.financeActionItems(allNodes).map { it.node.id }
        assertEquals(listOf(1L, 2L).sorted(), actionItems.sorted())

        // Active knowledge matching finance heuristics
        val knowledgeItems = DomainLensQueries.financeKnowledgeItems(allNodes).map { it.node.id }
        assertEquals(listOf(3L, 4L), knowledgeItems.sorted()) // using sorted for stable assertion if updated_at is same
    }

    @Test
    fun healthQueries_heuristics_edge_cases() {
        // healthTitleKeywords includes "doctor", "symptom"
        val titleMatch = createTestNode(id = 1, type = ItemKind.TASK.storageKey, title = "See the doctor")
        val contentMatch = createTestNode(id = 2, type = ItemKind.TASK.storageKey, title = "Task", updatedAt = 1_000L, content = "Feeling a symptom")
        val reflectionNote = createTestNode(id = 3, type = ItemKind.RECORD.storageKey, title = "Daily", noteType = NoteKind.REFLECTION.storageKey, updatedAt = 1_000L)
        val journalNote = createTestNode(id = 4, type = ItemKind.RECORD.storageKey, title = "Log", noteType = NoteKind.JOURNAL.storageKey, updatedAt = 1_000L)
        val unrelatedTask = createTestNode(id = 5, type = ItemKind.TASK.storageKey, title = "Buy groceries")

        val allNodes = listOf(titleMatch, contentMatch, reflectionNote, journalNote, unrelatedTask)

        val actionItems = DomainLensQueries.healthActionItems(allNodes).map { it.node.id }
        assertEquals(listOf(1L, 2L).sorted(), actionItems.sorted())

        val knowledgeItems = DomainLensQueries.healthKnowledgeItems(allNodes).map { it.node.id }
        assertEquals(listOf(3L, 4L), knowledgeItems.sorted())
    }

}
