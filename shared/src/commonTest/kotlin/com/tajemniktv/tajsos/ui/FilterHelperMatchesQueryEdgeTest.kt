package com.tajemniktv.tajsos.ui

import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.TagEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FilterHelperMatchesQueryEdgeTest {

    private fun createNodeWithPin(
        title: String = "",
        content: String = "",
        tags: List<String> = emptyList()
    ): NodeWithPin {
        return NodeWithPin(
            node = NodeEntity(
                id = 1L,
                title = title,
                content = content,
                type = "task"
            ),
            pin = null,
            tags = tags.mapIndexed { index, name ->
                TagEntity(id = index.toLong(), name = name, normalizedName = name.lowercase())
            }
        )
    }

    @Test
    fun matchesQuery_blank_queries_return_false() {
        val node = createNodeWithPin(title = "test", content = "test", tags = listOf("test"))

        assertFalse(FilterHelper.matchesQuery(node, ""))
        assertFalse(FilterHelper.matchesQuery(node, " "))
        assertFalse(FilterHelper.matchesQuery(node, "   "))
        assertFalse(FilterHelper.matchesQuery(node, "\t\n"))
    }

    @Test
    fun matchesQuery_hashtag_blank_after_hash_returns_false() {
        val node = createNodeWithPin(tags = listOf("tag"))

        assertFalse(FilterHelper.matchesQuery(node, "#"))
        assertFalse(FilterHelper.matchesQuery(node, "#  "))
    }

    @Test
    fun matchesQuery_hashtag_matches_tags_case_insensitive() {
        val node = createNodeWithPin(tags = listOf("MyTag", "Another"))

        assertTrue(FilterHelper.matchesQuery(node, "#mytag"))
        assertTrue(FilterHelper.matchesQuery(node, "#MYTAG"))
        assertTrue(FilterHelper.matchesQuery(node, "#  mytag  ")) // Trimming applies
        assertTrue(FilterHelper.matchesQuery(node, "#tag")) // Partial match
        assertFalse(FilterHelper.matchesQuery(node, "#notag"))
    }

    @Test
    fun matchesQuery_hashtag_ignores_title_and_content() {
        val node = createNodeWithPin(title = "keyword", content = "keyword", tags = listOf("other"))

        // Even though "keyword" is in title and content, it's a tag search
        assertFalse(FilterHelper.matchesQuery(node, "#keyword"))
    }

    @Test
    fun matchesQuery_normal_query_matches_title_content_tags_case_insensitive() {
        val node1 = createNodeWithPin(title = "FindMe")
        val node2 = createNodeWithPin(content = "findMeHere")
        val node3 = createNodeWithPin(tags = listOf("FindMeTag"))
        val node4 = createNodeWithPin(title = "nope", content = "nope")

        assertTrue(FilterHelper.matchesQuery(node1, "findme"))
        assertTrue(FilterHelper.matchesQuery(node2, "FINDME"))
        assertTrue(FilterHelper.matchesQuery(node3, "find"))
        assertFalse(FilterHelper.matchesQuery(node4, "findme"))
    }

    @Test
    fun relevanceScore_exact_matches() {
        // Can't test relevanceScore directly as it's private, but can test indirectly via sorting
        val exactTitle = createNodeWithPin(title = "Apple")
        val startsTitle = createNodeWithPin(title = "Apple Pie")
        val containsTitle = createNodeWithPin(title = "Big Apple")
        val containsContent = createNodeWithPin(title = "Fruit", content = "An Apple")
        val exactTag = createNodeWithPin(title = "Fruit", tags = listOf("Apple"))
        val partialTag = createNodeWithPin(title = "Fruit", tags = listOf("AppleTree"))

        val nodes = listOf(partialTag, containsContent, startsTitle, exactTag, containsTitle, exactTitle)

        val sorted = FilterHelper.filterAndSortNodes(
            nodes = nodes,
            query = "apple",
            type = null, status = null, projectId = null, areaId = null, linkedToId = null,
            maxMins = null, energy = null, friction = null, locationContext = null,
            energyContext = null, deviceContext = null, socialContext = null,
            timeWindowContext = null, timeHorizon = null, relations = emptyList(),
            sortMode = "relevance"
        )

        // Exact title (100+60+30=190) should be first
        assertEquals(exactTitle, sorted[0])
        // Starts title (60+30=90) should be second
        assertEquals(startsTitle, sorted[1])
    }
}
