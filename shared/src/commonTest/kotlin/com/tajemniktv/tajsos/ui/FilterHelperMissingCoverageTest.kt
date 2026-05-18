package com.tajemniktv.tajsos.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import com.tajemniktv.tajsos.data.RelationEntity

class FilterHelperMissingCoverageTest {

    @Test
    fun testContextFilteringMissedBranches() {
        val node1 = buildTestNode(1, "title", "content", type = "task").copy(
            node = buildTestNode(1, "title", "content", type = "task").node.copy(
                locationContext = "office",
                energyContext = "low",
                deviceContext = "phone",
                socialContext = "pair",
                timeWindowContext = "morning"
            )
        )

        val node2 = buildTestNode(2, "title", "content", type = "task").copy(
            node = buildTestNode(2, "title", "content", type = "task").node.copy(
                locationContext = "home",
                energyContext = "high",
                deviceContext = "laptop",
                socialContext = "solo",
                timeWindowContext = "evening"
            )
        )

        // Test filtering by all these missing branches explicitly
        val result = FilterHelper.filterAndSortNodes(
            nodes = listOf(node1, node2),
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
            energyContext = "high",
            deviceContext = "laptop",
            socialContext = "solo",
            timeWindowContext = "evening",
            timeHorizon = null,
            relations = emptyList(),
            sortMode = "relevance"
        )

        assertEquals(1, result.size)
        assertEquals(2L, result[0].node.id)
    }

    @Test
    fun testProjectAndAreaAndMinsAndEnergyMismatches() {
        // We want to test where projectId is NOT null but doesn't match
        val nodeProjectMismatch = buildTestNode(1, "title").copy(
            node = buildTestNode(1, "title").node.copy(projectId = 999L)
        )
        val resultProject = FilterHelper.filterAndSortNodes(
            nodes = listOf(nodeProjectMismatch), query = "", type = null, status = null,
            projectId = 1L, areaId = null, linkedToId = null, maxMins = null, energy = null, friction = null,
            locationContext = null, energyContext = null, deviceContext = null, socialContext = null,
            timeWindowContext = null, timeHorizon = null, relations = emptyList(), sortMode = "relevance"
        )
        assertEquals(0, resultProject.size)

        // areaId NOT null but doesn't match
        val nodeAreaMismatch = buildTestNode(2, "title").copy(
            node = buildTestNode(2, "title").node.copy(areaId = 999L)
        )
        val resultArea = FilterHelper.filterAndSortNodes(
            nodes = listOf(nodeAreaMismatch), query = "", type = null, status = null,
            projectId = null, areaId = 1L, linkedToId = null, maxMins = null, energy = null, friction = null,
            locationContext = null, energyContext = null, deviceContext = null, socialContext = null,
            timeWindowContext = null, timeHorizon = null, relations = emptyList(), sortMode = "relevance"
        )
        assertEquals(0, resultArea.size)

        // maxMins NOT null but doesn't match
        val nodeMinsMismatch = buildTestNode(3, "title").copy(
            node = buildTestNode(3, "title").node.copy(estimatedMinutes = 60)
        )
        val resultMins = FilterHelper.filterAndSortNodes(
            nodes = listOf(nodeMinsMismatch), query = "", type = null, status = null,
            projectId = null, areaId = null, linkedToId = null, maxMins = 30, energy = null, friction = null,
            locationContext = null, energyContext = null, deviceContext = null, socialContext = null,
            timeWindowContext = null, timeHorizon = null, relations = emptyList(), sortMode = "relevance"
        )
        assertEquals(0, resultMins.size)

        // energy NOT null but doesn't match
        val nodeEnergyMismatch = buildTestNode(4, "title").copy(
            node = buildTestNode(4, "title").node.copy(energyLevel = 3)
        )
        val resultEnergy = FilterHelper.filterAndSortNodes(
            nodes = listOf(nodeEnergyMismatch), query = "", type = null, status = null,
            projectId = null, areaId = null, linkedToId = null, maxMins = null, energy = 1, friction = null,
            locationContext = null, energyContext = null, deviceContext = null, socialContext = null,
            timeWindowContext = null, timeHorizon = null, relations = emptyList(), sortMode = "relevance"
        )
        assertEquals(0, resultEnergy.size)
    }

    @Test
    fun testShortCircuitAnds() {
        val node1 = buildTestNode(1, "title", "content", type = "task").copy(
            node = buildTestNode(1, "title", "content", type = "task").node.copy(
                energyContext = "high",
                deviceContext = "laptop",
                socialContext = "solo",
                timeWindowContext = "evening"
            )
        )

        // Fail on energy context
        val result1 = FilterHelper.filterAndSortNodes(
            nodes = listOf(node1), query = "", type = null, status = null, projectId = null, areaId = null, linkedToId = null, maxMins = null, energy = null, friction = null,
            locationContext = null, energyContext = "low", deviceContext = null, socialContext = null,
            timeWindowContext = null, timeHorizon = null, relations = emptyList(), sortMode = "relevance"
        )
        assertEquals(0, result1.size)

        // Fail on device context
        val result2 = FilterHelper.filterAndSortNodes(
            nodes = listOf(node1), query = "", type = null, status = null, projectId = null, areaId = null, linkedToId = null, maxMins = null, energy = null, friction = null,
            locationContext = null, energyContext = null, deviceContext = "phone", socialContext = null,
            timeWindowContext = null, timeHorizon = null, relations = emptyList(), sortMode = "relevance"
        )
        assertEquals(0, result2.size)

        // Fail on social context
        val result3 = FilterHelper.filterAndSortNodes(
            nodes = listOf(node1), query = "", type = null, status = null, projectId = null, areaId = null, linkedToId = null, maxMins = null, energy = null, friction = null,
            locationContext = null, energyContext = null, deviceContext = null, socialContext = "pair",
            timeWindowContext = null, timeHorizon = null, relations = emptyList(), sortMode = "relevance"
        )
        assertEquals(0, result3.size)

        // Fail on time window context
        val result4 = FilterHelper.filterAndSortNodes(
            nodes = listOf(node1), query = "", type = null, status = null, projectId = null, areaId = null, linkedToId = null, maxMins = null, energy = null, friction = null,
            locationContext = null, energyContext = null, deviceContext = null, socialContext = null,
            timeWindowContext = "morning", timeHorizon = null, relations = emptyList(), sortMode = "relevance"
        )
        assertEquals(0, result4.size)
    }

    @Test
    fun testTimeHorizonsNullDue() {
        val nodeNullDue = buildTestNode(1, "title", "content", type = "task", dueAt = null)

        // All should fail since dueAt is null
        listOf("today", "week", "month", "semester", "short").forEach { horizon ->
            val result = FilterHelper.filterAndSortNodes(
                nodes = listOf(nodeNullDue), query = "", type = null, status = null, projectId = null, areaId = null, linkedToId = null, maxMins = null, energy = null, friction = null,
                locationContext = null, energyContext = null, deviceContext = null, socialContext = null,
                timeWindowContext = null, timeHorizon = horizon, relations = emptyList(), sortMode = "relevance"
            )
            assertEquals(0, result.size)
        }
    }

    @Test
    fun testLinkedToFromNodeId() {
        val node1 = buildTestNode(1, "title")
        val relations = listOf(
            RelationEntity(id = 1, fromNodeId = 1, toNodeId = 2, relationType = "RELATED"),
            RelationEntity(id = 2, fromNodeId = 3, toNodeId = 4, relationType = "RELATED")
        )

        // Matches where fromNodeId == node.id and toNodeId == linkedToId
        val result1 = FilterHelper.filterAndSortNodes(
            nodes = listOf(node1), query = "", type = null, status = null, projectId = null, areaId = null, linkedToId = 2L, maxMins = null, energy = null, friction = null,
            locationContext = null, energyContext = null, deviceContext = null, socialContext = null,
            timeWindowContext = null, timeHorizon = null, relations = relations, sortMode = "relevance"
        )
        assertEquals(1, result1.size)

        // Non match
        val result2 = FilterHelper.filterAndSortNodes(
            nodes = listOf(node1), query = "", type = null, status = null, projectId = null, areaId = null, linkedToId = 4L, maxMins = null, energy = null, friction = null,
            locationContext = null, energyContext = null, deviceContext = null, socialContext = null,
            timeWindowContext = null, timeHorizon = null, relations = relations, sortMode = "relevance"
        )
        assertEquals(0, result2.size)
    }

    @Test
    fun testShortCircuitLeftSidesNullMatches() {
        val node1 = buildTestNode(1, "title").copy(
            node = buildTestNode(1, "title").node.copy(
                projectId = null,
                areaId = null,
                estimatedMinutes = null,
                energyLevel = null
            )
        )
        // Providing non-null id arguments but node has null ids
        val result1 = FilterHelper.filterAndSortNodes(
            nodes = listOf(node1), query = "", type = null, status = null, projectId = 100L, areaId = 200L, linkedToId = null, maxMins = 30, energy = 3, friction = null,
            locationContext = null, energyContext = null, deviceContext = null, socialContext = null,
            timeWindowContext = null, timeHorizon = null, relations = emptyList(), sortMode = "relevance"
        )
        assertEquals(0, result1.size)

        // Testing relations where `it.fromNodeId == node.id` is false and `it.fromNodeId == linkedToId` is false
        val relations = listOf(
            RelationEntity(id = 1, fromNodeId = 999, toNodeId = 1000, relationType = "RELATED"),
            RelationEntity(id = 2, fromNodeId = 2000, toNodeId = 999, relationType = "RELATED")
        )
        val result2 = FilterHelper.filterAndSortNodes(
            nodes = listOf(node1), query = "", type = null, status = null, projectId = null, areaId = null, linkedToId = 1000L, maxMins = null, energy = null, friction = null,
            locationContext = null, energyContext = null, deviceContext = null, socialContext = null,
            timeWindowContext = null, timeHorizon = null, relations = relations, sortMode = "relevance"
        )
        assertEquals(0, result2.size)

        val result3 = FilterHelper.filterAndSortNodes(
            nodes = listOf(node1), query = "", type = null, status = null, projectId = null, areaId = null, linkedToId = 2000L, maxMins = null, energy = null, friction = null,
            locationContext = null, energyContext = null, deviceContext = null, socialContext = null,
            timeWindowContext = null, timeHorizon = null, relations = relations, sortMode = "relevance"
        )
        assertEquals(0, result3.size)
    }

    @Test
    fun testShortCircuitLeftSides() {
        val node1 = buildTestNode(1, "title").copy(
            node = buildTestNode(1, "title").node.copy(
                projectId = 100L,
                areaId = 200L,
                estimatedMinutes = 30,
                energyLevel = 3
            )
        )
        // Here we provide the required ids, so the left side `id == null` is false and it evaluates right side
        // we'll pass matching values so right side is true
        val result1 = FilterHelper.filterAndSortNodes(
            nodes = listOf(node1), query = "", type = null, status = null, projectId = 100L, areaId = 200L, linkedToId = null, maxMins = 30, energy = 3, friction = null,
            locationContext = null, energyContext = null, deviceContext = null, socialContext = null,
            timeWindowContext = null, timeHorizon = null, relations = emptyList(), sortMode = "relevance"
        )
        assertEquals(1, result1.size)

        // Let's test the other branch of `relations.any`
        val relations = listOf(
            RelationEntity(id = 1, fromNodeId = 1, toNodeId = 1000, relationType = "RELATED"),
            RelationEntity(id = 2, fromNodeId = 2000, toNodeId = 1, relationType = "RELATED")
        )

        // This exercises `it.fromNodeId == node.id && it.toNodeId == linkedToId` (first relation matches)
        val result2 = FilterHelper.filterAndSortNodes(
            nodes = listOf(node1), query = "", type = null, status = null, projectId = null, areaId = null, linkedToId = 1000L, maxMins = null, energy = null, friction = null,
            locationContext = null, energyContext = null, deviceContext = null, socialContext = null,
            timeWindowContext = null, timeHorizon = null, relations = relations, sortMode = "relevance"
        )
        assertEquals(1, result2.size)

        // This exercises `it.fromNodeId == linkedToId && it.toNodeId == node.id` (second relation matches)
        val result3 = FilterHelper.filterAndSortNodes(
            nodes = listOf(node1), query = "", type = null, status = null, projectId = null, areaId = null, linkedToId = 2000L, maxMins = null, energy = null, friction = null,
            locationContext = null, energyContext = null, deviceContext = null, socialContext = null,
            timeWindowContext = null, timeHorizon = null, relations = relations, sortMode = "relevance"
        )
        assertEquals(1, result3.size)
    }

    @Test
    fun testShortCircuitDueAtConditions() {
        // "today" -> due != null && due in now..(now + dayMs)
        // If due is NOT in the range (too early)
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
        val nodePast = buildTestNode(1, "past", dueAt = now - 1000L)
        val nodeWayPast = buildTestNode(2, "way past", dueAt = now - 100L * 24 * 60 * 60 * 1000L)

        listOf("today", "week", "month", "semester", "short").forEach { horizon ->
            val result = FilterHelper.filterAndSortNodes(
                nodes = listOf(nodePast, nodeWayPast), query = "", type = null, status = null, projectId = null, areaId = null, linkedToId = null, maxMins = null, energy = null, friction = null,
                locationContext = null, energyContext = null, deviceContext = null, socialContext = null,
                timeWindowContext = null, timeHorizon = horizon, relations = emptyList(), sortMode = "relevance"
            )
            assertEquals(0, result.size)
        }
    }

    @Test
    fun testShortCircuitForBasicFields() {
        val node1 = buildTestNode(1, "title") // projectId null, areaId null, maxMins null, energy null

        // Pass on projectId (both null)
        val result1 = FilterHelper.filterAndSortNodes(
            nodes = listOf(node1), query = "", type = null, status = null, projectId = null, areaId = null, linkedToId = null, maxMins = null, energy = null, friction = null,
            locationContext = null, energyContext = null, deviceContext = null, socialContext = null,
            timeWindowContext = null, timeHorizon = null, relations = emptyList(), sortMode = "relevance"
        )
        assertEquals(1, result1.size)

        // timeHorizon null due
        val nodeNullDue = buildTestNode(2, "title")
        val result2 = FilterHelper.filterAndSortNodes(
            nodes = listOf(nodeNullDue), query = "", type = null, status = null, projectId = null, areaId = null, linkedToId = null, maxMins = null, energy = null, friction = null,
            locationContext = null, energyContext = null, deviceContext = null, socialContext = null,
            timeWindowContext = null, timeHorizon = "today", relations = emptyList(), sortMode = "relevance"
        )
        assertEquals(0, result2.size)

        // timeHorizon not null due but wrong
        val nodeFutureDue = buildTestNode(3, "title", dueAt = kotlin.time.Clock.System.now().toEpochMilliseconds() + 300L * 24 * 60 * 60 * 1000L)
        listOf("today", "week", "month", "semester", "short").forEach { horizon ->
            val result = FilterHelper.filterAndSortNodes(
                nodes = listOf(nodeFutureDue), query = "", type = null, status = null, projectId = null, areaId = null, linkedToId = null, maxMins = null, energy = null, friction = null,
                locationContext = null, energyContext = null, deviceContext = null, socialContext = null,
                timeWindowContext = null, timeHorizon = horizon, relations = emptyList(), sortMode = "relevance"
            )
            assertEquals(0, result.size)
        }

        // relation not matching second condition
        val relations = listOf(
            RelationEntity(id = 1, fromNodeId = 100, toNodeId = 1, relationType = "RELATED")
        )
        val result3 = FilterHelper.filterAndSortNodes(
            nodes = listOf(node1), query = "", type = null, status = null, projectId = null, areaId = null, linkedToId = 100L, maxMins = null, energy = null, friction = null,
            locationContext = null, energyContext = null, deviceContext = null, socialContext = null,
            timeWindowContext = null, timeHorizon = null, relations = relations, sortMode = "relevance"
        )
        assertEquals(1, result3.size)
    }
}
