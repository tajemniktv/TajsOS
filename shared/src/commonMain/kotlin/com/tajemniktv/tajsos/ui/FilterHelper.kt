package com.tajemniktv.tajsos.ui

import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.RelationEntity

object FilterHelper {
    fun filterAndSortNodes(
        nodes: List<NodeWithPin>,
        query: String,
        type: String?,
        status: String?,
        projectId: Long?,
        areaId: Long?,
        linkedToId: Long?,
        maxMins: Int?,
        energy: Int?,
        friction: String?,
        relations: List<RelationEntity>
    ): List<NodeWithPin> {
        val cleanQuery = query.trim()
        val isQueryEmpty = cleanQuery.isBlank()

        return nodes.filter { nodeWithPin ->
            val node = nodeWithPin.node
            val matchesQuery = if (isQueryEmpty) true else matchesQuery(nodeWithPin, cleanQuery)
            val matchesType = type == null || node.type == type

            // Allow mode/status filtering logic (comma separated status like "active,on_hold")
            val matchesStatus = status == null || status.split(",").map { it.trim() }.contains(node.status)

            val matchesProject = projectId == null || node.projectId == projectId
            val matchesArea = areaId == null || node.areaId == areaId
            val matchesMins = maxMins == null || (node.estimatedMinutes ?: 0) <= maxMins
            val matchesEnergy = energy == null || node.energyLevel == energy
            val matchesFriction = friction == null || node.friction == friction
            val matchesLinkedTo = linkedToId == null || relations.any {
                (it.fromNodeId == node.id && it.toNodeId == linkedToId) ||
                        (it.fromNodeId == linkedToId && it.toNodeId == node.id)
            }
            matchesQuery && matchesType && matchesStatus && matchesProject && matchesArea && matchesLinkedTo && matchesMins && matchesEnergy && matchesFriction
        }.sortedWith(
            compareByDescending<NodeWithPin> { it.node.updatedAt }
                .thenByDescending { it.node.id }
        )
    }

    fun matchesQuery(
        nodeWithPin: NodeWithPin,
        query: String,
    ): Boolean {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) return false

        return if (cleanQuery.startsWith("#")) {
            val tagQuery = cleanQuery.substring(1).trim()
            if (tagQuery.isBlank()) {
                false
            } else {
                nodeWithPin.tags.any { it.name.contains(tagQuery, ignoreCase = true) }
            }
        } else {
            val titleMatches = nodeWithPin.node.title.contains(cleanQuery, ignoreCase = true)
            val contentMatches = nodeWithPin.node.content.contains(cleanQuery, ignoreCase = true)
            val tagMatches = nodeWithPin.tags.any { tag -> tag.name.contains(cleanQuery, ignoreCase = true) }
            titleMatches || contentMatches || tagMatches
        }
    }
}
