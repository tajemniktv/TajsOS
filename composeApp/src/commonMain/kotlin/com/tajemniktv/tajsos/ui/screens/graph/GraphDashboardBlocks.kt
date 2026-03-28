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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.TextUnit
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.graph_subtitle
import tajsos.composeapp.generated.resources.graph_title
import kotlin.random.Random

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
    var offset by remember { mutableStateOf(Offset(500f, 500f)) }
    val nodePositions =
        remember(nodes, areas) {
            val areaCenters =
                areas.associate { area ->
                    area.id to Offset(Random.nextFloat() * 2000f, Random.nextFloat() * 2000f)
                }
            val unassignedCenter = Offset(1000f, 1000f)

            nodes.associate { nodeWithPin ->
                val center = areaCenters[nodeWithPin.node.areaId] ?: unassignedCenter
                nodeWithPin.node.id to
                    Offset(
                        center.x + (Random.nextFloat() - 0.5f) * 500f,
                        center.y + (Random.nextFloat() - 0.5f) * 500f,
                    )
            }
        }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .pointerInput(nodes) {
                    detectTapGestures { tapOffset ->
                        val adjustedTap = tapOffset - offset
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
                        offset += dragAmount
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
                                "BELONGS_TO" -> TactileTheme.Primary.copy(alpha = 0.3f)
                                "DEPENDS_ON" -> TactileTheme.Error.copy(alpha = 0.3f)
                                else -> Color.White.copy(alpha = 0.1f)
                            },
                        start = start + offset,
                        end = end + offset,
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
                            TactileTheme.Primary
                        }

                        "area" -> {
                            TactileTheme.Accent
                        }

                        "task" -> {
                            if (nodeWithPin.node.status == "done") {
                                TactileTheme.Success
                            } else {
                                TactileTheme.Muted
                            }
                        }

                        "note", "idea" -> {
                            TactileTheme.Accent
                        }

                        else -> {
                            TactileTheme.Muted
                        }
                    }

                drawCircle(
                    color = color.copy(alpha = 0.8f),
                    radius = 12f,
                    center = pos + offset,
                )

                if (nodeWithPin.node.type == "project" || nodeWithPin.node.type == "area") {
                    drawText(
                        textMeasurer = textMeasurer,
                        text = nodeWithPin.node.title.uppercase(),
                        topLeft = pos + offset + Offset(15f, -10f),
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
