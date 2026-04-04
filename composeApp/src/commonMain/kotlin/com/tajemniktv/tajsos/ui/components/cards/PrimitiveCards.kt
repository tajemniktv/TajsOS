/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.components.common.GlassMaterial
import com.tajemniktv.tajsos.ui.components.common.glassContainerColor
import com.tajemniktv.tajsos.ui.components.common.glassChrome
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme

@Composable
fun DashCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.glassChrome(shape = RoundedCornerShape(TajsOSTheme.RadiusMd), material = GlassMaterial.REGULAR),
        color = glassContainerColor(TajsOSTheme.Surface),
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
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
                .glassChrome(shape = RoundedCornerShape(TajsOSTheme.RadiusMd), material = GlassMaterial.REGULAR)
                .background(glassContainerColor(TajsOSTheme.Surface), RoundedCornerShape(TajsOSTheme.RadiusMd))
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(TajsOSTheme.SpacingMd),
        contentAlignment = Alignment.CenterStart,
        content = content,
    )
}
