/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.graph

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.graph_subtitle
import tajsos.composeapp.generated.resources.graph_title

object GraphDashboardBlockRegistry {
    private val renderers: Map<String, GraphDashboardBlockRenderer> =
        mapOf("graph_main" to ::renderGraphMainBlock)

    fun resolve(id: String): GraphDashboardBlockRenderer? = renderers[id]
}

@Composable
private fun renderGraphMainBlock(context: GraphDashboardContext) {
    GraphMainBlock(viewModel = context.viewModel, onNodeClick = context.onNodeClick)
}

@Composable
internal fun GraphMainBlock(
    viewModel: MainViewModel,
    onNodeClick: (Long) -> Unit,
) {
    val nodes by viewModel.allNodes.collectAsState()
    val relations by viewModel.allRelations.collectAsState()
    val areas by viewModel.allAreas.collectAsState()

    val textMeasurer = rememberTextMeasurer()
    var viewportOffset by remember { mutableStateOf(Offset.Zero) }
    var canvasSize by remember { mutableStateOf(Offset.Zero) }
    var hasInitializedViewport by remember { mutableStateOf(false) }
    val nodePositions =
        remember(nodes, areas) {
            val groupedByArea =
                nodes
                    .map { it.node }
                    .groupBy { it.areaId ?: -1L }
                    .toList()
                    .sortedBy { it.first }

            val areaSpacing = 260f
            val itemSpacing = 72f
            val columns = 4

            groupedByArea
                .flatMapIndexed { groupIndex, (_, areaNodes) ->
                    val areaOrigin =
                        Offset((groupIndex % 3) * areaSpacing, (groupIndex / 3) * areaSpacing)
                    areaNodes
                        .sortedBy { it.id }
                        .mapIndexed { nodeIndex, node ->
                            val row = nodeIndex / columns
                            val column = nodeIndex % columns
                            val nodeOffset = Offset(column * itemSpacing, row * itemSpacing)
                            node.id to (areaOrigin + nodeOffset)
                        }
                }.toMap()
        }
    val renderableNodeIds = remember(nodePositions) { nodePositions.keys }
    if (renderableNodeIds.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Card(
                colors = CardDefaults.cardColors(containerColor = TajsOSTheme.SurfaceLow),
                border = androidx.compose.foundation.BorderStroke(1.dp, TajsOSTheme.Border),
            ) {
                Text(
                    text = "No graph data yet. Create items and relations to populate the graph.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TajsOSTheme.Muted,
                    modifier = Modifier.padding(TajsOSTheme.SpacingMd),
                )
            }
        }
        return
    }
    if (!hasInitializedViewport && canvasSize != Offset.Zero) {
        val positions = nodePositions.values
        val minX = positions.minOf { it.x }
        val minY = positions.minOf { it.y }
        val maxX = positions.maxOf { it.x }
        val maxY = positions.maxOf { it.y }
        val graphCenter = Offset((minX + maxX) / 2f, (minY + maxY) / 2f)
        val viewportCenter = Offset(canvasSize.x / 2f, canvasSize.y / 2f)
        viewportOffset = viewportCenter - graphCenter
        hasInitializedViewport = true
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .onSizeChanged { size ->
                    canvasSize = Offset(size.width.toFloat(), size.height.toFloat())
                }.pointerInput(nodes) {
                    detectTapGestures { tapOffset ->
                        val adjustedTap = tapOffset - viewportOffset
                        val clickedNodeId =
                            nodePositions.entries
                                .find { (_, pos) ->
                                    (pos - adjustedTap).getDistance() < 30f
                                }?.key
                        clickedNodeId?.let { onNodeClick(it) }
                    }
                }.pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        viewportOffset += dragAmount
                    }
                },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            relations.forEach { relation ->
                val start = nodePositions[relation.fromNodeId]
                val end = nodePositions[relation.toNodeId]
                if (start != null && end != null) {
                    drawLine(
                        color =
                            when (relation.relationType)
                            {
                                "BELONGS_TO" -> TajsOSTheme.Primary.copy(alpha = 0.3f)
                                "DEPENDS_ON" -> TajsOSTheme.Error.copy(alpha = 0.3f)
                                else -> Color.White.copy(alpha = 0.1f)
                            },
                        start = start + viewportOffset,
                        end = end + viewportOffset,
                        strokeWidth = 2f,
                    )
                }
            }

            nodes.forEach { nodeWithPin ->
                val pos = nodePositions[nodeWithPin.node.id] ?: return@forEach
                val color =
                    when (nodeWithPin.node.type)
                    {
                        "project" -> {
                            TajsOSTheme.Primary
                        }

                        "area" -> {
                            TajsOSTheme.Accent
                        }

                        "task" -> {
                            if (nodeWithPin.node.status == "done") {
                                TajsOSTheme.Success
                            } else {
                                TajsOSTheme.Muted
                            }
                        }

                        "note", "idea" -> {
                            TajsOSTheme.Accent
                        }

                        else -> {
                            TajsOSTheme.Muted
                        }
                    }

                drawCircle(
                    color = color.copy(alpha = 0.8f),
                    radius = 12f,
                    center = pos + viewportOffset,
                )

                if (nodeWithPin.node.type == "project" || nodeWithPin.node.type == "area") {
                    drawText(
                        textMeasurer = textMeasurer,
                        text = nodeWithPin.node.title.uppercase(),
                        topLeft = pos + viewportOffset + Offset(15f, -10f),
                        style =
                            TextStyle(
                                color = color,
                                fontSize = TextUnit.Unspecified,
                                fontWeight = FontWeight.Bold,
                            ),
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(TajsOSTheme.SpacingMd)) {
            Text(
                stringResource(Res.string.graph_title),
                style = MaterialTheme.typography.labelSmall,
                color = TajsOSTheme.Primary,
            )
            Text(
                stringResource(Res.string.graph_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = TajsOSTheme.Muted,
            )
        }
    }
}
