/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui

import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.RelationEntity
import com.tajemniktv.tajsos.data.TagEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FilterHelperTest {
    private fun createTestNode(
        id: Long,
        title: String,
        content: String = "",
        type: String = "note",
        tags: List<String> = emptyList(),
        updatedAt: Long = 0,
        status: String = "active",
        projectId: Long? = null,
        areaId: Long? = null,
        estimatedMinutes: Int? = null,
        energyLevel: Int? = null,
        friction: String? = null,
        locationContext: String? = null,
        energyContext: String? = null,
        dueAt: Long? = null,
    ): NodeWithPin {
        val node =
            NodeEntity(
                id = id,
                title = title,
                content = content,
                type = type,
                updatedAt = updatedAt,
                status = status,
                projectId = projectId,
                areaId = areaId,
                estimatedMinutes = estimatedMinutes,
                energyLevel = energyLevel,
                friction = friction,
                locationContext = locationContext,
                energyContext = energyContext,
                dueAt = dueAt,
            )
        val tagEntities =
            tags.mapIndexed { index, name ->
                TagEntity(id = index.toLong(), name = name, normalizedName = name.lowercase())
            }
        return NodeWithPin(node = node, pin = null, tags = tagEntities)
    }

    @Test
    fun testMatchesQueryEmpty() {
        val node = createTestNode(1, "Title")
        assertFalse(FilterHelper.matchesQuery(node, ""))
        assertFalse(FilterHelper.matchesQuery(node, "   "))
    }

    @Test
    fun testMatchesQueryTitleAndContent() {
        val node = createTestNode(1, "Hello World", "This is some content")

        assertTrue(FilterHelper.matchesQuery(node, "hello"))
        assertTrue(FilterHelper.matchesQuery(node, "WORLD"))
        assertTrue(FilterHelper.matchesQuery(node, "some"))
        assertFalse(FilterHelper.matchesQuery(node, "missing"))
    }

    @Test
    fun testMatchesQueryTags() {
        val node = createTestNode(1, "Title", tags = listOf("Urgent", "Work"))

        assertTrue(FilterHelper.matchesQuery(node, "urgent"))
        assertTrue(FilterHelper.matchesQuery(node, "WORK"))
        assertTrue(FilterHelper.matchesQuery(node, "#urgent"))
        assertFalse(FilterHelper.matchesQuery(node, "#missing"))
    }

    @Test
    fun testFilterAndSortNodes() {
        val node1 = createTestNode(1, "First Node", updatedAt = 100)
        val node2 = createTestNode(2, "Second Node", updatedAt = 200)
        val node3 = createTestNode(3, "Third Node", updatedAt = 150)

        val nodes = listOf(node1, node2, node3)

        // Empty query returns all, sorted by updatedAt descending
        val sortedNodes =
            FilterHelper.filterAndSortNodes(
                nodes = nodes,
                query = "  ",
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
            )

        assertEquals(3, sortedNodes.size)
        assertEquals(2, sortedNodes[0].node.id)
        assertEquals(3, sortedNodes[1].node.id)
        assertEquals(1, sortedNodes[2].node.id)

        // Query filtering
        val filteredNodes =
            FilterHelper.filterAndSortNodes(
                nodes = nodes,
                query = "second",
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
            )

        assertEquals(1, filteredNodes.size)
        assertEquals(2, filteredNodes[0].node.id)
    }

    @Test
    fun testFilterMultipleStatuses() {
        val nodeActive = createTestNode(1, "Active Node")
        val nodeOnHold = createTestNode(2, "On Hold Node", status = "on_hold")
        val nodeArchived = createTestNode(3, "Archived Node", status = "archived")

        val nodes = listOf(nodeActive, nodeOnHold, nodeArchived)

        val filteredNodes =
            FilterHelper.filterAndSortNodes(
                nodes = nodes,
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
            )

        assertEquals(2, filteredNodes.size)
        assertTrue(filteredNodes.any { it.node.id == 1L })
        assertTrue(filteredNodes.any { it.node.id == 2L })
    }

    private fun createNodesForExtendedTests(): List<NodeWithPin> {
        val node1 = createTestNode(
            id = 1,
            title = "Node 1",
            projectId = 10,
            areaId = 20,
            estimatedMinutes = 15,
            energyLevel = 1,
            friction = "easy",
        )
        val node2 = createTestNode(
            id = 2,
            title = "Node 2",
            projectId = 11,
            areaId = 20,
            estimatedMinutes = 60,
            energyLevel = 3,
            friction = "hard",
        )
        return listOf(node1, node2)
    }

    private fun assertFilterResult(
        nodes: List<NodeWithPin>,
        expectedCount: Int,
        expectedFirstId: Long? = null,
        projectId: Long? = null,
        areaId: Long? = null,
        maxMins: Int? = null,
        energy: Int? = null,
        friction: String? = null,
    ) {
        val filtered = FilterHelper.filterAndSortNodes(
            nodes = nodes,
            query = "",
            type = null,
            status = null,
            projectId = projectId,
            areaId = areaId,
            linkedToId = null,
            maxMins = maxMins,
            energy = energy,
            friction = friction,
            locationContext = null,
            energyContext = null,
            deviceContext = null,
            socialContext = null,
            timeWindowContext = null,
            timeHorizon = null,
            relations = emptyList(),
        )
        assertEquals(expectedCount, filtered.size)
        if (expectedFirstId != null) {
            assertEquals(expectedFirstId, filtered[0].node.id)
        }
    }

    @Test
    fun testFilterExtendedProperties() {
        val nodes = createNodesForExtendedTests()

        // By Project
        assertFilterResult(nodes, projectId = 10L, expectedCount = 1, expectedFirstId = 1L)

        // By Area
        assertFilterResult(nodes, areaId = 20L, expectedCount = 2)

        // By MaxMins
        assertFilterResult(nodes, maxMins = 30, expectedCount = 1, expectedFirstId = 1L)

        // By Energy
        assertFilterResult(nodes, energy = 3, expectedCount = 1, expectedFirstId = 2L)

        // By Friction
        assertFilterResult(nodes, friction = "easy", expectedCount = 1, expectedFirstId = 1L)
    }

    @Test
    fun testFilterLinkedToId() {
        val node1 = createTestNode(1, "Linked Node")
        val node2 = createTestNode(2, "Another Linked Node")
        val node3 = createTestNode(3, "Unlinked Node")

        val relations = listOf(
            RelationEntity(id = 1, fromNodeId = 1, toNodeId = 100, relationType = "RELATED"),
            RelationEntity(id = 2, fromNodeId = 100, toNodeId = 2, relationType = "RELATED"),
        )

        val filtered =
            FilterHelper.filterAndSortNodes(
                nodes = listOf(node1, node2, node3),
                query = "",
                type = null,
                status = null,
                projectId = null,
                areaId = null,
                linkedToId = 100L,
                maxMins = null,
                energy = null,
                friction = null,
                locationContext = null,
                energyContext = null,
                deviceContext = null,
                socialContext = null,
                timeWindowContext = null,
                timeHorizon = null,
                relations = relations,
            )

        assertEquals(2, filtered.size)
        assertTrue(filtered.any { it.node.id == 1L })
        assertTrue(filtered.any { it.node.id == 2L })
    }

    @Test
    fun testMatchesQueryEmptyTag() {
        val node = createTestNode(1, "Title", tags = listOf("Tag"))

        // Blank tag query "# " or "#" should be false
        assertFalse(FilterHelper.matchesQuery(node, "#"))
        assertFalse(FilterHelper.matchesQuery(node, "#   "))
    }

    @Test
    fun testFilterTimeHorizon() {
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
        val dayMs = 24 * 60 * 60 * 1000L

        // Node due today (within 24h)
        val nodeToday = createTestNode(1, "Today", type = "task", dueAt = now + (dayMs / 2))

        // Node due this week (within 7 days, e.g. 3 days)
        val nodeWeek = createTestNode(2, "Week", type = "task", dueAt = now + (3 * dayMs))

        // Node due in a month (within 30 days, e.g. 15 days)
        val nodeMonth = createTestNode(3, "Month", type = "task", dueAt = now + (15 * dayMs))

        // Node due long term (after 30 days, e.g. 40 days)
        val nodeLong = createTestNode(4, "Long", type = "task", dueAt = now + (40 * dayMs))

        // Node with no due date
        val nodeNoDue = createTestNode(5, "No Due", type = "task")

        val nodes = listOf(nodeToday, nodeWeek, nodeMonth, nodeLong, nodeNoDue)

        fun filterWithHorizon(horizon: String): List<NodeWithPin> {
            return FilterHelper.filterAndSortNodes(
                nodes = nodes,
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
                timeHorizon = horizon,
                relations = emptyList(),
            )
        }

        // "today" includes only nodeToday
        val todayNodes = filterWithHorizon("today")
        assertEquals(1, todayNodes.size)
        assertEquals(1L, todayNodes[0].node.id)

        // "week" includes today and week (due <= 7 days)
        val weekNodes = filterWithHorizon("week")
        assertEquals(2, weekNodes.size)
        assertTrue(weekNodes.any { it.node.id == 1L })
        assertTrue(weekNodes.any { it.node.id == 2L })

        // "long" includes nodeLong
        val longNodes = filterWithHorizon("long")
        assertEquals(1, longNodes.size)
        assertEquals(4L, longNodes[0].node.id)
    }

    @Test
    fun testContextFiltering() {
        val taskNodeMatch = createTestNode(
            id = 1,
            title = "Task Match",
            type = "task",
            locationContext = "home",
            energyContext = "high",
        )
        val taskNodeMismatch = createTestNode(
            id = 2,
            title = "Task Mismatch",
            type = "task",
            locationContext = "office",
            energyContext = "high",
        )
        // Notes should be excluded entirely if any context filter is active
        val noteNode = createTestNode(
            id = 3,
            title = "Note",
            type = "note",
            locationContext = "home",
        )

        val nodes = listOf(taskNodeMatch, taskNodeMismatch, noteNode)

        val filtered = FilterHelper.filterAndSortNodes(
            nodes = nodes,
            query = "",
            type = null,
            status = null,
            projectId = null,
            areaId = null,
            linkedToId = null,
            maxMins = null,
            energy = null,
            friction = null,
            locationContext = "home",
            energyContext = null,
            deviceContext = null,
            socialContext = null,
            timeWindowContext = null,
            timeHorizon = null,
            relations = emptyList(),
        )

        // Note is excluded because type != "task" when anyContextFilter is true
        // Mismatch task is excluded because location doesn't match
        assertEquals(1, filtered.size)
        assertEquals(1L, filtered[0].node.id)
    }
}
