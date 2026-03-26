/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

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
        locationContext: String?,
        energyContext: String?,
        deviceContext: String?,
        socialContext: String?,
        timeWindowContext: String?,
        timeHorizon: String?,
        relations: List<RelationEntity>,
    ): List<NodeWithPin> {
        val cleanQuery = query.trim()
        val isQueryEmpty = cleanQuery.isBlank()
        val now =
            kotlin.time.Clock.System
                .now()
                .toEpochMilliseconds()
        val dayMs = 24 * 60 * 60 * 1000L

        return nodes
            .filter { nodeWithPin ->
                val node = nodeWithPin.node
                val matchesQuery = if (isQueryEmpty) true else matchesQuery(nodeWithPin, cleanQuery)
                val matchesType = type == null || node.type == type

                // Allow mode/status filtering logic (comma separated status like "active,on_hold")
                val matchesStatus =
                    status == null || status.split(",").map { it.trim() }.contains(node.status)

                val matchesProject = projectId == null || node.projectId == projectId
                val matchesArea = areaId == null || node.areaId == areaId
                val matchesMins = maxMins == null || (node.estimatedMinutes ?: 0) <= maxMins
                val matchesEnergy = energy == null || node.energyLevel == energy
                val matchesFriction = friction == null || node.friction == friction
                val anyContextFilter =
                    locationContext != null ||
                        energyContext != null ||
                        deviceContext != null ||
                        socialContext != null ||
                        timeWindowContext != null
                val matchesContextScope = !anyContextFilter || node.type == "task"
                val matchesLocationContext =
                    locationContext == null || node.locationContext == locationContext
                val matchesEnergyContext =
                    energyContext == null || node.energyContext == energyContext
                val matchesDeviceContext =
                    deviceContext == null || node.deviceContext == deviceContext
                val matchesSocialContext =
                    socialContext == null || node.socialContext == socialContext
                val matchesTimeWindowContext =
                    timeWindowContext == null || node.timeWindowContext == timeWindowContext
                val matchesTimeHorizon =
                    if (timeHorizon == null) {
                        true
                    } else {
                        val due = node.dueAt
                        when (timeHorizon)
                        {
                            "today" -> due != null && due in now..(now + dayMs)
                            "week" -> due != null && due in now..(now + 7 * dayMs)
                            "month" -> due != null && due in now..(now + 30 * dayMs)
                            "semester" -> due != null && due in now..(now + 120 * dayMs)
                            "short" -> due != null && due in now..(now + 7 * dayMs)
                            "long" -> due != null && due > (now + 30 * dayMs)
                            else -> true
                        }
                    }
                val matchesLinkedTo =
                    linkedToId == null ||
                        relations.any {
                            (it.fromNodeId == node.id && it.toNodeId == linkedToId) ||
                                (it.fromNodeId == linkedToId && it.toNodeId == node.id)
                        }
                matchesQuery &&
                    matchesType &&
                    matchesStatus &&
                    matchesProject &&
                    matchesArea &&
                    matchesLinkedTo &&
                    matchesMins &&
                    matchesEnergy &&
                    matchesFriction &&
                    matchesContextScope &&
                    matchesLocationContext &&
                    matchesEnergyContext &&
                    matchesDeviceContext &&
                    matchesSocialContext &&
                    matchesTimeWindowContext &&
                    matchesTimeHorizon
            }.sortedWith(
                compareByDescending<NodeWithPin> { it.node.updatedAt }
                    .thenByDescending { it.node.id },
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
            val tagMatches =
                nodeWithPin.tags.any { tag -> tag.name.contains(cleanQuery, ignoreCase = true) }
            titleMatches || contentMatches || tagMatches
        }
    }
}
