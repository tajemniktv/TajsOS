/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.notifications

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Visual variants for notifications determining accent color and aesthetic.
 */
enum class NotificationVariant {
    ALERT,
    WARNING,
    SYNC,
    INFO,
    LOW_PRIORITY
}

/**
 * Data model for the premium "system-monitor" notification card.
 */
data class NotificationUiModel(
    val id: String,
    val title: String,
    val body: String,
    val category: String,
    val variant: NotificationVariant = NotificationVariant.INFO,
    val isUnread: Boolean = false,
    val icon: ImageVector? = null,
    val progress: Float? = null,
    val onClick: () -> Unit = {}
)
