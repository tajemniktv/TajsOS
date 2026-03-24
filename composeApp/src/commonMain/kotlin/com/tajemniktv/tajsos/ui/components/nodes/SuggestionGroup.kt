/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved. 
 */

package com.tajemniktv.tajsos.ui.components.nodes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
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
import com.tajemniktv.tajsos.ui.theme.TactileTheme

@Composable
fun SuggestionGroup(
    title: String,
    icon: ImageVector,
    color: Color,
    nodes: List<NodeWithPin>,
    onEditNode: (Long) -> Unit,
    description: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
        Text(
            title,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        if (description != null) Text(
            description,
            style = MaterialTheme.typography.bodySmall,
            color = TactileTheme.Muted
        )
        nodes.take(2).forEach { nodeWithPin ->
            Surface(
                onClick = { onEditNode(nodeWithPin.node.id) },
                modifier = Modifier.fillMaxWidth(),
                color = TactileTheme.Surface,
                shape = RoundedCornerShape(TactileTheme.RadiusSm),
                border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(TactileTheme.SpacingMd),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    ); Spacer(Modifier.width(TactileTheme.SpacingMd)); Text(
                    nodeWithPin.node.title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                }
            }
        }
    }
}
