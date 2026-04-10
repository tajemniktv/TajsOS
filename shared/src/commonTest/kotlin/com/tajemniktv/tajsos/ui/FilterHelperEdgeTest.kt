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

        // Exact match on title gets 100 + 60 (startsWith) + 30 (contains) + 5 (active status) = 195
        // Starts with match gets 60 + 30 (contains) + 5 (active status) = 95
        // exactTagMatchNode gets 20 (exact) + 10 (contains) + 5 = 35

        assertEquals(6, result.size)
        // Check exact match is first
        assertEquals(1L, result[0].node.id)
    }
}
