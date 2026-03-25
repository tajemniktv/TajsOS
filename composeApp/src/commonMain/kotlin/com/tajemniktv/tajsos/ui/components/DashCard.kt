/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved. 
 */

package com.tajemniktv.tajsos.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.theme.TactileTheme

/**
 * Renders a clickable, card-styled Surface with the app's themed background, rounded corners, and a subtle border, and displays the provided composable content inside.
 *
 * @param modifier Modifier applied to the Surface.
 * @param onClick Lambda invoked when the card is clicked.
 * @param content Composable slot for the card's inner content.
 */
@Composable
fun DashCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    content: @Composable () -> Unit,
)
{
    Surface(
        onClick = onClick,
        modifier = modifier,
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        content = content,
    )
}
