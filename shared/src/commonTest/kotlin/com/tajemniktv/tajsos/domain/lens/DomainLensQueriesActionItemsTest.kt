package com.tajemniktv.tajsos.domain.lens

import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.TagEntity
import kotlin.test.Test
import kotlin.test.assertEquals

class DomainLensQueriesActionItemsTest {
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
            node = NodeEntity(
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
            tags = tags.mapIndexed { index, tag -> TagEntity(id = index.toLong(), name = tag, normalizedName = tag.lowercase()) }
        )
    }

    @Test
    fun financeActionItems_excludes_inactive_tasks() {
        val activeFinanceTask = createNode(id = 1, title = "Pay tax", status = "active")
        val doneFinanceTask = createNode(id = 2, title = "Pay rent", status = "done")
        val archivedFinanceTask = createNode(id = 3, title = "Budget review", status = "archived")

        val result = DomainLensQueries.financeActionItems(listOf(activeFinanceTask, doneFinanceTask, archivedFinanceTask))

        assertEquals(setOf(1L), result.map { it.node.id }.toSet())
    }

    @Test
    fun financeActionItems_excludes_non_task_items() {
        val financeTask = createNode(id = 1, title = "Pay tax", type = "task")
        val financeNote = createNode(id = 2, title = "Tax reference", type = "note")
        val financeRecord = createNode(id = 3, title = "Paid tax today", type = "record")

        val result = DomainLensQueries.financeActionItems(listOf(financeTask, financeNote, financeRecord))

        assertEquals(setOf(1L), result.map { it.node.id }.toSet())
    }

    @Test
    fun healthActionItems_excludes_inactive_tasks() {
        val activeHealthTask = createNode(id = 1, title = "See doctor", status = "active")
        val doneHealthTask = createNode(id = 2, title = "Take meds", status = "done")
        val archivedHealthTask = createNode(id = 3, title = "Medical review", status = "archived")

        val result = DomainLensQueries.healthActionItems(listOf(activeHealthTask, doneHealthTask, archivedHealthTask))

        assertEquals(setOf(1L), result.map { it.node.id }.toSet())
    }

    @Test
    fun healthActionItems_excludes_non_task_items() {
        val healthTask = createNode(id = 1, title = "See doctor", type = "task")
        val healthNote = createNode(id = 2, title = "Medical history", type = "note")
        val healthRecord = createNode(id = 3, title = "Felt sick", type = "record")

        val result = DomainLensQueries.healthActionItems(listOf(healthTask, healthNote, healthRecord))

        assertEquals(setOf(1L), result.map { it.node.id }.toSet())
    }
}
