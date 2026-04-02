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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.tajemniktv.tajsos.ui.components.common.glassChrome
import com.tajemniktv.tajsos.ui.components.notifications.NotificationUiModel
import com.tajemniktv.tajsos.ui.components.notifications.TajsNotificationWidget
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import dev.chrisbanes.haze.HazeState
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.header_search_placeholder

/**
 * Mode option model shown by the header mode switcher.
 *
 * @property id Unique identifier for the mode.
 * @property name Display name of the mode.
 * @property color Theme color associated with the mode.
 * @property isSelectable Whether this mode can be actively selected by the user.
 */
data class ShellModeOption(
    val id: Long,
    val name: String,
    val color: Color,
    val isSelectable: Boolean = true,
)

/**
 * Fixed shell header with greeting, global search, mode switcher, and notifications popover.
 *
 * @param greeting The greeting message (e.g., "Good morning, user").
 * @param protocolText The text indicating current protocol state.
 * @param currentModeLabel The label of the currently active mode.
 * @param modeOptions Available modes to switch to.
 * @param notifications List of current notifications.
 * @param shellState The current UI state of the app shell components.
 * @param isDesktop Whether the current environment is a desktop layout.
 * @param hazeState Shared haze state used for shell glass surfaces.
 * @param onModeSelect Callback when a mode is selected from the switcher.
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
fun AppShellHeader(
    greeting: String,
    protocolText: String,
    currentModeLabel: String,
    modeOptions: List<ShellModeOption>,
    notifications: List<NotificationUiModel>,
    shellState: AppShellState,
    isDesktop: Boolean,
    hazeState: HazeState,
    onModeSelect: (Long) -> Unit,
    modifier: Modifier = Modifier,
    onMenuClick: (() -> Unit)? = null,
) {
    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .glassChrome(
                    hazeState = hazeState,
                    shape = RoundedCornerShape(0.dp),
                )
                .then(if (!isDesktop) Modifier.statusBarsPadding() else Modifier),
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = if (isDesktop) 24.dp else 16.dp,
                        vertical = if (isDesktop) 16.dp else 12.dp,
                    ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!isDesktop && onMenuClick != null) {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = TajsOSTheme.Text,
                    )
                }
                Spacer(Modifier.width(8.dp))
            }

            HeaderGreeting(
                greeting = greeting,
                protocolText = protocolText,
                modifier = if (isDesktop) Modifier.width(320.dp) else Modifier.weight(1f),
            )

            if (isDesktop) {
                Spacer(Modifier.width(24.dp))
                GlobalSearchBar(modifier = Modifier.weight(1f))
                Spacer(Modifier.width(24.dp))
            } else {
                Spacer(Modifier.width(12.dp))
            }

            HeaderModeSwitcher(
                currentModeLabel = currentModeLabel,
                modeOptions = modeOptions,
                expanded = shellState.modeDropdownExpanded,
                onExpandedChange = { shellState.modeDropdownExpanded = it },
                hazeState = hazeState,
            ) {
                onModeSelect(it)
                shellState.modeDropdownExpanded = false
            }
            Spacer(Modifier.width(10.dp))
            NotificationsPopover(
                expanded = shellState.notificationsExpanded,
                notifications = notifications,
                onExpandedChange = { shellState.notificationsExpanded = it },
                hazeState = hazeState,
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
            color = TajsOSTheme.Text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = protocolText.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = TajsOSTheme.Muted,
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
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = TajsOSTheme.Text),
        placeholder = {
            Text(
                text = stringResource(Res.string.header_search_placeholder),
                style = MaterialTheme.typography.bodyMedium,
                color = TajsOSTheme.Muted,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = TajsOSTheme.Muted,
            )
        },
        shape = RoundedCornerShape(12.dp),
    )
}

/**
 * Status + mode dropdown trigger used in the shell header.
 *
 * @param currentModeLabel The label of the currently active mode.
 * @param modeOptions Available modes to switch to.
 * @param expanded Whether the mode dropdown is currently expanded.
 * @param onExpandedChange Callback to update the expanded state.
 * @param hazeState Shared haze state used for shell glass surfaces.
 * @param modifier The modifier to be applied to the layout.
 * @param onModeSelect Callback when a mode is selected.
 */
@Composable
fun HeaderModeSwitcher(
    currentModeLabel: String,
    modeOptions: List<ShellModeOption>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
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

    Box(modifier = modifier) {
        Surface(
            onClick = { onExpandedChange(!expanded) },
            modifier = Modifier.glassChrome(hazeState = hazeState, shape = RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            color = Color.Transparent,
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
                            .background(TajsOSTheme.Success, CircleShape),
                )
                Text(
                    text = "SYSTEM: ${currentModeLabel.uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TajsOSTheme.Text,
                    maxLines = 1,
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TajsOSTheme.Muted,
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            containerColor = TajsOSTheme.SurfaceHigh,
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
                                color = if (option.isSelectable) TajsOSTheme.Text else TajsOSTheme.Muted,
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
 *
 * @param expanded Whether the notification popover is expanded.
 * @param notifications List of current notifications.
 * @param onExpandedChange Callback to update the expanded state.
 * @param hazeState Shared haze state used for shell glass surfaces.
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
fun NotificationsPopover(
    expanded: Boolean,
    notifications: List<NotificationUiModel>,
    onExpandedChange: (Boolean) -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Surface(
            modifier = Modifier.glassChrome(hazeState = hazeState, shape = RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            color = Color.Transparent,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Box {
                IconButton(
                    onClick = { onExpandedChange(!expanded) },
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = TajsOSTheme.Text,
                    )
                }

                if (notifications.any { it.isUnread }) {
                    Box(
                        modifier =
                            Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 8.dp, end = 8.dp)
                                .size(6.dp)
                                .background(TajsOSTheme.AccentCyan, CircleShape),
                    )
                }
            }
        }

        if (expanded) {
            Popup(
                alignment = Alignment.TopEnd,
                onDismissRequest = { onExpandedChange(false) },
                properties = PopupProperties(focusable = true),
            ) {
                Surface(
                    modifier =
                        Modifier
                            .padding(top = 48.dp, end = 16.dp)
                            .glassChrome(hazeState = hazeState, shape = RoundedCornerShape(TajsOSTheme.RadiusLg)),
                    shape = RoundedCornerShape(TajsOSTheme.RadiusLg),
                    color = Color.Transparent,
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                ) {
                    TajsNotificationWidget(
                        notifications = notifications,
                        title = "SYSTEM STATUS",
                    )
                }
            }
        }
    }
}
