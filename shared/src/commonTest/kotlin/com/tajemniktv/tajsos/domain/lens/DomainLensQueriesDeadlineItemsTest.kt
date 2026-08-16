package com.tajemniktv.tajsos.domain.lens

import kotlin.test.Test
import kotlin.test.assertEquals

class DomainLensQueriesDeadlineItemsTest {

    @Test
    fun financeDeadlineItems_excludes_items_without_deadline() {
        val nodeWithDeadline = createTestNode(id = 1, title = "Pay tax", status = "active", dueAt = 1000L, tags = listOf("finance"))
        val nodeWithoutDeadline = createTestNode(id = 2, title = "Review budget", status = "active", dueAt = null, tags = listOf("finance"))

        val result = DomainLensQueries.financeDeadlineItems(listOf(nodeWithDeadline, nodeWithoutDeadline))

        assertEquals(1, result.size)
        assertEquals(1L, result.first().node.id)
    }

    @Test
    fun financeDeadlineItems_excludes_inactive_items() {
        val activeNode = createTestNode(id = 1, title = "Pay tax", status = "active", dueAt = 1000L, tags = listOf("finance"))
        val doneNode = createTestNode(id = 2, title = "Paid tax", status = "done", dueAt = 1000L, tags = listOf("finance"))

        val result = DomainLensQueries.financeDeadlineItems(listOf(activeNode, doneNode))

        assertEquals(1, result.size)
        assertEquals(1L, result.first().node.id)
    }

    @Test
    fun financeDeadlineItems_includes_both_tasks_and_notes() {
        val taskNode = createTestNode(id = 1, title = "Pay tax", status = "active", dueAt = 1000L, type = "task", tags = listOf("finance"))
        val noteNode = createTestNode(id = 2, title = "Tax deadline", status = "active", dueAt = 2000L, type = "note", tags = listOf("finance"))
        val unrelatedTask = createTestNode(id = 3, title = "Normal task", status = "active", dueAt = 3000L, type = "task", tags = emptyList())

        val result = DomainLensQueries.financeDeadlineItems(listOf(taskNode, noteNode, unrelatedTask))

        assertEquals(2, result.size)
        assertEquals(setOf(1L, 2L), result.map { it.node.id }.toSet())
    }
}
