/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import kotlin.math.roundToInt

/**
 * Displays a horizontal "slide to abort" control with a draggable knob.
 *
 * Dragging the knob to the right invokes the provided `onAbort` callback when the knob reaches 90% of the track; the knob then resets to the start position when the drag ends.
 *
 * @param onAbort Callback invoked when the user slides the knob past the abort threshold. */
@Composable
fun AbortSlider(onAbort: () -> Unit) {
    val maxOffset = 240.dp
    var offset by remember { mutableFloatStateOf(0f) }
    val maxOffsetPx = with(LocalDensity.current) { maxOffset.toPx() }

    Box(
        modifier =
            Modifier
                .width(300.dp)
                .height(56.dp)
                .background(TajsOSTheme.Surface, RoundedCornerShape(TajsOSTheme.RadiusMd))
                .border(1.dp, TajsOSTheme.Muted, RoundedCornerShape(TajsOSTheme.RadiusMd)),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            "SLIDE TO ABORT",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall,
            color = TajsOSTheme.Muted
        )
        Box(
            modifier =
                Modifier
                    .offset { IntOffset(offset.roundToInt(), 0) }
                    .size(56.dp)
                    .background(TajsOSTheme.Error, RoundedCornerShape(TajsOSTheme.RadiusMd))
                    .draggable(
                        orientation = Orientation.Horizontal,
                        state =
                            rememberDraggableState { delta ->
                                val newOffset = (offset + delta).coerceIn(0f, maxOffsetPx)
                                offset = newOffset
                            },
                        onDragStopped = {
                            if (offset >= maxOffsetPx * 0.9f) {
                                onAbort()
                            }
                            offset = 0f
                        },
                    ),
        )
    }
}
