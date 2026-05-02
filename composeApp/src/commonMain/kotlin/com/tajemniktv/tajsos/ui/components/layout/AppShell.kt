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
import androidx.compose.ui.Modifier
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
import com.tajemniktv.tajsos.ui.components.common.glassChrome
import com.tajemniktv.tajsos.ui.components.common.glassContainerColor
import com.tajemniktv.tajsos.ui.components.notifications.NotificationUiModel
import com.tajemniktv.tajsos.ui.components.screen.ScreenHeaderModel
import com.tajemniktv.tajsos.ui.screens.tasks.TasksTab
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.briefing_greeting_afternoon
import tajsos.composeapp.generated.resources.briefing_greeting_evening
import tajsos.composeapp.generated.resources.briefing_greeting_morning
import tajsos.composeapp.generated.resources.briefing_greeting_night
import tajsos.composeapp.generated.resources.shell_mode_execution
import tajsos.composeapp.generated.resources.shell_mode_focus
import tajsos.composeapp.generated.resources.shell_mode_recovery
import tajsos.composeapp.generated.resources.shell_mode_standby
import tajsos.composeapp.generated.resources.shell_protocol_state_standby
import tajsos.composeapp.generated.resources.shell_protocol_state_with_name
import kotlin.time.Clock

/**
 * Top-level application chrome with persistent sidebar, shell header, and routed content slot.
 *
 * @param isDesktop Whether to use the wide desktop layout with persistent sidebar.
 * @param shellState The current state of the application shell (e.g. expanded state).
 * @param currentDestination The currently active navigation destination.
 * @param activeTasksTab The active tab within the tasks domain.
 * @param onNavigate Callback for screen-level navigation.
 * @param onNavigateToTasksTab Callback for switching between task filter tabs.
 * @param sidebarExpandedWidthDp The current width of the expanded sidebar.
 * @param onSidebarExpandedWidthChange Callback for updating the sidebar width.
 * @param onNewEntry Callback to trigger the global capture flow.
 * @param currentMode The currently active focus/operating mode.
 * @param allModes List of all available operating modes.
 * @param packRegistry Registry of enabled feature packs for conditional UI.
 * @param userProfile The current user's profile information.
 * @param isGlassmorphismEnabled Whether to apply glassmorphism effects to the UI.
 * @param onModeSelect Callback for switching between operating modes.
 * @param drawerState State of the navigation drawer used in mobile layout.
 * @param scope Coroutine scope for shell-level animations and actions.
 * @param screenHeader Model defining the title and actions for the current screen.
 * @param notifications List of pending system notifications.
 * @param modifier The modifier to be applied to the shell root.
 * @param content The main screen content to be rendered within the shell.
 */
@Composable
fun AppShell(
    isDesktop: Boolean,
    shellState: AppShellState,
    currentDestination: NavDestination?,
    activeTasksTab: TasksTab,
    onNavigate: (Screen) -> Unit,
    onNavigateToTasksTab: (TasksTab) -> Unit,
    sidebarExpandedWidthDp: Int,
    onSidebarExpandedWidthChange: (Int) -> Unit,
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
    notifications: List<NotificationUiModel>,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val currentScreen = Screen.fromRoute(currentDestination?.route)
    val currentRoot = currentScreen?.sidebarContextRoot
    val userName = userProfile.resolveDisplayName()
    val greeting = timeGreeting(userName)
    val protocolText =
        currentMode?.name?.let { stringResource(Res.string.shell_protocol_state_with_name, it) }
            ?: stringResource(Res.string.shell_protocol_state_standby)
    val modeOptions = rememberModeOptions(allModes)
    val standbyLabel = stringResource(Res.string.shell_mode_standby)
    val hazeState = rememberHazeState(blurEnabled = isGlassmorphismEnabled)
    ProvideGlassSystem(
        enabled = isGlassmorphismEnabled,
        hazeState = hazeState,
    ) {
        Box(modifier = modifier.fillMaxSize()) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(TajsOSTheme.Background)
                        .then(
                            if (isGlassmorphismEnabled) {
                                Modifier.hazeSource(hazeState)
                            } else {
                                Modifier
                            },
                        ),
            )
            if (isDesktop) {
                Row(modifier = Modifier.fillMaxSize().background(TajsOSTheme.Background)) {
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
                        expandedWidthDp = sidebarExpandedWidthDp,
                        resizeEnabled = true,
                        onExpandedWidthCommit = onSidebarExpandedWidthChange,
                        onNewEntry = onNewEntry,
                        onNavigateToProfile = { onNavigate(Screen.Profile) },
                    )

                    Column(modifier = Modifier.fillMaxSize()) {
                        AppShellHeader(
                            greeting = greeting,
                            protocolText = protocolText,
                            currentModeLabel = currentMode?.name ?: standbyLabel,
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
                            modifier =
                                Modifier.glassChrome(
                                    shape =
                                        androidx.compose.foundation.shape
                                            .RoundedCornerShape(0.dp),
                                    material = GlassMaterial.THICK,
                                ),
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
                                expandedWidthDp = sidebarExpandedWidthDp,
                                onNewEntry = onNewEntry,
                                onNavigateToProfile = {
                                    onNavigate(Screen.Profile)
                                    scope.launch { drawerState.close() }
                                },
                                forceExpandedPresentation = true,
                                useFixedWidth = false,
                                applyGlass = false,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    },
                ) {
                    Column(modifier = Modifier.fillMaxSize().background(TajsOSTheme.Background)) {
                        AppShellHeader(
                            greeting = greeting,
                            protocolText = protocolText,
                            currentModeLabel = currentMode?.name ?: standbyLabel,
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

/**
 * Maps raw mode entities to display-ready shell mode options.
 *
 * @param modes The list of available modes from the repository.
 * @return A list of [ShellModeOption] for the mode selector.
 */
@Composable
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
            name = stringResource(Res.string.shell_mode_focus),
            color = TajsOSTheme.PrimaryDim,
            isSelectable = false,
        ),
        ShellModeOption(
            id = -2L,
            name = stringResource(Res.string.shell_mode_execution),
            color = TajsOSTheme.AccentBlue,
            isSelectable = false,
        ),
        ShellModeOption(
            id = -3L,
            name = stringResource(Res.string.shell_mode_recovery),
            color = TajsOSTheme.AccentAmber,
            isSelectable = false,
        ),
    )
}

/**
 * Generates a time-aware greeting message for the user.
 *
 * @param displayName The name to address the user by.
 * @return A localized greeting string (e.g. "Good morning, human").
 */
@Composable
private fun timeGreeting(displayName: String): String {
    val hour =
        Clock.System
            .now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .hour
    val dayGreeting =
        when (hour) {
            in 5..11 -> stringResource(Res.string.briefing_greeting_morning)
            in 12..17 -> stringResource(Res.string.briefing_greeting_afternoon)
            in 18..22 -> stringResource(Res.string.briefing_greeting_evening)
            else -> stringResource(Res.string.briefing_greeting_night)
        }

    return "$dayGreeting, $displayName"
}
