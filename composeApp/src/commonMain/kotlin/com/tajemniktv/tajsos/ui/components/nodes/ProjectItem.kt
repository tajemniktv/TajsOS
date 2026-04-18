/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.nodes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.ui.components.common.MouseContextMenuHost
import com.tajemniktv.tajsos.ui.components.common.mouseClickable
import com.tajemniktv.tajsos.ui.components.common.rememberMouseContextMenuState
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.common_open

/**
 * Renders a clickable project card for a NodeEntity, optionally showing a percentage and a thin progress bar.
 *
 * @param project Source data; used for the card title and optional content body.
 * @param progress Fraction in the range 0..1 used to compute the displayed percentage and drive the progress indicator.
 * @param totalItems If greater than 0, the percentage text and linear progress indicator are shown; otherwise they are omitted.
 * @param onLongClick Handler invoked on long press; defaults to a no-op.
 * @param onClick Handler invoked on click.
 */
@Composable
fun ProjectItem(
    project: NodeEntity,
    progress: Float,
    totalItems: Int,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit,
) {
    val contextMenuState = rememberMouseContextMenuState()
    MouseContextMenuHost(
        state = contextMenuState,
        modifier = Modifier.fillMaxWidth(),
        menuContent = {
            DropdownMenuItem(
                text = { Text(text = stringResource(Res.string.common_open)) },
                onClick = {
                    contextMenuState.dismiss()
                    onClick()
                },
            )
        },
    ) {
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .mouseClickable(
                        onClick = onClick,
                        onLongClick = onLongClick,
                        onSecondaryClickAt = { contextMenuState.showAt(it) },
                        middleClickFallbackToPrimary = true,
                    ),
            color = TajsOSTheme.CardSurface,
            shape = RoundedCornerShape(TajsOSTheme.RadiusSm),
            border =
                androidx.compose.foundation.BorderStroke(
                    1.dp,
                    TajsOSTheme.Muted.copy(alpha = 0.2f),
                ),
        ) {
            Column(modifier = Modifier.padding(TajsOSTheme.SpacingMd)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        project.title.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = TajsOSTheme.Primary,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                    )
                    if (totalItems > 0) {
                        Text(
                            "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = TajsOSTheme.Muted,
                        )
                    }
                }
                if (project.content.isNotEmpty()) {
                    Text(
                        project.content,
                        style = MaterialTheme.typography.bodySmall,
                        color = TajsOSTheme.Muted,
                    )
                }
                if (totalItems > 0) {
                    Spacer(modifier = Modifier.height(TajsOSTheme.SpacingMd))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(2.dp),
                        color = TajsOSTheme.Primary,
                        trackColor = TajsOSTheme.Muted.copy(alpha = 0.2f),
                    )
                }
            }
        }
    }
}

