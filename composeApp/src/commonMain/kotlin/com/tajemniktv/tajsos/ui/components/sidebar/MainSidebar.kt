/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.sidebar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Renders the default navigation sidebar with grouped root-level screens.
 */
@Composable
internal fun MainSidebar(
    currentDestination: NavDestination?,
    menuGroups: List<Pair<StringResource, List<Screen>>>,
    onNavigate: (Screen) -> Unit,
    onNavigateFromSidebar: () -> Unit,
) {
    menuGroups.forEach { (headerRes, items) ->
        Text(
            stringResource(headerRes).uppercase(),
            modifier =
                Modifier.padding(
                    horizontal = TactileTheme.SpacingMd,
                    vertical = TactileTheme.SpacingSm,
                ),
            style = MaterialTheme.typography.labelSmall,
            color = TactileTheme.Muted.copy(alpha = 0.6f),
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
        )

        items.forEach { screen ->
            val selected =
                remember(currentDestination, screen.route) {
                    currentDestination?.hierarchy?.any { it.route == screen.route } == true
                }

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp),
            ) {
                if (selected) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxHeight()
                                .width(3.dp)
                                .background(TactileTheme.Primary),
                    )
                }

                Surface(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(TactileTheme.RadiusSm),
                    border =
                        if (selected) {
                            BorderStroke(
                                1.dp,
                                TactileTheme.Primary.copy(alpha = 0.3f),
                            )
                        } else {
                            null
                        },
                    modifier =
                        Modifier
                            .padding(start = if (selected) 2.dp else 0.dp)
                            .padding(horizontal = 12.dp, vertical = 2.dp),
                ) {
                    NavigationDrawerItem(
                        label = {
                            Text(
                                stringResource(screen.label).uppercase(),
                                style = MaterialTheme.typography.labelLarge,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            )
                        },
                        selected = selected,
                        onClick = {
                            onNavigate(screen)
                            onNavigateFromSidebar()
                        },
                        icon = {
                            Icon(
                                screen.icon,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = drawerItemColors(),
                        shape = RoundedCornerShape(TactileTheme.RadiusSm),
                    )
                }
            }
        }
        Spacer(Modifier.height(TactileTheme.SpacingSm))
    }
}

@Composable
private fun drawerItemColors() =
    NavigationDrawerItemDefaults.colors(
        selectedContainerColor = TactileTheme.Primary.copy(alpha = 0.15f),
        selectedIconColor = TactileTheme.Primary,
        selectedTextColor = TactileTheme.Primary,
        unselectedIconColor = TactileTheme.Muted,
        unselectedTextColor = TactileTheme.Muted,
        unselectedContainerColor = Color.Transparent,
    )
