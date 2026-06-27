package com.tajemniktv.tajsos.domain.lens

import kotlin.test.Test
import kotlin.test.assertEquals

class DomainLensQueriesKnowledgeItemsTest {

    @Test
    fun financeKnowledgeItems_excludes_inactive_items() {
        val activeFinanceNote = createTestNode(id = 1, title = "Tax reference", type = "note", noteType = "reference", status = "active", tags = listOf("finance"))
        val archivedFinanceNote = createTestNode(id = 2, title = "Old budget", type = "note", noteType = "reference", status = "archived", tags = listOf("finance"))
        val activeNonFinanceNote = createTestNode(id = 3, title = "Random thoughts", type = "note", status = "active")

        val result = DomainLensQueries.financeKnowledgeItems(listOf(activeFinanceNote, archivedFinanceNote, activeNonFinanceNote))

        assertEquals(setOf(1L), result.map { it.node.id }.toSet())
    }

    @Test
    fun financeKnowledgeItems_excludes_non_knowledge_items() {
        val financeNote = createTestNode(id = 1, title = "Tax reference", type = "note", noteType = "reference", tags = listOf("finance"))
        val financeRecord = createTestNode(id = 2, title = "Paid tax", type = "record", tags = listOf("finance"))
        val financeTask = createTestNode(id = 3, title = "Pay tax", type = "task", tags = listOf("finance"))

        val result = DomainLensQueries.financeKnowledgeItems(listOf(financeNote, financeRecord, financeTask))

        assertEquals(setOf(1L, 2L), result.map { it.node.id }.toSet())
    }

    @Test
    fun healthKnowledgeItems_excludes_inactive_items() {
        val activeHealthNote = createTestNode(id = 1, title = "Medical history", type = "note", status = "active", tags = listOf("health"))
        val archivedHealthNote = createTestNode(id = 2, title = "Old symptoms", type = "note", status = "archived", tags = listOf("health"))
        val activeNonHealthNote = createTestNode(id = 3, title = "Meeting notes", type = "note", status = "active")

        val result = DomainLensQueries.healthKnowledgeItems(listOf(activeHealthNote, archivedHealthNote, activeNonHealthNote))

        assertEquals(setOf(1L), result.map { it.node.id }.toSet())
    }

    @Test
    fun healthKnowledgeItems_excludes_non_knowledge_items() {
        val healthNote = createTestNode(id = 1, title = "Medical history", type = "note", tags = listOf("health"))
        val healthRecord = createTestNode(id = 2, title = "Symptom log", type = "record", tags = listOf("health"))
        val healthTask = createTestNode(id = 3, title = "See doctor", type = "task", tags = listOf("health"))

        val result = DomainLensQueries.healthKnowledgeItems(listOf(healthNote, healthRecord, healthTask))

        assertEquals(setOf(1L, 2L), result.map { it.node.id }.toSet())
    }
}
