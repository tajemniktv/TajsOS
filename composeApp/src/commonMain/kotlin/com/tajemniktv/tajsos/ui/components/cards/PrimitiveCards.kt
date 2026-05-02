/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.components.common.mouseClickable
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme

@Composable
fun DashCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        color = TajsOSTheme.CardSurface,
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
        border = BorderStroke(1.dp, TajsOSTheme.GhostBorder),
        content = content,
    )
}

@Composable
fun TactileCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(TajsOSTheme.CardSurface, RoundedCornerShape(TajsOSTheme.RadiusMd))
                .border(1.dp, TajsOSTheme.GhostBorder, RoundedCornerShape(TajsOSTheme.RadiusMd))
                .then(
                    if (onClick != null) {
                        Modifier.mouseClickable(
                            onClick = onClick,
                            onSecondaryClick = onClick,
                            middleClickFallbackToPrimary = true,
                        )
                    } else {
                        Modifier
                    },
                ).padding(TajsOSTheme.SpacingMd),
        contentAlignment = Alignment.CenterStart,
        content = content,
    )
}
