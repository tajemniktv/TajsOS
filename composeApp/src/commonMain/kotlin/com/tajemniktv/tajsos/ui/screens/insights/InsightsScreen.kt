/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.insights

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.EventLogEntity
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.cards.AdvancedSystemCard
import com.tajemniktv.tajsos.ui.components.cards.AreaHealthInsightCard
import com.tajemniktv.tajsos.ui.components.cards.AreaHealthSystemCard
import com.tajemniktv.tajsos.ui.components.cards.AutoReviewCard
import com.tajemniktv.tajsos.ui.components.cards.CompletionCard
import com.tajemniktv.tajsos.ui.components.cards.CorrelationsCard
import com.tajemniktv.tajsos.ui.components.cards.EfficiencyCard
import com.tajemniktv.tajsos.ui.components.cards.FocusInsightCard
import com.tajemniktv.tajsos.ui.components.cards.InsightPatternCard
import com.tajemniktv.tajsos.ui.components.cards.StateAveragesCard
import com.tajemniktv.tajsos.ui.components.cards.VaultInsightCard
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*
import kotlin.time.Instant

/**
 * Render the Insights screen, displaying computed insight cards, pattern alerts, project lists, and recent activity
 * based on the provided view model state.
 *
 * The UI reacts to the view model's current insights, recent logs, and areas, conditionally showing cards and
 * lists (e.g., auto-review, completion/focus/efficiency/vault/system summaries, pattern alerts, neglected and
 * high-entropy projects, and recent activity). Project items call the navigation callback when selected.
 *
 * @param viewModel Source of truth for insights, recent activity, and area/project data.
 * @param onNavigateToProject Callback invoked with a project's id when the user selects a project item.
 */
@Composable
fun InsightsScreen(
    viewModel: MainViewModel,
    onNavigateToProject: (Long) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val surface =
            if (maxWidth >
                900.dp
            ) {
                InsightsDashboardSurface.DESKTOP
            } else {
                InsightsDashboardSurface.MOBILE
            }
        val plan =
            remember(surface) {
                buildInsightsDashboardPlan(
                    surface,
                )
            }
        val context =
            remember(viewModel, onNavigateToProject) {
                InsightsDashboardContext(
                    viewModel,
                    onNavigateToProject,
                )
            }
        Column(modifier = Modifier.fillMaxSize()) {
            plan.primary.forEach { block ->
                InsightsDashboardBlockRegistry
                    .resolve(
                        block.id,
                    )?.invoke(context)
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
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusSm),
        border =
            BorderStroke(
                1.dp,
                TactileTheme.Muted.copy(alpha = 0.1f),
            ),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    project.title.uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    color = TactileTheme.Text,
                )
                Text(
                    "Entropy: ${(entropy * 100).toInt()}% (Unstructured/Postponed)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Muted,
                )
            }
            LinearProgressIndicator(
                progress = { entropy.toFloat() },
                modifier = Modifier.width(60.dp).height(4.dp),
                color = if (entropy > 0.7) TactileTheme.Error else TactileTheme.Primary,
                trackColor = TactileTheme.Border,
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
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusSm),
        border =
            BorderStroke(
                1.dp,
                TactileTheme.Muted.copy(alpha = 0.1f),
            ),
    ) {
        Row(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                timeStr,
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Muted,
                modifier = Modifier.width(48.dp),
            )
            Text(
                log.eventType.replace("_", " "),
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Primary,
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
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusSm),
        border =
            BorderStroke(
                1.dp,
                TactileTheme.Error.copy(alpha = 0.3f),
            ),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = TactileTheme.Error)
            Spacer(Modifier.width(TactileTheme.SpacingMd))
            Column {
                Text(
                    project.title.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = TactileTheme.Text,
                )
                Text(
                    stringResource(Res.string.insights_needs_attention),
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Error,
                )
            }
        }
    }
}
