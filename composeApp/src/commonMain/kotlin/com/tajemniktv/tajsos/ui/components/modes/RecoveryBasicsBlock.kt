/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.modes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.ui.components.cards.BasicSurvivalCard
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme

/**
 * Displays a "SURVIVAL BASICS" header and a row of three equally sized actionable cards:
 * MEDS, WATER, and FOOD.
 *
 * @param onMedsClick Invoked when the MEDS card is clicked.
 * @param onHydrationClick Invoked when the WATER card is clicked.
 * @param onFoodClick Invoked when the FOOD card is clicked.
 */
@Composable
fun RecoveryBasicsBlock(
    onMedsClick: () -> Unit,
    onHydrationClick: () -> Unit,
    onFoodClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)) {
        Text(
            "SURVIVAL BASICS",
            style = MaterialTheme.typography.labelSmall,
            color = TajsOSTheme.Muted,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)
        ) {
            BasicSurvivalCard(
                label = "MEDS",
                icon = Icons.Default.MedicalServices,
                color = TajsOSTheme.Error,
                onClick = onMedsClick,
                modifier = Modifier.weight(1f),
            )
            BasicSurvivalCard(
                label = "WATER",
                icon = Icons.Default.LocalDrink,
                color = TajsOSTheme.Accent,
                onClick = onHydrationClick,
                modifier = Modifier.weight(1f),
            )
            BasicSurvivalCard(
                label = "FOOD",
                icon = Icons.Default.Restaurant,
                color = TajsOSTheme.Success,
                onClick = onFoodClick,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
