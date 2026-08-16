package com.tajemniktv.tajsos.domain.lens

import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.TagEntity
import kotlin.test.Test
import kotlin.test.assertEquals

class DomainLensQueriesFinanceDeadlineTest {
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
    fun financeDeadlineItems_filters_by_active_status_and_due_date() {
        val activeWithDate = createNode(id = 1, title = "Tax return", status = "active", dueAt = 1000L, tags = listOf("finance"))
        val activeNoDate = createNode(id = 2, title = "Tax return", status = "active", dueAt = null, tags = listOf("finance"))
        val inactiveWithDate = createNode(id = 3, title = "Tax return", status = "archived", dueAt = 1000L, tags = listOf("finance"))

        val allNodes = listOf(activeWithDate, activeNoDate, inactiveWithDate)

        val result = DomainLensQueries.financeDeadlineItems(allNodes)

        assertEquals(1, result.size)
        assertEquals(1L, result.first().node.id)
    }

    @Test
    fun financeDeadlineItems_sorts_by_due_date_ascending() {
        val farDeadline = createNode(id = 1, title = "Tax return", status = "active", dueAt = 3000L, tags = listOf("finance"))
        val nearDeadline = createNode(id = 2, title = "Tax return", status = "active", dueAt = 1000L, tags = listOf("finance"))
        val mediumDeadline = createNode(id = 3, title = "Tax return", status = "active", dueAt = 2000L, tags = listOf("finance"))

        val allNodes = listOf(farDeadline, nearDeadline, mediumDeadline)

        val result = DomainLensQueries.financeDeadlineItems(allNodes)

        assertEquals(3, result.size)
        assertEquals(2L, result[0].node.id)
        assertEquals(3L, result[1].node.id)
        assertEquals(1L, result[2].node.id)
    }
}
