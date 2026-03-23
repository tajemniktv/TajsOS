/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.*
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.CaptureSheet
import com.tajemniktv.tajsos.ui.components.SidebarContent
import com.tajemniktv.tajsos.ui.screens.*
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*

/**
 *
 */
/**
 * Main application entry point for all platforms.
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
    var showCaptureSheet by remember { mutableStateOf(false) }

    // When a voice capture result arrives, show the sheet and set the initial text
    LaunchedEffect(voiceCaptureResult) {
        if (voiceCaptureResult != null) {
            showCaptureSheet = true
        }
    }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val trackEntries by viewModel.trackEntries.collectAsState()
    val allProjects by viewModel.allProjects.collectAsState()
    val allAreas by viewModel.allAreas.collectAsState()
    val allTemplates by viewModel.allTemplates.collectAsState()
    val latestTrack = remember(trackEntries) { trackEntries.firstOrNull() }

    val lastActiveProjectId by viewModel.lastActiveProjectId.collectAsState()
    val lastActiveAreaId by viewModel.lastActiveAreaId.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showGlobalTopBar = remember(currentDestination) {
        val route = currentDestination?.route ?: return@remember true
        val internalHeaderRoutes = listOf(
            Screen.Dashboard.route,
            Screen.NoteDetail.route,
            Screen.ProjectDetail.route,
            Screen.AreaDetail.route,
            Screen.Review.route,
            Screen.Templates.route
        )
        internalHeaderRoutes.none { pattern ->
            if (pattern.contains("{")) {
                route.startsWith(pattern.substringBefore("{"))
            } else {
                route == pattern
            }
        }
    }

    TajsOSTheme {
        BoxWithConstraints {
            val isDesktop = maxWidth > 800.dp
            val isDashboard = currentDestination?.route == Screen.Dashboard.route

            if (isDesktop && !isDashboard) {
                // Desktop layout with permanent sidebar for non-dashboard screens
                Row(modifier = Modifier.fillMaxSize().background(TactileTheme.Background)) {
                    Surface(
                        modifier = Modifier.width(280.dp).fillMaxHeight(),
                        color = Color(0xFF0A0A0E),
                        border = BorderStroke(1.dp, TactileTheme.Border)
                    ) {
                        SidebarContent(
                            currentDestination = currentDestination,
                            onNavigate = { screen ->
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onNewEntry = { showCaptureSheet = true }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        AppScaffold(
                            showGlobalTopBar = showGlobalTopBar,
                            drawerState = drawerState,
                            scope = scope,
                            latestTrack = latestTrack,
                            showCaptureSheet = showCaptureSheet,
                            onShowCaptureSheet = { showCaptureSheet = it },
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
                            isDesktop = true
                        )
                    }
                }
            } else if (isDesktop && isDashboard) {
                // Dashboard handles its own layout on Desktop
                AppScaffold(
                    showGlobalTopBar = false, // Dashboard has its own header
                    drawerState = drawerState,
                    scope = scope,
                    latestTrack = latestTrack,
                    showCaptureSheet = showCaptureSheet,
                    onShowCaptureSheet = { showCaptureSheet = it },
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
                    isDesktop = true
                )
            } else {
                // Mobile layout with modal drawer
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet(
                            drawerContainerColor = TactileTheme.Background,
                            drawerShape = RoundedCornerShape(0.dp),
                        ) {
                            SidebarContent(
                                currentDestination = currentDestination,
                                onNavigate = { screen ->
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                    scope.launch { drawerState.close() }
                                },
                            )
                        }
                    },
                ) {
                    AppScaffold(
                        showGlobalTopBar = showGlobalTopBar,
                        drawerState = drawerState,
                        scope = scope,
                        latestTrack = latestTrack,
                        showCaptureSheet = showCaptureSheet,
                        onShowCaptureSheet = { showCaptureSheet = it },
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
                        isDesktop = false
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppScaffold(
    showGlobalTopBar: Boolean,
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
    allProjects: List<com.tajemniktv.tajsos.data.NodeEntity>,
    allAreas: List<com.tajemniktv.tajsos.data.NodeEntity>,
    allTemplates: List<com.tajemniktv.tajsos.data.TemplateEntity>,
    lastActiveProjectId: Long?,
    lastActiveAreaId: Long?,
    currentDestination: NavDestination?,
    isDesktop: Boolean
) {
    Scaffold(
        topBar = {
            if (showGlobalTopBar) {
                TopAppBar(
                    navigationIcon = {
                        if (!isDesktop) {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    Icons.Default.Menu,
                                    contentDescription = stringResource(Res.string.nav_menu),
                                    tint = TactileTheme.Primary,
                                )
                            }
                        }
                    },
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(Res.string.app_status_ok),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            if (latestTrack != null) {
                                Text(
                                    text = "E:${latestTrack.energyScore} M:${latestTrack.moodScore} F:${latestTrack.focusScore ?: "-"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    },
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                        ),
                )
            }
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
        NavHost(
            navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(
                if (currentDestination?.route == Screen.Dashboard.route && isDesktop) PaddingValues(
                    0.dp
                ) else innerPadding
            ),
        ) {
                    val onEditNode: (Long) -> Unit = { id ->
                        navController.navigate(
                            Screen.NoteDetail.route.replace(
                                "{noteId}",
                                id.toString(),
                            ),
                        )
                    }

                    composable(Screen.Dashboard.route) {
                        DashboardScreen(
                            viewModel,
                            onNavigateTo = { screen ->
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onEditNode = onEditNode,
                            onNavigateToProject = { id ->
                                navController.navigate(
                                    Screen.ProjectDetail.route.replace(
                                        "{projectId}",
                                        id.toString(),
                                    ),
                                )
                            },
                            onOpenDrawer = { scope.launch { drawerState.open() } },
                            onNewEntry = { onShowCaptureSheet(true) },
                            currentDestination = currentDestination,
                        )
                    }
                    composable(Screen.Inbox.route) { InboxScreen(viewModel, onEditNode) }
                    composable(Screen.Search.route) {
                        SearchScreen(viewModel, onItemClick = onEditNode)
                    }
                    composable(Screen.Today.route) { TodayScreen(viewModel, onEditNode) }
                    composable(Screen.Calendar.route) {
                        CalendarScreen(viewModel, onEditNode)
                    }
                    composable(Screen.CalendarSettings.route) {
                        CalendarSettingsScreen(viewModel)
                    }
                    composable(Screen.Focus.route) { FocusScreen(viewModel) }
                    composable(Screen.Track.route) { TrackScreen(viewModel) }
                    composable(Screen.Tasks.route) { TasksScreen(viewModel, onEditNode) }
                    composable(Screen.Notes.route) {
                        NotesScreen(viewModel, onNoteClick = onEditNode)
                    }
                    composable(Screen.Settings.route) {
                        SettingsScreen(
                            viewModel,
                            onNavigateToCalendarSettings = {
                                navController.navigate(Screen.CalendarSettings.route)
                            },
                            onNavigateToTemplates = {
                                navController.navigate(Screen.Templates.route)
                            },
                        )
                    }
                    composable(Screen.Templates.route) {
                        TemplatesScreen(viewModel, onBack = { navController.popBackStack() })
                    }
                    composable(Screen.Projects.route) {
                        ProjectsScreen(
                            viewModel,
                            onNavigateTo = { route -> navController.navigate(route) },
                        )
                    }
                    composable(Screen.Areas.route) {
                        AreasScreen(viewModel, onNavigateTo = { route -> navController.navigate(route) })
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
                                navController.navigate(
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
                            onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                        )
                    }
                    composable(Screen.Insights.route) {
                        InsightsScreen(viewModel, onNavigateToProject = { id ->
                            navController.navigate(
                                Screen.ProjectDetail.route.replace(
                                    "{projectId}",
                                    id.toString(),
                                ),
                            )
                        })
                    }
                    composable(Screen.Graph.route) {
                        GraphScreen(viewModel, onNodeClick = onEditNode)
                    }
                    composable(Screen.Archive.route) { ArchiveScreen(viewModel, onEditNode) }
                    composable(Screen.Review.route) {
                        ReviewScreen(viewModel, onBack = { navController.popBackStack() })
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
                                sticky
                            ->
                            when (type) {
                                "project" -> viewModel.addProject(text, "", areaId)
                                "area" -> viewModel.addArea(text)
                                else ->
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
                                    )
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



