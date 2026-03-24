/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved. 
 */

package com.tajemniktv.tajsos.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.data.ModeEntity
import com.tajemniktv.tajsos.ui.theme.TactileTheme

@Composable
fun ModeSwitcherHeader(
    currentMode: ModeEntity?,
    allModes: List<ModeEntity>,
    onModeSelect: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            "OPERATING MODE",
            style = MaterialTheme.typography.labelSmall,
            color = TactileTheme.Muted,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(TactileTheme.SpacingSm))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
            contentPadding = PaddingValues(end = TactileTheme.SpacingMd)
        ) {
            items(allModes) { mode ->
                val isSelected = mode.id == currentMode?.id
                val themeColor = mode.themeColor?.let { Color(it) } ?: TactileTheme.Primary

                Surface(
                    onClick = { onModeSelect(mode.id) },
                    color = if (isSelected) themeColor.copy(alpha = 0.2f) else Color.Transparent,
                    shape = RoundedCornerShape(TactileTheme.RadiusMd),
                    border = BorderStroke(1.dp, if (isSelected) themeColor else TactileTheme.Border)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSelected) {
                            Box(modifier = Modifier.size(6.dp).background(themeColor, CircleShape))
                            Spacer(Modifier.width(6.dp))
                        }
                        Text(
                            mode.name.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) themeColor else TactileTheme.Muted,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModeSuggestionBanner(
    suggestion: String,
    onAccept: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Accent.copy(alpha = 0.1f),
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, TactileTheme.Accent.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = TactileTheme.Accent)
            Spacer(Modifier.width(TactileTheme.SpacingMd))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "MODE SUGGESTION",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Accent,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Switch to $suggestion mode?",
                    style = MaterialTheme.typography.bodySmall,
                    color = TactileTheme.Text
                )
            }
            TextButton(onClick = onDismiss) {
                Text(
                    "DISMISS",
                    color = TactileTheme.Muted,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Button(
                onClick = onAccept,
                colors = ButtonDefaults.buttonColors(containerColor = TactileTheme.Accent),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp),
                shape = RoundedCornerShape(2.dp)
            ) {
                Text("SWITCH", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun RecoveryBasicsBlock(
    onMedsClick: () -> Unit,
    onHydrationClick: () -> Unit,
    onFoodClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
        DetailSectionHeader(
            title = "SURVIVAL BASICS",
            icon = Icons.Default.MedicalServices,
            color = Color(0xFF4CAF50)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
            BasicSurvivalCard(
                "MEDS",
                Icons.Default.Medication,
                Color(0xFF4CAF50),
                onMedsClick,
                Modifier.weight(1f)
            )
            BasicSurvivalCard(
                "WATER",
                Icons.Default.WaterDrop,
                Color(0xFF2196F3),
                onHydrationClick,
                Modifier.weight(1f)
            )
            BasicSurvivalCard(
                "FOOD",
                Icons.Default.Restaurant,
                Color(0xFFFFC107),
                onFoodClick,
                Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BasicSurvivalCard(
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
        border = BorderStroke(1.dp, TactileTheme.Border)
    ) {
        Column(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color)
            Spacer(Modifier.height(TactileTheme.SpacingSm))
            Text(label, style = MaterialTheme.typography.labelSmall, color = TactileTheme.Muted)
        }
    }
}
