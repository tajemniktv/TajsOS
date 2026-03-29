/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tajemniktv.tajsos.ui.theme.TactileTheme

/**
 * Displays a full-screen selector dialog presenting a grid of selectable options.
 *
 * When `show` is true this composable renders a modal dialog with a header, a two-column grid of option tiles, and a footer; tapping a tile calls `onSelect`, and dismissing calls `onDismiss`.
 *
 * @param show Whether the dialog is visible.
 * @param onDismiss Callback invoked when the dialog should be dismissed.
 * @param title Dialog title displayed in the header.
 * @param prefix Short label shown above the title; defaults to "SYSTEM_SELECTOR // MODULE_INTAKE".
 * @param options List of items to render as selectable tiles.
 * @param selectedOption Currently highlighted option, or `null` if none.
 * @param onSelect Callback invoked with the option that was tapped.
 * @param optionName Function that returns the display name for an option.
 * @param optionIcon Function that returns the icon for an option.
 * @param optionSubtext Function that returns optional subtext for an option; return an empty string to omit it.
 */
@Composable
fun <T> SelectorDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    title: String,
    prefix: String = "SYSTEM_SELECTOR // MODULE_INTAKE",
    options: List<T>,
    selectedOption: T?,
    onSelect: (T) -> Unit,
    optionName: (T) -> String,
    optionIcon: (T) -> ImageVector,
    optionSubtext: (T) -> String = { "" },
) {
    if (!show) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(TactileTheme.Background.copy(alpha = 0.95f)),
            color = TactileTheme.Background.copy(alpha = 0.95f),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(TactileTheme.SpacingMd),
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column {
                        Text(
                            prefix.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = TactileTheme.Muted,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                        )
                        Text(
                            title.uppercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = TactileTheme.Text,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                    Surface(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, TactileTheme.Border),
                    ) {
                        Text(
                            "STATUS: READY",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = TactileTheme.Muted,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Spacer(Modifier.height(TactileTheme.SpacingLg))

                // Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
                    verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
                    modifier = Modifier.weight(1f),
                ) {
                    items(options) { option ->
                        val isSelected = option == selectedOption
                        Surface(
                            onClick = { onSelect(option) },
                            color = if (isSelected) TactileTheme.Primary else TactileTheme.Surface,
                            shape = RoundedCornerShape(TactileTheme.RadiusMd),
                            modifier = Modifier.height(140.dp),
                            border =
                                if (isSelected) {
                                    null
                                } else {
                                    BorderStroke(
                                        1.dp,
                                        TactileTheme.Border,
                                    )
                                },
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(TactileTheme.SpacingMd),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Icon(
                                    optionIcon(option),
                                    contentDescription = null,
                                    tint = if (isSelected) TactileTheme.Background else TactileTheme.Primary,
                                    modifier = Modifier.size(32.dp),
                                )
                                Spacer(Modifier.height(TactileTheme.SpacingMd))
                                Text(
                                    optionName(option).uppercase(),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) TactileTheme.Background else TactileTheme.Text,
                                )
                                val sub = optionSubtext(option)
                                if (sub.isNotEmpty()) {
                                    Text(
                                        sub.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) TactileTheme.Background.copy(alpha = 0.7f) else TactileTheme.Muted,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }
                    }
                }

                // Footer
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = TactileTheme.SpacingMd),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            "TAJS_OS_v1.2.0  •  NEURAL_INTERFACE",
                            style = MaterialTheme.typography.labelSmall,
                            color = TactileTheme.Muted,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = TactileTheme.Muted,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Button(
                        onClick = onDismiss,
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = TactileTheme.Surface,
                                contentColor = TactileTheme.Text,
                            ),
                        shape = RoundedCornerShape(TactileTheme.RadiusMd),
                        modifier = Modifier.height(48.dp),
                    ) {
                        Text(
                            "CANCEL SESSION",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}
