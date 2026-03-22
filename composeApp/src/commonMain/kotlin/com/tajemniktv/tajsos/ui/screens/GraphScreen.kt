/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlin.random.Random

@Composable
fun GraphScreen(viewModel: MainViewModel, onNodeClick: (Long) -> Unit) {
    val nodes by viewModel.allNodes.collectAsState()
    val relations by viewModel.allRelations.collectAsState()

    var offset by remember { mutableStateOf(Offset.Zero) }
    val nodePositions = remember(nodes) {
        nodes.associate {
            it.node.id to Offset(
                Random.nextFloat() * 1000f,
                Random.nextFloat() * 1000f
            )
        }.toMutableMap()
    }

    Box(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
        detectDragGestures { change, dragAmount ->
            change.consume()
            offset += dragAmount
        }
    }) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw relations (lines)
            relations.forEach { relation ->
                val start = nodePositions[relation.fromNodeId]
                val end = nodePositions[relation.toNodeId]
                if (start != null && end != null) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.2f),
                        start = start + offset,
                        end = end + offset,
                        strokeWidth = 1f
                    )
                }
            }

            // Draw nodes
            nodes.forEach { nodeWithPin ->
                val pos = nodePositions[nodeWithPin.node.id] ?: Offset.Zero
                drawCircle(
                    color = when (nodeWithPin.node.type) {
                        "project" -> TactileTheme.Primary
                        "area" -> TactileTheme.Accent
                        else -> TactileTheme.Muted
                    },
                    radius = 8f,
                    center = pos + offset
                )
            }
        }

        Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
            Text(
                "GRAPH VIEW (EXPERIMENTAL)",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary
            )
            Text(
                "Drag to pan. Nodes are randomly placed.",
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Muted
            )
        }
    }
}
