/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import com.tajemniktv.tajsos.data.ModeEntity
import com.tajemniktv.tajsos.data.PackRegistry
import com.tajemniktv.tajsos.data.UserProfile
import com.tajemniktv.tajsos.data.resolveDisplayName
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.common.GlassMaterial
import com.tajemniktv.tajsos.ui.components.common.ProvideGlassSystem
import com.tajemniktv.tajsos.ui.components.common.glassContainerColor
import com.tajemniktv.tajsos.ui.components.common.glassChrome
import com.tajemniktv.tajsos.ui.components.notifications.NotificationUiModel
import com.tajemniktv.tajsos.ui.components.notifications.NotificationVariant
import com.tajemniktv.tajsos.ui.components.screen.ScreenHeaderModel
import com.tajemniktv.tajsos.ui.screens.tasks.TasksTab
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/**
 * Top-level application chrome with persistent sidebar, shell header, and routed content slot.
 */
@Composable
fun AppShell(
    isDesktop: Boolean,
    shellState: AppShellState,
    currentDestination: NavDestination?,
    activeTasksTab: TasksTab,
    onNavigate: (Screen) -> Unit,
    onNavigateToTasksTab: (TasksTab) -> Unit,
    onNewEntry: () -> Unit,
    currentMode: ModeEntity?,
    allModes: List<ModeEntity>,
    packRegistry: PackRegistry,
    userProfile: UserProfile,
    isGlassmorphismEnabled: Boolean,
    onModeSelect: (Long) -> Unit,
    drawerState: DrawerState,
    scope: CoroutineScope,
    screenHeader: ScreenHeaderModel,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val currentScreen = Screen.fromRoute(currentDestination?.route)
    val currentRoot = currentScreen?.let(Screen::sidebarContextRoot)
    val userName = userProfile.resolveDisplayName()
    val greeting = remember(userName) { timeGreeting(userName) }
    val protocolText = currentMode?.name?.let { "Protocol state: $it" } ?: "Protocol state: standby"
    val modeOptions = rememberModeOptions(allModes)
    val hazeState = rememberHazeState(blurEnabled = isGlassmorphismEnabled)
    val notifications =
        remember {
            listOf(
                NotificationUiModel(
                    id = "SYS-001",
                    title = "System Integrity Nominal",
                    body = "All local-first modules are operational.",
                    category = "SYSTEM",
                    variant = NotificationVariant.INFO,
                ),
                NotificationUiModel(
                    id = "SYNC-042",
                    title = "Pending Sync Activity",
                    body = "3 nodes awaiting finalization in background.",
                    category = "NETWORK",
                    variant = NotificationVariant.SYNC,
                    isUnread = true,
                ),
                NotificationUiModel(
                    id = "ALRT-099",
                    title = "Low Focus Threshold",
                    body = "Productivity dip detected. Consider a recovery block.",
                    category = "INSIGHT",
                    variant = NotificationVariant.WARNING,
                ),
            )
        }

    ProvideGlassSystem(
        enabled = isGlassmorphismEnabled,
        hazeState = hazeState,
    ) {
        Box(modifier = modifier.fillMaxSize()) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            brush =
                                Brush.linearGradient(
                                    colors =
                                        listOf(
                                            TajsOSTheme.Background,
                                            TajsOSTheme.SurfaceLow,
                                            TajsOSTheme.Background,
                                        ),
                                ),
                        )
                        .then(
                            if (isGlassmorphismEnabled) {
                                Modifier.hazeSource(hazeState)
                            } else {
                                Modifier
                            },
                        ),
            )
            if (isDesktop) {
                Row(modifier = Modifier.fillMaxSize().background(TajsOSTheme.Background.copy(alpha = 0.74f))) {
                    AppSidebar(
                        shellState = shellState,
                        menuGroups = Screen.groupedItemsForPacks(packRegistry),
                        currentRootScreen = currentRoot,
                        currentScreen = currentScreen,
                        activeTasksTab = activeTasksTab,
                        currentMode = currentMode,
                        userProfile = userProfile,
                        onNavigate = onNavigate,
                        onNavigateToTasksTab = onNavigateToTasksTab,
                        onNewEntry = onNewEntry,
                        onNavigateToProfile = { onNavigate(Screen.Profile) },
                    )

                    Column(modifier = Modifier.fillMaxSize()) {
                        AppShellHeader(
                            greeting = greeting,
                            protocolText = protocolText,
                            currentModeLabel = currentMode?.name ?: "STANDBY",
                            modeOptions = modeOptions,
                            notifications = notifications,
                            shellState = shellState,
                            isDesktop = true,
                            onModeSelect = onModeSelect,
                            screenHeader = screenHeader,
                            modifier = Modifier.padding(bottom = 2.dp),
                        )

                        Box(modifier = Modifier.fillMaxSize()) {
                            content()
                        }
                    }
                }
            } else {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet(
                            modifier = Modifier.glassChrome(material = GlassMaterial.THICK),
                            drawerContainerColor = glassContainerColor(TajsOSTheme.SidebarBackground),
                        ) {
                            AppSidebar(
                                shellState = shellState,
                                menuGroups = Screen.groupedItemsForPacks(packRegistry),
                                currentRootScreen = currentRoot,
                                currentScreen = currentScreen,
                                activeTasksTab = activeTasksTab,
                                currentMode = currentMode,
                                userProfile = userProfile,
                                onNavigate = { screen ->
                                    onNavigate(screen)
                                    scope.launch { drawerState.close() }
                                },
                                onNavigateToTasksTab = { tab ->
                                    onNavigateToTasksTab(tab)
                                    scope.launch { drawerState.close() }
                                },
                                onNewEntry = onNewEntry,
                                onNavigateToProfile = {
                                    onNavigate(Screen.Profile)
                                    scope.launch { drawerState.close() }
                                },
                                useFixedWidth = false,
                                applyGlass = false,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    },
                ) {
                    Column(modifier = Modifier.fillMaxSize().background(TajsOSTheme.Background.copy(alpha = 0.74f))) {
                        AppShellHeader(
                            greeting = greeting,
                            protocolText = protocolText,
                            currentModeLabel = currentMode?.name ?: "STANDBY",
                            modeOptions = modeOptions,
                            notifications = notifications,
                            shellState = shellState,
                            isDesktop = false,
                            onModeSelect = onModeSelect,
                            onMenuClick = { scope.launch { drawerState.open() } },
                            screenHeader = screenHeader,
                        )
                        Box(modifier = Modifier.fillMaxSize()) {
                            content()
                        }
                    }
                }
            }
        }
    }
}

private fun rememberModeOptions(modes: List<ModeEntity>): List<ShellModeOption> {
    if (modes.isNotEmpty()) {
        return modes.map { mode ->
            ShellModeOption(
                id = mode.id,
                name = mode.name,
                color = mode.themeColor?.let(::Color) ?: TajsOSTheme.Primary,
            )
        }
    }

    return listOf(
        ShellModeOption(
            id = -1L,
            name = "Focus",
            color = TajsOSTheme.PrimaryDim,
            isSelectable = false,
        ),
        ShellModeOption(
            id = -2L,
            name = "Execution",
            color = TajsOSTheme.AccentBlue,
            isSelectable = false,
        ),
        ShellModeOption(
            id = -3L,
            name = "Recovery",
            color = TajsOSTheme.AccentAmber,
            isSelectable = false,
        ),
    )
}

private fun timeGreeting(displayName: String): String {
    val hour =
        Clock.System
            .now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .hour
    val dayGreeting =
        when (hour) {
            in 5..11 -> "Good morning"
            in 12..17 -> "Good afternoon"
            in 18..22 -> "Good evening"
            else -> "Good night"
        }

    return "$dayGreeting, $displayName"
}
