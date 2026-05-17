package com.tajemniktv.tajsos.domain.lens

import kotlin.test.Test
import kotlin.test.assertEquals

class DomainLensQueriesActionItemsTest {

    @Test
    fun financeActionItems_excludes_inactive_tasks() {
        val activeFinanceTask = createTestNode(id = 1, title = "Pay tax", status = "active")
        val doneFinanceTask = createTestNode(id = 2, title = "Pay rent", status = "done")
        val archivedFinanceTask = createTestNode(id = 3, title = "Budget review", status = "archived")
        val activeNonFinanceTask = createTestNode(id = 4, title = "Clean the house", status = "active")

        val result = DomainLensQueries.financeActionItems(listOf(activeFinanceTask, doneFinanceTask, archivedFinanceTask, activeNonFinanceTask))

        assertEquals(setOf(1L), result.map { it.node.id }.toSet())
    }

    @Test
    fun financeActionItems_excludes_non_task_items() {
        val financeTask = createTestNode(id = 1, title = "Pay tax", type = "task")
        val financeNote = createTestNode(id = 2, title = "Tax reference", type = "note")
        val financeRecord = createTestNode(id = 3, title = "Paid tax today", type = "record")

        val result = DomainLensQueries.financeActionItems(listOf(financeTask, financeNote, financeRecord))

        assertEquals(setOf(1L), result.map { it.node.id }.toSet())
    }

    @Test
    fun healthActionItems_excludes_inactive_tasks() {
        val activeHealthTask = createTestNode(id = 1, title = "Pick up medication", status = "active", tags = listOf("medical"))
        val doneHealthTask = createTestNode(id = 2, title = "See doctor", status = "done", tags = listOf("health"))
        val archivedHealthTask = createTestNode(id = 3, title = "Therapy session", status = "archived", tags = listOf("health"))
        val activeNonHealthTask = createTestNode(id = 4, title = "Buy groceries", status = "active")

        val result = DomainLensQueries.healthActionItems(listOf(activeHealthTask, doneHealthTask, archivedHealthTask, activeNonHealthTask))

        assertEquals(setOf(1L), result.map { it.node.id }.toSet())
    }

    @Test
    fun healthActionItems_excludes_non_task_items() {
        val healthTask = createTestNode(id = 1, title = "Pick up prescription", type = "task", tags = listOf("health"))
        val healthNote = createTestNode(id = 2, title = "Medical history", type = "note", tags = listOf("health"))
        val healthRecord = createTestNode(id = 3, title = "Symptom log", type = "record", tags = listOf("health"))

        val result = DomainLensQueries.healthActionItems(listOf(healthTask, healthNote, healthRecord))

        assertEquals(setOf(1L), result.map { it.node.id }.toSet())
    }
}
