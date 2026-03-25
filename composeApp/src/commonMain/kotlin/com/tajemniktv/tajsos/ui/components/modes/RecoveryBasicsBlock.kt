/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved. 
 */

package com.tajemniktv.tajsos.ui.components.modes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.ui.theme.TactileTheme

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
    onFoodClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
        Text(
            "SURVIVAL BASICS",
            style = MaterialTheme.typography.labelSmall,
            color = TactileTheme.Muted,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)
        ) {
            BasicSurvivalCard(
                label = "MEDS",
                icon = Icons.Default.MedicalServices,
                color = TactileTheme.Error,
                onClick = onMedsClick,
                modifier = Modifier.weight(1f)
            )
            BasicSurvivalCard(
                label = "WATER",
                icon = Icons.Default.LocalDrink,
                color = TactileTheme.Accent,
                onClick = onHydrationClick,
                modifier = Modifier.weight(1f)
            )
            BasicSurvivalCard(
                label = "FOOD",
                icon = Icons.Default.Restaurant,
                color = TactileTheme.Success,
                onClick = onFoodClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun BasicSurvivalCard(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(4.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Text,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp
            )
        }
    }
}
