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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import com.tajemniktv.tajsos.data.ModeEntity
import com.tajemniktv.tajsos.data.PackRegistry
import com.tajemniktv.tajsos.data.UserProfile
import com.tajemniktv.tajsos.data.resolveDisplayName
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.screens.tasks.TasksTab
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/**
 * Top-level shell layout with persistent desktop sidebar and top header.
 *
 * The shell remains static while content routes change in the main content area.
 */
@Composable
fun AppLayout(
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
    onModeSelect: (Long) -> Unit,
    drawerState: DrawerState,
    scope: CoroutineScope,
    content: @Composable () -> Unit,
) {
    val currentScreen = Screen.fromRoute(currentDestination?.route)
    val currentRoot = currentScreen?.let(Screen::sidebarContextRoot)
    val userName = userProfile.resolveDisplayName()
    val greeting = remember(userName) { timeGreeting(userName) }
    val protocolText = currentMode?.name?.let { "Protocol state: $it" } ?: "Protocol state: standby"
    val modeOptions = rememberModeOptions(allModes)
    val notifications =
        remember {
            listOf(
                "No urgent alerts right now.",
                "Daily review window opens at 19:00.",
                "Focus block available this afternoon.",
            )
        }

    if (isDesktop) {
        Row(modifier = Modifier.fillMaxSize().background(TactileTheme.Background)) {
            AppSidebar(
                shellState = shellState,
                menuGroups = Screen.groupedItemsForPacks(packRegistry),
                currentRootScreen = currentRoot,
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
                    onModeSelect = onModeSelect,
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
                    drawerContainerColor = TactileTheme.SidebarBackground,
                ) {
                    AppSidebar(
                        shellState = shellState,
                        menuGroups = Screen.groupedItemsForPacks(packRegistry),
                        currentRootScreen = currentRoot,
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
                    )
                }
            },
        ) {
            Column(modifier = Modifier.fillMaxSize().background(TactileTheme.Background)) {
                AppShellHeader(
                    greeting = greeting,
                    protocolText = protocolText,
                    currentModeLabel = currentMode?.name ?: "STANDBY",
                    modeOptions = modeOptions,
                    notifications = notifications,
                    shellState = shellState,
                    onModeSelect = onModeSelect,
                )
                Box(modifier = Modifier.fillMaxSize()) {
                    content()
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
                color = mode.themeColor?.let(::Color) ?: TactileTheme.Primary,
            )
        }
    }

    return listOf(
        ShellModeOption(
            id = -1L,
            name = "Focus",
            color = TactileTheme.PrimaryDim,
            isSelectable = false,
        ),
        ShellModeOption(
            id = -2L,
            name = "Execution",
            color = TactileTheme.AccentBlue,
            isSelectable = false,
        ),
        ShellModeOption(
            id = -3L,
            name = "Recovery",
            color = TactileTheme.AccentAmber,
            isSelectable = false,
        ),
    )
}

private fun timeGreeting(displayName: String): String {
    val hour = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
    val dayGreeting =
        when (hour) {
            in 5..11 -> "Good morning"
            in 12..17 -> "Good afternoon"
            in 18..22 -> "Good evening"
            else -> "Good night"
        }

    return "$dayGreeting, $displayName"
}
