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
    fun financeActionItems_includes_all_signals() {
        val nodeTitle = createNode(1, "my budget is tight")
        val nodeContent = createNode(2, "some task", "need to pay tax")
        val nodeTag = createNode(3, "some task", tags = listOf("money"))
        val nodeMaintenance = createNode(4, "some task", maintenanceType = "bill")
        // this is note, so it shouldn't be included in action items
        val nodeReference = createNode(5, "some budget", type = "note", noteType = "reference")
        val nodeNone = createNode(6, "unrelated")

        val result = DomainLensQueries.financeActionItems(listOf(nodeTitle, nodeContent, nodeTag, nodeMaintenance, nodeReference, nodeNone))
        assertDomainQueryResult(listOf(1L, 2L, 3L, 4L), result)
    }

    @Test
    fun financeActionItems_case_insensitivity() {
        val nodeTitle = createNode(1, "MY BUDGET")
        val nodeContent = createNode(2, "some task", "PAY TAX")
        val nodeTag = createNode(3, "some task", tags = listOf("MONEY"))

        val result = DomainLensQueries.financeActionItems(listOf(nodeTitle, nodeContent, nodeTag))
        assertDomainQueryResult(listOf(1L, 2L, 3L), result)
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
    fun healthActionItems_includes_all_signals() {
        val nodeTitle = createNode(1, "see doctor")
        val nodeContent = createNode(2, "some task", "pick up medication")
        val nodeTag = createNode(3, "some task", tags = listOf("medical"))
        val nodeMaintenance = createNode(4, "some task", maintenanceType = "appointment")
        // this is note, so it shouldn't be included in action items
        val nodeReflection = createNode(5, "feeling better", type = "note", noteType = "reflection")
        val nodeNone = createNode(6, "unrelated")

        val result = DomainLensQueries.healthActionItems(listOf(nodeTitle, nodeContent, nodeTag, nodeMaintenance, nodeReflection, nodeNone))
        assertDomainQueryResult(listOf(1L, 2L, 3L, 4L), result)
    }

    @Test
    fun healthActionItems_case_insensitivity() {
        val nodeTitle = createNode(1, "SEE DOCTOR")
        val nodeContent = createNode(2, "some task", "PICK UP MEDICATION")
        val nodeTag = createNode(3, "some task", tags = listOf("MEDICAL"))

        val result = DomainLensQueries.healthActionItems(listOf(nodeTitle, nodeContent, nodeTag))
        assertDomainQueryResult(listOf(1L, 2L, 3L), result)
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
    fun financeKnowledgeItems_includes_reference_note_if_tag_matches() {
        val nodeReference = createNode(1, "some unrelated title", type = "note", noteType = "reference", tags = listOf("finance"))
        val result = DomainLensQueries.financeKnowledgeItems(listOf(nodeReference))
        assertEquals(1, result.size)
    }

    @Test
    fun financeKnowledgeItems_excludes_reference_note_without_signals() {
        val nodeReference = createNode(1, "some unrelated title", type = "note", noteType = "reference")
        val result = DomainLensQueries.financeKnowledgeItems(listOf(nodeReference))
        assertEquals(0, result.size)
    }

    @Test
    fun healthKnowledgeItems_excludes_general_note_without_signals() {
        val nodeNote = createNode(1, "random thought", type = "note")
        val result = DomainLensQueries.healthKnowledgeItems(listOf(nodeNote))
        assertEquals(0, result.size)
    }
    @Test
    fun actionItems_edge_empty_strings() {
        val nodeEmpty = createNode(1, "")
        val financeResult = DomainLensQueries.financeActionItems(listOf(nodeEmpty))
        val healthResult = DomainLensQueries.healthActionItems(listOf(nodeEmpty))
        assertEquals(0, financeResult.size)
        assertEquals(0, healthResult.size)
    }
    @Test
    fun healthKnowledgeItems_implicit_note_types_without_keywords() {
        val nodeJournal = createNode(1, "random thought", type = "note", noteType = "journal")
        val nodeReflection = createNode(2, "another thought", type = "note", noteType = "reflection")
        val result = DomainLensQueries.healthKnowledgeItems(listOf(nodeJournal, nodeReflection))
        assertEquals(2, result.size)
    }
}
