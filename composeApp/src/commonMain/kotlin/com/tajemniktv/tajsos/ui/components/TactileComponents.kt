/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                    activeTrackColor = TajsOSTheme.SurfaceHigh,
                    inactiveTrackColor = TajsOSTheme.SurfaceLow,
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent,
                ),
            thumb = {
                Box(
                    modifier = Modifier.size(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    // Core thumb
                    Box(
                        modifier =
                            Modifier
                                .size(16.dp)
                                .background(TajsOSTheme.Primary, RoundedCornerShape(TajsOSTheme.RadiusXs)),
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
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

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
                    .fillMaxWidth(),
        ) {


            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp)
                        .background(
                            TajsOSTheme.SurfaceLowest,
                            RoundedCornerShape(TajsOSTheme.RadiusMd),
                        )
                        // Obey DESIGN.md: no solid borders. Use GhostBorder.
                        .border(
                            1.dp,
                            if (isFocused) TajsOSTheme.GhostBorder else Color.Transparent,
                            RoundedCornerShape(TajsOSTheme.RadiusMd),
                        ).padding(TajsOSTheme.SpacingMd),
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
                    modifier = Modifier.fillMaxSize(),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = TajsOSTheme.Text),
                    cursorBrush = SolidColor(TajsOSTheme.Primary),
                    interactionSource = interactionSource,
                )
            }
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


@Composable
fun TactileOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    containerModifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = TajsOSTheme.SurfaceLowest,
        unfocusedContainerColor = TajsOSTheme.SurfaceLowest,
        disabledContainerColor = TajsOSTheme.SurfaceLowest,
        focusedBorderColor = TajsOSTheme.GhostBorder,
        unfocusedBorderColor = Color.Transparent,
        disabledBorderColor = Color.Transparent,
    )
) {
    val isFocused by interactionSource.collectIsFocusedAsState()

    Box(modifier = containerModifier, propagateMinConstraints = true) {

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            enabled = enabled,
            readOnly = readOnly,
            textStyle = textStyle,
            label = label,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            prefix = prefix,
            suffix = suffix,
            supportingText = supportingText,
            isError = isError,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            interactionSource = interactionSource,
            shape = shape,
            colors = colors,
        )
    }
}
