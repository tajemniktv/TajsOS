package com.tajemniktv.tajsos

import kotlin.test.Test
import kotlin.system.measureTimeMillis

class PerformanceTest {

    data class Node(val id: Long, val title: String)
    data class NodeWithPin(val node: Node)
    data class Relation(val fromNodeId: Long, val toNodeId: Long)

    @Test
    fun benchmarkNodesFind() {
        // Increase iterations and nodes count to get more pronounced results
        val totalNodes = 100000
        val relationsCount = 5000
        val noteId = 1L

        val nodes = (1..totalNodes).map { NodeWithPin(Node(it.toLong(), "Node $it")) }
        val relations = (1..relationsCount).map { Relation(noteId, it.toLong() + 10) }

        // Warmup
        for (i in 0..100) {
            relations.forEach { relation ->
                val relatedId = if (relation.fromNodeId == noteId) relation.toNodeId else relation.fromNodeId
                nodes.find { it.node.id == relatedId }?.node
            }
        }

        // Baseline O(R * N)
        val time1 = measureTimeMillis {
            var found = 0
            relations.forEach { relation ->
                val relatedId = if (relation.fromNodeId == noteId) relation.toNodeId else relation.fromNodeId
                val relatedNode = nodes.find { it.node.id == relatedId }?.node
                if (relatedNode != null) {
                    found++
                }
            }
        }

        // Warmup optimized
        val warmupNodesMap = nodes.associateBy { it.node.id }
        for (i in 0..100) {
            relations.forEach { relation ->
                val relatedId = if (relation.fromNodeId == noteId) relation.toNodeId else relation.fromNodeId
                warmupNodesMap[relatedId]?.node
            }
        }

        // Optimized O(N)
        val time2 = measureTimeMillis {
            var found = 0
            val nodesMap = nodes.associateBy { it.node.id }
            relations.forEach { relation ->
                val relatedId = if (relation.fromNodeId == noteId) relation.toNodeId else relation.fromNodeId
                val relatedNode = nodesMap[relatedId]?.node
                if (relatedNode != null) {
                    found++
                }
            }
        }

        println("Baseline time: $time1 ms")
        println("Optimized time: $time2 ms")
    }
}
