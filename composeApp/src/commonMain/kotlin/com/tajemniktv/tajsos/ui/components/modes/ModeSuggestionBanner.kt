/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.modes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme

/**
 * Renders a themed banner suggesting a mode change and exposes actions to accept or dismiss it.
 *
 * @param suggestion The mode name inserted into the banner subtitle (e.g., "Driving").
 * @param onAccept Callback invoked when the "SWITCH" button is pressed.
 * @param onDismiss Callback invoked when the "IGNORE" text button is pressed.
 */
@Composable
fun ModeSuggestionBanner(
    suggestion: String,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TajsOSTheme.Primary.copy(alpha = 0.1f),
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
        border =
            androidx.compose.foundation.BorderStroke(
                1.dp,
                TajsOSTheme.Primary.copy(alpha = 0.3f),
            ),
    ) {
        Row(
            modifier = Modifier.padding(TajsOSTheme.SpacingMd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = TajsOSTheme.Primary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(TajsOSTheme.SpacingMd))
                Column {
                    Text(
                        "MODE SUGGESTION",
                        style = MaterialTheme.typography.labelSmall,
                        color = TajsOSTheme.Primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Switch to $suggestion mode based on current activity?",
                        style = MaterialTheme.typography.bodySmall,
                        color = TajsOSTheme.Text,
                    )
                }
            }
            Row {
                TextButton(onClick = onDismiss) {
                    Text(
                        "IGNORE",
                        style = MaterialTheme.typography.labelSmall,
                        color = TajsOSTheme.Muted,
                    )
                }
                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(containerColor = TajsOSTheme.Primary),
                    shape = RoundedCornerShape(TajsOSTheme.RadiusSm),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp),
                ) {
                    Text("SWITCH", style = MaterialTheme.typography.labelSmall, color = Color.White)
                }
            }
        }
    }
}
