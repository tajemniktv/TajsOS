/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.TemplateEntity
import com.tajemniktv.tajsos.ui.DetailNavigationContract
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.common.CaptureSheet
import com.tajemniktv.tajsos.ui.components.layout.AppLayout
import com.tajemniktv.tajsos.ui.components.layout.rememberAppShellState
import com.tajemniktv.tajsos.ui.screens.AreaDetailScreen
import com.tajemniktv.tajsos.ui.screens.CalendarSettingsScreen
import com.tajemniktv.tajsos.ui.screens.IdentityScreen
import com.tajemniktv.tajsos.ui.screens.NoteDetailScreen
import com.tajemniktv.tajsos.ui.screens.ProfileScreen
import com.tajemniktv.tajsos.ui.screens.ProjectDetailScreen
import com.tajemniktv.tajsos.ui.screens.RecordDetailScreen
import com.tajemniktv.tajsos.ui.screens.ReviewScreen
import com.tajemniktv.tajsos.ui.screens.RulesScreen
import com.tajemniktv.tajsos.ui.screens.SearchScreen
import com.tajemniktv.tajsos.ui.screens.SettingsAppearanceScreen
import com.tajemniktv.tajsos.ui.screens.SettingsDataScreen
import com.tajemniktv.tajsos.ui.screens.SettingsDebugScreen
import com.tajemniktv.tajsos.ui.screens.SettingsFeaturePacksScreen
import com.tajemniktv.tajsos.ui.screens.SettingsHealthScreen
import com.tajemniktv.tajsos.ui.screens.SettingsScreen
import com.tajemniktv.tajsos.ui.screens.TaskDetailScreen
import com.tajemniktv.tajsos.ui.screens.tasks.TasksScreen
import com.tajemniktv.tajsos.ui.screens.tasks.TasksTab
import com.tajemniktv.tajsos.ui.screens.TemplatesScreen
import com.tajemniktv.tajsos.ui.screens.TimeArchitectureScreen
import com.tajemniktv.tajsos.ui.screens.TrackScreen
import com.tajemniktv.tajsos.ui.screens.archive.ArchiveScreen
import com.tajemniktv.tajsos.ui.screens.areas.AreasScreen
import com.tajemniktv.tajsos.ui.screens.calendar.CalendarScreen
import com.tajemniktv.tajsos.ui.screens.capacity.CapacityScreen
import com.tajemniktv.tajsos.ui.screens.dashboard.DashboardScreen
import com.tajemniktv.tajsos.ui.screens.decisions.DecisionsScreen
import com.tajemniktv.tajsos.ui.screens.finance.FinancesScreen
import com.tajemniktv.tajsos.ui.screens.focus.FocusScreen
import com.tajemniktv.tajsos.ui.screens.graph.GraphScreen
import com.tajemniktv.tajsos.ui.screens.health.HealthScreen
import com.tajemniktv.tajsos.ui.screens.inbox.InboxScreen
import com.tajemniktv.tajsos.ui.screens.insights.InsightsScreen
import com.tajemniktv.tajsos.ui.screens.notes.NotesScreen
import com.tajemniktv.tajsos.ui.screens.openloops.OpenLoopsScreen
import com.tajemniktv.tajsos.ui.screens.places.PlacesScreen
import com.tajemniktv.tajsos.ui.screens.projects.ProjectsScreen
import com.tajemniktv.tajsos.ui.screens.protocols.ProtocolsScreen
import com.tajemniktv.tajsos.ui.screens.relationships.RelationshipsScreen
import com.tajemniktv.tajsos.ui.screens.study.StudyScreen
import com.tajemniktv.tajsos.ui.screens.today.TodayScreen
import com.tajemniktv.tajsos.ui.screens.vaults.VaultsScreen
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.nav_capture

/**
 * Hosts the application's top-level UI: sets up navigation, collects app state from the ViewModel,
 * manages the capture-sheet and voice-capture lifecycles, and composes the app theme, layout, and
 * navigation graph.
 *
 * @param viewModel The main ViewModel providing app state (projects, areas, templates, modes, tracks, etc.).
 * @param onVoiceCapture Optional callback invoked to start a voice capture session.
 * @param voiceCaptureResult Optional text result from a completed voice capture to prefill the capture sheet.
 * @param onVoiceCaptureConsumed Callback invoked when the voice capture result has been consumed (clears or acknowledges the result).
 * @param onPickAvatar Optional callback used by the profile screen to request a platform avatar picker.
 * @param avatarPickResult Optional selected avatar reference (URI/path) from a platform picker.
 * @param onAvatarPickConsumed Callback invoked after the avatar picker result has been consumed by UI state.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    viewModel: MainViewModel,
    onVoiceCapture: (() -> Unit)? = null,
    voiceCaptureResult: String? = null,
    onVoiceCaptureConsumed: () -> Unit = {},
    onPickAvatar: (() -> Unit)? = null,
    avatarPickResult: String? = null,
    onAvatarPickConsumed: () -> Unit = {},
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val allProjects by viewModel.allProjects.collectAsState()
    val allAreas by viewModel.allAreas.collectAsState()
    val allTemplates by viewModel.allTemplates.collectAsState()
    val allNodes by viewModel.allNodes.collectAsState()
    val lastActiveProjectId by viewModel.lastActiveProjectId.collectAsState()
    val lastActiveAreaId by viewModel.lastActiveAreaId.collectAsState()

    val currentMode by viewModel.currentMode.collectAsState()
    val allModes by viewModel.allModes.collectAsState()
    val enabledPacks by viewModel.enabledPacks.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    var showCaptureSheetState by remember { mutableStateOf(false) }
    val shellState = rememberAppShellState()

    val screen = remember(currentDestination) { Screen.fromRoute(currentDestination?.route) }
    val activeTasksTab =
        remember(navBackStackEntry, screen) {
            if (screen == Screen.Tasks) {
                val tabFromArgs = navBackStackEntry?.savedStateHandle?.get<Any>("tab")?.toString()
                TasksTab.fromRouteSegment(tabFromArgs)
            } else {
                TasksTab.COMMAND
            }
        }

    TajsOSTheme(darkTheme = isDarkTheme) {
        BoxWithConstraints {
            val isDesktop = maxWidth > 800.dp

            val navigate: (String) -> Unit = { route ->
                if (currentDestination?.route != route) {
                    val targetScreen = Screen.fromRoute(route)
                    navController.navigate(route) {
                        if (targetScreen?.isRoot == true) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            restoreState = true
                        }
                        launchSingleTop = true
                    }
                }
            }

            AppLayout(
                isDesktop = isDesktop,
                shellState = shellState,
                currentDestination = currentDestination,
                activeTasksTab = activeTasksTab,
                onNavigate = { screen -> navigate(screen.route) },
                onNavigateToTasksTab = { tab ->
                    navigate(Screen.Tasks.route + "?tab=" + tab.routeSegment)
                },
                onNewEntry = { showCaptureSheetState = true },
                currentMode = currentMode,
                allModes = allModes,
                packRegistry = enabledPacks,
                userProfile = userProfile,
                onModeSelect = { viewModel.switchMode(it) },
                drawerState = drawerState,
                scope = scope,
            ) {
                AppScaffold(
                    showCaptureSheet = showCaptureSheetState,
                    onShowCaptureSheet = { showCaptureSheetState = it },
                    navController = navController,
                    viewModel = viewModel,
                    onVoiceCapture = onVoiceCapture,
                    voiceCaptureResult = voiceCaptureResult,
                    onVoiceCaptureConsumed = onVoiceCaptureConsumed,
                    onPickAvatar = onPickAvatar,
                    avatarPickResult = avatarPickResult,
                    onAvatarPickConsumed = onAvatarPickConsumed,
                    allProjects = allProjects,
                    allAreas = allAreas,
                    allNodes = allNodes,
                    allTemplates = allTemplates,
                    lastActiveProjectId = lastActiveProjectId,
                    lastActiveAreaId = lastActiveAreaId,
                    currentDestination = currentDestination,
                    isDesktop = isDesktop,
                    onNavigate = navigate,
                )
            }
        }
    }
}

/**
 * Renders route content inside the stable shell frame and handles capture entry overlay behavior.
 *
 * The header/sidebar shell is rendered by [AppLayout]. This host keeps content transitions local to
 * the main content area and shows a mobile floating action button plus capture sheet interactions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppScaffold(
    showCaptureSheet: Boolean,
    onShowCaptureSheet: (Boolean) -> Unit,
    navController: NavHostController,
    viewModel: MainViewModel,
    onVoiceCapture: (() -> Unit)?,
    voiceCaptureResult: String?,
    onVoiceCaptureConsumed: () -> Unit,
    onPickAvatar: (() -> Unit)?,
    avatarPickResult: String?,
    onAvatarPickConsumed: () -> Unit,
    allProjects: List<NodeEntity>,
    allAreas: List<NodeEntity>,
    allNodes: List<com.tajemniktv.tajsos.data.NodeWithPin>,
    allTemplates: List<TemplateEntity>,
    lastActiveProjectId: Long?,
    lastActiveAreaId: Long?,
    currentDestination: NavDestination?,
    isDesktop: Boolean,
    onNavigate: (String) -> Unit,
) {
    val onEditNode: (Long) -> Unit = { id ->
        onNavigate(DetailNavigationContract.routeForNodeId(id, allNodes))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.fillMaxSize(),
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel,
                    onNavigateTo = { screen -> onNavigate(screen.route) },
                    onEditNode = onEditNode,
                    onNavigateToProject = { id ->
                        onNavigate(
                            Screen.ProjectDetail.route.replace(
                                "{projectId}",
                                id.toString(),
                            ),
                        )
                    },
                    onNewEntry = { onShowCaptureSheet(true) },
                    currentDestination = currentDestination,
                )
            }
            composable(Screen.Inbox.route) { InboxScreen(viewModel, onEditNode) }
            composable(Screen.Search.route) { SearchScreen(viewModel, onEditNode) }
            composable(Screen.Today.route) { TodayScreen(viewModel, onEditNode) }
            composable(Screen.Focus.route) { FocusScreen(viewModel) }
            composable(Screen.Track.route) { TrackScreen(viewModel) }
            composable(Screen.Tasks.route + "?tab={tab}") { backStackEntry ->
                val tabSegment = backStackEntry.savedStateHandle.get<Any>("tab")?.toString()
                val tab = TasksTab.fromRouteSegment(tabSegment)
                TasksScreen(
                    viewModel = viewModel,
                    onEditNode = onEditNode,
                    currentTab = tab,
                    onTabChange = { newTab ->
                        onNavigate(Screen.Tasks.route + "?tab=" + newTab.routeSegment)
                    },
                )
            }
            composable(Screen.Notes.route) { NotesScreen(viewModel, onEditNode) }
            composable(Screen.Calendar.route) { CalendarScreen(viewModel, onEditNode) }
            composable(Screen.Decisions.route) { DecisionsScreen(viewModel, onEditNode) }
            composable(Screen.OpenLoops.route) { OpenLoopsScreen(viewModel, onEditNode) }
            composable(Screen.Protocols.route) { ProtocolsScreen(viewModel, onEditNode) }
            composable(Screen.TimeArchitecture.route) {
                TimeArchitectureScreen(viewModel, onEditNode)
            }
            composable(Screen.Places.route) { PlacesScreen(viewModel, onEditNode) }
            composable(Screen.Finances.route) { FinancesScreen(viewModel, onEditNode) }
            composable(Screen.Health.route) { HealthScreen(viewModel, onEditNode) }
            composable(Screen.Relationships.route) { RelationshipsScreen(viewModel, onEditNode) }
            composable(Screen.Education.route) { StudyScreen(viewModel, onEditNode) }
            composable(Screen.StudyLegacy.route) { StudyScreen(viewModel, onEditNode) }
            composable(Screen.Rules.route) { RulesScreen(viewModel, onEditNode) }
            composable(Screen.Vaults.route) { VaultsScreen(viewModel, onEditNode) }
            composable(Screen.Capacity.route) { CapacityScreen(viewModel) }
            composable(Screen.Identity.route) { IdentityScreen(viewModel, onEditNode) }
            composable(Screen.Templates.route) {
                TemplatesScreen(viewModel, onBack = { navController.popBackStack() })
            }
            composable(Screen.Settings.route) {
                SettingsScreen(viewModel)
            }
            composable(Screen.SettingsHealth.route) {
                SettingsHealthScreen(viewModel = viewModel)
            }
            composable(Screen.SettingsAppearance.route) {
                SettingsAppearanceScreen(viewModel = viewModel)
            }
            composable(Screen.SettingsFeaturePacks.route) {
                SettingsFeaturePacksScreen(viewModel = viewModel)
            }
            composable(Screen.SettingsData.route) {
                SettingsDataScreen(viewModel = viewModel)
            }
            composable(Screen.SettingsDebug.route) {
                SettingsDebugScreen()
            }
            composable(Screen.CalendarSettings.route) {
                CalendarSettingsScreen(viewModel)
            }
            composable(Screen.Projects.route) {
                ProjectsScreen(
                    viewModel,
                    onNavigateTo = { route -> onNavigate(route) },
                )
            }
            composable(Screen.Areas.route) {
                AreasScreen(viewModel, onNavigateTo = { route -> onNavigate(route) })
            }
            composable(Screen.ProjectDetail.route) { backStackEntry ->
                val projectId =
                    backStackEntry.savedStateHandle
                        .get<Any>("projectId")
                        ?.toString()
                        ?.toLongOrNull() ?: -1L
                ProjectDetailScreen(
                    viewModel,
                    projectId,
                    onEditNode,
                    onBack = { navController.popBackStack() },
                    isDesktop = isDesktop,
                )
            }
            composable(Screen.AreaDetail.route) { backStackEntry ->
                val areaId =
                    backStackEntry.savedStateHandle
                        .get<Any>("areaId")
                        ?.toString()
                        ?.toLongOrNull() ?: -1L
                AreaDetailScreen(
                    viewModel,
                    areaId,
                    onNavigateToProject = { id ->
                        onNavigate(
                            Screen.ProjectDetail.route.replace(
                                "{projectId}",
                                id.toString(),
                            ),
                        )
                    },
                    onEditNode = onEditNode,
                    onBack = { navController.popBackStack() },
                    isDesktop = isDesktop,
                )
            }
            composable(Screen.NoteDetail.route) { backStackEntry ->
                val noteId =
                    backStackEntry.savedStateHandle
                        .get<Any>("noteId")
                        ?.toString()
                        ?.toLongOrNull() ?: -1L
                NoteDetailScreen(
                    viewModel,
                    noteId,
                    onBack = { navController.popBackStack() },
                    onNavigateToNode = onEditNode,
                    onNavigateToSearch = { onNavigate(Screen.Search.route) },
                    isDesktop = isDesktop,
                )
            }
            composable(Screen.TaskDetail.route) { backStackEntry ->
                val taskId =
                    backStackEntry.savedStateHandle
                        .get<Any>("taskId")
                        ?.toString()
                        ?.toLongOrNull() ?: -1L
                TaskDetailScreen(
                    viewModel = viewModel,
                    taskId = taskId,
                    onBack = { navController.popBackStack() },
                    onNavigateToNode = onEditNode,
                    onNavigateToSearch = { onNavigate(Screen.Search.route) },
                    isDesktop = isDesktop,
                )
            }
            composable(Screen.RecordDetail.route) { backStackEntry ->
                val recordId =
                    backStackEntry.savedStateHandle
                        .get<Any>("recordId")
                        ?.toString()
                        ?.toLongOrNull() ?: -1L
                RecordDetailScreen(
                    viewModel = viewModel,
                    recordId = recordId,
                    onBack = { navController.popBackStack() },
                    onNavigateToNode = onEditNode,
                    onNavigateToSearch = { onNavigate(Screen.Search.route) },
                    isDesktop = isDesktop,
                )
            }
            composable(Screen.Insights.route) {
                InsightsScreen(
                    viewModel,
                    onNavigateToProject = { id ->
                        onNavigate(
                            Screen.ProjectDetail.route.replace(
                                "{projectId}",
                                id.toString(),
                            ),
                        )
                    },
                )
            }
            composable(Screen.Graph.route) {
                GraphScreen(viewModel, onNodeClick = onEditNode)
            }
            composable(Screen.Archive.route) { ArchiveScreen(viewModel, onEditNode) }
            composable(Screen.Review.route) {
                ReviewScreen(viewModel, onBack = { navController.popBackStack() })
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = viewModel,
                    onPickAvatar = onPickAvatar,
                    pickedAvatarRef = avatarPickResult,
                    onAvatarPickedConsumed = onAvatarPickConsumed,
                )
            }
        }

        if (!isDesktop || currentDestination?.route != Screen.Dashboard.route) {
            FloatingActionButton(
                onClick = { onShowCaptureSheet(true) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(Res.string.nav_capture),
                )
            }
        }

        if (showCaptureSheet) {
            CaptureSheet(
                onDismiss = {
                    onShowCaptureSheet(false)
                    onVoiceCaptureConsumed()
                },
                onCapture = {
                    text,
                    type,
                    projectId,
                    areaId,
                    isRec,
                    recInt,
                    remAt,
                    ctx,
                    sticky,
                    decisionCat,
                    ->
                    when (type)
                    {
                        "inbox" -> {
                            viewModel.captureInboxEntry(
                                rawText = text,
                                areaId = areaId,
                                projectId = projectId,
                                contextScreen = ctx,
                            )
                        }

                        "project" -> {
                            viewModel.addProject(text, areaId = areaId)
                        }

                        "area" -> {
                            viewModel.addArea(text)
                        }

                        else -> {
                            viewModel.addNode(
                                text,
                                type = type,
                                projectId = projectId,
                                areaId = areaId,
                                isRecurring = isRec,
                                recurringInterval = recInt,
                                reminderAt = remAt,
                                contextScreen = ctx,
                                isSticky = sticky,
                                decisionCategory = decisionCat,
                            )
                        }
                    }
                    // Note: if multi-capture is on, CaptureSheet handles not closing itself
                },
                projects = allProjects,
                areas = allAreas,
                templates = allTemplates,
                defaultProjectId = lastActiveProjectId,
                defaultAreaId = lastActiveAreaId,
                initialText = voiceCaptureResult ?: "",
                onVoiceCaptureClick = onVoiceCapture,
                contextScreen = currentDestination?.route,
            )
        }
    }
}
