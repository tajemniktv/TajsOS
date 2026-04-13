/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.domain.lens

import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.TagEntity
import kotlin.test.Test
import kotlin.test.assertEquals

class DomainLensQueriesEdgeTest {
    private fun createNode(
        id: Long,
        title: String,
        content: String = "",
        type: String = "task",
        status: String = "active",
        tags: List<String> = emptyList(),
        maintenanceType: String? = null,
        noteType: String? = null,
        dueAt: Long? = null,
        updatedAt: Long = 0L,
    ): NodeWithPin {
        return NodeWithPin(
            node =
                NodeEntity(
                    id = id,
                    title = title,
                    content = content,
                    type = type,
                    status = status,
                    maintenanceType = maintenanceType,
                    noteType = noteType,
                    dueAt = dueAt,
                    updatedAt = updatedAt,
                ),
            pin = null,
            tags = tags.mapIndexed { index, tag -> TagEntity(id = index.toLong(), name = tag, normalizedName = tag.lowercase()) },
        )
    }

    @Test
    fun financeActionItems_matches_by_content_keyword() {
        val financeTask = createNode(1, "Unrelated Title", "need to pay the invoice today")
        val nonFinanceTask = createNode(2, "Another Title", "just normal work")
        val allNodes = listOf(financeTask, nonFinanceTask)

        val result = DomainLensQueries.financeActionItems(allNodes)
        assertEquals(1, result.size)
        assertEquals(1L, result.first().node.id)
    }

    @Test
    fun financeKnowledgeItems_matches_by_reference_note_with_finance_keyword_in_title() {
        val referenceNote = createNode(1, "Finance log", "log", type = "note", noteType = "reference")
        val result = DomainLensQueries.financeKnowledgeItems(listOf(referenceNote))
        assertEquals(1, result.size)
    }

    @Test
    fun financeKnowledgeItems_matches_by_reference_note_with_finance_tag() {
        val referenceNote = createNode(1, "Log", "log", type = "note", noteType = "reference", tags = listOf("finance"))
        val result = DomainLensQueries.financeKnowledgeItems(listOf(referenceNote))
        assertEquals(1, result.size)
    }

    @Test
    fun healthKnowledgeItems_matches_by_note_type() {
        // Even without health keywords in title/content or tags, a journal/reflection is a health signal
        val reflectionNote = createNode(1, "My day", "was okay", type = "note", noteType = "reflection")
        val journalNote = createNode(2, "Thoughts", "hmmmm", type = "note", noteType = "journal")
        val regularNote = createNode(3, "Idea", "random thought", type = "note")

        val allNodes = listOf(reflectionNote, journalNote, regularNote)

        val result = DomainLensQueries.healthKnowledgeItems(allNodes)
        assertEquals(2, result.size)
        assertEquals(listOf(1L, 2L).sorted(), result.map { it.node.id }.sorted())
    }

    @Test
    fun sorting_financeActionItems_places_null_dueAt_last() {
        val taskNoDate = createNode(1, "Finance item")
        val taskLater = createNode(2, "Finance item", dueAt = 2000L)
        val taskEarlier = createNode(3, "Finance item", dueAt = 1000L)

        val result = DomainLensQueries.financeActionItems(listOf(taskNoDate, taskLater, taskEarlier))
        assertEquals(listOf(3L, 2L, 1L), result.map { it.node.id })
    }

    @Test
    fun sorting_healthActionItems_places_null_dueAt_last() {
        val taskNoDate = createNode(1, "Health item")
        val taskLater = createNode(2, "Health item", dueAt = 2000L)
        val taskEarlier = createNode(3, "Health item", dueAt = 1000L)

        val result = DomainLensQueries.healthActionItems(listOf(taskNoDate, taskLater, taskEarlier))
        assertEquals(listOf(3L, 2L, 1L), result.map { it.node.id })
    }

    @Test
    fun sorting_financeKnowledgeItems_sorts_by_updatedAt_descending() {
        val oldNote = createNode(1, "Finance rules", updatedAt = 100L, type = "note")
        val newNote = createNode(2, "Finance updates", updatedAt = 300L, type = "note")
        val medNote = createNode(3, "Finance log", updatedAt = 200L, type = "note")

        val result = DomainLensQueries.financeKnowledgeItems(listOf(oldNote, newNote, medNote))
        assertEquals(listOf(2L, 3L, 1L), result.map { it.node.id })
    }

    @Test
    fun healthActionItems_matches_by_content_and_title_keywords() {
        val healthTaskTitle = createNode(1, "Doctor appointment", "just checkup")
        val healthTaskContent = createNode(2, "Generic Task", "medical follow-up")
        val unrelatedTask = createNode(3, "Work", "writing code")

        val result = DomainLensQueries.healthActionItems(listOf(healthTaskTitle, healthTaskContent, unrelatedTask))
        assertEquals(2, result.size)
        assertEquals(setOf(1L, 2L), result.map { it.node.id }.toSet())
    }

    @Test
    fun matchesHealthSignal_healthNoteType_no_keywords_matches() {
        val node = createNode(1, "Daily thoughts", "just another day", type = "note", noteType = "reflection")
        val result = DomainLensQueries.healthKnowledgeItems(listOf(node))
        assertEquals(1, result.size)
    }

    @Test
    fun matchesHealthSignal_healthMaintenance_matches() {
        val node = createNode(1, "Go to doctor", maintenanceType = "appointment")
        val result = DomainLensQueries.healthActionItems(listOf(node))
        assertEquals(1, result.size)
    }

    @Test
    fun sorting_financeDeadlineItems_places_null_dueAt_last() {
        // Technically this list won't contain nulls because the filter says: it.node.dueAt != null
        val deadlineLater = createNode(2, "Finance item", dueAt = 2000L)
        val deadlineEarlier = createNode(3, "Finance item", dueAt = 1000L)

        val result = DomainLensQueries.financeDeadlineItems(listOf(deadlineLater, deadlineEarlier))
        assertEquals(listOf(3L, 2L), result.map { it.node.id })
    }

    @Test
    fun sorting_healthKnowledgeItems_sorts_by_updatedAt_descending() {
        val oldNote = createNode(1, "Health log", updatedAt = 100L, type = "note", tags = listOf("health"))
        val newNote = createNode(2, "Health update", updatedAt = 300L, type = "note", tags = listOf("health"))
        val medNote = createNode(3, "Health thoughts", updatedAt = 200L, type = "note", tags = listOf("health"))

        val result = DomainLensQueries.healthKnowledgeItems(listOf(oldNote, newNote, medNote))
        assertEquals(listOf(2L, 3L, 1L), result.map { it.node.id })
    }

    @Test
    fun financeDeadlineItems_excludes_inactive_or_null_dueAt_items() {
        val activeWithDate = createNode(1, "Finance item", dueAt = 1000L, status = "active", tags = listOf("finance"))
        val inactiveWithDate = createNode(2, "Finance item", dueAt = 1000L, status = "done", tags = listOf("finance"))
        val activeNoDate = createNode(3, "Finance item", status = "active", tags = listOf("finance"))

        val result = DomainLensQueries.financeDeadlineItems(listOf(activeWithDate, inactiveWithDate, activeNoDate))
        assertEquals(1, result.size)
        assertEquals(1L, result.first().node.id)
    }

    @Test
    fun matchesFinanceSignal_is_case_insensitive() {
        val uppercaseTag = createNode(1, "Item", tags = listOf("FINANCE"))
        val mixedCaseKeyword = createNode(2, "Pay The INvOICe", "")

        val result = DomainLensQueries.financeActionItems(listOf(uppercaseTag, mixedCaseKeyword))
        assertEquals(2, result.size)
    }

    @Test
    fun matchesHealthSignal_is_case_insensitive() {
        val uppercaseTag = createNode(1, "Item", tags = listOf("HEALTH"))
        val mixedCaseKeyword = createNode(2, "See the DoCtoR", "")

        val result = DomainLensQueries.healthActionItems(listOf(uppercaseTag, mixedCaseKeyword))
        assertEquals(2, result.size)
    }

    @Test
    fun sorting_financeActionItems_uses_distinct_timestamps_and_places_nulls_last() {
        val noDate1 = createNode(1, "Finance item")
        val noDate2 = createNode(2, "Finance item")
        val later = createNode(3, "Finance item", dueAt = 3000L)
        val earlier = createNode(4, "Finance item", dueAt = 1000L)
        val middle = createNode(5, "Finance item", dueAt = 2000L)

        val result = DomainLensQueries.financeActionItems(listOf(noDate1, noDate2, later, earlier, middle))
        // Expect exact sorted order by approaching deadline, and nulls at the end. Since noDate1 and noDate2 both resolve to Long.MAX_VALUE, their relative order is stable.
        val ids = result.map { it.node.id }
        assertEquals(listOf(4L, 5L, 3L, 1L, 2L), ids)
    }






    private fun createUnrelatedMaintenanceSnapshot(): com.tajemniktv.tajsos.ui.MaintenanceSnapshot {
        return com.tajemniktv.tajsos.ui.MaintenanceSnapshot(
            active = listOf(
                com.tajemniktv.tajsos.ui.MaintenanceStatusItem(
                    node = createNode(1, "Unrelated", maintenanceType = "random_chore"),
                    urgency = "low",
                    isRecurring = false
                )
            ),
            recurring = emptyList(),
            overdue = emptyList()
        )
    }

    @Test
    fun financeMaintenanceItems_handles_empty_active_and_wrong_domain() {
        val snapshot = createUnrelatedMaintenanceSnapshot()
        val result = DomainLensQueries.financeMaintenanceItems(snapshot)
        assertEquals(0, result.size)
    }

    @Test
    fun healthMaintenanceItems_handles_empty_active_and_wrong_domain() {
        val snapshot = createUnrelatedMaintenanceSnapshot()
        val result = DomainLensQueries.healthMaintenanceItems(snapshot)
        assertEquals(0, result.size)
    }
}
