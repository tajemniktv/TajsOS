/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved. 
 */

package com.tajemniktv.tajsos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import kotlin.math.roundToInt
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TactileSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..5f,
    steps: Int = 4,
    minLabel: String = "",
    maxLabel: String = "",
    valueSuffix: String = "/ 05"
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = TactileTheme.SpacingSm)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = TactileTheme.Muted
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = if (value % 1 == 0f) value.roundToInt().toString()
                        .padStart(2, '0') else value.toString().padStart(4, '0'),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = TactileTheme.Primary
                )
                Text(
                    text = " $valueSuffix",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Muted,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }

        Spacer(Modifier.height(TactileTheme.SpacingSm))

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = Color.Transparent, // Custom thumb below
                activeTrackColor = Color(0xFF27272A),
                inactiveTrackColor = Color(0xFF18181B),
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent
            ),
            thumb = {
                Box(
                    modifier = Modifier.size(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Glow effect
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .blur(8.dp)
                            .background(
                                TactileTheme.Primary.copy(alpha = 0.5f),
                                RoundedCornerShape(4.dp)
                            )
                    )
                    // Core thumb
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(TactileTheme.Primary, RoundedCornerShape(4.dp))
                    )
                }
            },
            modifier = Modifier.height(24.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                minLabel,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = TactileTheme.Muted
            )
            Text(
                maxLabel,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = TactileTheme.Muted
            )
        }
    }
}

@Composable
fun TactileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "Type system log entries here..."
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = TactileTheme.Primary
        )
        Spacer(Modifier.height(TactileTheme.SpacingMd))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp)
                .background(Color.Black, RoundedCornerShape(TactileTheme.RadiusMd))
                .border(1.dp, Color(0xFF1C1C21), RoundedCornerShape(TactileTheme.RadiusMd))
                .padding(TactileTheme.SpacingMd)
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TactileTheme.Muted
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = TactileTheme.Text),
                cursorBrush = SolidColor(TactileTheme.Primary)
            )
        }
    }
}

@Preview
@Composable
fun TactileSliderPreview() {
    TajsOSTheme {
        Surface(color = TactileTheme.Background) {
            Column(Modifier.padding(16.dp)) {
                TactileSlider(
                    label = "ENERGY_RESERVES",
                    value = 4f,
                    onValueChange = {},
                    minLabel = "DEPLETED",
                    maxLabel = "OPTIMAL"
                )
            }
        }
    }
}
