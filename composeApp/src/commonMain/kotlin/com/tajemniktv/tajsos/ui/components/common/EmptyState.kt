/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.empty_state_default_desc

/**
 * Renders a centered empty-state UI with an icon, a primary message, and an optional secondary description.
 *
 * The icon displays a subtle pulsing tint to draw attention; when `description` is `null` the secondary text is omitted.
 *
 * @param message Primary text displayed below the icon.
 * @param modifier Modifier applied to the root container.
 * @param icon Icon shown above the text.
 * @param description Optional secondary text shown under the primary message; pass `null` to hide it.
 * @param fillParent When `true`, the component expands to available size; set `false` for inline/card usage.
 * @param showContainer When `true`, wraps content in a bordered container. Disable when parent already provides chrome.
 * @param content Optional content (e.g., buttons) rendered below the primary text.
 */
@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Info,
    description: String? = stringResource(Res.string.empty_state_default_desc),
    fillParent: Boolean = true,
    showContainer: Boolean = true,
    pulseIcon: Boolean = showContainer,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    val infiniteTransition = rememberInfiniteTransition(label = "EmptyStatePulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = if (pulseIcon) 0.1f else 0.6f,
        targetValue = 0.6f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "PulseAlpha",
    )

    Box(
        modifier = if (fillParent) modifier.fillMaxSize() else modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        val contentComposable: @Composable () -> Unit = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier =
                    Modifier
                        .defaultMinSize(minHeight = if (fillParent) 200.dp else if (showContainer) 120.dp else 80.dp)
                        .padding(if (fillParent) TajsOSTheme.SpacingXl else if (showContainer) TajsOSTheme.SpacingLg else TajsOSTheme.SpacingMd),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(if (fillParent) 48.dp else if (showContainer) 40.dp else 32.dp),
                    tint = TajsOSTheme.Primary.copy(alpha = alpha),
                )
Spacer(modifier = Modifier.height(if (fillParent) TajsOSTheme.SpacingMd else if (showContainer) TajsOSTheme.SpacingSm else TajsOSTheme.SpacingXs))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TajsOSTheme.Muted,
                    textAlign = TextAlign.Center,
                )
                if (description != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                        color = TajsOSTheme.Primary.copy(alpha = 0.2f),
                    )
                }

                content()
            }
        }

        if (showContainer) {
            Surface(
                color = Color.Transparent,
                shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
                border =
                    androidx.compose.foundation.BorderStroke(
                        1.dp,
                        TajsOSTheme.Primary.copy(alpha = 0.2f),
                    ),
            ) {
                contentComposable()
            }
        } else {
            contentComposable()
        }
    }
}
