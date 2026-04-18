/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.modes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme

/**
 * Renders a horizontal row of three equally sized actionable cards that apply preset search filters and navigate to the Search screen.
 *
 * Tapping a card clears existing search filters, applies the card's preset filters (easy friction / low energy / max minutes), and then invokes navigation to Screen.Search.
 *
 * @param viewModel The ViewModel used to clear and update search filters.
 * @param onNavigateTo Callback invoked with the destination Screen; called with Screen.Search after a card is selected.
 */
@Composable
fun StateAwareActionsGrid(
    viewModel: MainViewModel,
    onNavigateTo: (Screen) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
    ) {
        listOf(
            Triple(Icons.Default.Bolt, "OVERWHELMED", "Easy wins"),
            Triple(Icons.Default.BatteryChargingFull, "CANNOT THINK", "Low energy"),
            Triple(Icons.Default.Timer, "10 MINUTES", "Quick steps"),
        ).forEach { (icon, title, desc) ->
            Surface(
                onClick = {
                    viewModel.clearSearchFilters()
                    when (title)
                    {
                        "OVERWHELMED" -> {
                            viewModel.updateSearchFrictionFilter("easy")
                            viewModel.updateSearchEnergyFilter(
                                1,
                            )
                        }

                        "CANNOT THINK" -> {
                            viewModel.updateSearchEnergyFilter(1)
                        }

                        "10 MINUTES" -> {
                            viewModel.updateSearchMaxMinutesFilter(
                                10,
                            )
                        }
                    }
                    onNavigateTo(Screen.Search)
                },
                modifier = Modifier.weight(1f),
                color = TajsOSTheme.CardSurface,
                shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
                border = BorderStroke(1.dp, TajsOSTheme.CardStroke),
            ) {
                Column(modifier = Modifier.padding(TajsOSTheme.SpacingMd)) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = TajsOSTheme.Primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                    )
                    Text(
                        desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = TajsOSTheme.Muted,
                        fontSize = 9.sp,
                    )
                }
            }
        }
    }
}

