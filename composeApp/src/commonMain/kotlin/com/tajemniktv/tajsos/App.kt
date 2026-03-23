/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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

    val groupedItems =
        remember {
            listOf(
                Res.string.nav_core to listOf(Screen.Dashboard, Screen.Inbox, Screen.Search),
                Res.string.nav_execution to listOf(
                    Screen.Today,
                    Screen.Tasks,
                    Screen.Focus,
                    Screen.Calendar
                ),
                Res.string.nav_brain to listOf(Screen.Notes, Screen.Projects, Screen.Areas),
                Res.string.nav_status to listOf(
                    Screen.Track,
                    Screen.Insights,
                    Screen.Graph,
                    Screen.Review
                ),
                Res.string.nav_system to listOf(Screen.Archive, Screen.Settings),
            )
        }

    TajsOSTheme {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = TactileTheme.Background,
                    drawerShape = RoundedCornerShape(0.dp),
                ) {
                    SidebarContent(
                        groupedItems = groupedItems,
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
            Scaffold(
                topBar = {
                    if (currentDestination?.route != Screen.Dashboard.route) {
                        TopAppBar(
                            navigationIcon = {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(
                                        Icons.Default.Menu,
                                        contentDescription = stringResource(Res.string.nav_menu),
                                        tint = TactileTheme.Primary,
                                    )
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
                    FloatingActionButton(
                        onClick = { showCaptureSheet = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(TactileTheme.RadiusMd),
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(Res.string.nav_capture),
                        )
                    }
                },
            ) { innerPadding ->
                NavHost(
                    navController,
                    startDestination = Screen.Dashboard.route,
                    modifier = Modifier.padding(innerPadding),
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
                            showCaptureSheet = false
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
    }
}

/**
 * Sidebar content with profile, navigation items, and system uptime.
 */
@Composable
private fun SidebarContent(
    groupedItems: List<Pair<StringResource, List<Screen>>>,
    currentDestination: NavDestination?,
    onNavigate: (Screen) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxHeight(),
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
                        .background(Color(0xFFFDE68A), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = TactileTheme.Surface,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(Modifier.width(TactileTheme.SpacingMd))
            Column {
                Text(
                    stringResource(Res.string.app_name),
                    style = MaterialTheme.typography.titleMedium,
                    color = TactileTheme.Text,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    stringResource(Res.string.nav_role_admin),
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Primary,
                    letterSpacing = 1.sp,
                )
            }
        }

        Spacer(Modifier.height(TactileTheme.SpacingSm))

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
        ) {
            groupedItems.forEach { (headerRes, items) ->
                Text(
                    stringResource(headerRes),
                    modifier =
                        Modifier.padding(
                            horizontal = TactileTheme.SpacingMd,
                            vertical = TactileTheme.SpacingSm,
                        ),
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Muted.copy(alpha = 0.6f),
                )
                items.forEach { screen ->
                    val selected = remember(currentDestination, screen.route) {
                        currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    }

                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp),
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

                        NavigationDrawerItem(
                            label = {
                                Text(
                                    stringResource(screen.label),
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            },
                            selected = selected,
                            onClick = { onNavigate(screen) },
                            icon = { Icon(screen.icon, contentDescription = null) },
                            modifier =
                                Modifier
                                    .padding(start = if (selected) 2.dp else 0.dp)
                                    .padding(NavigationDrawerItemDefaults.ItemPadding),
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
                Spacer(Modifier.height(TactileTheme.SpacingSm))
            }
        }

        // Uptime Footer
        Column(
            modifier =
                Modifier
                    .padding(TactileTheme.SpacingMd)
                    .background(
                        TactileTheme.Surface.copy(alpha = 0.5f),
                        RoundedCornerShape(TactileTheme.RadiusMd)
                    )
                    .padding(TactileTheme.SpacingMd),
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


