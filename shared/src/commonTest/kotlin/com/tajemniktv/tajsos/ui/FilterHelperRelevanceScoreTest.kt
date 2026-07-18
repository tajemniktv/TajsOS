package com.tajemniktv.tajsos.ui

import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.TagEntity
import kotlin.test.Test
import kotlin.test.assertEquals

class FilterHelperRelevanceScoreTest {

    private fun buildTestNode(
        id: Long,
        title: String,
        content: String = "",
        tags: List<String> = emptyList(),
        status: String = "active"
    ): NodeWithPin {
        return NodeWithPin(
            node = NodeEntity(
                id = id,
                title = title,
                content = content,
                type = "task",
                status = status,
                updatedAt = 0L
            ),
            pin = null,
            tags = tags.mapIndexed { index, tag -> TagEntity(id = index.toLong(), name = tag, normalizedName = tag.lowercase()) }
        )
    }

    @Test
    fun testRelevanceScore_variousMatches() {
        val exactTitle = buildTestNode(1, "query")
        val startsWithTitle = buildTestNode(2, "query prefix")
        val containsTitle = buildTestNode(3, "prefix query suffix")
        val contentMatch = buildTestNode(4, "other", "content contains query")
        val exactTag = buildTestNode(5, "other", tags = listOf("query"))
        val containsTag = buildTestNode(6, "other", tags = listOf("prefix query"))
        val inactiveStatus = buildTestNode(7, "query", status = "inactive")

        val result = FilterHelper.filterAndSortNodes(
            nodes = listOf(exactTitle, startsWithTitle, containsTitle, contentMatch, exactTag, containsTag, inactiveStatus),
            query = "query",
            type = null, status = null, projectId = null, areaId = null, linkedToId = null,
            maxMins = null, energy = null, friction = null, locationContext = null,
            energyContext = null, deviceContext = null, socialContext = null,
            timeWindowContext = null, timeHorizon = null, relations = emptyList(),
            sortMode = "relevance"
        )
        // Scores:
        // exactTitle: 100(exact) + 60(starts) + 30(contains) + 5(active) = 195
        // inactiveStatus: 100(exact) + 60(starts) + 30(contains) + 0(inactive) = 190
        // startsWithTitle: 60(starts) + 30(contains) + 5(active) = 95
        // containsTitle: 30(contains) + 5(active) = 35
        // exactTag: 20(exact tag) + 10(contains tag) + 5(active) = 35
        // containsTag: 10(contains tag) + 5(active) = 15
        // contentMatch: 15(content) + 5(active) = 20

        // Let's assert the order: 1, 7, 2, 3/5, 4, 6
        assertEquals(7, result.size)
        assertEquals(1L, result[0].node.id)
        assertEquals(7L, result[1].node.id)
        assertEquals(2L, result[2].node.id)
        // Tie between 3 and 5 (both score 35), tie-broken by ID descending? ID 5 > 3
        assertEquals(5L, result[3].node.id)
        assertEquals(3L, result[4].node.id)
        assertEquals(4L, result[5].node.id)
        assertEquals(6L, result[6].node.id)
    }
}
