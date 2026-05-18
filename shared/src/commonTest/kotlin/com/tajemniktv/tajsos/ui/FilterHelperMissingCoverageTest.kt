package com.tajemniktv.tajsos.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import com.tajemniktv.tajsos.data.RelationEntity
import com.tajemniktv.tajsos.data.NodeWithPin

class FilterHelperMissingCoverageTest {

    private fun filter(
        nodes: List<NodeWithPin>,
        projectId: Long? = null,
        areaId: Long? = null,
        linkedToId: Long? = null,
        maxMins: Int? = null,
        energy: Int? = null,
        locationContext: String? = null,
        energyContext: String? = null,
        deviceContext: String? = null,
        socialContext: String? = null,
        timeWindowContext: String? = null,
        timeHorizon: String? = null,
        relations: List<RelationEntity> = emptyList()
    ): List<NodeWithPin> {
        return FilterHelper.filterAndSortNodes(
            nodes = nodes, query = "", type = null, status = null, projectId = projectId, areaId = areaId,
            linkedToId = linkedToId, maxMins = maxMins, energy = energy, friction = null,
            locationContext = locationContext, energyContext = energyContext, deviceContext = deviceContext,
            socialContext = socialContext, timeWindowContext = timeWindowContext, timeHorizon = timeHorizon,
            relations = relations, sortMode = "relevance"
        )
    }


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
        val result = filter(nodes = listOf(node1, node2), locationContext = "home", energyContext = "high", deviceContext = "laptop", socialContext = "solo", timeWindowContext = "evening")

        assertEquals(1, result.size)
        assertEquals(2L, result[0].node.id)
    }

    @Test
    fun testProjectAndAreaAndMinsAndEnergyMismatches() {
        // We want to test where projectId is NOT null but doesn't match
        val nodeProjectMismatch = buildTestNode(1, "title").copy(
            node = buildTestNode(1, "title").node.copy(projectId = 999L)
        )
        val resultProject = filter(nodes = listOf(nodeProjectMismatch), projectId = 1L)
        assertEquals(0, resultProject.size)

        // areaId NOT null but doesn't match
        val nodeAreaMismatch = buildTestNode(2, "title").copy(
            node = buildTestNode(2, "title").node.copy(areaId = 999L)
        )
        val resultArea = filter(nodes = listOf(nodeAreaMismatch), areaId = 1L)
        assertEquals(0, resultArea.size)

        // maxMins NOT null but doesn't match
        val nodeMinsMismatch = buildTestNode(3, "title").copy(
            node = buildTestNode(3, "title").node.copy(estimatedMinutes = 60)
        )
        val resultMins = filter(nodes = listOf(nodeMinsMismatch), maxMins = 30)
        assertEquals(0, resultMins.size)

        // energy NOT null but doesn't match
        val nodeEnergyMismatch = buildTestNode(4, "title").copy(
            node = buildTestNode(4, "title").node.copy(energyLevel = 3)
        )
        val resultEnergy = filter(nodes = listOf(nodeEnergyMismatch), energy = 1)
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
        val result1 = filter(nodes = listOf(node1), energyContext = "low")
        assertEquals(0, result1.size)

        // Fail on device context
        val result2 = filter(nodes = listOf(node1), deviceContext = "phone")
        assertEquals(0, result2.size)

        // Fail on social context
        val result3 = filter(nodes = listOf(node1), socialContext = "pair")
        assertEquals(0, result3.size)

        // Fail on time window context
        val result4 = filter(nodes = listOf(node1), timeWindowContext = "morning")
        assertEquals(0, result4.size)
    }

    @Test
    fun testTimeHorizonsNullDue() {
        val nodeNullDue = buildTestNode(1, "title", "content", type = "task", dueAt = null)

        // All should fail since dueAt is null
        listOf("today", "week", "month", "semester", "short").forEach { horizon ->
            val result = filter(nodes = listOf(nodeNullDue), timeHorizon = horizon)
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
        val result1 = filter(nodes = listOf(node1), linkedToId = 2L, relations = relations)
        assertEquals(1, result1.size)

        // Non match
        val result2 = filter(nodes = listOf(node1), linkedToId = 4L, relations = relations)
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
        val result1 = filter(nodes = listOf(node1), projectId = 100L, areaId = 200L, maxMins = 30, energy = 3)
        assertEquals(0, result1.size)

        // Testing relations where `it.fromNodeId == node.id` is false and `it.fromNodeId == linkedToId` is false
        val relations = listOf(
            RelationEntity(id = 1, fromNodeId = 999, toNodeId = 1000, relationType = "RELATED"),
            RelationEntity(id = 2, fromNodeId = 2000, toNodeId = 999, relationType = "RELATED")
        )
        val result2 = filter(nodes = listOf(node1), linkedToId = 1000L, relations = relations)
        assertEquals(0, result2.size)

        val result3 = filter(nodes = listOf(node1), linkedToId = 2000L, relations = relations)
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
        val result1 = filter(nodes = listOf(node1), projectId = 100L, areaId = 200L, maxMins = 30, energy = 3)
        assertEquals(1, result1.size)

        // Let's test the other branch of `relations.any`
        val relations = listOf(
            RelationEntity(id = 1, fromNodeId = 1, toNodeId = 1000, relationType = "RELATED"),
            RelationEntity(id = 2, fromNodeId = 2000, toNodeId = 1, relationType = "RELATED")
        )

        // This exercises `it.fromNodeId == node.id && it.toNodeId == linkedToId` (first relation matches)
        val result2 = filter(nodes = listOf(node1), linkedToId = 1000L, relations = relations)
        assertEquals(1, result2.size)

        // This exercises `it.fromNodeId == linkedToId && it.toNodeId == node.id` (second relation matches)
        val result3 = filter(nodes = listOf(node1), linkedToId = 2000L, relations = relations)
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
            val result = filter(nodes = listOf(nodePast, nodeWayPast), timeHorizon = horizon)
            assertEquals(0, result.size)
        }
    }

    @Test
    fun testShortCircuitForBasicFields() {
        val node1 = buildTestNode(1, "title") // projectId null, areaId null, maxMins null, energy null

        // Pass on projectId (both null)
        val result1 = filter(nodes = listOf(node1))
        assertEquals(1, result1.size)

        // timeHorizon null due
        val nodeNullDue = buildTestNode(2, "title")
        val result2 = filter(nodes = listOf(nodeNullDue), timeHorizon = "today")
        assertEquals(0, result2.size)

        // timeHorizon not null due but wrong
        val nodeFutureDue = buildTestNode(3, "title", dueAt = kotlin.time.Clock.System.now().toEpochMilliseconds() + 300L * 24 * 60 * 60 * 1000L)
        listOf("today", "week", "month", "semester", "short").forEach { horizon ->
            val result = filter(nodes = listOf(nodeFutureDue), timeHorizon = horizon)
            assertEquals(0, result.size)
        }

        // relation not matching second condition
        val relations = listOf(
            RelationEntity(id = 1, fromNodeId = 100, toNodeId = 1, relationType = "RELATED")
        )
        val result3 = filter(nodes = listOf(node1), linkedToId = 100L, relations = relations)
        assertEquals(1, result3.size)
    }
}
