/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme

/**
 * Premium "system-monitor" notification card with futuristic aesthetics.
 */
@Composable
fun TajsNotificationCard(notification: NotificationUiModel, modifier: Modifier = Modifier) {
    val accentColor =
        when (notification.variant)
        {
            NotificationVariant.ALERT -> TajsOSTheme.Error
            NotificationVariant.WARNING -> TajsOSTheme.AccentAmber
            NotificationVariant.SYNC -> TajsOSTheme.AccentBlue
            NotificationVariant.INFO -> TajsOSTheme.AccentCyan
            NotificationVariant.LOW_PRIORITY -> TajsOSTheme.Muted
        }

    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(TajsOSTheme.RadiusMd))
                .clickable { notification.onClick() },
        color = TajsOSTheme.SurfaceLow,
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd)
    ) {
        Column {
            Row(
                modifier = Modifier.padding(TajsOSTheme.SpacingMd),
                verticalAlignment = Alignment.Top
            ) {
                // Icon or Unread Dot
                Box(
                    modifier = Modifier.size(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (notification.isUnread) {
                        Box(
                            modifier =
                                Modifier
                                    .size(8.dp)
                                    .background(accentColor, CircleShape)
                        )
                    } else if (notification.icon != null) {
                        Icon(
                            imageVector = notification.icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        // Default small icon if nothing else
                        Box(
                            modifier =
                                Modifier
                                    .size(4.dp)
                                    .background(accentColor.copy(alpha = 0.5f), CircleShape)
                        )
                    }
                }

                Spacer(Modifier.width(TajsOSTheme.SpacingSm))

                Column(modifier = Modifier.weight(1f)) {
                    // Metadata Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = notification.category.uppercase(),
                            style =
                                MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Black
                                ),
                            color = accentColor.copy(alpha = 0.8f)
                        )
                        Text(
                            text = notification.id.uppercase(),
                            style =
                                MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    letterSpacing = 0.5.sp
                                ),
                            color = TajsOSTheme.Muted.copy(alpha = 0.6f)
                        )
                    }

                    Spacer(Modifier.height(TajsOSTheme.SpacingXs))

                    // Title
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = TajsOSTheme.Text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Body
                    Text(
                        text = notification.body,
                        style = MaterialTheme.typography.bodySmall,
                        color = TajsOSTheme.Muted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Accent Line or Progress Bar
            if (notification.progress != null) {
                LinearProgressIndicator(
                    progress = { notification.progress },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(2.dp),
                    color = accentColor,
                    trackColor = Color.Transparent
                )
            } else {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(accentColor.copy(alpha = 0.15f))
                )
            }
        }
    }
}

/**
 * Container for a list of notification cards.
 */
@Composable
fun TajsNotificationWidget(
    notifications: List<NotificationUiModel>,
    modifier: Modifier = Modifier,
    title: String = "SYSTEM ALERTS"
) {
    Column(
        modifier =
            modifier
                .width(320.dp)
                .padding(TajsOSTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style =
                    MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    ),
                color = TajsOSTheme.Muted
            )
            if (notifications.any { it.isUnread }) {
                Box(
                    modifier =
                        Modifier
                            .size(6.dp)
                            .background(TajsOSTheme.AccentCyan, CircleShape)
                )
            }
        }

        if (notifications.isEmpty()) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = TajsOSTheme.SpacingLg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No active system alerts.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TajsOSTheme.Muted.copy(alpha = 0.5f)
                )
            }
        } else {
            notifications.forEach { notification ->
                TajsNotificationCard(notification = notification)
            }
        }
    }
}

@Preview
@Composable
private fun PreviewNotificationCards() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Column(
            modifier =
                Modifier
                    .background(Color(0xFF0E0E12))
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TajsNotificationCard(
                notification =
                    NotificationUiModel(
                        id = "SYS-001",
                        title = "Database Sync Complete",
                        body = "Local records have been synchronized with the neural node.",
                        category = "SYSTEM",
                        variant = NotificationVariant.SYNC,
                        icon = Icons.Default.Sync
                    )
            )
            TajsNotificationCard(
                notification =
                    NotificationUiModel(
                        id = "SEC-042",
                        title = "Unauthorized Access Attempt",
                        body = "Multiple failed login attempts detected from a remote endpoint.",
                        category = "SECURITY",
                        variant = NotificationVariant.ALERT,
                        isUnread = true,
                        icon = Icons.Default.Warning
                    )
            )
            TajsNotificationCard(
                notification =
                    NotificationUiModel(
                        id = "LOG-128",
                        title = "Maintenance Required",
                        body = "Disk space usage has exceeded the safe operational threshold.",
                        category = "STORAGE",
                        variant = NotificationVariant.WARNING,
                        progress = 0.85f,
                        icon = Icons.Default.Info
                    )
            )
        }
    }
}

@Preview
@Composable
private fun PreviewNotificationWidget() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = Color(0xFF1F1F24)) {
            TajsNotificationWidget(
                notifications =
                    listOf(
                        NotificationUiModel(
                            id = "SYS-001",
                            title = "Sync Complete",
                            body = "Local node up to date.",
                            category = "SYSTEM",
                            variant = NotificationVariant.SYNC
                        ),
                        NotificationUiModel(
                            id = "SEC-042",
                            title = "Alert Triggered",
                            body = "Intrusion detected.",
                            category = "SECURITY",
                            variant = NotificationVariant.ALERT,
                            isUnread = true
                        )
                    )
            )
        }
    }
}
