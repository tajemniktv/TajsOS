package com.tajemniktv.tajsos.ui

import com.tajemniktv.tajsos.data.RelationEntity
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class FilterHelperMatchesLinkedToTest {
    private fun matchesLinkedToProxy(nodeId: Long, linkedToId: Long?, relations: List<RelationEntity>): Boolean {
        val nodeWithPin1 = buildTestNode(nodeId, "title1")
        val result = FilterHelper.filterAndSortNodes(
            nodes = listOf(nodeWithPin1),
            query = "",
            type = null,
            status = null,
            projectId = null,
            areaId = null,
            linkedToId = linkedToId,
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
            sortMode = "relevance"
        )
        return result.isNotEmpty()
    }

    @Test
    fun testMatchesLinkedTo_nullLinkedToId() {
        assertTrue(matchesLinkedToProxy(1L, null, emptyList()))
    }

    @Test
    fun testMatchesLinkedTo_matchFromNodeId() {
        val relations = listOf(
            RelationEntity(id = 1L, fromNodeId = 1L, toNodeId = 2L, relationType = "RELATED")
        )
        assertTrue(matchesLinkedToProxy(1L, 2L, relations))
    }

    @Test
    fun testMatchesLinkedTo_matchToNodeId() {
        val relations = listOf(
            RelationEntity(id = 1L, fromNodeId = 2L, toNodeId = 1L, relationType = "RELATED")
        )
        assertTrue(matchesLinkedToProxy(1L, 2L, relations))
    }

    @Test
    fun testMatchesLinkedTo_noMatch() {
        val relations = listOf(
            RelationEntity(id = 1L, fromNodeId = 3L, toNodeId = 4L, relationType = "RELATED")
        )
        assertFalse(matchesLinkedToProxy(1L, 2L, relations))
    }

    @Test
    fun testMatchesLinkedTo_multipleRelationsMatch() {
        val relations = listOf(
            RelationEntity(id = 1L, fromNodeId = 3L, toNodeId = 4L, relationType = "RELATED"),
            RelationEntity(id = 2L, fromNodeId = 1L, toNodeId = 2L, relationType = "RELATED")
        )
        assertTrue(matchesLinkedToProxy(1L, 2L, relations))
    }
}
