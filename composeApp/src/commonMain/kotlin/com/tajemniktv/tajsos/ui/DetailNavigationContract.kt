/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui

import com.tajemniktv.tajsos.data.ItemKind
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.itemKindOrNull

/**
 * Central contract for node-detail navigation resolution.
 *
 * All callers should resolve a node id through this contract so every entry point lands on the
 * typed detail route that matches the canonical life-object kind.
 */
object DetailNavigationContract {
    /**
     * Resolves a typed detail route for [nodeId] using the current in-memory [nodes] snapshot.
     *
     * Falls back to note detail when the node cannot be found.
     */
    fun routeForNodeId(
        nodeId: Long,
        nodes: List<NodeWithPin>,
    ): String = routeForNode(nodes.find { it.node.id == nodeId }?.node, nodeId)

    /**
     * Resolves a typed detail route for [node] and [fallbackNodeId].
     */
    fun routeForNode(
        node: NodeEntity?,
        fallbackNodeId: Long,
    ): String =
        when (node?.itemKindOrNull())
        {
            ItemKind.TASK -> {
                Screen.TaskDetail.route.replace(
                    "{taskId}",
                    fallbackNodeId.toString(),
                )
            }

            ItemKind.NOTE -> {
                Screen.NoteDetail.route.replace(
                    "{noteId}",
                    fallbackNodeId.toString(),
                )
            }

            ItemKind.RECORD -> {
                Screen.RecordDetail.route.replace(
                    "{recordId}",
                    fallbackNodeId.toString(),
                )
            }

            ItemKind.PROJECT -> {
                Screen.ProjectDetail.route.replace(
                    "{projectId}",
                    fallbackNodeId.toString(),
                )
            }

            ItemKind.AREA -> {
                Screen.AreaDetail.route.replace(
                    "{areaId}",
                    fallbackNodeId.toString(),
                )
            }

            null -> {
                Screen.NoteDetail.route.replace(
                    "{noteId}",
                    fallbackNodeId.toString(),
                )
            }
        }
}
