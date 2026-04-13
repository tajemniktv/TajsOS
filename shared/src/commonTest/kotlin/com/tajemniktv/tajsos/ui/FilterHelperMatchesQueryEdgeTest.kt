package com.tajemniktv.tajsos.ui

import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.TagEntity
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class FilterHelperMatchesQueryEdgeTest {

    private fun createNode(
        id: Long,
        title: String,
        content: String = "",
        tags: List<String> = emptyList()
    ): NodeWithPin {
        return NodeWithPin(
            node = NodeEntity(
                id = id,
                title = title,
                content = content,
                type = "task",
                status = "active",
                updatedAt = 0L
            ),
            pin = null,
            tags = tags.mapIndexed { index, tag -> TagEntity(id = index.toLong(), name = tag, normalizedName = tag.lowercase()) }
        )
    }

    @Test
    fun testMatchesQuery_blankQuery() {
        val node = createNode(1, "title", "content", listOf("tag1"))
        assertFalse(FilterHelper.matchesQuery(node, ""))
        assertFalse(FilterHelper.matchesQuery(node, "   "))
    }

    @Test
    fun testMatchesQuery_hashtagSearch() {
        val node1 = createNode(1, "title", "content", listOf("tag1"))
        val node2 = createNode(2, "title", "content", listOf("other"))

        assertTrue(FilterHelper.matchesQuery(node1, "#tag1"))
        assertTrue(FilterHelper.matchesQuery(node1, "#TAG1")) // case insensitive
        assertTrue(FilterHelper.matchesQuery(node1, "# tag1")) // space handled by substring(1).trim()
        assertFalse(FilterHelper.matchesQuery(node2, "#tag1"))
    }

    @Test
    fun testMatchesQuery_normalSearch() {
        val nodeTitle = createNode(1, "my title", "content")
        val nodeContent = createNode(2, "other", "my content")
        val nodeTag = createNode(3, "other", "other", listOf("my tag"))
        val nodeNone = createNode(4, "other", "other", listOf("other"))

        assertTrue(FilterHelper.matchesQuery(nodeTitle, "title"))
        assertTrue(FilterHelper.matchesQuery(nodeContent, "content"))
        assertTrue(FilterHelper.matchesQuery(nodeTag, "tag"))
        assertFalse(FilterHelper.matchesQuery(nodeNone, "title"))
    }
}
