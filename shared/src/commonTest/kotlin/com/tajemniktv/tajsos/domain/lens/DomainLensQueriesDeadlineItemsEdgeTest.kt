package com.tajemniktv.tajsos.domain.lens

import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.TagEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DomainLensQueriesDeadlineItemsEdgeTest {
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
            tags = tags.mapIndexed { index, tag -> TagEntity(id = index.toLong(), name = tag, normalizedName = tag.lowercase()) },
        )
    }

    @Test
    fun financeDeadlineItems_includes_only_items_with_due_date_and_sorts_correctly() {
        // Includes notes, tasks, etc., as long as it has dueAt and matches finance signal
        val noteNoDate = createNode(1, "Tax stuff", type = "note", dueAt = null, tags = listOf("finance"))
        val taskNoDate = createNode(2, "Pay bills", type = "task", dueAt = null, tags = listOf("finance"))
        val recordNoDate = createNode(3, "Budget record", type = "record", dueAt = null, tags = listOf("finance"))

        val taskLater = createNode(4, "Pay more bills", type = "task", dueAt = 5000L, tags = listOf("finance"))
        val taskEarlier = createNode(5, "Urgent tax", type = "task", dueAt = 1000L, tags = listOf("finance"))
        val noteMiddle = createNode(6, "Important finance note", type = "note", dueAt = 3000L, tags = listOf("finance"))

        val unrelatedTask = createNode(7, "Grocery list", type = "task", dueAt = 1000L) // No finance signal

        val allNodes = listOf(noteNoDate, taskNoDate, recordNoDate, taskLater, taskEarlier, noteMiddle, unrelatedTask)

        val result = DomainLensQueries.financeDeadlineItems(allNodes)

        // Should only include items with a due date that match the finance signal
        assertEquals(3, result.size)

        // Items without due dates or unrelated items should be excluded completely
        val resultIds = result.map { it.node.id }
        assertTrue(!resultIds.contains(1L))
        assertTrue(!resultIds.contains(2L))
        assertTrue(!resultIds.contains(3L))
        assertTrue(!resultIds.contains(7L))

        // Sort order should be earlier deadline first
        assertEquals(listOf(5L, 6L, 4L), resultIds)
    }
}
