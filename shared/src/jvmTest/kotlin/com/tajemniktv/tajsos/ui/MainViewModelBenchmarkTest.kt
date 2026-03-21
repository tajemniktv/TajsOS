package com.tajemniktv.tajsos.ui

import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import kotlin.test.Test

class MainViewModelBenchmarkTest {

    @Test
    fun benchmarkAlgorithms() {
        val numProjects = 500
        val numNodesPerProject = 100
        val sevenDaysAgo = 100L

        val projects = (1..numProjects).map {
            NodeEntity(id = it.toLong(), type = "project", title = "Project " + it)
        }

        val nodes = mutableListOf<NodeWithPin>()
        for (p in projects) {
            for (i in 1..numNodesPerProject) {
                val status = listOf("active", "done", "archived").random()
                val updatedAt = if (listOf(true, false).random()) 200L else 0L
                val nodeEntity = NodeEntity(
                    id = (p.id * 1000 + i),
                    projectId = p.id,
                    type = "task",
                    title = "Task",
                    status = status,
                    updatedAt = updatedAt
                )
                nodes.add(NodeWithPin(nodeEntity, null))
            }
        }

        println("Measuring calculateInsights performance with ${projects.size} projects and ${nodes.size} nodes...")

        // Warmup
        for (i in 0..5) {
            runOriginal(nodes, projects, sevenDaysAgo)
            runOptimized(nodes, projects, sevenDaysAgo)
        }

        val t1 = System.nanoTime()
        val result1 = runOriginal(nodes, projects, sevenDaysAgo)
        val t2 = System.nanoTime()
        val timeOriginal = (t2 - t1) / 1_000_000L
        println("Original result size: " + result1.size)

        val t3 = System.currentTimeMillis()
        val result2 = runOptimized(nodes, projects, sevenDaysAgo)
        val t4 = System.currentTimeMillis()
        val timeOptimized = t4 - t3
        println("Optimized result size: ${result2.size}")

        kotlin.test.assertEquals(result1.map { it.id }.toSet(), result2.map { it.id }.toSet(), "The results of original and optimized algorithms should be the same.")

        println("===============================")
        println("Original time: " + timeOriginal + "ms")
        println("Optimized time: " + timeOptimized + "ms")
        if (timeOptimized > 0) {
            println("Improvement: " + (timeOriginal.toDouble() / timeOptimized.toDouble()) + "x")
        }
        println("===============================")
    }

    private fun runOriginal(nodes: List<NodeWithPin>, projects: List<NodeEntity>, sevenDaysAgo: Long): List<NodeEntity> {
        val neglectedProjects = projects.filter { project ->
            val projectNodes = nodes.filter { it.node.projectId == project.id }
            val hasActiveItems = projectNodes.any { it.node.status == "active" }
            val hasRecentCompletions = projectNodes.any { it.node.status == "done" && it.node.updatedAt >= sevenDaysAgo }
            hasActiveItems && !hasRecentCompletions
        }
        return neglectedProjects
    }

    private fun runOptimized(nodes: List<NodeWithPin>, projects: List<NodeEntity>, sevenDaysAgo: Long): List<NodeEntity> {
        val nodesByProjectId = nodes.groupBy { it.node.projectId }
        val neglectedProjects = projects.filter { project ->
            val projectNodes = nodesByProjectId[project.id] ?: emptyList()
            val hasActiveItems = projectNodes.any { it.node.status == "active" }
            val hasRecentCompletions = projectNodes.any { it.node.status == "done" && it.node.updatedAt >= sevenDaysAgo }
            hasActiveItems && !hasRecentCompletions
        }
        return neglectedProjects
    }
}
