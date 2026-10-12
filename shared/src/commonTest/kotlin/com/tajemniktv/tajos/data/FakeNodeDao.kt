package com.tajemniktv.tajos.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine

/** Observes node and pin storage through separate fake DAO ownership boundaries. */
class FakeNodeDao(private val todayPins: FakeTodayPinDao = FakeTodayPinDao()) : NodeDao, TodayPinDao by todayPins {
    private val nodes = mutableListOf<NodeEntity>()

    private val nodesFlow = MutableStateFlow<List<NodeEntity>>(emptyList())

    override fun getAllNodesWithPins(): Flow<List<NodeWithPin>> {
        return combine(nodesFlow, todayPins.rows) { nodesList, pins ->
            nodesList.map { node ->
                val pin = pins.find { it.nodeId == node.id }
                NodeWithPin(node, pin)
            }
        }
    }

    override fun getTodayNodes(date: String): Flow<List<NodeEntity>> {
        return combine(nodesFlow, todayPins.rows) { nodesList, pins ->
            nodesList.filter { node ->
                node.status == "active" && pins.any { it.nodeId == node.id && it.date == date }
            }
        }
    }

    override fun getNodesByType(type: String): Flow<List<NodeEntity>> {
        return nodesFlow.map { it.filter { node -> node.type == type && node.status != "archived" } }
    }

    override fun getNodesByProject(projectId: Long): Flow<List<NodeEntity>> {
        return nodesFlow.map { it.filter { node -> node.projectId == projectId && node.status != "archived" } }
    }

    override fun getNodesByProjectWithPins(projectId: Long): Flow<List<NodeWithPin>> {
        return combine(nodesFlow, todayPins.rows) { nodesList, pins ->
            nodesList.filter { node -> node.projectId == projectId && node.status != "archived" }
                .map { node -> NodeWithPin(node, pins.find { it.nodeId == node.id }) }
        }
    }

    override fun getNodesByArea(areaId: Long): Flow<List<NodeEntity>> {
        return nodesFlow.map { it.filter { node -> node.areaId == areaId && node.status != "archived" } }
    }

    override fun getNodesByAreaWithPins(areaId: Long): Flow<List<NodeWithPin>> {
        return combine(nodesFlow, todayPins.rows) { nodesList, pins ->
            nodesList.filter { node -> node.areaId == areaId && node.status != "archived" }
                .map { node -> NodeWithPin(node, pins.find { it.nodeId == node.id }) }
        }
    }

    override fun getProjectsByArea(areaId: Long): Flow<List<NodeEntity>> {
        return nodesFlow.map { it.filter { node -> node.areaId == areaId && node.type == "project" && node.status != "archived" } }
    }


    override suspend fun getNodeById(id: Long): NodeEntity? {
        return nodes.find { it.id == id }
    }

    override suspend fun getNodesByIds(ids: List<Long>): List<NodeEntity> {
        return nodes.filter { it.id in ids }
    }

    override suspend fun insertNode(node: NodeEntity): Long {
        val index = nodes.indexOfFirst { it.id == node.id }
        if (index != -1 && node.id != 0L) {
            nodes[index] = node
            nodesFlow.value = nodes.toList()
            return -1L
        } else {
            val newId = if (node.id != 0L) node.id else (nodes.maxOfOrNull { it.id } ?: 0L) + 1L
            val newNode = node.copy(id = newId)
            nodes.add(newNode)
            nodesFlow.value = nodes.toList()
            return newId
        }
    }

    override suspend fun insertNodes(nodes: List<NodeEntity>): List<Long> {
        return nodes.map { insertNode(it) }
    }


    override suspend fun updateNode(node: NodeEntity) {
        val index = nodes.indexOfFirst { it.id == node.id }
        if (index != -1) {
            nodes[index] = node
            nodesFlow.value = nodes.toList()
        }
    }

    override suspend fun updateNodes(updatedNodes: List<NodeEntity>) {
        var changed = false
        updatedNodes.forEach { node ->
            val index = nodes.indexOfFirst { it.id == node.id }
            if (index != -1) {
                nodes[index] = node
                changed = true
            }
        }
        if (changed) {
            nodesFlow.value = nodes.toList()
        }
    }

    override suspend fun deleteNode(node: NodeEntity) {
        nodes.removeAll { it.id == node.id }
        nodesFlow.value = nodes.toList()
    }

}
