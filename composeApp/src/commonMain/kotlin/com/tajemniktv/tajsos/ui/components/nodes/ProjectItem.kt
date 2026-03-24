/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.nodes

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.ui.design.theme.TactileTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProjectItem(
    project: NodeEntity,
    progress: Float,
    totalItems: Int,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusSm),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            TactileTheme.Muted.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    project.title.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = TactileTheme.Primary
                )
                if (totalItems > 0) {
                    Text(
                        "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Muted
                    )
                }
            }
            if (project.content.isNotEmpty()) {
                Text(
                    project.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = TactileTheme.Muted
                )
            }
            if (totalItems > 0) {
                Spacer(modifier = Modifier.height(TactileTheme.SpacingSm))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(2.dp),
                    color = TactileTheme.Primary,
                    trackColor = TactileTheme.Muted.copy(alpha = 0.2f)
                )
            }
        }
    }
}
