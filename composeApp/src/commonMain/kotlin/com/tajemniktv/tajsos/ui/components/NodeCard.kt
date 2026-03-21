package com.tajemniktv.tajsos.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NodeCard(
    nodeWithPin: NodeWithPin,
    modifier: Modifier = Modifier,
    onToggleDone: (String) -> Unit,
    onTogglePin: (Boolean) -> Unit,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit = {},
    onArchive: () -> Unit = {}
) {
    val node = nodeWithPin.node
    val isPinnedToToday = nodeWithPin.isPinnedToToday
    val isDone = node.status == "done"
    
    val animatedScale by animateFloatAsState(
        targetValue = if (isDone) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "NodeScale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = animatedScale, scaleY = animatedScale)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, if (isPinnedToToday) TactileTheme.Primary else TactileTheme.Border)
    ) {
        Row(
            modifier = Modifier
                .padding(TactileTheme.SpacingMd)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isDone,
                onCheckedChange = { onToggleDone(if (it) "done" else "active") },
                colors = CheckboxDefaults.colors(checkedColor = TactileTheme.Primary)
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = node.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            textDecoration = if (isDone) TextDecoration.LineThrough else null
                        ),
                        color = if (isDone) TactileTheme.Muted else TactileTheme.Text,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (node.isPinned) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Pinned knowledge",
                            tint = TactileTheme.Primary,
                            modifier = Modifier.padding(start = 4.dp).size(12.dp)
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = node.type.uppercase() + if (node.isRecurring) " // RECURRING" else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Primary
                    )
                    val dueAt = node.dueAt
                    if (dueAt != null) {
                        val due = kotlin.time.Instant.fromEpochMilliseconds(dueAt).toLocalDateTime(TimeZone.currentSystemDefault())
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = TactileTheme.Accent, modifier = Modifier.size(10.dp))
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = "${due.day}/${due.month.number}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TactileTheme.Accent
                        )
                    }
                }
            }
            if (isDone) {
                IconButton(onClick = onArchive) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Archive",
                        tint = TactileTheme.Muted
                    )
                }
            }
            IconButton(onClick = { onTogglePin(!isPinnedToToday) }) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Pin to today",
                    tint = if (isPinnedToToday) TactileTheme.Primary else TactileTheme.Muted.copy(alpha = 0.5f)
                )
            }
        }
    }
}
