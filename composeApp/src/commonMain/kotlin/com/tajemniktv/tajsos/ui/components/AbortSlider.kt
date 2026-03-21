package com.tajemniktv.tajsos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlin.math.roundToInt

@Composable
fun AbortSlider(onAbort: () -> Unit) {
    val maxOffset = 240.dp
    var offset by remember { mutableFloatStateOf(0f) }
    val maxOffsetPx = with(LocalDensity.current) { maxOffset.toPx() }

    Box(
        modifier = Modifier
            .width(300.dp)
            .height(56.dp)
            .background(TactileTheme.Surface, RoundedCornerShape(TactileTheme.RadiusMd))
            .border(1.dp, TactileTheme.Muted, RoundedCornerShape(TactileTheme.RadiusMd)),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            "SLIDE TO ABORT",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall,
            color = TactileTheme.Muted
        )
        Box(
            modifier = Modifier
                .offset { IntOffset(offset.roundToInt(), 0) }
                .size(56.dp)
                .background(TactileTheme.Error, RoundedCornerShape(TactileTheme.RadiusMd))
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        val newOffset = (offset + delta).coerceIn(0f, maxOffsetPx)
                        offset = newOffset
                    },
                    onDragStopped = {
                        if (offset >= maxOffsetPx * 0.9f) {
                            onAbort()
                        }
                        offset = 0f
                    }
                )
        )
    }
}
