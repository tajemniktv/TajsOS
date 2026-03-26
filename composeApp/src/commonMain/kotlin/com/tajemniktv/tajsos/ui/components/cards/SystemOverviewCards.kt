/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.ui.AreaHealthMetrics
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.dash_capacity
import tajsos.composeapp.generated.resources.dash_fragmentation
import tajsos.composeapp.generated.resources.dash_load
import tajsos.composeapp.generated.resources.dash_system_status

@Composable
fun SystemStatusCard(
    load: Int,
    fragmentation: Int,
    warning: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        color = if (warning != null) TactileTheme.Error.copy(alpha = 0.05f) else TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusLg),
        border =
            BorderStroke(
                1.dp,
                if (warning != null) TactileTheme.Error.copy(alpha = 0.3f) else TactileTheme.Border,
            ),
    ) {
        Column(modifier = Modifier.padding(TactileTheme.SpacingLg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(Res.string.dash_system_status),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (warning != null) TactileTheme.Error else TactileTheme.Primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                )
                if (warning != null) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = TactileTheme.Error,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
            Spacer(Modifier.height(TactileTheme.SpacingMd))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                StatusMetric(
                    label = stringResource(Res.string.dash_load),
                    value = "$load%",
                    color = getLoadColor(load),
                )
                StatusMetric(
                    label = stringResource(Res.string.dash_fragmentation),
                    value = "$fragmentation%",
                    color = getLoadColor(fragmentation),
                )
                StatusMetric(
                    label = stringResource(Res.string.dash_capacity),
                    value = "${100 - load}%",
                    color = getLoadColor(100 - load, inverse = true),
                )
            }
            if (warning != null) {
                Spacer(Modifier.height(TactileTheme.SpacingMd))
                Text(
                    warning,
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Error,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
    }
}

@Composable
private fun StatusMetric(
    label: String,
    value: String,
    color: Color,
) {
    Column {
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall,
            color = color,
            fontWeight = FontWeight.Bold,
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = TactileTheme.Muted)
    }
}

private fun getLoadColor(
    value: Int,
    inverse: Boolean = false,
): Color {
    val v = if (inverse) 100 - value else value
    return when
        {
            v > 80 -> TactileTheme.Error
            v > 50 -> TactileTheme.Accent
            else -> TactileTheme.Success
        }
}

@Composable
fun AreaHealthCard(
    area: NodeEntity,
    metrics: AreaHealthMetrics?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val status = metrics?.status ?: "stable"
    val load = metrics?.stressLoad ?: 0
    val (color, statusLabel) =
        when (status)
        {
            "on_fire" -> TactileTheme.Error to "ON FIRE"
            "overloaded" -> TactileTheme.Error to "OVERLOADED"
            "neglected" -> TactileTheme.Muted to "NEGLECTED"
            "active" -> TactileTheme.Primary to "ACTIVE"
            else -> TactileTheme.Success to "STABLE"
        }

    Surface(
        onClick = onClick,
        modifier = modifier.width(190.dp),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
    ) {
        Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
            Text(
                area.title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Text,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                statusLabel,
                style = MaterialTheme.typography.bodySmall,
                color = color,
                fontSize = 10.sp,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "L $load%",
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                )
                Text(
                    "O ${metrics?.openLoops ?: 0} • D ${metrics?.deadlines ?: 0}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Muted,
                )
            }
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { (load / 100f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(2.dp),
                color = color,
                trackColor = color.copy(alpha = 0.1f),
            )
            if (metrics?.isDisappearing == true) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "DISAPPEARING FROM RADAR",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Error,
                )
            }
        }
    }
}
