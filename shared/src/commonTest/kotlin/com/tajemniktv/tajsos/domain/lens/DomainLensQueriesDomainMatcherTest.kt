package com.tajemniktv.tajsos.domain.lens

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DomainLensQueriesDomainMatcherTest {

    @Test
    fun matches_handles_blank_title_and_content_safely() {
        val nodeBlank = createTestNode(1, title = "   ", content = "   ", type = "task")
        val result = DomainLensQueries.financeActionItems(listOf(nodeBlank))
        assertTrue(result.isEmpty())
    }

    @Test
    fun matches_requires_exact_match_for_maintenance_types_and_note_types() {
        val nodePartialMaint = createTestNode(1, "title", maintenanceType = "bill_payment", type = "task")
        val nodeExactMaint = createTestNode(2, "title", maintenanceType = "bill", type = "task")

        val nodePartialNote = createTestNode(3, "title", type = "note", noteType = "self_reflection")
        val nodeExactNote = createTestNode(4, "title", type = "note", noteType = "reflection")

        val financeResult = DomainLensQueries.financeActionItems(listOf(nodePartialMaint, nodeExactMaint))
        assertEquals(1, financeResult.size)
        assertEquals(2L, financeResult.first().node.id)

        val healthResult = DomainLensQueries.healthKnowledgeItems(listOf(nodePartialNote, nodeExactNote))
        assertEquals(1, healthResult.size)
        assertEquals(4L, healthResult.first().node.id)
    }

    @Test
    fun matches_handles_tags_with_different_casing_and_whitespace() {
        val exactTag = createTestNode(1, "title", tags = listOf("finance"), type = "task")
        val upperTag = createTestNode(2, "title", tags = listOf("FINANCE"), type = "task")
        val partialTag = createTestNode(3, "title", tags = listOf("my_finance"), type = "task")

        val result = DomainLensQueries.financeActionItems(listOf(exactTag, upperTag, partialTag))
        assertEquals(2, result.size)
        assertEquals(setOf(1L, 2L), result.map { it.node.id }.toSet())
    }

    @Test
    fun matches_handles_keywords_with_special_characters_in_title_and_content() {
        val titlePunctuation = createTestNode(1, "my budget!", type = "task")
        val contentPunctuation = createTestNode(2, "title", content = "see the budget...", type = "task")
        val contentNewline = createTestNode(3, "title", content = "first line\nsecond budget line", type = "task")

        val result = DomainLensQueries.financeActionItems(listOf(titlePunctuation, contentPunctuation, contentNewline))
        assertEquals(3, result.size)
        assertEquals(setOf(1L, 2L, 3L), result.map { it.node.id }.toSet())
    }
}
