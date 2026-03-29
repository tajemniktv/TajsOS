/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.layout

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.theme.TactileTheme

/**
 * Mode option model shown by the header mode switcher.
 */
data class ShellModeOption(
    val id: Long,
    val name: String,
    val color: Color,
    val isSelectable: Boolean = true,
)

/**
 * Fixed shell header with greeting, global search, mode switcher, and notifications popover.
 */
@Composable
fun AppShellHeader(
    greeting: String,
    protocolText: String,
    currentModeLabel: String,
    modeOptions: List<ShellModeOption>,
    notifications: List<String>,
    shellState: AppShellState,
    onModeSelect: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = TactileTheme.SurfaceLow,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HeaderGreeting(
                greeting = greeting,
                protocolText = protocolText,
                modifier = Modifier.width(320.dp),
            )
            Spacer(Modifier.width(24.dp))
            GlobalSearchBar(modifier = Modifier.weight(1f))
            Spacer(Modifier.width(24.dp))
            HeaderModeSwitcher(
                currentModeLabel = currentModeLabel,
                modeOptions = modeOptions,
                expanded = shellState.modeDropdownExpanded,
                onExpandedChange = { shellState.modeDropdownExpanded = it },
                onModeSelect = {
                    onModeSelect(it)
                    shellState.modeDropdownExpanded = false
                },
            )
            Spacer(Modifier.width(10.dp))
            NotificationsPopover(
                expanded = shellState.notificationsExpanded,
                notifications = notifications,
                onExpandedChange = { shellState.notificationsExpanded = it },
            )
        }
    }
}

/**
 * Left-side greeting and protocol state in the shell header.
 */
@Composable
fun HeaderGreeting(
    greeting: String,
    protocolText: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = greeting,
            style = MaterialTheme.typography.titleLarge,
            color = TactileTheme.Text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = protocolText.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = TactileTheme.Muted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * Header search entry placeholder prepared as a global shell search point.
 */
@Composable
fun GlobalSearchBar(modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = "",
        onValueChange = {},
        readOnly = true,
        singleLine = true,
        modifier = modifier,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = TactileTheme.Text),
        placeholder = {
            Text(
                text = "Search across TajsOS",
                style = MaterialTheme.typography.bodyMedium,
                color = TactileTheme.Muted,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = TactileTheme.Muted,
            )
        },
        shape = RoundedCornerShape(12.dp),
    )
}

/**
 * Status + mode dropdown trigger used in the shell header.
 */
@Composable
fun HeaderModeSwitcher(
    currentModeLabel: String,
    modeOptions: List<ShellModeOption>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onModeSelect: (Long) -> Unit,
) {
    val pulseTransition = rememberInfiniteTransition(label = "modePulse")
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 900, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "modePulseAlpha",
    )

    Box {
        Surface(
            onClick = { onExpandedChange(!expanded) },
            shape = RoundedCornerShape(12.dp),
            color = TactileTheme.SurfaceHigh,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(8.dp)
                            .alpha(pulseAlpha)
                            .background(TactileTheme.Success, CircleShape),
                )
                Text(
                    text = "SYSTEM: ${currentModeLabel.uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Text,
                    maxLines = 1,
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TactileTheme.Muted,
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            containerColor = TactileTheme.SurfaceHigh,
        ) {
            modeOptions.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                modifier =
                                    Modifier
                                        .size(8.dp)
                                        .background(option.color, CircleShape),
                            )
                            Text(
                                text = option.name,
                                color = if (option.isSelectable) TactileTheme.Text else TactileTheme.Muted,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    },
                    onClick = {
                        if (option.isSelectable) onModeSelect(option.id)
                    },
                )
            }
        }
    }
}

/**
 * Bell trigger + anchored lightweight notifications panel.
 */
@Composable
fun NotificationsPopover(
    expanded: Boolean,
    notifications: List<String>,
    onExpandedChange: (Boolean) -> Unit,
) {
    Box {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = TactileTheme.SurfaceHigh,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            IconButton(
                onClick = { onExpandedChange(!expanded) },
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = TactileTheme.Text,
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            containerColor = TactileTheme.SurfaceHigh,
        ) {
            if (notifications.isEmpty()) {
                DropdownMenuItem(
                    text = {
                        Text(
                            "No notifications",
                            color = TactileTheme.Muted,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    onClick = { onExpandedChange(false) },
                )
            } else {
                notifications.take(4).forEach { item ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                item,
                                color = TactileTheme.Text,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        onClick = { onExpandedChange(false) },
                    )
                }
            }
        }
    }
}
