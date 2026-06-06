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
import com.tajemniktv.tajsos.ui.components.ActionButton
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
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme

/**
 * Displays a full-screen selector dialog presenting a grid of selectable options.
 *
 * When `show` is true this composable renders a modal dialog with a header, a two-column grid of option tiles, and a footer; tapping a tile calls `onSelect`, and dismissing calls `onDismiss`.
 *
 * @param show Whether the dialog is visible.
 * @param onDismiss Callback invoked when the dialog should be dismissed.
 * @param title Dialog title displayed in the header.
 * @param prefix Short label shown above the title; defaults to "SYSTEM_SELECTOR // MODULE_INTAKE".
 * @param show Whether the selector is visible.
 * @param onDismiss Callback invoked when the selector is dismissed.
 * @param title The primary title to display.
 * @param prefix Functional prefix for the selector header.
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
                    .background(TajsOSTheme.Background.copy(alpha = 0.95f)),
            color = TajsOSTheme.Background.copy(alpha = 0.95f),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(TajsOSTheme.SpacingMd),
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
                            color = TajsOSTheme.Muted,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                        )
                        Text(
                            title.uppercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = TajsOSTheme.Text,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                    Surface(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(TajsOSTheme.RadiusXs),
                    ) {
                        Text(
                            "STATUS: READY",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = TajsOSTheme.Muted,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Spacer(Modifier.height(TajsOSTheme.SpacingLg))

                // Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd),
                    verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd),
                    modifier = Modifier.weight(1f),
                ) {
                    items(options) { option ->
                        val isSelected = option == selectedOption
                        Surface(
                            onClick = { onSelect(option) },
                            color = if (isSelected) TajsOSTheme.Primary else TajsOSTheme.Surface,
                            shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
                            modifier = Modifier.height(140.dp),
                            border =
                                if (isSelected) {
                                    null
                                } else {
                                    BorderStroke(
                                        1.dp,
                                        TajsOSTheme.Border,
                                    )
                                },
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(TajsOSTheme.SpacingMd),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Icon(
                                    optionIcon(option),
                                    contentDescription = null,
                                    tint = if (isSelected) TajsOSTheme.Background else TajsOSTheme.Primary,
                                    modifier = Modifier.size(32.dp),
                                )
                                Spacer(Modifier.height(TajsOSTheme.SpacingMd))
                                Text(
                                    optionName(option).uppercase(),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) TajsOSTheme.Background else TajsOSTheme.Text,
                                )
                                val sub = optionSubtext(option)
                                if (sub.isNotEmpty()) {
                                    Text(
                                        sub.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color =
                                            if (isSelected) {
                                                TajsOSTheme.Background.copy(
                                                    alpha = 0.7f,
                                                )
                                            } else {
                                                TajsOSTheme.Muted
                                            },
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
                    modifier = Modifier.fillMaxWidth().padding(vertical = TajsOSTheme.SpacingMd),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            "TAJS_OS_v1.2.0  •  NEURAL_INTERFACE",
                            style = MaterialTheme.typography.labelSmall,
                            color = TajsOSTheme.Muted,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = TajsOSTheme.Muted,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    ActionButton(
                        text = "CANCEL SESSION",
                        onClick = onDismiss,
                        containerColor = TajsOSTheme.CardSurface,
                    )
                }
            }
        }
    }
}
