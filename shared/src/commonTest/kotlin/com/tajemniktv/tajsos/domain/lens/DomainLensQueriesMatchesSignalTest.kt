package com.tajemniktv.tajsos.domain.lens

import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.TagEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DomainLensQueriesMatchesSignalTest {
    private fun createNode(
        id: Long,
        title: String,
        content: String = "",
        type: String = "task",
        status: String = "active",
        tags: List<String> = emptyList(),
        maintenanceType: String? = null,
        noteType: String? = null,
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
            ),
            pin = null,
            tags = tags.mapIndexed { index, tag -> TagEntity(id = index.toLong(), name = tag, normalizedName = tag.lowercase()) }
        )
    }


    private fun assertDomainQueryResult(expectedIds: List<Long>, result: List<NodeWithPin>) {
        assertEquals(expectedIds.size, result.size)
        assertEquals(expectedIds.sorted(), result.map { it.node.id }.sorted())
    }
    @Test
    fun financeActionItems_signals_and_case_insensitivity() {
        // Signals
        val nodeTitle = createNode(1, "my budget is tight")
        val nodeContent = createNode(2, "some task", "need to pay tax")
        val nodeTag = createNode(3, "some task", tags = listOf("money"))
        val nodeMaintenance = createNode(4, "some task", maintenanceType = "bill")
        val nodeReference = createNode(5, "some budget", type = "note", noteType = "reference")
        val nodeNone = createNode(6, "unrelated")

        // Case insensitivity
        val nodeTitleCase = createNode(7, "MY BUDGET")
        val nodeContentCase = createNode(8, "some task", "PAY TAX")
        val nodeTagCase = createNode(9, "some task", tags = listOf("MONEY"))

        val result = DomainLensQueries.financeActionItems(listOf(nodeTitle, nodeContent, nodeTag, nodeMaintenance, nodeReference, nodeNone, nodeTitleCase, nodeContentCase, nodeTagCase))
        assertDomainQueryResult(listOf(1L, 2L, 3L, 4L, 7L, 8L, 9L), result)
    }


    @Test
    fun financeKnowledgeItems_includes_reference_notes() {
        val nodeReferenceMatch = createNode(1, "some budget", type = "note", noteType = "reference")
        val nodeReferenceTagMatch = createNode(2, "some doc", type = "note", noteType = "reference", tags = listOf("finance"))
        val nodeReferenceNoMatch = createNode(3, "some doc", type = "note", noteType = "reference")

        val result = DomainLensQueries.financeKnowledgeItems(listOf(nodeReferenceMatch, nodeReferenceTagMatch, nodeReferenceNoMatch))
        assertDomainQueryResult(listOf(1L, 2L), result)
    }
    @Test
    fun healthActionItems_signals_and_case_insensitivity() {
        // Signals
        val nodeTitle = createNode(1, "see doctor")
        val nodeContent = createNode(2, "some task", "pick up medication")
        val nodeTag = createNode(3, "some task", tags = listOf("medical"))
        val nodeMaintenance = createNode(4, "some task", maintenanceType = "appointment")
        val nodeReflection = createNode(5, "feeling better", type = "note", noteType = "reflection")
        val nodeNone = createNode(6, "unrelated")

        // Case insensitivity
        val nodeTitleCase = createNode(7, "SEE DOCTOR")
        val nodeContentCase = createNode(8, "some task", "PICK UP MEDICATION")
        val nodeTagCase = createNode(9, "some task", tags = listOf("MEDICAL"))

        val result = DomainLensQueries.healthActionItems(listOf(nodeTitle, nodeContent, nodeTag, nodeMaintenance, nodeReflection, nodeNone, nodeTitleCase, nodeContentCase, nodeTagCase))
        assertDomainQueryResult(listOf(1L, 2L, 3L, 4L, 7L, 8L, 9L), result)
    }


    @Test
    fun healthKnowledgeItems_includes_health_notes() {
        val nodeReflection = createNode(1, "my health", type = "note", noteType = "reflection")
        val nodeJournal = createNode(2, "my thoughts", type = "note", noteType = "journal")
        val nodeOtherNote = createNode(3, "some text", type = "note")

        val result = DomainLensQueries.healthKnowledgeItems(listOf(nodeReflection, nodeJournal, nodeOtherNote))
        assertDomainQueryResult(listOf(1L, 2L), result)
    }
    @Test
    fun knowledgeItems_implicit_and_tag_matches() {
        val nodeFinanceRef = createNode(1, "some unrelated title", type = "note", noteType = "reference", tags = listOf("finance"))
        val nodeJournal = createNode(2, "random thought", type = "note", noteType = "journal")
        val nodeReflection = createNode(3, "another thought", type = "note", noteType = "reflection")

        val financeResult = DomainLensQueries.financeKnowledgeItems(listOf(nodeFinanceRef))
        val healthResult = DomainLensQueries.healthKnowledgeItems(listOf(nodeJournal, nodeReflection))

        assertEquals(1, financeResult.size)
        assertEquals(2, healthResult.size)
    }

    @Test
    fun edge_cases_and_exclusions() {
        val nodeReference = createNode(1, "some unrelated title", type = "note", noteType = "reference")
        val nodeNote = createNode(2, "random thought", type = "note")
        val nodeEmpty = createNode(3, "")

        val financeKnowledge = DomainLensQueries.financeKnowledgeItems(listOf(nodeReference))
        val healthKnowledge = DomainLensQueries.healthKnowledgeItems(listOf(nodeNote))
        val financeAction = DomainLensQueries.financeActionItems(listOf(nodeEmpty))
        val healthAction = DomainLensQueries.healthActionItems(listOf(nodeEmpty))

        assertEquals(0, financeKnowledge.size)
        assertEquals(0, healthKnowledge.size)
        assertEquals(0, financeAction.size)
        assertEquals(0, healthAction.size)
    }

}
