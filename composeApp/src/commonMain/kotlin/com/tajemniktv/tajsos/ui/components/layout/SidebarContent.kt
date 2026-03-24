/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.tajemniktv.tajsos.data.ModeEntity
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.design.theme.TactileTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*

@Composable
fun SidebarContent(
    currentDestination: NavDestination?,
    onNavigate: (Screen) -> Unit,
    onNewEntry: (() -> Unit)? = null,
    currentMode: ModeEntity? = null,
    allModes: List<ModeEntity> = emptyList(),
    onModeSelect: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxHeight(),
    ) {
        // Profile Header
        Row(
            modifier =
                Modifier
                    .padding(TactileTheme.SpacingMd)
                    .padding(top = TactileTheme.SpacingSm)
                    .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(44.dp)
                        .background(
                            currentMode?.themeColor?.let { Color(it) }?.copy(alpha = 0.2f) ?: Color(
                                0xFFFDE68A
                            ), CircleShape
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = currentMode?.themeColor?.let { Color(it) } ?: TactileTheme.Primary,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(Modifier.width(TactileTheme.SpacingMd))
            Column {
                Text(
                    stringResource(Res.string.app_name),
                    style = MaterialTheme.typography.titleMedium,
                    color = TactileTheme.Text,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    currentMode?.name?.uppercase() ?: stringResource(Res.string.nav_role_admin),
                    style = MaterialTheme.typography.labelSmall,
                    color = currentMode?.themeColor?.let { Color(it) } ?: TactileTheme.Primary,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (allModes.isNotEmpty()) {
            Column(
                modifier = Modifier.padding(
                    horizontal = TactileTheme.SpacingMd,
                    vertical = TactileTheme.SpacingSm
                )
            ) {
                Text(
                    "CURRENT MODE",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Muted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(TactileTheme.SpacingSm))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                    items(allModes) { mode ->
                        val isSelected = mode.id == currentMode?.id
                        val color = mode.themeColor?.let { Color(it) } ?: TactileTheme.Primary
                        Surface(
                            onClick = { onModeSelect(mode.id) },
                            color = if (isSelected) color.copy(alpha = 0.15f) else Color.Transparent,
                            shape = RoundedCornerShape(TactileTheme.RadiusSm),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) color else TactileTheme.Border.copy(alpha = 0.3f)
                            )
                        ) {
                            Text(
                                mode.name.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) color else TactileTheme.Muted,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
                HorizontalDivider(
                    color = TactileTheme.Border.copy(alpha = 0.3f),
                    modifier = Modifier.padding(top = TactileTheme.SpacingMd)
                )
            }
        }

        Spacer(Modifier.height(TactileTheme.SpacingSm))

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
        ) {
            Screen.groupedItems.forEach { (headerRes, items) ->
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
                    fontWeight = FontWeight.ExtraBold
                )
                items.forEach { screen ->
                    val selected = remember(currentDestination, screen.route) {
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

                        NavigationDrawerItem(
                            label = {
                                Text(
                                    stringResource(screen.label).uppercase(),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            selected = selected,
                            onClick = { onNavigate(screen) },
                            icon = {
                                Icon(
                                    screen.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            modifier =
                                Modifier
                                    .padding(start = if (selected) 2.dp else 0.dp)
                                    .padding(horizontal = 12.dp, vertical = 2.dp),
                            colors =
                                NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = TactileTheme.Primary.copy(alpha = 0.15f),
                                    selectedIconColor = TactileTheme.Primary,
                                    selectedTextColor = TactileTheme.Primary,
                                    unselectedIconColor = TactileTheme.Muted,
                                    unselectedTextColor = TactileTheme.Muted,
                                    unselectedContainerColor = Color.Transparent,
                                ),
                            shape = RoundedCornerShape(TactileTheme.RadiusSm),
                        )
                    }
                }
                Spacer(Modifier.height(TactileTheme.SpacingSm))
            }
        }

        if (onNewEntry != null) {
            Box(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
                Button(
                    onClick = onNewEntry,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TactileTheme.Primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("NEW ENTRY", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Uptime Footer
        Column(
            modifier =
                Modifier
                    .padding(TactileTheme.SpacingMd)
                    .background(
                        TactileTheme.Surface.copy(alpha = 0.5f),
                        RoundedCornerShape(TactileTheme.RadiusMd)
                    )
                    .padding(TactileTheme.SpacingMd),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(Res.string.nav_uptime),
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Muted,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    stringResource(Res.string.nav_uptime_value),
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Primary,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(TactileTheme.SpacingSm))
            LinearProgressIndicator(
                progress = { 0.999f },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                color = TactileTheme.Primary,
                trackColor = TactileTheme.Muted.copy(alpha = 0.2f),
                strokeCap = StrokeCap.Round,
            )
        }
        Spacer(Modifier.height(TactileTheme.SpacingMd))
    }
}
