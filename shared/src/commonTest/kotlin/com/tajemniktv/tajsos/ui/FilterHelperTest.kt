/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui

import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
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
    ): NodeWithPin {
        val node =
            NodeEntity(
                id = id,
                title = title,
                content = content,
                type = "note",
                updatedAt = updatedAt,
                status = status,
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
}
