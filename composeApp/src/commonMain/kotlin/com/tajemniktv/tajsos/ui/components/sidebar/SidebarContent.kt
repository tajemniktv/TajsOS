/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.sidebar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import com.tajemniktv.tajsos.data.AppPack
import com.tajemniktv.tajsos.data.ModeEntity
import com.tajemniktv.tajsos.data.PackRegistry
import com.tajemniktv.tajsos.data.UserProfile
import com.tajemniktv.tajsos.data.resolveDisplayName
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.nav_role_admin
import tajsos.composeapp.generated.resources.nav_uptime
import tajsos.composeapp.generated.resources.nav_uptime_value

/**
 * Sidebar host that switches between the default main sidebar and contextual screen-specific sidebars.
 */
@Composable
fun SidebarContent(
    currentDestination: NavDestination?,
    onNavigate: (Screen) -> Unit,
    onNewEntry: (() -> Unit)? = null,
    currentMode: ModeEntity? = null,
    allModes: List<ModeEntity> = emptyList(),
    packRegistry: PackRegistry =
        PackRegistry(
            ownedPackKeys = AppPack.defaultFreePackKeys,
            enabledPackKeys = AppPack.defaultFreePackKeys,
        ),
    userProfile: UserProfile = UserProfile(),
    onModeSelect: (Long) -> Unit = {},
    useContextualSidebar: Boolean = false,
    onBackToMainSidebar: () -> Unit = {},
    onNavigateFromSidebar: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val currentScreen = Screen.fromRoute(currentDestination?.route)
    val contextScreen = currentScreen?.let(Screen::sidebarContextRoot)
    val menuGroups = Screen.groupedItemsForPacks(packRegistry)
    val mainSidebarScrollState = rememberScrollState()
    val contextualSidebarScrollState = rememberScrollState()
    val contextualHeader =
        if (useContextualSidebar && contextScreen != null) {
            Screen.contextualHeaderFor(contextScreen, packRegistry)?.let { stringResource(it) }
        } else {
            null
        }
    val panelLabel = contextScreen?.let { stringResource(it.label) } ?: "PANEL"

    Column(modifier = modifier.fillMaxHeight()) {
        SidebarProfileHeader(
            currentMode = currentMode,
            userProfile = userProfile,
        )

        if (allModes.isNotEmpty()) {
            SidebarModeSelector(
                allModes = allModes,
                currentMode = currentMode,
                onModeSelect = onModeSelect,
            )
        }

        Spacer(Modifier.height(TactileTheme.SpacingSm))

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(
                        if (contextualHeader != null) {
                            contextualSidebarScrollState
                        } else {
                            mainSidebarScrollState
                        },
                    ),
        ) {
            if (contextualHeader != null && contextScreen != null) {
                when (contextScreen) {
                    Screen.Projects ->
                        ProjectsSidebar(
                            contextHeader = contextualHeader,
                            panelLabel = panelLabel,
                            onBackToMainSidebar = onBackToMainSidebar,
                        )

                    Screen.Focus ->
                        FocusSidebar(
                            contextHeader = contextualHeader,
                            panelLabel = panelLabel,
                            onBackToMainSidebar = onBackToMainSidebar,
                        )

                    else ->
                        GenericContextSidebar(
                            screen = contextScreen,
                            contextHeader = contextualHeader,
                            panelLabel = panelLabel,
                            onBackToMainSidebar = onBackToMainSidebar,
                        )
                }
            } else {
                MainSidebar(
                    currentDestination = currentDestination,
                    menuGroups = menuGroups,
                    onNavigate = onNavigate,
                    onNavigateFromSidebar = onNavigateFromSidebar,
                )
            }
        }

        if (onNewEntry != null) {
            Box(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
                Button(
                    onClick = onNewEntry,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TactileTheme.Primary),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("NEW ENTRY", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        SidebarFooter()
        Spacer(Modifier.height(TactileTheme.SpacingMd))
    }
}

@Composable
private fun SidebarProfileHeader(
    currentMode: ModeEntity?,
    userProfile: UserProfile,
) {
    val displayName = userProfile.resolveDisplayName()
    val avatarInitials = profileInitials(userProfile)

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
                        currentMode?.themeColor?.let { Color(it) }?.copy(alpha = 0.2f)
                            ?: Color(0xFFFDE68A),
                        CircleShape,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            if (avatarInitials.isBlank()) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = currentMode?.themeColor?.let { Color(it) } ?: TactileTheme.Primary,
                    modifier = Modifier.size(24.dp),
                )
            } else {
                Text(
                    avatarInitials,
                    color = currentMode?.themeColor?.let { Color(it) } ?: TactileTheme.Primary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Spacer(Modifier.width(TactileTheme.SpacingMd))
        Column {
            Text(
                displayName,
                style = MaterialTheme.typography.titleMedium,
                color = TactileTheme.Text,
                fontWeight = FontWeight.Bold,
            )
            Text(
                currentMode?.name?.uppercase() ?: stringResource(Res.string.nav_role_admin),
                style = MaterialTheme.typography.labelSmall,
                color = currentMode?.themeColor?.let { Color(it) } ?: TactileTheme.Primary,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun SidebarModeSelector(
    allModes: List<ModeEntity>,
    currentMode: ModeEntity?,
    onModeSelect: (Long) -> Unit,
) {
    Column(
        modifier =
            Modifier.padding(
                horizontal = TactileTheme.SpacingMd,
                vertical = TactileTheme.SpacingSm,
            ),
    ) {
        Text(
            "CURRENT MODE",
            style = MaterialTheme.typography.labelSmall,
            color = TactileTheme.Muted,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
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
                    border =
                        BorderStroke(
                            1.dp,
                            if (isSelected) color else TactileTheme.Border.copy(alpha = 0.3f),
                        ),
                ) {
                    Text(
                        mode.name.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) color else TactileTheme.Muted,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }
        HorizontalDivider(
            color = TactileTheme.Border.copy(alpha = 0.3f),
            modifier = Modifier.padding(top = TactileTheme.SpacingMd),
        )
    }
}

@Composable
private fun SidebarFooter() {
    Column(
        modifier =
            Modifier
                .padding(TactileTheme.SpacingMd)
                .background(
                    TactileTheme.Surface.copy(alpha = 0.5f),
                    RoundedCornerShape(TactileTheme.RadiusMd),
                ).padding(TactileTheme.SpacingMd),
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
}

private fun profileInitials(profile: UserProfile): String {
    val first = profile.firstName.trim().firstOrNull()?.uppercaseChar()?.toString().orEmpty()
    val last = profile.lastName.trim().firstOrNull()?.uppercaseChar()?.toString().orEmpty()
    val initials = first + last
    if (initials.isNotBlank()) return initials
    return profile.nickname.trim().take(2).uppercase()
}
