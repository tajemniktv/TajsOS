package com.tajemniktv.tajsos.ui

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class FilterHelperMatchesQueryEdgeTest {

    @Test
    fun testMatchesQuery_blankQuery() {
        val node = buildTestNode(1, "title", "content", tags = listOf("tag1"))
        assertFalse(FilterHelper.matchesQuery(node, ""))
        assertFalse(FilterHelper.matchesQuery(node, "   "))
    }

    @Test
    fun testMatchesQuery_hashtagSearch() {
        val node1 = buildTestNode(1, "title", "content", tags = listOf("tag1"))
        val node2 = buildTestNode(2, "title", "content", tags = listOf("other"))

        assertTrue(FilterHelper.matchesQuery(node1, "#tag1"))
        assertTrue(FilterHelper.matchesQuery(node1, "#TAG1")) // case insensitive
        assertTrue(FilterHelper.matchesQuery(node1, "# tag1")) // space handled by substring(1).trim()
        assertFalse(FilterHelper.matchesQuery(node2, "#tag1"))
    }

    @Test
    fun testMatchesQuery_hashtagSearch_empty() {
        val node1 = buildTestNode(1, "title", "content", tags = listOf("tag1"))
        assertFalse(FilterHelper.matchesQuery(node1, "#"))
        assertFalse(FilterHelper.matchesQuery(node1, "#   "))
    }

    @Test
    fun testMatchesQuery_normalSearch() {
        val nodeTitle = buildTestNode(1, "my title", "content")
        val nodeContent = buildTestNode(2, "other", "my content")
        val nodeTag = buildTestNode(3, "other", "other", tags = listOf("my tag"))
        val nodeNone = buildTestNode(4, "other", "other", tags = listOf("other"))

        assertTrue(FilterHelper.matchesQuery(nodeTitle, "title"))
        assertTrue(FilterHelper.matchesQuery(nodeContent, "content"))
        assertTrue(FilterHelper.matchesQuery(nodeTag, "tag"))
        assertFalse(FilterHelper.matchesQuery(nodeNone, "title"))
    }

    @Test
    fun testMatchesQuery_multipleNormalSearchTerms() {
        // We only check if the entire query matches the text (so a multi-word query acts like a phrase)
        val nodeTitle = buildTestNode(1, "my specific phrase", "content")
        val nodeTitleMismatch = buildTestNode(2, "my phrase specific", "content")

        assertTrue(FilterHelper.matchesQuery(nodeTitle, "specific phrase"))
        assertFalse(FilterHelper.matchesQuery(nodeTitleMismatch, "specific phrase"))
    }
}
