/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.tajemniktv.tajsos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tajemniktv.tajsos.data.ModeEntity
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.TemplateEntity
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.common.CaptureSheet
import com.tajemniktv.tajsos.ui.components.layout.*
import com.tajemniktv.tajsos.ui.screens.*
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*
import kotlin.time.Clock

/**
 * Hosts the application's top-level UI: sets up navigation, collects app state from the ViewModel,
 * manages the capture-sheet and voice-capture lifecycles, and composes the app theme, layout, and
 * navigation graph.
 *
 * @param viewModel The main ViewModel providing app state (projects, areas, templates, modes, tracks, etc.).
 * @param onVoiceCapture Optional callback invoked to start a voice capture session.
 * @param voiceCaptureResult Optional text result from a completed voice capture to prefill the capture sheet.
 * @param onVoiceCaptureConsumed Callback invoked when the voice capture result has been consumed (clears or acknowledges the result).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    viewModel: MainViewModel,
    onVoiceCapture: (() -> Unit)? = null,
    voiceCaptureResult: String? = null,
    onVoiceCaptureConsumed: () -> Unit = {},
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val allProjects by viewModel.allProjects.collectAsState()
    val allAreas by viewModel.allAreas.collectAsState()
    val allTemplates by viewModel.allTemplates.collectAsState()
    val latestTrack by viewModel.trackEntries.collectAsState().let {
        derivedStateOf { it.value.lastOrNull() }
    }
    val lastActiveProjectId by viewModel.lastActiveProjectId.collectAsState()
    val lastActiveAreaId by viewModel.lastActiveAreaId.collectAsState()

    val currentMode by viewModel.currentMode.collectAsState()
    val allModes by viewModel.allModes.collectAsState()
    val enabledPacks by viewModel.enabledPacks.collectAsState()

    var showCaptureSheetState by remember { mutableStateOf(false) }

    val screen =
        remember(currentDestination) {
            Screen.fromRoute(currentDestination?.route)
        }

    TajsOSTheme {
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
                currentDestination = currentDestination,
                onNavigate = { screen -> navigate(screen.route) },
                onNewEntry = { showCaptureSheetState = true },
                currentMode = currentMode,
                allModes = allModes,
                packRegistry = enabledPacks,
                onModeSelect = { viewModel.switchMode(it) },
                drawerState = drawerState,
                scope = scope,
            ) {
                AppScaffold(
                    screen = screen,
                    drawerState = drawerState,
                    scope = scope,
                    latestTrack = latestTrack,
                    showCaptureSheet = showCaptureSheetState,
                    onShowCaptureSheet = { showCaptureSheetState = it },
                    navController = navController,
                    viewModel = viewModel,
                    onVoiceCapture = onVoiceCapture,
                    voiceCaptureResult = voiceCaptureResult,
                    onVoiceCaptureConsumed = onVoiceCaptureConsumed,
                    allProjects = allProjects,
                    allAreas = allAreas,
                    allTemplates = allTemplates,
                    lastActiveProjectId = lastActiveProjectId,
                    lastActiveAreaId = lastActiveAreaId,
                    currentDestination = currentDestination,
                    currentMode = currentMode,
                    isDesktop = isDesktop,
                    onNavigate = navigate,
                )
            }
        }
    }
}

/**
 * Renders the app scaffold including the top app bar, floating action button, navigation host, and capture sheet.
 *
 * The composable displays a dynamic header based on `screen` and local time, shows a FAB for creating entries when appropriate,
 * hosts the navigation graph, and conditionally presents the capture sheet for creating projects, areas, or nodes.
 *
 * @param screen The current screen descriptor or `null` when unknown; used to derive title, subtitle, and root behavior.
 * @param drawerState Drawer state used to open/close the navigation drawer.
 * @param scope Coroutine scope for launching drawer/opening and other UI coroutines.
 * @param latestTrack Most recent track entry; used by destination screens that display tracking info.
 * @param showCaptureSheet Whether the capture sheet is currently visible.
 * @param onShowCaptureSheet Callback to show or hide the capture sheet.
 * @param navController Navigation controller used by the NavHost.
 * @param viewModel View model providing data and actions for screens and capture operations.
 * @param onVoiceCapture Optional callback invoked when the user triggers voice capture from the UI.
 * @param voiceCaptureResult Optional initial text produced by voice capture; supplied to the capture sheet.
 * @param onVoiceCaptureConsumed Callback invoked after the voice capture result has been consumed.
 * @param allProjects List of available project nodes for selection in the capture sheet.
 * @param allAreas List of available area nodes for selection in the capture sheet.
 * @param allTemplates List of available templates for the capture sheet.
 * @param lastActiveProjectId Default project id to preselect in the capture sheet, if any.
 * @param lastActiveAreaId Default area id to preselect in the capture sheet, if any.
 * @param currentDestination The current navigation destination; used by child screens for context.
 * @param currentMode Current UI mode providing theme color and related styling.
 * @param isDesktop True when running in a desktop-sized layout (affects header and FAB behavior).
 * @param onNavigate Callback to request navigation to a given route.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppScaffold(
    screen: Screen?,
    drawerState: DrawerState,
    scope: kotlinx.coroutines.CoroutineScope,
    latestTrack: com.tajemniktv.tajsos.data.TrackEntryEntity?,
    showCaptureSheet: Boolean,
    onShowCaptureSheet: (Boolean) -> Unit,
    navController: NavHostController,
    viewModel: MainViewModel,
    onVoiceCapture: (() -> Unit)?,
    voiceCaptureResult: String?,
    onVoiceCaptureConsumed: () -> Unit,
    allProjects: List<NodeEntity>,
    allAreas: List<NodeEntity>,
    allTemplates: List<TemplateEntity>,
    lastActiveProjectId: Long?,
    lastActiveAreaId: Long?,
    currentDestination: NavDestination?,
    currentMode: ModeEntity?,
    isDesktop: Boolean,
    onNavigate: (String) -> Unit,
) {
    val now = Clock.System.now()
    val localNow = now.toLocalDateTime(TimeZone.currentSystemDefault())
    val currentHour = localNow.hour

    val vibeStringRes =
        when (currentHour)
        {
            in 5..11 -> Res.string.dash_vibe_morning
            in 12..17 -> Res.string.dash_vibe_afternoon
            in 18..22 -> Res.string.dash_vibe_evening
            else -> Res.string.dash_vibe_night
        }

    val subtitle =
        if (screen == Screen.Dashboard) {
            stringResource(vibeStringRes)
        } else {
            screen?.label?.let { stringResource(it) } ?: ""
        }

    val tintColor = currentMode?.themeColor?.let { Color(it) } ?: TactileTheme.Primary

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (screen?.isRoot == true) {
                        if (!isDesktop) {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                TBoxIcon(tintColor = tintColor)
                            }
                        }
                    } else {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(Res.string.common_back),
                                tint = TactileTheme.Text,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                },
                title = {
                    StatusHeader(
                        status = "OK",
                        color = tintColor,
                        subtitle = subtitle,
                        subtitleStyle =
                            if (screen ==
                                Screen.Dashboard
                            ) {
                                MaterialTheme.typography.titleSmall
                            } else {
                                MaterialTheme.typography.titleMedium
                            },
                    )
                },
                actions = {
                    val localActions = LocalHeaderActions.current
                    localActions()

                    if (isDesktop && screen == Screen.Dashboard) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(end = 16.dp),
                        ) {
                            DesktopSearchSurface()

                            Box(
                                modifier =
                                    Modifier
                                        .size(40.dp)
                                        .background(
                                            TactileTheme.Surface,
                                            RoundedCornerShape(TactileTheme.RadiusMd),
                                        ).border(
                                            1.dp,
                                            TactileTheme.Border,
                                            RoundedCornerShape(TactileTheme.RadiusMd),
                                        ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = TactileTheme.Text,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 8.dp),
                        ) {
                            SystemOnlineStatus(tintColor = tintColor)
                            Spacer(Modifier.width(12.dp))
                            IconButton(onClick = { onNavigate(Screen.Settings.route) }) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = TactileTheme.Muted,
                                )
                            }
                        }
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
            )
        },
        floatingActionButton = {
            if (!isDesktop || currentDestination?.route != Screen.Dashboard.route) {
                FloatingActionButton(
                    onClick = { onShowCaptureSheet(true) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(TactileTheme.RadiusMd),
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(Res.string.nav_capture),
                    )
                }
            }
        },
    ) { innerPadding ->
        val onEditNode: (Long) -> Unit = { id ->
            onNavigate(
                Screen.NoteDetail.route.replace(
                    "{noteId}",
                    id.toString(),
                ),
            )
        }

        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding),
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
            composable(Screen.Tasks.route) { TasksScreen(viewModel, onEditNode) }
            composable(Screen.Notes.route) { NotesScreen(viewModel, onEditNode) }
            composable(Screen.Calendar.route) { CalendarScreen(viewModel, onEditNode) }
            composable(Screen.Decisions.route) { DecisionsScreen(viewModel, onEditNode) }
            composable(Screen.Operations.route) {
                DashboardScreen(
                    viewModel,
                    onNavigateTo = { screen -> onNavigate(screen.route) },
                    onEditNode = onEditNode,
                    onNavigateToProject = { projectId ->
                        onNavigate(
                            Screen.ProjectDetail.route.replace(
                                "{projectId}",
                                projectId.toString(),
                            ),
                        )
                    },
                    onNewEntry = { onShowCaptureSheet(true) },
                    currentDestination = navController.currentDestination,
                )
            }
            composable(Screen.OpenLoops.route) { OpenLoopsScreen(viewModel, onEditNode) }
            composable(Screen.Protocols.route) { ProtocolsScreen(viewModel, onEditNode) }
            composable(Screen.TimeArchitecture.route) {
                TimeArchitectureScreen(viewModel, onEditNode)
            }
            composable(Screen.Places.route) { PlacesScreen(viewModel, onEditNode) }
            composable(Screen.Finances.route) { FinancesScreen(viewModel, onEditNode) }
            composable(Screen.Relationships.route) { RelationshipsScreen(viewModel, onEditNode) }
            composable(Screen.Study.route) { StudyScreen(viewModel, onEditNode) }
            composable(Screen.Rules.route) { RulesScreen(viewModel, onEditNode) }
            composable(Screen.Vaults.route) { VaultsScreen(viewModel, onEditNode) }
            composable(Screen.Capacity.route) { CapacityScreen(viewModel) }
            composable(Screen.Identity.route) { IdentityScreen(viewModel, onEditNode) }
            composable(Screen.StudentBoard.route) { StudentBoardScreen(viewModel, onEditNode) }
            composable(Screen.Templates.route) {
                TemplatesScreen(viewModel, onBack = { navController.popBackStack() })
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel,
                    onNavigateToCalendarSettings = { onNavigate(Screen.CalendarSettings.route) },
                    onNavigateToTemplates = { onNavigate(Screen.Templates.route) },
                )
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
            composable(Screen.Profile.route) { ProfileScreen(viewModel) }
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
                        "project" -> {
                            viewModel.addProject(text, "", areaId)
                        }

                        "area" -> {
                            viewModel.addArea(text)
                        }

                        else -> {
                            viewModel.addNode(
                                text,
                                "",
                                type,
                                projectId,
                                areaId,
                                isRec,
                                recInt,
                                remAt,
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
