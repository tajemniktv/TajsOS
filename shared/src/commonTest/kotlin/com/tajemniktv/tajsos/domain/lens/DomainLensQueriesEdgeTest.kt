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
        val taskNoDate = createNode(1, "Finance item", dueAt = null)
        val taskLater = createNode(2, "Finance item", dueAt = 2000L)
        val taskEarlier = createNode(3, "Finance item", dueAt = 1000L)

        val result = DomainLensQueries.financeActionItems(listOf(taskNoDate, taskLater, taskEarlier))
        assertEquals(listOf(3L, 2L, 1L), result.map { it.node.id })
    }

    @Test
    fun sorting_healthActionItems_places_null_dueAt_last() {
        val taskNoDate = createNode(1, "Health item", dueAt = null)
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
}
