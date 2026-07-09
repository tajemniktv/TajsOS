package com.tajemniktv.tajsos.ui

import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.TagEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FilterHelperRelevanceScoreTest {

    // We can't access private fun relevanceScore directly, so we test it via filterAndSortNodes
    // sorting by relevance mode.

    private class NodeBuilder(val id: Long, val title: String) {
        var content: String = ""
        var tags: List<String> = emptyList()
        var isPinned: Boolean = false
        var status: String = "active"

        fun build(): NodeWithPin {
            return NodeWithPin(
                node = NodeEntity(id = id, title = title, content = content, status = status, type = "note"),
                pin = if (isPinned) com.tajemniktv.tajsos.data.TodayPinEntity(id = id, nodeId = id, date = "2024-01-01", position = 0) else null,
                tags = tags.mapIndexed { index, tag -> TagEntity(id = index.toLong(), name = tag, normalizedName = tag.lowercase()) }
            )
        }
    }

    private fun buildNode(id: Long, title: String, block: NodeBuilder.() -> Unit = {}): NodeWithPin {
        return NodeBuilder(id, title).apply(block).build()
    }

    @Test
    fun testRelevanceScore_ranking() {
        val query = "query"

        // Exact match -> +100, starts with -> +60, contains -> +30 = 190
        val exactMatch = buildNode(1, "query") { status = "inactive" }

        // Starts with -> +60, contains -> +30 = 90
        val startsWith = buildNode(2, "query is good") { status = "inactive" }

        // Contains -> +30
        val containsTitle = buildNode(3, "this is a query") { status = "inactive" }

        // Content contains -> +15
        val containsContent = buildNode(4, "title") { content = "this has a query"; status = "inactive" }

        // Tag exact match -> +20, Tag contains match -> +10 = 30
        val tagExactMatch = buildNode(5, "title") { tags = listOf("query"); status = "inactive" }

        // Tag contains match -> +10
        val tagContainsMatch = buildNode(6, "title") { tags = listOf("myquery"); status = "inactive" }

        // Pinned -> +8 (on top of content match) -> 15 + 8 = 23
        val pinnedContent = buildNode(7, "title") { content = "query"; isPinned = true; status = "inactive" }

        // Active status -> +5 (on top of content match) -> 15 + 5 = 20
        val activeContent = buildNode(8, "title") { content = "query"; status = "active" }

        val nodes = listOf(exactMatch, startsWith, containsTitle, containsContent, tagExactMatch, tagContainsMatch, pinnedContent, activeContent).shuffled()

        val result = FilterHelper.filterAndSortNodes(
            nodes = nodes,
            query = query,
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

        // Note: FilterHelper.filterAndSortNodes also sorts by updatedAt and id descending as tie-breakers.
        // We'll just verify the primary ranking based on relevance scores.

        // 190 > 90 > 30 (title) == 30 (tag exact) > 23 > 20 > 15 > 10

        assertEquals(1L, result[0].node.id) // 190
        assertEquals(2L, result[1].node.id) // 90

        val thirdAndFourth = result.subList(2, 4).map { it.node.id }.toSet()
        assertTrue(thirdAndFourth.contains(3L)) // 30
        assertTrue(thirdAndFourth.contains(5L)) // 30

        assertEquals(7L, result[4].node.id) // 23
        assertEquals(8L, result[5].node.id) // 20
        assertEquals(4L, result[6].node.id) // 15
        assertEquals(6L, result[7].node.id) // 10
    }

    @Test
    fun testRelevanceScore_emptyQueryReturnsZero() {
        val node1 = buildNode(1, "query") { isPinned = true; status = "active" }
        val node2 = buildNode(2, "other")

        // Blank query defaults to filterAndSortNodes fallback boolean logic instead of checking relevance?
        // Let's test with a blank query on filterAndSortNodes (should maintain standard order or relevance score 0 for both)
        val result = FilterHelper.filterAndSortNodes(
            nodes = listOf(node1, node2),
            query = "   ", // blank query
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
        // With blank query, both nodes have score 0. Sort ties are broken by updatedAt, then id desc.
        // Node 1 and Node 2 both have updatedAt=0, so ID descending means Node 2 comes before Node 1.
        assertEquals(2, result.size)
        assertEquals(2L, result[0].node.id)
        assertEquals(1L, result[1].node.id)
    }
}
