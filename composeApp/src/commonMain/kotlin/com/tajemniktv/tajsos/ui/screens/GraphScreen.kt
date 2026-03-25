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
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlin.random.Random

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*

/**
 * Renders an interactive, pannable graph of nodes and relations driven by the provided view model.
 *
 * The UI displays nodes and connecting relations, allows dragging to pan the canvas, and detects taps on nodes.
 *
 * @param viewModel Provides the streams of nodes, relations, and areas used to populate and position the graph.
 * @param onNodeClick Called with the tapped node's id when the user taps a node on the graph.
 */
@Composable
fun GraphScreen(viewModel: MainViewModel, onNodeClick: (Long) -> Unit)
{
    val nodes by viewModel.allNodes.collectAsState()
    val relations by viewModel.allRelations.collectAsState()
    val areas by viewModel.allAreas.collectAsState()

    val textMeasurer = rememberTextMeasurer()
    var offset by remember { mutableStateOf(Offset(500f, 500f)) }
    val nodePositions = remember(nodes, areas) {
        val areaCenters = areas.associate { area ->
            area.id to Offset(Random.nextFloat() * 2000f, Random.nextFloat() * 2000f)
        }
        val unassignedCenter = Offset(1000f, 1000f)

        nodes.associate { nodeWithPin ->
            val center = areaCenters[nodeWithPin.node.areaId] ?: unassignedCenter
            nodeWithPin.node.id to Offset(
                center.x + (Random.nextFloat() - 0.5f) * 500f,
                center.y + (Random.nextFloat() - 0.5f) * 500f,
            )
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().pointerInput(nodes) {
            detectTapGestures { tapOffset ->
                val adjustedTap = tapOffset - offset
                val clickedNodeId = nodePositions.entries.find { (_, pos) ->
                    (pos - adjustedTap).getDistance() < 30f
                }?.key
                clickedNodeId?.let { onNodeClick(it) }
            }
        }.pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
                change.consume()
                offset += dragAmount
            }
        },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw relations (lines)
            relations.forEach { relation ->
                val start = nodePositions[relation.fromNodeId]
                val end = nodePositions[relation.toNodeId]
                if (start != null && end != null)
                {
                    drawLine(
                        color = when (relation.relationType)
                        {
                            "BELONGS_TO" -> TactileTheme.Primary.copy(alpha = 0.3f)
                            "DEPENDS_ON" -> TactileTheme.Error.copy(alpha = 0.3f)
                            else         -> Color.White.copy(alpha = 0.1f)
                        },
                        start = start + offset,
                        end = end + offset,
                        strokeWidth = 2f,
                    )
                }
            }

            // Draw nodes
            nodes.forEach { nodeWithPin ->
                val pos = nodePositions[nodeWithPin.node.id] ?: return@forEach
                val color = when (nodeWithPin.node.type)
                {
                    "project" -> TactileTheme.Primary
                    "area" -> TactileTheme.Accent
                    "task" -> if (nodeWithPin.node.status == "done") TactileTheme.Success else TactileTheme.Muted
                    "note", "idea" -> TactileTheme.Accent
                    else -> TactileTheme.Muted
                }

                drawCircle(
                    color = color.copy(alpha = 0.8f),
                    radius = 12f,
                    center = pos + offset,
                )

                if (nodeWithPin.node.type == "project" || nodeWithPin.node.type == "area")
                {
                    drawText(
                        textMeasurer = textMeasurer,
                        text = nodeWithPin.node.title.uppercase(),
                        topLeft = pos + offset + Offset(15f, -10f),
                        style = androidx.compose.ui.text.TextStyle(
                            color = color,
                            fontSize = androidx.compose.ui.unit.TextUnit.Unspecified,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        ),
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
            Text(
                stringResource(Res.string.graph_title),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
            )
            Text(
                stringResource(Res.string.graph_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Muted,
            )
        }
    }
}
