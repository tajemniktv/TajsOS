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
    fun financeDeadlineItems_includes_any_type_with_date_and_signal() {
        val financeTaskWithDate = createNode(1, "Tax task", dueAt = 100L, type = "task")
        val financeNoteWithDate = createNode(2, "Budget reference", dueAt = 200L, type = "note")
        val nonFinanceWithDate = createNode(3, "Generic idea", dueAt = 300L, type = "note")
        val financeNoDate = createNode(4, "Insurance file", type = "record")

        val allNodes = listOf(financeTaskWithDate, financeNoteWithDate, nonFinanceWithDate, financeNoDate)
        val result = DomainLensQueries.financeDeadlineItems(allNodes)

        // Only items 1 and 2 have both a finance signal AND a due date
        assertEquals(2, result.size)
        assertEquals(listOf(1L, 2L), result.map { it.node.id })
    }

    @Test
    fun financeKnowledgeItems_includes_reference_notes_with_signal_only() {
        // "reference" alone is not enough, must have signal
        val refNoteWithSignal = createNode(1, "Tax form 1040", type = "note", noteType = "reference")
        val refNoteNoSignal = createNode(2, "CSS guide", type = "note", noteType = "reference")
        val regNoteWithSignal = createNode(3, "My budget rules", type = "note")

        val allNodes = listOf(refNoteWithSignal, refNoteNoSignal, regNoteWithSignal)
        val result = DomainLensQueries.financeKnowledgeItems(allNodes)

        // 1 matches because reference + title signal
        // 3 matches because note (knowledge item) + title signal
        assertEquals(2, result.size)
        assertEquals(listOf(1L, 3L).sorted(), result.map { it.node.id }.sorted())
    }
}
