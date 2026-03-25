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
        tags: List<String> = emptyList(),
        updatedAt: Long = 0,
        status: String = "active",
        projectId: Long? = null,
        areaId: Long? = null,
        estimatedMinutes: Int? = null,
        energyLevel: Int? = null,
        friction: String? = null,
    ): NodeWithPin {
        val node =
            NodeEntity(
                id = id,
                title = title,
                content = content,
                type = "note",
                updatedAt = updatedAt,
                status = status,
                projectId = projectId,
                areaId = areaId,
                estimatedMinutes = estimatedMinutes,
                energyLevel = energyLevel,
                friction = friction,
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
                relations = emptyList(),
            )

        assertEquals(2, filteredNodes.size)
        assertTrue(filteredNodes.any { it.node.id == 1L })
        assertTrue(filteredNodes.any { it.node.id == 2L })
    }

    @Test
    fun testFilterExtendedProperties() {
        val node1 = createTestNode(
            id = 1,
            title = "Node 1",
            projectId = 10,
            areaId = 20,
            estimatedMinutes = 15,
            energyLevel = 1,
            friction = "easy"
        )
        val node2 = createTestNode(
            id = 2,
            title = "Node 2",
            projectId = 11,
            areaId = 20,
            estimatedMinutes = 60,
            energyLevel = 3,
            friction = "hard"
        )

        val nodes = listOf(node1, node2)

        val filteredByProject =
            FilterHelper.filterAndSortNodes(
                nodes = nodes,
                query = "",
                type = null,
                status = null,
                projectId = 10L,
                areaId = null,
                linkedToId = null,
                maxMins = null,
                energy = null,
                friction = null,
                relations = emptyList(),
            )
        assertEquals(1, filteredByProject.size)
        assertEquals(1L, filteredByProject[0].node.id)

        val filteredByArea =
            FilterHelper.filterAndSortNodes(
                nodes = nodes,
                query = "",
                type = null,
                status = null,
                projectId = null,
                areaId = 20L,
                linkedToId = null,
                maxMins = null,
                energy = null,
                friction = null,
                relations = emptyList(),
            )
        assertEquals(2, filteredByArea.size)

        val filteredByMins =
            FilterHelper.filterAndSortNodes(
                nodes = nodes,
                query = "",
                type = null,
                status = null,
                projectId = null,
                areaId = null,
                linkedToId = null,
                maxMins = 30,
                energy = null,
                friction = null,
                relations = emptyList(),
            )
        assertEquals(1, filteredByMins.size)
        assertEquals(1L, filteredByMins[0].node.id)

        val filteredByEnergy =
            FilterHelper.filterAndSortNodes(
                nodes = nodes,
                query = "",
                type = null,
                status = null,
                projectId = null,
                areaId = null,
                linkedToId = null,
                maxMins = null,
                energy = 3,
                friction = null,
                relations = emptyList(),
            )
        assertEquals(1, filteredByEnergy.size)
        assertEquals(2L, filteredByEnergy[0].node.id)

        val filteredByFriction =
            FilterHelper.filterAndSortNodes(
                nodes = nodes,
                query = "",
                type = null,
                status = null,
                projectId = null,
                areaId = null,
                linkedToId = null,
                maxMins = null,
                energy = null,
                friction = "easy",
                relations = emptyList(),
            )
        assertEquals(1, filteredByFriction.size)
        assertEquals(1L, filteredByFriction[0].node.id)
    }

    @Test
    fun testFilterLinkedToId() {
        val node1 = createTestNode(1, "Linked Node")
        val node2 = createTestNode(2, "Another Linked Node")
        val node3 = createTestNode(3, "Unlinked Node")

        val relations = listOf(
            RelationEntity(id = 1, fromNodeId = 1, toNodeId = 100, relationType = "RELATED"),
            RelationEntity(id = 2, fromNodeId = 100, toNodeId = 2, relationType = "RELATED")
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
}
