/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.theme.TactileTheme

@Composable
fun AreaHealthOverviewCard(
    dominantArea: String?,
    imbalanceScore: Int,
    imbalanceLabel: String,
    disappearingCount: Int,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape =
            androidx.compose.foundation.shape
                .RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, TactileTheme.Border),
    ) {
        Column(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "LIFE AREAS HEALTH",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Dominant this week: ${dominantArea ?: "N/A"}",
                style = MaterialTheme.typography.bodyMedium,
                color = TactileTheme.Text,
            )
            Text(
                "Imbalance: $imbalanceScore% (${imbalanceLabel.uppercase()})",
                style = MaterialTheme.typography.bodySmall,
                color = if (imbalanceScore >= 60) TactileTheme.Error else TactileTheme.Muted,
            )
            LinearProgressIndicator(
                progress = { (imbalanceScore / 100f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = if (imbalanceScore >= 60) TactileTheme.Error else TactileTheme.Primary,
                trackColor = TactileTheme.Border,
            )
            if (disappearingCount > 0) {
                Text(
                    "Radar drop detected in $disappearingCount area(s).",
                    style = MaterialTheme.typography.bodySmall,
                    color = TactileTheme.Error,
                )
            }
        }
    }
}
