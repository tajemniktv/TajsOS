package com.tajemniktv.tajsos.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeNodeDao : NodeDao {
    private val nodes = mutableListOf<NodeEntity>()
    private val pins = mutableListOf<TodayPinEntity>()

    private val nodesFlow = MutableStateFlow<List<NodeEntity>>(emptyList())
    private val pinsFlow = MutableStateFlow<List<TodayPinEntity>>(emptyList())

    override fun getAllNodesWithPins(): Flow<List<NodeWithPin>> {
        return nodesFlow.map { nodesList ->
            nodesList.map { node ->
                val pin = pins.find { it.nodeId == node.id }
                NodeWithPin(node, pin)
            }
        }
    }

    override fun getTodayNodes(date: String): Flow<List<NodeEntity>> {
        return nodesFlow.map { nodesList ->
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
        return nodesFlow.map { nodesList ->
            nodesList.filter { node -> node.projectId == projectId && node.status != "archived" }
                .map { node -> NodeWithPin(node, pins.find { it.nodeId == node.id }) }
        }
    }

    override fun getNodesByArea(areaId: Long): Flow<List<NodeEntity>> {
        return nodesFlow.map { it.filter { node -> node.areaId == areaId && node.status != "archived" } }
    }

    override fun getNodesByAreaWithPins(areaId: Long): Flow<List<NodeWithPin>> {
        return nodesFlow.map { nodesList ->
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

    override suspend fun insertNode(node: NodeEntity): Long {
        val index = nodes.indexOfFirst { it.id == node.id }
        if (index != -1 && node.id != 0L) {
            nodes[index] = node
            nodesFlow.value = nodes.toList()
            return node.id
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

    override suspend fun deleteNode(node: NodeEntity) {
        nodes.removeAll { it.id == node.id }
        nodesFlow.value = nodes.toList()
    }

    override suspend fun pinToToday(pin: TodayPinEntity) {
        val newId = (pins.size + 1).toLong()
        val newPin = pin.copy(id = newId)
        pins.removeAll { it.nodeId == pin.nodeId }
        pins.add(newPin)
        pinsFlow.value = pins.toList()
    }

    override suspend fun unpinFromToday(nodeId: Long) {
        pins.removeAll { it.nodeId == nodeId }
        pinsFlow.value = pins.toList()
    }

    override fun isPinnedToToday(nodeId: Long): Flow<Boolean> {
        return pinsFlow.map { pinsList -> pinsList.any { it.nodeId == nodeId } }
    }
}
