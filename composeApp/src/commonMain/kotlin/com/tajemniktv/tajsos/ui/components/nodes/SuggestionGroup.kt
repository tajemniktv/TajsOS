/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.nodes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme

/**
 * Renders a titled suggestion section displaying up to two clickable node entries with an optional description.
 *
 * @param title Section header text.
 * @param icon Icon shown for each node row.
 * @param color Color used for the title text, icon tint, and row border.
 * @param nodes Candidate nodes to display; at most the first two items are rendered.
 * @param onEditNode Callback invoked when a node row is clicked; receives the selected node's `id`.
 * @param description Optional secondary text shown under the title when non-null.
 */
@Composable
fun SuggestionGroup(
    title: String,
    icon: ImageVector,
    color: Color,
    nodes: List<NodeWithPin>,
    onEditNode: (Long) -> Unit,
    description: String? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)) {
        Text(
            title,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
        )
        if (description != null) {
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = TajsOSTheme.Muted,
            )
        }
        nodes.take(2).forEach { nodeWithPin ->
            Surface(
                onClick = { onEditNode(nodeWithPin.node.id) },
                modifier = Modifier.fillMaxWidth(),
                color = TajsOSTheme.CardSurface,
                shape = RoundedCornerShape(TajsOSTheme.RadiusSm),
                border = BorderStroke(1.dp, TajsOSTheme.GhostBorder),
            ) {
                Row(
                    modifier = Modifier.padding(TajsOSTheme.SpacingMd),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(TajsOSTheme.SpacingMd))
                    Text(
                        nodeWithPin.node.title,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}
