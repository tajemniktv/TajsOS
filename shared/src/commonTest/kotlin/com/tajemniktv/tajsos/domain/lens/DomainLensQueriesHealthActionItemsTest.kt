package com.tajemniktv.tajsos.domain.lens

import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.TagEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DomainLensQueriesHealthActionItemsTest {

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
    fun healthActionItems_filters_for_tasks_only() {
        val taskNode = createNode(id = 1, title = "see doctor", type = "task")
        val noteNode = createNode(id = 2, title = "see doctor", type = "note")
        val recordNode = createNode(id = 3, title = "see doctor", type = "record")
        val projectNode = createNode(id = 4, title = "see doctor", type = "project")
        val openLoopNode = createNode(id = 5, title = "see doctor", type = "open_loop")

        val nodes = listOf(taskNode, noteNode, recordNode, projectNode, openLoopNode)

        val result = DomainLensQueries.healthActionItems(nodes)

        assertEquals(2, result.size)
        assertTrue(result.any { it.node.id == 1L })
        assertTrue(result.any { it.node.id == 5L })
    }

    @Test
    fun healthActionItems_filters_for_active_status_only() {
        val activeTask = createNode(id = 1, title = "see doctor", status = "active")
        val completedTask = createNode(id = 2, title = "see doctor", status = "completed")
        val archivedTask = createNode(id = 3, title = "see doctor", status = "archived")
        val somedayTask = createNode(id = 4, title = "see doctor", status = "someday")

        val nodes = listOf(activeTask, completedTask, archivedTask, somedayTask)

        val result = DomainLensQueries.healthActionItems(nodes)

        assertEquals(1, result.size)
        assertEquals(1L, result.first().node.id)
    }

    @Test
    fun healthActionItems_sorts_by_deadline_ascending_placing_nulls_last() {
        val noDeadline = createNode(id = 1, title = "see doctor")
        val farDeadline = createNode(id = 2, title = "see doctor", dueAt = 3000L)
        val nearDeadline = createNode(id = 3, title = "see doctor", dueAt = 1000L)
        val mediumDeadline = createNode(id = 4, title = "see doctor", dueAt = 2000L)

        val nodes = listOf(noDeadline, farDeadline, nearDeadline, mediumDeadline)

        val result = DomainLensQueries.healthActionItems(nodes)

        assertEquals(4, result.size)
        assertEquals(3L, result[0].node.id)
        assertEquals(4L, result[1].node.id)
        assertEquals(2L, result[2].node.id)
        assertEquals(1L, result[3].node.id)
    }

    @Test
    fun healthActionItems_handles_empty_list_correctly() {
        val result = DomainLensQueries.healthActionItems(emptyList())
        assertTrue(result.isEmpty())
    }
}
