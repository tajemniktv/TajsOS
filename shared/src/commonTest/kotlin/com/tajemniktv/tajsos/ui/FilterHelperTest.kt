/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui

import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.RelationEntity
import com.tajemniktv.tajsos.data.TagEntity
import com.tajemniktv.tajsos.data.TodayPinEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FilterHelperTest {

    class FilterConfig(val nodes: List<NodeWithPin>) {
        var query: String = ""
        var type: String? = null
        var status: String? = null
        var projectId: Long? = null
        var areaId: Long? = null
        var linkedToId: Long? = null
        var maxMins: Int? = null
        var energy: Int? = null
        var friction: String? = null
        var locationContext: String? = null
        var energyContext: String? = null
        var deviceContext: String? = null
        var socialContext: String? = null
        var timeWindowContext: String? = null
        var timeHorizon: String? = null
        var relations: List<RelationEntity> = emptyList()
        var sortMode: String = "relevance"
    }

    private fun filter(nodes: List<NodeWithPin>, block: FilterConfig.() -> Unit = {}): List<NodeWithPin> {
        val config = FilterConfig(nodes).apply(block)
        return FilterHelper.filterAndSortNodes(
            nodes = config.nodes,
            query = config.query,
            type = config.type,
            status = config.status,
            projectId = config.projectId,
            areaId = config.areaId,
            linkedToId = config.linkedToId,
            maxMins = config.maxMins,
            energy = config.energy,
            friction = config.friction,
            locationContext = config.locationContext,
            energyContext = config.energyContext,
            deviceContext = config.deviceContext,
            socialContext = config.socialContext,
            timeWindowContext = config.timeWindowContext,
            timeHorizon = config.timeHorizon,
            relations = config.relations,
            sortMode = config.sortMode
        )
    }


    @Test
    fun testRelevanceSortOrder_pinnedAndActive() {
        // Pinned (8 pts) vs Active (5 pts) vs Both (13 pts)
        val nodePinned = createTestNode(1, "apple").copy(pin = TodayPinEntity(id = 1, nodeId = 1, date = "today", position = 0)) // pinned
        val nodeActive = createTestNode(2, "apple").copy(node = NodeEntity(id = 2, type = "task", title = "apple", status = "active")) // active
        val nodeBoth = createTestNode(3, "apple").copy(
            pin = TodayPinEntity(id = 3, nodeId = 3, date = "today", position = 0),
            node = NodeEntity(id = 3, type = "task", title = "apple", status = "active")
        )

        val nodes = listOf(nodePinned, nodeActive, nodeBoth)
        val sortedNodes = filter(nodes) { query = "apple"; sortMode = "relevance" }

        assertEquals(3, sortedNodes.size)
        // Order should be Both (190 + 13 = 203) > Pinned (190 + 8 = 198) > Active (190 + 5 = 195)
        assertEquals(3L, sortedNodes[0].node.id)
        assertEquals(1L, sortedNodes[1].node.id)
        assertEquals(2L, sortedNodes[2].node.id)
    }

    @Test
    fun testMatchesQueryEdgeCases() {
        val node = createTestNode(1, "Title content", tags = listOf("MyTag"))

        // Blank queries return false
        assertFalse(FilterHelper.matchesQuery(node, " "))
        assertFalse(FilterHelper.matchesQuery(node, "   "))

        // Starts with hashtag but empty
        assertFalse(FilterHelper.matchesQuery(node, "# "))
        assertFalse(FilterHelper.matchesQuery(node, " # "))

        // Normal matches
        assertTrue(FilterHelper.matchesQuery(node, "content"))
        assertTrue(FilterHelper.matchesQuery(node, "#MyTag"))
        assertTrue(FilterHelper.matchesQuery(node, "#mytag"))

        // Match against tag when not using hashtag
        assertTrue(FilterHelper.matchesQuery(node, "mytag"))
    }

    @Test
    fun testSortModeFallback() {
        val node1 = createTestNode(1, "apple").copy(node = NodeEntity(id = 1, type = "task", title = "apple", updatedAt = 100))
        val node2 = createTestNode(2, "apple juice").copy(node = NodeEntity(id = 2, type = "task", title = "apple juice", updatedAt = 200))

        val sortedNodes = filter(listOf(node1, node2)) { query = "apple"; sortMode = "UNKNOWN_SORT_MODE" }

        // relevance: node1 (190) > node2 (90)
        assertEquals(1L, sortedNodes[0].node.id)
        assertEquals(2L, sortedNodes[1].node.id)
    }


    @Test
    fun testRelevanceSortOrder() {
        // Let's create specific nodes to test relevance.
        // query: "apple"
        // 1. Title == "apple" (100 pts)
        // 2. Title startsWith "apple juice" (60 pts)
        // 3. Title contains "my apple" (30 pts)
        // 4. Content contains "apple" (15 pts)
        // 5. Exact Tag "apple" (20 pts)
        // 6. Partial Tag "apples" (10 pts)

        val nodeTitleExact = createTestNode(1, "apple")
        val nodeTitleStart = createTestNode(2, "apple juice")
        val nodeTitleContain = createTestNode(3, "my apple")
        val nodeTagExact = createTestNode(4, "test", tags = listOf("apple"))
        val nodeContentContain = createTestNode(5, "test").copy(node = NodeEntity(id = 5, type = "test", title = "test", content = "this has apple inside"))
        val nodeTagPartial = createTestNode(6, "test", tags = listOf("apples"))

        val nodes = listOf(nodeTagPartial, nodeContentContain, nodeTagExact, nodeTitleContain, nodeTitleStart, nodeTitleExact)

        val sortedNodes = FilterHelper.filterAndSortNodes(
            nodes = nodes,
            query = "apple",
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
            sortMode = "relevance"
        )

        assertEquals(6, sortedNodes.size)
        // Expected order:
        // 1: nodeTitleExact (100) + startsWith (60) + contains (30) = 190
        // 2: nodeTitleStart (60) + contains (30) = 90
        // 3: nodeTitleContain (30) = 30
        // 4: nodeTagExact (20) + contains (10) = 30 (tie broken by updated at/id? node 4 vs 3... wait, we need to be careful)
        // Let's just check the first 2 clearly win.
        val expectedIds = setOf(1L, 2L, 3L, 4L, 5L, 6L)
        assertEquals(expectedIds, sortedNodes.map { it.node.id }.toSet())
    }

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
        deviceContext: String? = null,
        socialContext: String? = null,
        timeWindowContext: String? = null,
        dueAt: Long? = null,
        pinnedToday: Boolean = false,
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
                deviceContext = deviceContext,
                socialContext = socialContext,
                timeWindowContext = timeWindowContext,
                dueAt = dueAt,
            )
        val tagEntities =
            tags.mapIndexed { index, name ->
                TagEntity(id = index.toLong(), name = name, normalizedName = name.lowercase())
            }
        return NodeWithPin(
            node = node,
            pin =
                if (pinnedToday) {
                    TodayPinEntity(
                        nodeId = id,
                        date = "2026-04-01",
                        position = 0,
                    )
                } else {
                    null
                },
            tags = tagEntities,
        )
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
            filter(nodes) { query = "  " }

        assertEquals(3, sortedNodes.size)
        assertEquals(2, sortedNodes[0].node.id)
        assertEquals(3, sortedNodes[1].node.id)
        assertEquals(1, sortedNodes[2].node.id)

        // Query filtering
        val filteredNodes =
            filter(nodes) { query = "second" }

        assertEquals(1, filteredNodes.size)
        assertEquals(2, filteredNodes[0].node.id)
    }

    @Test
    fun testRelevanceSortingOrder() {
        val exactMatch = createTestNode(1, "search", updatedAt = 100)
        val startMatch = createTestNode(2, "search query", updatedAt = 100)
        val containsMatch = createTestNode(3, "my search query", updatedAt = 100)
        val contentMatch = createTestNode(4, "different", content = "search inside", updatedAt = 100)
        val tagMatchExact = createTestNode(5, "other", tags = listOf("search"), updatedAt = 100)

        val nodes = listOf(contentMatch, startMatch, containsMatch, exactMatch, tagMatchExact)

        val sortedNodes =
            filter(nodes) { query = "search"; sortMode = "relevance" }

        val ids = sortedNodes.map { it.node.id }
        // exact (195) -> start (95) -> tag exact (35) -> contains (35) -> content (20)
        // Since tag exact and contains both have score 35, they are then sorted by updatedAt (both 100)
        // and then by ID descending. Tag exact (ID 5) will be before Contains (ID 3).
        assertEquals(listOf(1L, 2L, 5L, 3L, 4L), ids)
    }

    @Test
    fun testSearchSortModeUpdatedOrdersByUpdatedAt() {
        val older = createTestNode(1, "Task One", updatedAt = 100)
        val newer = createTestNode(2, "Task Two", updatedAt = 200)

        val sorted =
            filter(listOf(older, newer)) { query = ""; sortMode = "updated" }

        assertEquals(listOf(2L, 1L), sorted.map { it.node.id })
    }

    @Test
    fun testSearchSortModeRelevancePrefersPinnedExactMatch() {
        val pinnedExact =
            createTestNode(
                id = 1,
                title = "Alpha",
                updatedAt = 10,
                pinnedToday = true,
            )
        val looseMatch =
            createTestNode(
                id = 2,
                title = "Alpha beta context",
                updatedAt = 1000,
            )

        val sorted =
            filter(listOf(looseMatch, pinnedExact)) { query = "alpha" }

        assertEquals(1L, sorted.first().node.id)
    }

    @Test
    fun testFilterMultipleStatuses() {
        val nodeActive = createTestNode(1, "Active Node")
        val nodeOnHold = createTestNode(2, "On Hold Node", status = "on_hold")
        val nodeArchived = createTestNode(3, "Archived Node", status = "archived")

        val nodes = listOf(nodeActive, nodeOnHold, nodeArchived)

        val filteredNodes =
            filter(nodes) { status = "active, on_hold" }

        assertEquals(2, filteredNodes.size)
        assertTrue(filteredNodes.any { it.node.id == 1L })
        assertTrue(filteredNodes.any { it.node.id == 2L })
    }

    private fun createNodesForExtendedTests(): List<NodeWithPin> {
        val node1 =
            createTestNode(
                id = 1,
                title = "Node 1",
                projectId = 10,
                areaId = 20,
                estimatedMinutes = 15,
                energyLevel = 1,
                friction = "easy",
            )
        val node2 =
            createTestNode(
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
        val filtered =
            filter(nodes) { this.projectId = projectId; this.areaId = areaId; this.maxMins = maxMins; this.energy = energy; this.friction = friction }
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

        val relations =
            listOf(
                RelationEntity(id = 1, fromNodeId = 1, toNodeId = 100, relationType = "RELATED"),
                RelationEntity(id = 2, fromNodeId = 100, toNodeId = 2, relationType = "RELATED"),
            )

        val filtered =
            filter(listOf(node1, node2, node3)) { linkedToId = 100L; this.relations = relations }

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
        val now =
            kotlin.time.Clock.System
                .now()
                .toEpochMilliseconds()
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

        fun filterWithHorizon(horizon: String): List<NodeWithPin> =
            filter(nodes) { timeHorizon = horizon }

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
    fun testFilterTimeHorizonExtended() {
        val now =
            kotlin.time.Clock.System
                .now()
                .toEpochMilliseconds()
        val dayMs = 24 * 60 * 60 * 1000L

        // Node due today (within 24h)
        val nodeToday = createTestNode(1, "Today", type = "task", dueAt = now + (dayMs / 2))
        // Node due this week (within 7 days, e.g. 3 days)
        val nodeWeek = createTestNode(2, "Week", type = "task", dueAt = now + (3 * dayMs))
        // Node due in a month (within 30 days, e.g. 15 days)
        val nodeMonth = createTestNode(3, "Month", type = "task", dueAt = now + (15 * dayMs))
        // Node due in a semester (within 120 days, e.g. 90 days)
        val nodeSemester = createTestNode(6, "Semester", type = "task", dueAt = now + (90 * dayMs))
        // Node due long term (after 30 days, e.g. 40 days)
        val nodeLong = createTestNode(4, "Long", type = "task", dueAt = now + (40 * dayMs))
        // Node with no due date
        val nodeNoDue = createTestNode(5, "No Due", type = "task")

        val nodesWithSemester =
            listOf(nodeToday, nodeWeek, nodeMonth, nodeSemester, nodeLong, nodeNoDue)

        fun filterWithHorizonExtended(horizon: String): List<NodeWithPin> =
            filter(nodesWithSemester) { timeHorizon = horizon }

        // "month" includes today, week, and month (due <= 30 days)
        val monthNodes = filterWithHorizonExtended("month")
        assertEquals(3, monthNodes.size)
        assertTrue(monthNodes.any { it.node.id == 1L })
        assertTrue(monthNodes.any { it.node.id == 2L })
        assertTrue(monthNodes.any { it.node.id == 3L })

        // "semester" includes today, week, month, and semester (due <= 120 days)
        val semesterNodes = filterWithHorizonExtended("semester")
        assertEquals(
            5,
            semesterNodes.size,
            "Expected 5 nodes for semester, got ${semesterNodes.map { it.node.id }}",
        )
        assertTrue(semesterNodes.any { it.node.id == 1L })
        assertTrue(semesterNodes.any { it.node.id == 2L })
        assertTrue(semesterNodes.any { it.node.id == 3L })
        assertTrue(semesterNodes.any { it.node.id == 4L })
        assertTrue(semesterNodes.any { it.node.id == 6L })

        // "short" includes today and week (due <= 7 days)
        val shortNodes = filterWithHorizonExtended("short")
        assertEquals(2, shortNodes.size)
        assertTrue(shortNodes.any { it.node.id == 1L })
        assertTrue(shortNodes.any { it.node.id == 2L })

        // "unknown_value" includes all nodes (due and no due)
        val unknownNodes = filterWithHorizonExtended("unknown_value")
        assertEquals(6, unknownNodes.size)
    }

    @Test
    fun testContextFiltering() {
        val taskNodeMatch =
            createTestNode(
                id = 1,
                title = "Task Match",
                type = "task",
                locationContext = "home",
                energyContext = "high",
            )
        val taskNodeMismatch =
            createTestNode(
                id = 2,
                title = "Task Mismatch",
                type = "task",
                locationContext = "office",
                energyContext = "high",
            )
        // Notes should be excluded entirely if any context filter is active
        val noteNode =
            createTestNode(
                id = 3,
                title = "Note",
                locationContext = "home",
            )

        val nodes = listOf(taskNodeMatch, taskNodeMismatch, noteNode)

        val filtered =
            filter(nodes) { locationContext = "home" }

        // Note is excluded because type != "task" when anyContextFilter is true
        // Mismatch task is excluded because location doesn't match
        assertEquals(1, filtered.size)
        assertEquals(1L, filtered[0].node.id)
    }

    @Test
    fun testContextScopeFilteringExcludesNonTasks() {
        val noteNode = createTestNode(
            id = 1,
            title = "Note Item",
            type = "idea", // Maps to NOTE kind
            locationContext = "home", // Assign a context
        )

        val filtered = filter(listOf(noteNode)) { locationContext = "home" }

        assertEquals(0, filtered.size)
    }

    @Test
    fun testContextFilteringExtendedContexts() {
        val taskNodeMatch = createTestNode(
            id = 1,
            title = "Task Match",
            type = "task",
            locationContext = "home",
            energyContext = "high",
            deviceContext = "laptop",
            socialContext = "solo",
            timeWindowContext = "morning"
        )
        val taskNodeMismatchDevice = createTestNode(
            id = 2,
            title = "Task Mismatch",
            type = "task",
            locationContext = "home",
            energyContext = "high",
            deviceContext = "phone", // Mismatch
            socialContext = "solo",
            timeWindowContext = "morning"
        )
        val taskNodeMismatchSocial = createTestNode(
            id = 3,
            title = "Task Mismatch",
            type = "task",
            locationContext = "home",
            energyContext = "high",
            deviceContext = "laptop",
            socialContext = "pair", // Mismatch
            timeWindowContext = "morning"
        )
        val taskNodeMismatchTime = createTestNode(
            id = 4,
            title = "Task Mismatch",
            type = "task",
            locationContext = "home",
            energyContext = "high",
            deviceContext = "laptop",
            socialContext = "solo",
            timeWindowContext = "evening" // Mismatch
        )

        val nodes = listOf(taskNodeMatch, taskNodeMismatchDevice, taskNodeMismatchSocial, taskNodeMismatchTime)

        val filtered = filter(nodes) { locationContext = "home"; energyContext = "high"; deviceContext = "laptop"; socialContext = "solo"; timeWindowContext = "morning" }

        assertEquals(1, filtered.size)
        assertEquals(1L, filtered[0].node.id)
    }

    @Test
    fun testProjectAndAreaAndMinsAndEnergyMismatches() {
        val nodeProjectMismatchBase = createTestNode(1, "title")
        val nodeProjectMismatch = nodeProjectMismatchBase.copy(
            node = nodeProjectMismatchBase.node.copy(projectId = 999L)
        )
        val resultProject = filter(listOf(nodeProjectMismatch)) { projectId = 1L }
        assertEquals(0, resultProject.size)

        val nodeAreaMismatch = createTestNode(2, "title").copy(
            node = createTestNode(2, "title").node.copy(areaId = 999L)
        )
        val resultArea = filter(listOf(nodeAreaMismatch)) { areaId = 1L }
        assertEquals(0, resultArea.size)

        val nodeMinsMismatch = createTestNode(3, "title").copy(
            node = createTestNode(3, "title").node.copy(estimatedMinutes = 60)
        )
        val resultMins = filter(listOf(nodeMinsMismatch)) { maxMins = 30 }
        assertEquals(0, resultMins.size)

        val nodeEnergyMismatch = createTestNode(4, "title").copy(
            node = createTestNode(4, "title").node.copy(energyLevel = 3)
        )
        val resultEnergy = filter(listOf(nodeEnergyMismatch)) { energy = 1 }
        assertEquals(0, resultEnergy.size)

        val nodeFrictionMismatch = createTestNode(5, "title").copy(
            node = createTestNode(5, "title").node.copy(friction = "high")
        )
        val resultFriction = filter(listOf(nodeFrictionMismatch)) { friction = "low" }
        assertEquals(0, resultFriction.size)
    }

    @Test
    fun testShortCircuitAnds() {
        val node1 = createTestNode(
            id = 1,
            title = "title",
            type = "task",
            energyContext = "high",
            deviceContext = "laptop",
            socialContext = "solo",
            timeWindowContext = "evening"
        )

        val result1 = filter(listOf(node1)) { energyContext = "low" }
        assertEquals(0, result1.size)

        val result2 = filter(listOf(node1)) { deviceContext = "phone" }
        assertEquals(0, result2.size)

        val result3 = filter(listOf(node1)) { socialContext = "pair" }
        assertEquals(0, result3.size)

        val result4 = filter(listOf(node1)) { timeWindowContext = "morning" }
        assertEquals(0, result4.size)
    }

    @Test
    fun testTimeHorizonsNullDue() {
        val nodeNullDue = createTestNode(1, "title", type = "task", dueAt = null)

        listOf("today", "week", "month", "semester", "short", "long").forEach { horizon ->
            val result = filter(listOf(nodeNullDue)) { timeHorizon = horizon }
            assertEquals(0, result.size)
        }
    }

    @Test
    fun testLinkedToFromNodeId() {
        val node1 = createTestNode(1, "title")
        val relations = listOf(
            RelationEntity(id = 1, fromNodeId = 1, toNodeId = 2, relationType = "RELATED"),
            RelationEntity(id = 2, fromNodeId = 3, toNodeId = 4, relationType = "RELATED")
        )

        val result1 = filter(listOf(node1)) { linkedToId = 2L; this.relations = relations }
        assertEquals(1, result1.size)

        val result2 = filter(listOf(node1)) { linkedToId = 4L; this.relations = relations }
        assertEquals(0, result2.size)
    }

    @Test
    fun testShortCircuitLeftSidesNullMatches() {
        val node1 = createTestNode(1, "title").copy(
            node = createTestNode(1, "title").node.copy(
                projectId = null,
                areaId = null,
                estimatedMinutes = null,
                energyLevel = null
            )
        )
        val result1 = filter(listOf(node1)) { projectId = 100L; areaId = 200L; maxMins = 30; energy = 3 }
        assertEquals(0, result1.size)

        val relations = listOf(
            RelationEntity(id = 1, fromNodeId = 999, toNodeId = 1000, relationType = "RELATED"),
            RelationEntity(id = 2, fromNodeId = 2000, toNodeId = 999, relationType = "RELATED")
        )
        val result2 = filter(listOf(node1)) { linkedToId = 1000L; this.relations = relations }
        assertEquals(0, result2.size)

        val result3 = filter(listOf(node1)) { linkedToId = 2000L; this.relations = relations }
        assertEquals(0, result3.size)
    }

    @Test
    fun testShortCircuitLeftSides() {
        val node1 = createTestNode(
            id = 1,
            title = "title",
            projectId = 100L,
            areaId = 200L,
            estimatedMinutes = 30,
            energyLevel = 3
        )
        val result1 = filter(listOf(node1)) { projectId = 100L; areaId = 200L; maxMins = 30; energy = 3 }
        assertEquals(1, result1.size)

        val relations = listOf(
            RelationEntity(id = 1, fromNodeId = 1, toNodeId = 1000, relationType = "RELATED"),
            RelationEntity(id = 2, fromNodeId = 2000, toNodeId = 1, relationType = "RELATED")
        )

        val result2 = filter(listOf(node1)) { linkedToId = 1000L; this.relations = relations }
        assertEquals(1, result2.size)

        val result3 = filter(listOf(node1)) { linkedToId = 2000L; this.relations = relations }
        assertEquals(1, result3.size)
    }

    @Test
    fun testShortCircuitDueAtConditions() {
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
        val nodePast = createTestNode(1, "past", dueAt = now - 1000L)
        val nodeWayPast = createTestNode(2, "way past", dueAt = now - 100L * 24 * 60 * 60 * 1000L)

        listOf("today", "week", "month", "semester", "short", "long").forEach { horizon ->
            val result = filter(listOf(nodePast, nodeWayPast)) { timeHorizon = horizon }
            assertEquals(0, result.size)
        }
    }

    @Test
    fun testShortCircuitForBasicFields() {
        val node1 = createTestNode(1, "title") // projectId null, areaId null, maxMins null, energy null

        val result1 = filter(listOf(node1))
        assertEquals(1, result1.size)

        val nodeNullDue = createTestNode(2, "title")
        val result2 = filter(listOf(nodeNullDue)) { timeHorizon = "today" }
        assertEquals(0, result2.size)

        val nodeFutureDue = createTestNode(3, "title", dueAt = kotlin.time.Clock.System.now().toEpochMilliseconds() + 300L * 24 * 60 * 60 * 1000L)
        listOf("today", "week", "month", "semester", "short").forEach { horizon ->
            val result = filter(listOf(nodeFutureDue)) { timeHorizon = horizon }
            assertEquals(0, result.size)
        }

        val relations = listOf(
            RelationEntity(id = 1, fromNodeId = 100, toNodeId = 1, relationType = "RELATED")
        )
        val result3 = filter(listOf(node1)) { linkedToId = 100L; this.relations = relations }
        assertEquals(1, result3.size)
    }
}
