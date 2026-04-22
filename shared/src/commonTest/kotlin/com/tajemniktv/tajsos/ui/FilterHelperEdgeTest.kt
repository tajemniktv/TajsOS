package com.tajemniktv.tajsos.ui


import kotlin.test.Test
import kotlin.test.assertEquals

class FilterHelperEdgeTest {
    @Test
    fun testRelevanceScore_emptyQuery() {
        val node = buildTestNode(1, "Test Node")
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
        val exactMatchNode = buildTestNode(1, "exact match", "content", tags = listOf("tag"))
        val startsWithNode = buildTestNode(2, "exact match with suffix", "content", tags = listOf("tag"))
        val containsTitleNode = buildTestNode(3, "prefix exact match", "content", tags = listOf("tag"))
        val containsContentNode = buildTestNode(4, "title", "contains exact match", tags = listOf("tag"))
        val exactTagMatchNode = buildTestNode(5, "title", "content", tags = listOf("exact match"))
        val containsTagMatchNode = buildTestNode(6, "title", "content", tags = listOf("prefix exact match suffix"))

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
    fun testRelevanceScore_activeAndPinnedStatus() {
        val today = "2024-01-01"

        val activePinnedNode = buildTestNode(1, "query exact", "content", status = "active").copy(
            pin = TodayPinEntity(id = 1, nodeId = 1, date = today, position = 0)
        )
        val activeNode = buildTestNode(2, "query exact", "content", status = "active")
        val inactiveNode = buildTestNode(3, "query exact", "content", status = "on_hold")

        val result = FilterHelper.filterAndSortNodes(
            nodes = listOf(inactiveNode, activeNode, activePinnedNode),
            query = "query exact",
            type = null, status = null, projectId = null, areaId = null, linkedToId = null,
            maxMins = null, energy = null, friction = null, locationContext = null,
            energyContext = null, deviceContext = null, socialContext = null,
            timeWindowContext = null, timeHorizon = null, relations = emptyList(),
            sortMode = "relevance"
        )

        assertEquals(3, result.size)
        // Score order: activePinnedNode (1), activeNode (2), inactiveNode (3)
        assertEquals(1L, result[0].node.id)
        assertEquals(2L, result[1].node.id)
        assertEquals(3L, result[2].node.id)
    }

    @Test
    fun testRelevanceScore_tagMatches() {
        val exactTagMatch = buildTestNode(1, "title", "content", tags = listOf("query exact"))
        val containsTagMatch = buildTestNode(2, "title", "content", tags = listOf("prefix query exact suffix"))
        val noTagMatch = buildTestNode(3, "title", "content", tags = listOf("other"))

        val result = FilterHelper.filterAndSortNodes(
            nodes = listOf(noTagMatch, containsTagMatch, exactTagMatch),
            query = "query exact",
            type = null, status = null, projectId = null, areaId = null, linkedToId = null,
            maxMins = null, energy = null, friction = null, locationContext = null,
            energyContext = null, deviceContext = null, socialContext = null,
            timeWindowContext = null, timeHorizon = null, relations = emptyList(),
            sortMode = "relevance"
        )

        assertEquals(2, result.size) // noTagMatch shouldn't match the query
        // Score order: exactTagMatch (1), containsTagMatch (2)
        assertEquals(1L, result[0].node.id)
        assertEquals(2L, result[1].node.id)
    }

    @Test
    fun testRelevanceScore_titleAndContentMatches() {
        val exactMatch = buildTestNode(1, "query exact", "content")
        val startsWithMatch = buildTestNode(2, "query exact suffix", "content")
        val containsMatch = buildTestNode(3, "prefix query exact", "content")
        val contentMatch = buildTestNode(4, "other title", "content query exact")

        val result = FilterHelper.filterAndSortNodes(
            nodes = listOf(contentMatch, containsMatch, startsWithMatch, exactMatch),
            query = "query exact",
            type = null, status = null, projectId = null, areaId = null, linkedToId = null,
            maxMins = null, energy = null, friction = null, locationContext = null,
            energyContext = null, deviceContext = null, socialContext = null,
            timeWindowContext = null, timeHorizon = null, relations = emptyList(),
            sortMode = "relevance"
        )

        assertEquals(4, result.size)
        assertEquals(1L, result[0].node.id) // Exact
        assertEquals(2L, result[1].node.id) // Starts With
        assertEquals(3L, result[2].node.id) // Contains
        assertEquals(4L, result[3].node.id) // Content Match
    }

    @Test
    fun testRelevanceScore_tieBreakers() {
        val node1 = buildTestNode(1, "title", "content", tags = listOf("exact match"), updatedAt = 100L)
        val node2 = buildTestNode(2, "prefix exact match", "content", updatedAt = 200L)
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


    @Test
    fun testFilterStatus_commaSeparated() {
        val nodeActive = buildTestNode(1, "title", status = "active")
        val nodeOnHold = buildTestNode(2, "title", status = "on_hold")
        val nodeArchived = buildTestNode(3, "title", status = "archived")

        val result = FilterHelper.filterAndSortNodes(
            nodes = listOf(nodeActive, nodeOnHold, nodeArchived),
            query = "",
            type = null,
            status = "active, on_hold",
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
        assertEquals(listOf(1L, 2L), result.map { it.node.id }.sorted())
    }

}
