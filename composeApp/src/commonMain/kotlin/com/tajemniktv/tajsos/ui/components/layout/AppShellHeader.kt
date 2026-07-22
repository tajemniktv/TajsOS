/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.layout

import com.tajemniktv.tajsos.ui.components.TactileOutlinedTextField
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.tajemniktv.tajsos.ui.components.common.GlassMaterial
import com.tajemniktv.tajsos.ui.components.common.glassChrome
import com.tajemniktv.tajsos.ui.components.common.glassContainerColor
import com.tajemniktv.tajsos.ui.components.common.mouseClickable
import com.tajemniktv.tajsos.ui.components.notifications.NotificationUiModel
import com.tajemniktv.tajsos.ui.components.notifications.TajsNotificationWidget
import com.tajemniktv.tajsos.ui.components.screen.ScreenHeaderBreadcrumb
import com.tajemniktv.tajsos.ui.components.screen.ScreenHeaderModel
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.header_menu
import tajsos.composeapp.generated.resources.header_mode_label
import tajsos.composeapp.generated.resources.header_notifications
import tajsos.composeapp.generated.resources.header_notifications_title
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
    onModeSelect: (Long) -> Unit,
    screenHeader: ScreenHeaderModel,
    onSearchClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    onMenuClick: (() -> Unit)? = null,
) {
    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .glassChrome(
                    shape = RoundedCornerShape(0.dp),
                    material = GlassMaterial.THICK,
                ).then(if (!isDesktop) Modifier.statusBarsPadding() else Modifier),
        color = glassContainerColor(TajsOSTheme.SurfaceLow),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column {
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
                    IconButton(onClick = onMenuClick, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = stringResource(Res.string.header_menu),
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
                    GlobalSearchBar(modifier = Modifier.weight(1f), onClick = onSearchClick)
                    Spacer(Modifier.width(24.dp))
                    HeaderScreenContext(
                        model = screenHeader,
                        modifier = Modifier.width(300.dp),
                    )
                    if (screenHeader.actions != null) {
                        Spacer(Modifier.width(16.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            content = screenHeader.actions,
                        )
                    }
                } else {
                    Spacer(Modifier.width(12.dp))
                    if (screenHeader.actions != null) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            content = screenHeader.actions,
                        )
                        Spacer(Modifier.width(12.dp))
                    }
                }

                HeaderModeSwitcher(
                    currentModeLabel = currentModeLabel,
                    modeOptions = modeOptions,
                    expanded = shellState.modeDropdownExpanded,
                    onExpandedChange = { shellState.modeDropdownExpanded = it },
                ) {
                    onModeSelect(it)
                    shellState.modeDropdownExpanded = false
                }
                Spacer(Modifier.width(10.dp))
                NotificationsPopover(
                    expanded = shellState.notificationsExpanded,
                    notifications = notifications,
                    onExpandedChange = { shellState.notificationsExpanded = it },
                )
            }

            screenHeader.toolbar?.invoke()
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
        Surface(
            color = TajsOSTheme.Primary.copy(alpha = 0.1f),
            shape = RoundedCornerShape(TajsOSTheme.RadiusSm),
        ) {
            Text(
                text = protocolText.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = TajsOSTheme.Primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }
    }
}

@Composable
private fun HeaderScreenContext(
    model: ScreenHeaderModel,
    modifier: Modifier = Modifier,
) {
    if (model.breadcrumbs.isEmpty() && model.title.isNullOrBlank() && model.subtitle.isNullOrBlank()) {
        return
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingXs),
    ) {
        if (model.breadcrumbs.isNotEmpty()) {
            HeaderBreadcrumbs(model.breadcrumbs)
        }
        model.title?.let { title ->
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TajsOSTheme.Text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        model.subtitle?.takeIf { it.isNotBlank() }?.let { subtitle ->
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = TajsOSTheme.Muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun HeaderBreadcrumbs(breadcrumbs: List<ScreenHeaderBreadcrumb>) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingXs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        breadcrumbs.forEachIndexed { index, breadcrumb ->
            if (index > 0) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = TajsOSTheme.Muted,
                )
            }

            Text(
                text = breadcrumb.label,
                style = MaterialTheme.typography.labelMedium,
                color =
                    if (breadcrumb.onClick != null) {
                        TajsOSTheme.Text
                    } else {
                        TajsOSTheme.Muted.copy(
                            alpha = 0.8f,
                        )
                    },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier =
                    if (breadcrumb.onClick != null) {
                        Modifier
                            .semantics {
                                role = Role.Button
                                contentDescription = breadcrumb.label
                            }.mouseClickable(
                                onClick = breadcrumb.onClick,
                                onSecondaryClick = breadcrumb.onClick,
                                middleClickFallbackToPrimary = true,
                            )
                    } else {
                        Modifier
                    },
            )
        }
    }
}

/**
 * Header search entry placeholder prepared as a global shell search point.
 */
@Composable
fun GlobalSearchBar(modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    TactileOutlinedTextField(
        value = "",
        onValueChange = {},
        readOnly = true,
        singleLine = true,
        modifier =
            modifier.mouseClickable(onClick = onClick)
                .glassChrome(
                shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
                material = GlassMaterial.THIN,
            ),
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = TajsOSTheme.Text),
        colors =
            androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = glassContainerColor(TajsOSTheme.SurfaceHigh.copy(alpha = 0.5f)),
                focusedContainerColor = glassContainerColor(TajsOSTheme.SurfaceHighest.copy(alpha = 0.7f)),
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = TajsOSTheme.GhostBorder,
            ),
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
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
    )
}

/**
 * Status + mode dropdown trigger used in the shell header.
 *
 * @param currentModeLabel The label of the currently active mode.
 * @param modeOptions Available modes to switch to.
 * @param expanded Whether the mode dropdown is currently expanded.
 * @param onExpandedChange Callback to update the expanded state.
 * @param modifier The modifier to be applied to the layout.
 * @param onModeSelect Callback when a mode is selected.
 */
@Composable
fun HeaderModeSwitcher(
    currentModeLabel: String,
    modeOptions: List<ShellModeOption>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onModeSelect: (Long) -> Unit,
) {
    val pulseTransition = rememberInfiniteTransition(label = "modePulse")
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 1500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "modePulseAlpha",
    )

    Box(modifier = modifier) {
        Surface(
            onClick = { onExpandedChange(!expanded) },
            modifier =
                Modifier
                    .height(42.dp)
                    .glassChrome(
                        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
                        material = GlassMaterial.REGULAR,
                    ),
            shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
            color = glassContainerColor(TajsOSTheme.SurfaceHigh),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
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
                    text =
                        stringResource(
                            Res.string.header_mode_label,
                            currentModeLabel.uppercase(),
                        ),
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
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
fun NotificationsPopover(
    expanded: Boolean,
    notifications: List<NotificationUiModel>,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Surface(
            modifier =
                Modifier.glassChrome(
                    shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
                    material = GlassMaterial.REGULAR,
                ),
            shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
            color = glassContainerColor(TajsOSTheme.SurfaceHigh),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Box {
                IconButton(
                    onClick = { onExpandedChange(!expanded) },
                    modifier = Modifier.size(42.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = stringResource(Res.string.header_notifications),
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
                            .glassChrome(
                                shape = RoundedCornerShape(TajsOSTheme.RadiusLg),
                                material = GlassMaterial.THICK,
                            ),
                    shape = RoundedCornerShape(TajsOSTheme.RadiusLg),
                    color = glassContainerColor(TajsOSTheme.SurfaceHighest),
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                ) {
                    TajsNotificationWidget(
                        notifications = notifications,
                        title = stringResource(Res.string.header_notifications_title),
                    )
                }
            }
        }
    }
}
