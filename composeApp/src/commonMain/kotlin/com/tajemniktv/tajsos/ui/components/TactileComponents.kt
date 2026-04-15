/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import kotlin.math.roundToInt

/**
 * A tactile-themed slider component with glow effects and value display.
 *
 * @param label The text label for the slider.
 * @param value The current value of the slider.
 * @param onValueChange Callback to be invoked when the value changes.
 * @param modifier The modifier to be applied to the layout.
 * @param valueRange The range of values the slider can represent.
 * @param steps The number of discrete steps in the slider.
 * @param minLabel The label for the minimum value.
 * @param maxLabel The label for the maximum value.
 * @param valueSuffix The suffix for the displayed value (e.g., "/ 05").
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TactileSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..5f,
    steps: Int = 4,
    minLabel: String = "",
    maxLabel: String = "",
    valueSuffix: String = "/ 05",
) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = TajsOSTheme.SpacingSm)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = label,
                style =
                    MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                    ),
                color = TajsOSTheme.Muted,
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text =
                        if ((value % 1f) == 0f) {
                            value
                                .roundToInt()
                                .toString()
                                .padStart(2, '0')
                        } else {
                            value.toString().padStart(4, '0')
                        },
                    style =
                        MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                        ),
                    color = TajsOSTheme.Primary,
                )
                Text(
                    text = " $valueSuffix",
                    style = MaterialTheme.typography.labelSmall,
                    color = TajsOSTheme.Muted,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
        }

        Spacer(Modifier.height(TajsOSTheme.SpacingSm))

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors =
                SliderDefaults.colors(
                    thumbColor = Color.Transparent, // Custom thumb below
                    activeTrackColor = Color(0xFF27272A),
                    inactiveTrackColor = Color(0xFF18181B),
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent,
                ),
            thumb = {
                Box(
                    modifier = Modifier.size(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    // Glow effect
                    Box(
                        modifier =
                            Modifier
                                .size(20.dp)
                                .blur(8.dp)
                                .background(
                                    TajsOSTheme.Primary.copy(alpha = 0.5f),
                                    RoundedCornerShape(4.dp),
                                ),
                    )
                    // Core thumb
                    Box(
                        modifier =
                            Modifier
                                .size(16.dp)
                                .background(TajsOSTheme.Primary, RoundedCornerShape(4.dp)),
                    )
                }
            },
            modifier = Modifier.height(24.dp),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                minLabel,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = TajsOSTheme.Muted,
            )
            Text(
                maxLabel,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = TajsOSTheme.Muted,
            )
        }
    }
}

/**
 * A tactile-themed text field component with a placeholder and label.
 *
 * @param value The current text value.
 * @param onValueChange Callback when text changes.
 * @param label The label text above the field.
 * @param modifier The modifier to be applied to the layout.
 * @param placeholder The placeholder text to show when value is empty.
 */
@Composable
fun TactileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "Type system log entries here...",
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style =
                MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                ),
            color = TajsOSTheme.Primary,
        )
        Spacer(Modifier.height(TajsOSTheme.SpacingMd))
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp)
                    .background(Color.Black, RoundedCornerShape(TajsOSTheme.RadiusMd))
                    .border(1.dp, Color(0xFF1C1C21), RoundedCornerShape(TajsOSTheme.RadiusMd))
                    .padding(TajsOSTheme.SpacingMd),
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TajsOSTheme.Muted,
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = TajsOSTheme.Text),
                cursorBrush = SolidColor(TajsOSTheme.Primary),
            )
        }
    }
}

@Preview
@Composable
private fun TactileSliderPreview() {
    TajsOSTheme {
        Surface(color = TajsOSTheme.Background) {
            Column(Modifier.padding(16.dp)) {
                TactileSlider(
                    label = "ENERGY_RESERVES",
                    value = 4f,
                    onValueChange = {},
                    minLabel = "DEPLETED",
                    maxLabel = "OPTIMAL",
                )
            }
        }
    }
}
