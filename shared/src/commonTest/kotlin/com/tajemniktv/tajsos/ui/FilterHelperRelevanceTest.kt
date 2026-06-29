package com.tajemniktv.tajsos.ui

import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.TagEntity
import com.tajemniktv.tajsos.data.TodayPinEntity
import kotlin.test.Test
import kotlin.test.assertEquals

class FilterHelperRelevanceTest {

    private fun createTestNode(
        id: Long,
        title: String,
        content: String = "",
        status: String = "active",
        isPinnedToToday: Boolean = false,
        tags: List<TagEntity> = emptyList(),
        updatedAt: Long = 0L
    ): NodeWithPin {
        val pin = if (isPinnedToToday) TodayPinEntity(id = id, nodeId = id, date = "2024-01-01", position = 0, selectedAt = 0L) else null
        return NodeWithPin(
            node = NodeEntity(
                id = id,
                title = title,
                content = content,
                status = status,
                createdAt = 0L,
                updatedAt = updatedAt,
                type = "task"
            ),
            pin = pin,
            tags = tags,
            snapshots = emptyList()
        )
    }

    @Test
    fun testRelevanceScoreOrdering() {
        val query = "apple"

        // Exact title match: 100 (exact) + 60 (starts with) + 30 (contains) + 5 (active) = 195
        val exactMatch = createTestNode(id = 1, title = "apple")

        // Title starts with: 60 (starts with) + 30 (contains) + 5 (active) = 95
        val startsWith = createTestNode(id = 2, title = "applesauce")

        // Title contains: 30 (contains) + 5 (active) = 35
        val containsTitle = createTestNode(id = 3, title = "green apple")

        // Tag exact match: 20 (exact) + 10 (contains) + 5 (active) = 35
        // Same score as containsTitle. We'll set a higher updatedAt to ensure deterministic ordering.
        val exactTag = createTestNode(id = 4, title = "fruit", tags = listOf(TagEntity(id=1, name="apple", normalizedName="apple")), updatedAt = 100L)

        // Pinned, active, and content match: 15 (content) + 8 (pinned) + 5 (active) = 28
        val contentPinnedActive = createTestNode(id = 5, title = "fruit", content = "apple", isPinnedToToday = true, status = "active")

        // Content match only: 15 + 5 (active) = 20
        val contentMatch = createTestNode(id = 6, title = "fruit", content = "an apple a day")

        // Tag contains match only: 10 + 5 (active) = 15
        val tagContains = createTestNode(id = 7, title = "fruit", tags = listOf(TagEntity(id=2, name="apples", normalizedName="apples")))

        // Content match + pinned: 15 (content) + 8 (pinned) + 5 (active) = 28 (wait, same as contentPinnedActive since all are active by default).
        // Let's make contentPinnedActive actually active, and contentPinned inactive to distinguish.
        val contentPinned = createTestNode(id = 8, title = "fruit", content = "apple", isPinnedToToday = true, status = "on_hold") // 15 + 8 = 23

        val nodes = listOf(
            contentMatch,         // id=6, score=20
            exactTag,             // id=4, score=35, updatedAt=100
            contentPinned,        // id=8, score=23
            startsWith,           // id=2, score=95
            contentPinnedActive,  // id=5, score=28
            exactMatch,           // id=1, score=195
            containsTitle,        // id=3, score=35, updatedAt=0
            tagContains           // id=7, score=15
        )

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

        // Expected order by relevance score descending:
        // 1. exactMatch (195)
        // 2. startsWith (95)
        // 3. exactTag (35) - Tie breaker: updatedAt = 100
        // 4. containsTitle (35) - Tie breaker: updatedAt = 0
        // 5. contentPinnedActive (28)
        // 6. contentPinned (23)
        // 7. contentMatch (20)
        // 8. tagContains (15)

        assertEquals(8, result.size)
        assertEquals(1L, result[0].node.id)
        assertEquals(2L, result[1].node.id)
        assertEquals(4L, result[2].node.id)
        assertEquals(3L, result[3].node.id)
        assertEquals(5L, result[4].node.id)
        assertEquals(8L, result[5].node.id)
        assertEquals(6L, result[6].node.id)
        assertEquals(7L, result[7].node.id)
    }

    @Test
    fun testRelevanceScoreCaseInsensitive() {
        val query = "ApPlE"

        val exactMatch = createTestNode(id = 1, title = "aPpLe") // exact match should ignore case
        val noMatch = createTestNode(id = 2, title = "banana")

        val nodes = listOf(noMatch, exactMatch)

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

        assertEquals(1, result.size)
        assertEquals(1L, result[0].node.id)
    }

    @Test
    fun testBlankQueryReturnsNoRelevanceScore() {

        val node1 = createTestNode(id = 1, title = "apple", updatedAt = 10L)
        val node2 = createTestNode(id = 2, title = "banana", updatedAt = 20L)

        val nodes = listOf(node1, node2)

        val result = FilterHelper.filterAndSortNodes(
            nodes = nodes,
            query = "   ",
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

        // Blank query gives score 0 to everything, so it sorts by updatedAt descending
        assertEquals(2, result.size)
        assertEquals(2L, result[0].node.id)
        assertEquals(1L, result[1].node.id)
    }
}
