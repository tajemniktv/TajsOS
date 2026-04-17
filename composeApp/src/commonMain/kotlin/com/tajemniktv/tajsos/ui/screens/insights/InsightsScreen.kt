/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.insights

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.EventLogEntity
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.screen.ScreenScaffold
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.insights_needs_attention
import kotlin.time.Instant

/**
 * Central insights entry point that collects system state and coordinates layout.
 *
 * @param viewModel Source of insights state.
 * @param onNavigateToProject Project navigation callback.
 * @param onNavigate Navigation callback.
 */
@Composable
fun InsightsRoute(
    viewModel: MainViewModel,
    onNavigateToProject: (Long) -> Unit,
    onNavigate: (String) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val surface = if (maxWidth > 900.dp) InsightsDashboardSurface.DESKTOP else InsightsDashboardSurface.MOBILE
        val plan = remember(surface) { buildInsightsDashboardPlan(surface) }
        val context = remember(viewModel, onNavigateToProject) { InsightsDashboardContext(viewModel, onNavigateToProject) }

        InsightsScreen(
            context = context,
            plan = plan,
            onNavigate = onNavigate,
        )
    }
}

/**
 * Stateless insights screen content.
 *
 * @param context Insights dashboard context.
 * @param plan Insights dashboard plan.
 * @param onNavigate Navigation callback.
 */
@Composable
fun InsightsScreen(
    context: InsightsDashboardContext,
    plan: InsightsDashboardPlan,
    onNavigate: (String) -> Unit,
) {
    ScreenScaffold(
        screen = Screen.Insights,
        onNavigate = onNavigate,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            plan.primary.forEach { block ->
                InsightsDashboardBlockRegistry.resolve(block.id)?.invoke(context)
            }
        }
    }
}

/**
 * Displays a clickable card for a project that shows its title, an entropy percentage label,
 * and a horizontal progress bar representing unstructured/postponed entropy.
 *
 * The progress bar is tinted as an alert when `entropy > 0.7` and uses a muted/primary tint otherwise.
 *
 * @param project The project node whose title is displayed.
 * @param entropy A value between 0.0 and 1.0 representing the proportion of unstructured/postponed work; shown as a percent label and as the progress amount.
 * @param onClick Callback invoked when the card is clicked.
 */
@Composable
fun ProjectEntropyItem(
    project: NodeEntity,
    entropy: Double,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TajsOSTheme.Surface,
        shape = RoundedCornerShape(TajsOSTheme.RadiusSm),
        border =
            BorderStroke(
                1.dp,
                TajsOSTheme.Muted.copy(alpha = 0.1f),
            ),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(TajsOSTheme.SpacingMd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    project.title.uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    color = TajsOSTheme.Text,
                )
                Text(
                    "Entropy: ${(entropy * 100).toInt()}% (Unstructured/Postponed)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TajsOSTheme.Muted,
                )
            }
            LinearProgressIndicator(
                progress = { entropy.toFloat() },
                modifier = Modifier.width(60.dp).height(4.dp),
                color = if (entropy > 0.7) TajsOSTheme.Error else TajsOSTheme.Primary,
                trackColor = TajsOSTheme.Border,
                strokeCap = StrokeCap.Round,
            )
        }
    }
}

/**
 * Renders a single activity log row showing the event time and a human-readable event type.
 *
 * The entry displays the log's timestamp converted from epoch milliseconds to local time as `H:MM`
 * (minutes zero-padded) in a fixed-width column, and the event type with underscores replaced by spaces.
 *
 * @param log The event log entity whose `timestamp` (epoch milliseconds) and `eventType` are displayed.
 */
@Composable
fun ActivityLogItem(log: EventLogEntity) {
    val time =
        Instant
            .fromEpochMilliseconds(log.timestamp)
            .toLocalDateTime(TimeZone.currentSystemDefault())
    val timeStr = "${time.hour}:${time.minute.toString().padStart(2, '0')}"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TajsOSTheme.Surface,
        shape = RoundedCornerShape(TajsOSTheme.RadiusSm),
        border =
            BorderStroke(
                1.dp,
                TajsOSTheme.Muted.copy(alpha = 0.1f),
            ),
    ) {
        Row(
            modifier = Modifier.padding(TajsOSTheme.SpacingMd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                timeStr,
                style = MaterialTheme.typography.labelSmall,
                color = TajsOSTheme.Muted,
                modifier = Modifier.width(48.dp),
            )
            Text(
                log.eventType.replace("_", " "),
                style = MaterialTheme.typography.bodySmall,
                color = TajsOSTheme.Primary,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * Displays a clickable card highlighting a project that needs attention.
 *
 * Shows the project's title in uppercase alongside a warning icon and an attention label,
 * using error-themed styling and border to emphasize urgency. Invokes the provided callback
 * when the card is clicked.
 *
 * @param project The project entity whose title is displayed.
 * @param onClick Callback invoked when the item is clicked.
 */
@Composable
fun NeglectedProjectItem(
    project: NodeEntity,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TajsOSTheme.Surface,
        shape = RoundedCornerShape(TajsOSTheme.RadiusSm),
        border =
            BorderStroke(
                1.dp,
                TajsOSTheme.Error.copy(alpha = 0.3f),
            ),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(TajsOSTheme.SpacingMd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = TajsOSTheme.Error)
            Spacer(Modifier.width(TajsOSTheme.SpacingMd))
            Column {
                Text(
                    project.title.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = TajsOSTheme.Text,
                )
                Text(
                    stringResource(Res.string.insights_needs_attention),
                    style = MaterialTheme.typography.labelSmall,
                    color = TajsOSTheme.Error,
                )
            }
        }
    }
}
