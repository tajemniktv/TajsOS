/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui

import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import kotlin.test.Test

@Suppress("ReplacePrintlnWithLogging")
class MainViewModelBenchmarkTest {
    private val sevenDaysAgo = 100L

    @Test
    fun benchmarkAlgorithms() {
        val numProjects = 500
        val numNodesPerProject = 100

        val projects =
            (1..numProjects).map {
                NodeEntity(id = it.toLong(), type = "project", title = "Project $it")
            }

        val nodes = mutableListOf<NodeWithPin>()
        for (p in projects) {
            for (i in 1..numNodesPerProject) {
                val status = listOf("active", "done", "archived").random()
                val updatedAt = if (listOf(true, false).random()) 200L else 0L
                val nodeEntity =
                    NodeEntity(
                        id = (p.id * 1000 + i),
                        projectId = p.id,
                        type = "task",
                        title = "Task",
                        status = status,
                        updatedAt = updatedAt,
                    )
                nodes.add(NodeWithPin(nodeEntity, null))
            }
        }

        // Warmup
        for (i in 0..5) {
            runOriginal(nodes, projects)
            runOptimized(nodes, projects)
        }

        val t1 = System.nanoTime()
        val result1 = runOriginal(nodes, projects)
        val t2 = System.nanoTime()
        val timeOriginal = (t2 - t1) / 1_000_000L

        val t3 = System.currentTimeMillis()
        val result2 = runOptimized(nodes, projects)
        val t4 = System.currentTimeMillis()
        val timeOptimized = t4 - t3

        kotlin.test.assertTrue(timeOriginal >= 0)
        kotlin.test.assertTrue(timeOptimized >= 0)

        kotlin.test.assertEquals(
            result1.map { it.id }.toSet(),
            result2.map { it.id }.toSet(),
            "The results of original and optimized algorithms should be the same.",
        )
    }

    private fun runOriginal(
        nodes: List<NodeWithPin>,
        projects: List<NodeEntity>,
    ): List<NodeEntity> {
        val neglectedProjects =
            projects.filter { project ->
                val projectNodes = nodes.filter { it.node.projectId == project.id }
                val hasActiveItems = projectNodes.any { it.node.status == "active" }
                val hasRecentCompletions =
                    projectNodes.any { it.node.status == "done" && it.node.updatedAt >= sevenDaysAgo }
                hasActiveItems && !hasRecentCompletions
            }
        return neglectedProjects
    }

    private fun runOptimized(
        nodes: List<NodeWithPin>,
        projects: List<NodeEntity>,
    ): List<NodeEntity> {
        val nodesByProjectId = nodes.groupBy { it.node.projectId }
        val neglectedProjects =
            projects.filter { project ->
                val projectNodes = nodesByProjectId[project.id] ?: emptyList()
                val hasActiveItems = projectNodes.any { it.node.status == "active" }
                val hasRecentCompletions =
                    projectNodes.any { it.node.status == "done" && it.node.updatedAt >= sevenDaysAgo }
                hasActiveItems && !hasRecentCompletions
            }
        return neglectedProjects
    }
}
