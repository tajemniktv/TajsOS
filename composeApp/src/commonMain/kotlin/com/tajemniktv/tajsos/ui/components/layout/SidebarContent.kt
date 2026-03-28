/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.layout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.tajemniktv.tajsos.data.AppPack
import com.tajemniktv.tajsos.data.ModeEntity
import com.tajemniktv.tajsos.data.PackRegistry
import com.tajemniktv.tajsos.data.UserProfile
import com.tajemniktv.tajsos.data.resolveDisplayName
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*

/**
 * Renders the app sidebar UI: a profile header, an optional horizontal mode selector,
 * a scrollable grouped navigation menu, an optional "NEW ENTRY" action button, and an uptime footer.
 *
 * @param currentDestination Current navigation destination used to determine which menu item is selected.
 * @param onNavigate Callback invoked with the target `Screen` when a navigation item is clicked.
 * @param onNewEntry Optional callback invoked when the "NEW ENTRY" button is pressed; when `null` the button is omitted.
 * @param currentMode Currently active mode (used for header styling and mode selection state); may be `null`.
 * @param allModes List of available modes to display in the mode selector; when empty the selector is omitted.
 * @param onModeSelect Callback invoked with the selected mode's id when a mode chip is clicked.
 * @param useContextualSidebar When true, menu groups are derived from the current screen context.
 * @param onBackToMainSidebar Callback used to switch to the full/main sidebar menu.
 * @param onNavigateFromSidebar Callback used after navigation from sidebar items.
 * @param modifier Modifier for external layout adjustments.
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
            Screen.contextualHeaderFor(contextScreen, packRegistry)
        } else {
            null
        }
    val displayName = userProfile.resolveDisplayName()
    val avatarInitials = profileInitials(userProfile)

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
                            currentMode?.themeColor?.let { Color(it) }?.copy(alpha = 0.2f)
                                ?: Color(
                                    0xFFFDE68A,
                                ),
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

        if (allModes.isNotEmpty()) {
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
            if (contextualHeader != null) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = TactileTheme.SpacingMd,
                                vertical = TactileTheme.SpacingSm,
                            ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                ) {
                    OutlinedButton(
                        onClick = onBackToMainSidebar,
                        shape = RoundedCornerShape(TactileTheme.RadiusSm),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.common_back),
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    Text(
                        stringResource(contextualHeader).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Muted.copy(alpha = 0.85f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }

                val panelLabel =
                    contextScreen?.let { stringResource(it.label).uppercase() } ?: "PANEL"
                val sections =
                    remember(contextScreen) {
                        contextScreen?.let(::placeholderSectionsFor).orEmpty()
                    }

                Text(
                    panelLabel,
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

                sections.forEach { section ->
                    Text(
                        section.title,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = TactileTheme.SpacingMd,
                                    vertical = TactileTheme.SpacingSm,
                                ),
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Muted.copy(alpha = 0.75f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )

                    section.items.forEach { item ->
                        Surface(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 2.dp),
                            color = Color.Transparent,
                            shape = RoundedCornerShape(TactileTheme.RadiusSm),
                            border = BorderStroke(1.dp, TactileTheme.Border.copy(alpha = 0.25f)),
                        ) {
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = TactileTheme.Muted,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    item,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = TactileTheme.Muted,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(TactileTheme.SpacingSm))
                }
            } else {
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
                    }
                    Spacer(Modifier.height(TactileTheme.SpacingSm))
                }
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

        // Uptime Footer
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
        Spacer(Modifier.height(TactileTheme.SpacingMd))
    }
}

private data class SidebarPlaceholderSection(
    val title: String,
    val items: List<String>,
)

private fun placeholderSectionsFor(screen: Screen): List<SidebarPlaceholderSection> {
    val screenTag = screen.route.substringBefore("/")
    return listOf(
        SidebarPlaceholderSection(
            title = "PRIMARY",
            items =
                listOf(
                    "$screenTag overview placeholder",
                    "$screenTag shortcuts placeholder",
                    "$screenTag quick filters placeholder",
                ),
        ),
        SidebarPlaceholderSection(
            title = "WORKFLOW",
            items =
                listOf(
                    "$screenTag actions placeholder",
                    "$screenTag pinned context placeholder",
                    "$screenTag automation placeholder",
                ),
        ),
        SidebarPlaceholderSection(
            title = "INSIGHTS",
            items =
                listOf(
                    "$screenTag metrics placeholder",
                    "$screenTag anomalies placeholder",
                    "$screenTag recommendations placeholder",
                ),
        ),
    )
}

private fun profileInitials(profile: UserProfile): String {
    val first = profile.firstName.trim().firstOrNull()?.uppercaseChar()?.toString().orEmpty()
    val last = profile.lastName.trim().firstOrNull()?.uppercaseChar()?.toString().orEmpty()
    val initials = first + last
    if (initials.isNotBlank()) return initials
    val nickname = profile.nickname.trim()
    return nickname.take(2).uppercase()
}
