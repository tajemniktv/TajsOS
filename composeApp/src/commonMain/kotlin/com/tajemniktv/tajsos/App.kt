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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.StableList
import com.tajemniktv.tajsos.data.TemplateEntity
import com.tajemniktv.tajsos.data.toStableList
import com.tajemniktv.tajsos.ui.DetailNavigationContract
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.common.CaptureSheet
import com.tajemniktv.tajsos.ui.components.layout.AppLayout
import com.tajemniktv.tajsos.ui.components.layout.rememberAppShellState
import com.tajemniktv.tajsos.ui.screens.archive.ArchiveScreen
import com.tajemniktv.tajsos.ui.screens.areas.AreasScreen
import com.tajemniktv.tajsos.ui.screens.areas.detail.AreaDetailScreen
import com.tajemniktv.tajsos.ui.screens.briefing.BriefingScreen
import com.tajemniktv.tajsos.ui.screens.calendar.CalendarScreen
import com.tajemniktv.tajsos.ui.screens.calendar.CalendarSettingsScreen
import com.tajemniktv.tajsos.ui.screens.capacity.CapacityScreen
import com.tajemniktv.tajsos.ui.screens.dashboard.DashboardScreen
import com.tajemniktv.tajsos.ui.screens.decisions.DecisionsScreen
import com.tajemniktv.tajsos.ui.screens.finance.FinancesScreen
import com.tajemniktv.tajsos.ui.screens.focus.FocusScreen
import com.tajemniktv.tajsos.ui.screens.graph.GraphScreen
import com.tajemniktv.tajsos.ui.screens.health.HealthScreen
import com.tajemniktv.tajsos.ui.screens.identity.IdentityScreen
import com.tajemniktv.tajsos.ui.screens.inbox.InboxScreen
import com.tajemniktv.tajsos.ui.screens.insights.InsightsScreen
import com.tajemniktv.tajsos.ui.screens.notes.NotesScreen
import com.tajemniktv.tajsos.ui.screens.notes.detail.NoteDetailScreen
import com.tajemniktv.tajsos.ui.screens.openloops.OpenLoopsScreen
import com.tajemniktv.tajsos.ui.screens.places.PlacesScreen
import com.tajemniktv.tajsos.ui.screens.profile.ProfileScreen
import com.tajemniktv.tajsos.ui.screens.projects.ProjectsScreen
import com.tajemniktv.tajsos.ui.screens.projects.detail.ProjectDetailScreen
import com.tajemniktv.tajsos.ui.screens.protocols.ProtocolsScreen
import com.tajemniktv.tajsos.ui.screens.records.detail.RecordDetailScreen
import com.tajemniktv.tajsos.ui.screens.relationships.RelationshipsScreen
import com.tajemniktv.tajsos.ui.screens.review.ReviewScreen
import com.tajemniktv.tajsos.ui.screens.rules.RulesScreen
import com.tajemniktv.tajsos.ui.screens.search.SearchScreen
import com.tajemniktv.tajsos.ui.screens.settings.SettingsScreen
import com.tajemniktv.tajsos.ui.screens.study.StudyScreen
import com.tajemniktv.tajsos.ui.screens.tasks.TasksScreen
import com.tajemniktv.tajsos.ui.screens.tasks.TasksTab
import com.tajemniktv.tajsos.ui.screens.tasks.detail.TaskDetailScreen
import com.tajemniktv.tajsos.ui.screens.templates.TemplatesScreen
import com.tajemniktv.tajsos.ui.screens.timearchitecture.TimeArchitectureScreen
import com.tajemniktv.tajsos.ui.screens.today.TodayScreen
import com.tajemniktv.tajsos.ui.screens.track.TrackScreen
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
 * @param onVoiceCaptureConsume Callback invoked when the voice capture result has been consumed (clears or acknowledges the result).
 * @param onPickAvatar Optional callback used by the profile screen to request a platform avatar picker.
 * @param avatarPickResult Optional selected avatar reference (URI/path) from a platform picker.
 * @param onAvatarPickConsume Callback invoked after the avatar picker result has been consumed by UI state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    viewModel: MainViewModel,
    onVoiceCapture: (() -> Unit)? = null,
    voiceCaptureResult: String? = null,
    onVoiceCaptureConsume: () -> Unit = {},
    onPickAvatar: (() -> Unit)? = null,
    avatarPickResult: String? = null,
    onAvatarPickConsume: () -> Unit = {},
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
    val accentColorHex by viewModel.accentColorHex.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val sidebarMode by viewModel.sidebarMode.collectAsState()

    var showCaptureSheetState by remember { mutableStateOf(value = false) }
    var selectedTasksTab by rememberSaveable { mutableStateOf(TasksTab.COMMAND) }
    val shellState = rememberAppShellState(sidebarMode = sidebarMode)

    val accentColor =
        remember(accentColorHex) {
            try {
                val hex = accentColorHex.removePrefix("#")
                if (hex.length == 6) {
                    androidx.compose.ui.graphics.Color(
                        red = hex.substring(0, 2).toInt(16),
                        green = hex.substring(2, 4).toInt(16),
                        blue = hex.substring(4, 6).toInt(16),
                    )
                } else {
                    androidx.compose.ui.graphics
                        .Color(0xFFBA9EFF)
                }
            } catch (_: Exception) {
                androidx.compose.ui.graphics
                    .Color(0xFFBA9EFF)
            }
        }

    remember(currentDestination) { Screen.fromRoute(currentDestination?.route) }

    TajsOSTheme(darkTheme = isDarkTheme, accentColor = accentColor) {
        BoxWithConstraints {
            val isDesktop = maxWidth > 800.dp

            // The primary navigation callback for the main content area.
            val navigate: (String) -> Unit = {
                val resolvedRoute =
                    if (it == Screen.Tasks.route) {
                        Screen.Tasks.route + "?tab=" + selectedTasksTab.routeSegment
                    } else {
                        it
                    }

                if (resolvedRoute.startsWith(Screen.Tasks.route + "?tab=")) {
                    val tabSegment =
                        resolvedRoute.substringAfter("?tab=", missingDelimiterValue = "")
                    selectedTasksTab = TasksTab.fromRouteSegment(tabSegment)
                }

                val targetScreen = Screen.fromRoute(resolvedRoute)
                if (targetScreen?.isRoot == true) {
                    navController.popBackStack(Screen.Dashboard.route, inclusive = false)
                    if (resolvedRoute != Screen.Dashboard.route) {
                        navController.navigate(resolvedRoute) {
                            restoreState = true
                            launchSingleTop = true
                        }
                    }
                } else {
                    navController.navigate(resolvedRoute) {
                        launchSingleTop = true
                    }
                }
            }

            AppLayout(
                isDesktop = isDesktop,
                shellState = shellState,
                currentDestination = currentDestination,
                activeTasksTab = selectedTasksTab,
                onNavigate = { navigate(it.route) },
                onNavigateToTasksTab = {
                    selectedTasksTab = it
                    navigate(Screen.Tasks.route + "?tab=" + it.routeSegment)
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
                    onVoiceCaptureConsume = onVoiceCaptureConsume,
                    onPickAvatar = onPickAvatar,
                    avatarPickResult = avatarPickResult,
                    onAvatarPickConsume = onAvatarPickConsume,
                    allProjects = allProjects.toStableList(),
                    allAreas = allAreas.toStableList(),
                    allNodes = allNodes.toStableList(),
                    allTemplates = allTemplates.toStableList(),
                    lastActiveProjectId = lastActiveProjectId,
                    lastActiveAreaId = lastActiveAreaId,
                    currentDestination = currentDestination,
                    isDesktop = isDesktop,
                    currentTasksTab = selectedTasksTab,
                    onTasksTabChange = {
                        selectedTasksTab = it
                        navigate(Screen.Tasks.route + "?tab=" + it.routeSegment)
                    },
                    onNavigate = navigate,
                )
            }
        }
    }
}

/**
 * Renders the capture sheet overlay.
 *
 * @param show Whether to show the sheet.
 * @param onDismiss Callback to close.
 * @param viewModel Main ViewModel.
 * @param voiceResult Voice text result.
 * @param onVoiceConsume Callback to clear voice result.
 * @param projects Projects list.
 * @param areas Areas list.
 * @param templates Templates list.
 * @param defaultProjectId Default project.
 * @param defaultAreaId Default area.
 * @param onVoiceClick Voice click handler.
 * @param contextScreen Originating screen.
 */
@Suppress("KDocMissingDocumentation")
@Composable
private fun AppCaptureSheet(
    show: Boolean,
    onDismiss: () -> Unit,
    viewModel: MainViewModel,
    voiceResult: String?,
    onVoiceConsume: () -> Unit,
    projects: StableList<NodeEntity>,
    areas: StableList<NodeEntity>,
    templates: StableList<TemplateEntity>,
    defaultProjectId: Long?,
    defaultAreaId: Long?,
    onVoiceClick: (() -> Unit)?,
    contextScreen: String?,
) {
    if (show) {
        CaptureSheet(
            onDismiss = {
                onDismiss()
                onVoiceConsume()
            },
            onCapture = { text, type, projectId, areaId, isRec, recInt, remAt, ctx, sticky, decisionCat ->
                handleOnCapture(
                    viewModel = viewModel,
                    text = text,
                    type = type,
                    projectId = projectId,
                    areaId = areaId,
                    isRec = isRec,
                    recInt = recInt,
                    remAt = remAt,
                    ctx = ctx,
                    sticky = sticky,
                    decisionCat = decisionCat,
                )
            },
            projects = projects.items,
            areas = areas.items,
            templates = templates.items,
            defaultProjectId = defaultProjectId,
            defaultAreaId = defaultAreaId,
            initialText = voiceResult ?: "",
            onVoiceCaptureClick = onVoiceClick,
            contextScreen = contextScreen,
        )
    }
}

/**
 * Renders route content inside the stable shell frame and handles capture entry overlay behavior.
 *
 * The header/sidebar shell is rendered by [AppLayout]. This host keeps content transitions local to
 * the main content area and shows a mobile floating action button plus capture sheet interactions.
 *
 * @param showCaptureSheet Whether the capture sheet is currently visible.
 * @param onShowCaptureSheet Callback to show or hide the capture sheet.
 * @param navController The NavController for within-app content navigation.
 * @param viewModel The main ViewModel providing shared application state.
 * @param onVoiceCapture Optional callback to start voice capture.
 * @param voiceCaptureResult Optional voice capture result to prefill the sheet.
 * @param onVoiceCaptureConsume Callback invoked when the voice result has been consumed.
 * @param onPickAvatar Optional callback to request an avatar picker.
 * @param avatarPickResult Optional URI of a picked avatar.
 * @param onAvatarPickConsume Callback invoked when an avatar pick has been consumed.
 * @param allProjects List of all available projects.
 * @param allAreas List of all available areas.
 * @param allNodes List of all nodes with pin/tag state for navigation resolution.
 * @param allTemplates List of all node templates.
 * @param lastActiveProjectId ID of the project last active during creation.
 * @param lastActiveAreaId ID of the area last active during creation.
 * @param currentDestination The current navigation destination for highlighting/shell-sync.
 * @param isDesktop Whether the current window matches desktop layout constraints.
 * @param currentTasksTab The currently selected tab in the Tasks screen.
 * @param onTasksTabChange Callback to change the active Tasks tab.
 * @param onNavigate The primary navigation callback for the main content area.
 */
@Suppress("KDocMissingDocumentation")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppScaffold(
    showCaptureSheet: Boolean,
    onShowCaptureSheet: (Boolean) -> Unit,
    navController: NavHostController,
    viewModel: MainViewModel,
    onVoiceCapture: (() -> Unit)?,
    voiceCaptureResult: String?,
    onVoiceCaptureConsume: () -> Unit,
    onPickAvatar: (() -> Unit)?,
    avatarPickResult: String?,
    onAvatarPickConsume: () -> Unit,
    allProjects: StableList<NodeEntity>,
    allAreas: StableList<NodeEntity>,
    allNodes: StableList<com.tajemniktv.tajsos.data.NodeWithPin>,
    allTemplates: StableList<TemplateEntity>,
    lastActiveProjectId: Long?,
    lastActiveAreaId: Long?,
    currentDestination: NavDestination?,
    isDesktop: Boolean,
    currentTasksTab: TasksTab,
    onTasksTabChange: (TasksTab) -> Unit,
    onNavigate: (String) -> Unit,
) {
    val onEditNode: (Long) -> Unit = {
        onNavigate(DetailNavigationContract.routeForNodeId(it, allNodes.items))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.fillMaxSize(),
        ) {
            composable(Screen.Briefing.route) {
                BriefingScreen(
                    viewModel = viewModel,
                    onNavigateTo = { onNavigate(it.route) },
                    onNavigateToTasks = { onTasksTabChange(TasksTab.TODAY) },
                    onNewEntry = { onShowCaptureSheet(true) },
                )
            }
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel,
                    onNavigateTo = { onNavigate(it.route) },
                    onEditNode = onEditNode,
                    onNavigateToProject = {
                        onNavigate(
                            Screen.ProjectDetail.route.replace(
                                "{projectId}",
                                it.toString(),
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
            composable(Screen.Tasks.route) {
                TasksScreen(
                    viewModel = viewModel,
                    onEditNode = onEditNode,
                    currentTab = currentTasksTab,
                    onTabChange = onTasksTabChange,
                )
            }
            composable(Screen.Tasks.route + "?tab={tab}") {
                TasksScreen(
                    viewModel = viewModel,
                    onEditNode = onEditNode,
                    currentTab = currentTasksTab,
                    onTabChange = onTasksTabChange,
                )
            }
            composable(Screen.Notes.route) { NotesScreen(viewModel, onEditNode) }
            composable(Screen.Notes.route + "?noteId={noteId}") {
                val noteId =
                    it.savedStateHandle
                        .get<Any>("noteId")
                        ?.toString()
                        ?.toLongOrNull()
                NotesScreen(
                    viewModel = viewModel,
                    onNoteClick = onEditNode,
                    initialSelectedNoteId = noteId,
                )
            }
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
                TemplatesScreen(viewModel) { navController.popBackStack() }
            }
            composable(Screen.Settings.route) {
                SettingsScreen(viewModel)
            }
            val settingsPref =
                Screen.Settings.children.first {
                    (it is Screen.Sub) &&
                        it.route.contains("preferences")
                }
            composable(settingsPref.route) {
                SettingsScreen(viewModel)
            }
            val settingsCal =
                Screen.Settings.children.first {
                    (it is Screen.Sub) &&
                        it.route.contains("calendar")
                }
            composable(settingsCal.route) {
                CalendarSettingsScreen(viewModel)
            }
            composable(Screen.SettingsHealth.route) {
                SettingsScreen(viewModel, screenId = "health")
            }
            composable(Screen.SettingsAppearance.route) {
                SettingsScreen(viewModel, screenId = "appearance")
            }
            composable(Screen.SettingsFeaturePacks.route) {
                SettingsScreen(viewModel, screenId = "feature_packs")
            }
            composable(Screen.SettingsData.route) {
                SettingsScreen(viewModel, screenId = "data")
            }
            composable(Screen.SettingsDebug.route) {
                SettingsScreen(viewModel, screenId = "debug")
            }
            composable(Screen.CalendarSettings.route) {
                CalendarSettingsScreen(viewModel)
            }
            composable(Screen.Projects.route) {
                ProjectsScreen(viewModel) { onNavigate(it) }
            }
            composable(Screen.Areas.route) {
                AreasScreen(viewModel) { onNavigate(it) }
            }
            composable(Screen.ProjectDetail.route) {
                /**
                 * The back stack entry for the project detail screen.
                 */
                val projectId =
                    it.savedStateHandle
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
            composable(Screen.AreaDetail.route) {
                /**
                 * The back stack entry for the area detail screen.
                 */
                val areaId =
                    it.savedStateHandle
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
            composable(Screen.NoteDetail.route) {
                /**
                 * The back stack entry for the note detail screen.
                 */
                val noteId =
                    it.savedStateHandle
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
            composable(Screen.TaskDetail.route) {
                /**
                 * The back stack entry for the task detail screen.
                 */
                val taskId =
                    it.savedStateHandle
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
            composable(Screen.RecordDetail.route) {
                /**
                 * The back stack entry for the record detail screen.
                 */
                val recordId =
                    it.savedStateHandle
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
                InsightsScreen(viewModel) {
                    onNavigate(
                        Screen.ProjectDetail.route.replace(
                            "{projectId}",
                            it.toString(),
                        ),
                    )
                }
            }
            composable(Screen.Graph.route) {
                GraphScreen(viewModel, onNodeClick = onEditNode)
            }
            composable(Screen.Archive.route) { ArchiveScreen(viewModel, onEditNode) }
            composable(Screen.Review.route) {
                ReviewScreen(viewModel) {
                    navController.popBackStack()
                }
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = viewModel,
                    onPickAvatar = onPickAvatar,
                    pickedAvatarRef = avatarPickResult,
                    onAvatarPickConsume = onAvatarPickConsume,
                )
            }
        }

        if (!isDesktop || (currentDestination?.route != Screen.Dashboard.route)) {
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

        AppCaptureSheet(
            show = showCaptureSheet,
            onDismiss = { onShowCaptureSheet(false) },
            viewModel = viewModel,
            voiceResult = voiceCaptureResult,
            onVoiceConsume = onVoiceCaptureConsume,
            projects = allProjects,
            areas = allAreas,
            templates = allTemplates,
            defaultProjectId = lastActiveProjectId,
            defaultAreaId = lastActiveAreaId,
            onVoiceClick = onVoiceCapture,
            contextScreen = currentDestination?.route,
        )
    }
}

/**
 * Handles node capture submission from the capture sheet, delegating to the appropriate ViewModel action.
 *
 * @param viewModel The main ViewModel to trigger capture actions.
 * @param text The node title or raw text.
 * @param type The node type (inbox, project, area, or node type).
 * @param projectId Optional target project.
 * @param areaId Optional target area.
 * @param isRec Whether the node is recurring.
 * @param recInt Recurrence interval.
 * @param remAt Reminder timestamp.
 * @param ctx Creating screen context.
 * @param sticky Whether the node is pinned.
 * @param decisionCat Optional decision category.
 */
private fun handleOnCapture(
    viewModel: MainViewModel,
    text: String,
    type: String,
    projectId: Long?,
    areaId: Long?,
    isRec: Boolean,
    recInt: String?,
    remAt: Long?,
    ctx: String?,
    sticky: Boolean,
    decisionCat: String?,
) {
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
}
