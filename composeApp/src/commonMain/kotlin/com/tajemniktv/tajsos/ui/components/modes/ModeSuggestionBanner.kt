/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved. 
 */

package com.tajemniktv.tajsos.ui.components.modes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.ui.design.theme.TactileTheme

@Composable
fun ModeSuggestionBanner(
    suggestion: String,
    onAccept: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Primary.copy(alpha = 0.1f),
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            TactileTheme.Primary.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = TactileTheme.Primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(TactileTheme.SpacingMd))
                Column {
                    Text(
                        "MODE SUGGESTION",
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Switch to $suggestion mode based on current activity?",
                        style = MaterialTheme.typography.bodySmall,
                        color = TactileTheme.Text
                    )
                }
            }
            Row {
                TextButton(onClick = onDismiss) {
                    Text(
                        "IGNORE",
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Muted
                    )
                }
                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(containerColor = TactileTheme.Primary),
                    shape = RoundedCornerShape(TactileTheme.RadiusSm),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("SWITCH", style = MaterialTheme.typography.labelSmall, color = Color.White)
                }
            }
        }
    }
}
