package com.tajemniktv.tajsos.ui

import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.TagEntity
import kotlin.test.Test
import kotlin.test.assertEquals

class FilterHelperEdgeTest {
    private fun createTestNode(
        id: Long,
        title: String,
        content: String = "",
        type: String = "task",
        status: String = "active",
        tags: List<String> = emptyList(),
        updatedAt: Long = 0L,
    ): NodeWithPin {
        return NodeWithPin(
            node = NodeEntity(
                id = id,
                title = title,
                content = content,
                type = type,
                status = status,
                updatedAt = updatedAt
            ),
            pin = null,
            tags = tags.mapIndexed { index, tag -> TagEntity(id = index.toLong(), name = tag, normalizedName = tag.lowercase()) }
        )
    }

    @Test
    fun testRelevanceScore_emptyQuery() {
        val node = createTestNode(1, "Test Node")
        val result = FilterHelper.filterAndSortNodes(
            nodes = listOf(node),
            query = "",
            type = null,
            status = null,
            projectId = null,
            areaId = null,
            linkedToId = null,
            maxMins = null,
            energy = null,
            friction = null,
            locationContext = null,
            energyContext = null,
            deviceContext = null,
            socialContext = null,
            timeWindowContext = null,
            timeHorizon = null,
            relations = emptyList(),
            sortMode = "relevance"
        )
        // Ensure that with empty query it still returns nodes and sort mode works without error
        assertEquals(1, result.size)
    }

    @Test
    fun testRelevanceScore_variousMatches() {
        val exactMatchNode = createTestNode(1, "exact match", "content", tags = listOf("tag"))
        val startsWithNode = createTestNode(2, "exact match with suffix", "content", tags = listOf("tag"))
        val containsTitleNode = createTestNode(3, "prefix exact match", "content", tags = listOf("tag"))
        val containsContentNode = createTestNode(4, "title", "contains exact match", tags = listOf("tag"))
        val exactTagMatchNode = createTestNode(5, "title", "content", tags = listOf("exact match"))
        val containsTagMatchNode = createTestNode(6, "title", "content", tags = listOf("prefix exact match suffix"))

        val nodes = listOf(exactMatchNode, startsWithNode, containsTitleNode, containsContentNode, exactTagMatchNode, containsTagMatchNode)

        val result = FilterHelper.filterAndSortNodes(
            nodes = nodes,
            query = "exact match",
            type = null,
            status = null,
            projectId = null,
            areaId = null,
            linkedToId = null,
            maxMins = null,
            energy = null,
            friction = null,
            locationContext = null,
            energyContext = null,
            deviceContext = null,
            socialContext = null,
            timeWindowContext = null,
            timeHorizon = null,
            relations = emptyList(),
            sortMode = "relevance"
        )

        assertEquals(6, result.size)

        assertEquals(1L, result[0].node.id) // 195
        assertEquals(2L, result[1].node.id) // 95
        assertEquals(5L, result[2].node.id) // 35 (tiebreaker ID)
        assertEquals(3L, result[3].node.id) // 35
        assertEquals(4L, result[4].node.id) // 20
        assertEquals(6L, result[5].node.id) // 15
    }

    @Test
    fun testRelevanceScore_tieBreakers() {
        val node1 = createTestNode(1, "title", "content", tags = listOf("exact match"), updatedAt = 100L)
        val node2 = createTestNode(2, "prefix exact match", "content", updatedAt = 200L)
        val result = FilterHelper.filterAndSortNodes(
            nodes = listOf(node1, node2),
            query = "exact match",
            type = null,
            status = null,
            projectId = null,
            areaId = null,
            linkedToId = null,
            maxMins = null,
            energy = null,
            friction = null,
            locationContext = null,
            energyContext = null,
            deviceContext = null,
            socialContext = null,
            timeWindowContext = null,
            timeHorizon = null,
            relations = emptyList(),
            sortMode = "relevance"
        )
        assertEquals(2, result.size)
        // Score is the same, node2 has higher updatedAt so it should be first
        assertEquals(2L, result[0].node.id)
        assertEquals(1L, result[1].node.id)
    }

}