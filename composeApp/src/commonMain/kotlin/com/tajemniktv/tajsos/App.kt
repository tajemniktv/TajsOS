/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.tajemniktv.tajsos.ui.components.layout.AppLayout
import com.tajemniktv.tajsos.ui.design.theme.TactileTheme
import com.tajemniktv.tajsos.ui.design.theme.TajsOSTheme
import com.tajemniktv.tajsos.ui.screens.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*

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

    var showCaptureSheet by remember { mutableStateOf(false) }

    val internalHeaderRoutes = listOf(
        Screen.Dashboard.route,
        Screen.Review.route,
        Screen.Focus.route,
        Screen.Today.route,
        Screen.Graph.route,
        Screen.Review.route,
        Screen.Decisions.route,
        Screen.Profile.route,
        Screen.Track.route
    )

    val showGlobalTopBar = remember(currentDestination) {
        internalHeaderRoutes.none { pattern ->
            currentDestination?.route?.contains(
                pattern.replace("/{", "").split("/").first()
            ) == true
        }
    }

    TajsOSTheme {
        BoxWithConstraints {
            val isDesktop = maxWidth > 800.dp

            AppLayout(
                isDesktop = isDesktop,
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
                onNewEntry = { showCaptureSheet = true },
                currentMode = currentMode,
                allModes = allModes,
                onModeSelect = { viewModel.switchMode(it) },
                drawerState = drawerState,
                scope = scope
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
                    currentMode = currentMode,
                    allModes = allModes,
                    onModeSelect = { viewModel.switchMode(it) },
                    isDesktop = isDesktop
                )
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
    allProjects: List<NodeEntity>,
    allAreas: List<NodeEntity>,
    allTemplates: List<TemplateEntity>,
    lastActiveProjectId: Long?,
    lastActiveAreaId: Long?,
    currentDestination: NavDestination?,
    currentMode: ModeEntity?,
    allModes: List<ModeEntity>,
    onModeSelect: (Long) -> Unit,
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
        val onEditNode: (Long) -> Unit = { id ->
            navController.navigate(
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
                    onNavigateTo = { screen -> navController.navigate(screen.route) },
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
                    currentMode = currentMode,
                    allModes = allModes,
                    onModeSelect = onModeSelect
                )
            }
            composable(Screen.Inbox.route) { InboxScreen(viewModel, onEditNode) }
            composable(Screen.Search.route) { SearchScreen(viewModel, onEditNode) }
            composable(Screen.Today.route) { TodayScreen(viewModel, onEditNode) }
            composable(Screen.Focus.route) { FocusScreen(viewModel) }
            composable(Screen.Track.route) { TrackScreen(viewModel) }
            composable(Screen.Tasks.route) { TasksScreen(viewModel, onEditNode) }
            composable(Screen.Notes.route) { NotesScreen(viewModel, onEditNode) }
            composable(Screen.Decisions.route) { DecisionsScreen(viewModel, onEditNode) }
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
            composable(Screen.Profile.route) { ProfileScreen(viewModel) }
        }

        if (showCaptureSheet) {
            CaptureSheet(
                onDismiss = {
                    onShowCaptureSheet(false)
                    onVoiceCaptureConsumed()
                },
                onCapture = { text,
                              type,
                              projectId,
                              areaId,
                              isRec,
                              recInt,
                              remAt,
                              ctx,
                              sticky,
                              decisionCat
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
                                decisionCategory = decisionCat
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
                contextScreen = currentDestination?.route
            )
        }
    }
}
