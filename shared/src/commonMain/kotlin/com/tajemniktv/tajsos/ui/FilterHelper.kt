/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui

import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.RelationEntity
import com.tajemniktv.tajsos.data.isTaskItem
import com.tajemniktv.tajsos.data.matchesItemFilter

/**
 * A utility object that provides functions to filter, search, and sort lists of nodes
 * based on varying criteria like context, time horizons, energy levels, and query strings.
 */
object FilterHelper {
    /**
     * Filters a list of nodes based on a wide array of optional parameters, returning
     * only the nodes that match all specified criteria, and sorts the final list
     * based on their modification date (newest first).
     *
     * Nodes must meet all non-null conditions (logical AND) to be included in the results.
     * Context filters (location, energy, device, social, time-window) are only applied
     * strictly if the node is task-shaped work in the collapsed LifeOS model.
     *
     * @param nodes The initial list of nodes with associated pins and tags to filter.
     * `@param` query A text query for partial-matching against titles, content, or tags (prefix with # to search tags only).
     * @param type The specific type of node to include (e.g., "task", "project").
     * @param status A comma-separated string of statuses to include (e.g., "active,on_hold").
     * @param projectId The ID of the project the node must belong to.
     * @param areaId The ID of the area the node must belong to.
     * @param linkedToId The ID of another node that this node must have a bidirectional relationship with.
     * @param maxMins The maximum estimated minutes allowed for the node.
     * @param energy The specific energy level required for the node.
     * @param friction The friction level required for the node.
     * @param locationContext The required location context (e.g., "home", "office").
     * @param energyContext The required energy context (e.g., "high", "low").
     * @param deviceContext The required device context (e.g., "laptop", "phone").
     * @param socialContext The required social context (e.g., "solo", "pair").
     * @param timeWindowContext The required time window context (e.g., "morning", "evening").
     * @param timeHorizon A string determining the required temporal scope relative to the current time (e.g., "today", "week", "month", "semester", "short", "long").
     * @param relations The complete list of relationship entities used to evaluate bidirectional links for the `linkedToId` filter.
     * @return A list containing only the matching `NodeWithPin` elements, sorted by descending update time and descending ID.
     */
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
                val matchesType = node.matchesItemFilter(type)

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
                val matchesContextScope = !anyContextFilter || node.isTaskItem()
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

    /**
     * Determines whether a given node matches a user's search query.
     *
     * The match is evaluated based on the format of the query. If the query starts with
     * a hashtag (#), it performs a partial, case-insensitive match against the node's tags.
     * Otherwise, it performs a case-insensitive partial match against the node's title,
     * content, or tags.
     *
     * @param nodeWithPin The wrapper object containing the node entity and its associated tags.
     * @param query The raw query string input by the user. Must not be blank for a valid match.
     * @return `true` if the node matches the search query; `false` otherwise.
     */
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
