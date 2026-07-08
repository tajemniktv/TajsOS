package com.tajemniktv.tajsos.ui

import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.TagEntity
import com.tajemniktv.tajsos.data.TodayPinEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FilterHelperQueryTest {


    private fun createTestNode(id: Long, title: String): NodeWithPin {
        return NodeWithPin(
            node = NodeEntity(id = id, title = title, content = "", type = "task", status = "active", createdAt = 0L, updatedAt = 0L),
            pin = null,
            tags = emptyList(),
        )
    }


    @Test
    fun testRelevanceScoreOrdering() {
        val helper = FilterHelper
        val node1 = createTestNode(id = 1, title = "Exact Title Match").let { it.copy(node = it.node.copy(status = "inactive")) }
        val node2 = createTestNode(id = 2, title = "Exact Title Not Match").let { it.copy(node = it.node.copy(content = "Exact Title Match", status = "active")) }
        val node3 = createTestNode(id = 3, title = "Exact Prefix").let { it.copy(node = it.node.copy(content = "Something", status = "inactive")) }
        val node4 = createTestNode(id = 4, title = "Something").let { it.copy(node = it.node.copy(content = "Something", status = "inactive")) }
        val node5 = createTestNode(id = 5, title = "Another one").let { it.copy(node = it.node.copy(content = "Something", status = "inactive")) }

        val nodeWithPin1 = node1.copy(pin = TodayPinEntity(1, 1, "2024-01-01", 0)) // score + 8
        val nodeWithPin2 = node2 // score + 5 (active)
        val nodeWithPin3 = node3.copy(tags = listOf(TagEntity(id = 1, name = "Exact Title Match", normalizedName = "exact title match"))) // score + 20
        val nodeWithPin4 = node4.copy(tags = listOf(TagEntity(id = 2, name = "Title Match", normalizedName = "title match"))) // score + 10

        val nodes = listOf(nodeWithPin2, nodeWithPin1, nodeWithPin3, nodeWithPin4, node5)
        val sorted = helper.filterAndSortNodes(nodes = nodes, query = "Exact Title Match", type = null, status = null, projectId = null, areaId = null, linkedToId = null, maxMins = null, energy = null, friction = null, locationContext = null, energyContext = null, deviceContext = null, socialContext = null, timeWindowContext = null, timeHorizon = null, relations = emptyList(), sortMode = "relevance")

        assertEquals(1L, sorted[0].node.id)

        val sortedPrefix = helper.filterAndSortNodes(nodes = nodes, query = "Exact P", type = null, status = null, projectId = null, areaId = null, linkedToId = null, maxMins = null, energy = null, friction = null, locationContext = null, energyContext = null, deviceContext = null, socialContext = null, timeWindowContext = null, timeHorizon = null, relations = emptyList(), sortMode = "relevance")
        assertEquals(3L, sortedPrefix[0].node.id)
    }

    @Test
    fun testMatchesQueryAndRelevanceScore() {
        val helper = FilterHelper
        val node1 = createTestNode(id = 1, title = "Apple").let { it.copy(node = it.node.copy(content = "Banana")) }
        val nodeWithPin1 = node1.copy(tags = listOf(TagEntity(1, "fruit", "fruit")))

        assertTrue(helper.matchesQuery(nodeWithPin1, "Apple"))
        assertTrue(helper.matchesQuery(nodeWithPin1, "Banana"))
        assertTrue(helper.matchesQuery(nodeWithPin1, "fruit"))
        assertTrue(helper.matchesQuery(nodeWithPin1, "#fruit"))

        assertFalse(helper.matchesQuery(nodeWithPin1, "Orange"))
        assertFalse(helper.matchesQuery(nodeWithPin1, "#Orange"))
        assertFalse(helper.matchesQuery(nodeWithPin1, ""))
        assertFalse(helper.matchesQuery(nodeWithPin1, "   "))
        assertFalse(helper.matchesQuery(nodeWithPin1, "#"))
        assertFalse(helper.matchesQuery(nodeWithPin1, "#   "))

        val node2 = createTestNode(id = 2, title = "PrefixApple").let { it.copy(node = it.node.copy(content = "something")) }
        val node3 = createTestNode(id = 3, title = "Not exactly").let { it.copy(node = it.node.copy(content = "Apple")) }
        val nodes = listOf(node3, node2, node1)

        val sorted = helper.filterAndSortNodes(nodes = nodes, query = "Apple", type = null, status = null, projectId = null, areaId = null, linkedToId = null, maxMins = null, energy = null, friction = null, locationContext = null, energyContext = null, deviceContext = null, socialContext = null, timeWindowContext = null, timeHorizon = null, relations = emptyList(), sortMode = "relevance")

        assertEquals(3, sorted.size)
        assertEquals(1L, sorted[0].node.id)
        assertEquals(2L, sorted[1].node.id)
        assertEquals(3L, sorted[2].node.id)
    }
}
