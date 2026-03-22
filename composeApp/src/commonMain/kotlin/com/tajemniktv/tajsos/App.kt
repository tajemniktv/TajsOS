/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import androidx.navigation.*
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.CaptureSheet
import com.tajemniktv.tajsos.ui.screens.*
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(viewModel: MainViewModel) {
    val navController = rememberNavController()
    var showCaptureSheet by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val trackEntries by viewModel.trackEntries.collectAsState()
    val allProjects by viewModel.allProjects.collectAsState()
    val allAreas by viewModel.allAreas.collectAsState()
    val latestTrack = trackEntries.firstOrNull()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    TajsOSTheme {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = TactileTheme.Surface,
                    drawerShape = RoundedCornerShape(
                        topEnd = TactileTheme.RadiusMd,
                        bottomEnd = TactileTheme.RadiusMd
                    )
                ) {
                    Spacer(Modifier.height(TactileTheme.SpacingLg))
                    Text(
                        "TajsOS",
                        modifier = Modifier.padding(TactileTheme.SpacingMd),
                        style = MaterialTheme.typography.titleLarge,
                        color = TactileTheme.Primary
                    )
                    HorizontalDivider(color = TactileTheme.Muted)

                    val navItems = listOf(
                        Screen.Dashboard,
                        Screen.Search,
                        Screen.Tasks,
                        Screen.Notes,
                        Screen.Today,
                        Screen.Calendar,
                        Screen.Focus,
                        Screen.Projects,
                        Screen.Areas,
                        Screen.Track,
                        Screen.Insights,
                        Screen.Graph,
                        Screen.Archive,
                        Screen.Settings
                    )

                    navItems.forEach { screen ->
                        NavigationDrawerItem(
                            label = {
                                Text(
                                    screen.label,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                                scope.launch { drawerState.close() }
                            },
                            icon = { Icon(screen.icon, contentDescription = null) },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = TactileTheme.Primary.copy(alpha = 0.1f),
                                selectedIconColor = TactileTheme.Primary,
                                selectedTextColor = TactileTheme.Primary,
                                unselectedIconColor = TactileTheme.Muted,
                                unselectedTextColor = TactileTheme.Muted,
                                unselectedContainerColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    Icons.Default.Menu,
                                    contentDescription = "Menu",
                                    tint = TactileTheme.Primary
                                )
                            }
                        },
                        title = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "TajsOS // STATUS: OK",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (latestTrack != null) {
                                    Text(
                                        text = "E:${latestTrack.energyScore} M:${latestTrack.moodScore} F:${latestTrack.focusScore ?: "-"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                        )
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { showCaptureSheet = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(TactileTheme.RadiusMd)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Capture")
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController,
                    startDestination = Screen.Dashboard.route,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    val onEditNode: (Long) -> Unit = { id ->
                        navController.navigate(Screen.NoteDetail.route.replace("{noteId}", id.toString()))
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
                            onEditNode = onEditNode
                        )
                    }
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
                            }
                        )
                    }
                    composable(Screen.Projects.route) {
                        ProjectsScreen(viewModel, onNavigateTo = { route -> navController.navigate(route) })
                    }
                    composable(Screen.Areas.route) {
                        AreasScreen(viewModel, onNavigateTo = { route -> navController.navigate(route) })
                    }
                    composable(Screen.ProjectDetail.route) { backStackEntry ->
                        val projectId =
                            backStackEntry.savedStateHandle.get<Any>("projectId")?.toString()
                                ?.toLongOrNull() ?: -1L
                        ProjectDetailScreen(viewModel, projectId, onEditNode)
                    }
                    composable(Screen.AreaDetail.route) { backStackEntry ->
                        val areaId = backStackEntry.savedStateHandle.get<Any>("areaId")?.toString()
                            ?.toLongOrNull() ?: -1L
                        AreaDetailScreen(
                            viewModel, 
                            areaId, 
                            onNavigateToProject = { id ->
                                navController.navigate(Screen.ProjectDetail.route.replace("{projectId}", id.toString()))
                            },
                            onEditNode = onEditNode
                        )
                    }
                    composable(Screen.NoteDetail.route) { backStackEntry ->
                        val noteId = backStackEntry.savedStateHandle.get<Any>("noteId")?.toString()
                            ?.toLongOrNull() ?: -1L
                        NoteDetailScreen(
                            viewModel, 
                            noteId, 
                            onBack = { navController.popBackStack() },
                            onNavigateToNode = onEditNode,
                            onNavigateToSearch = { navController.navigate(Screen.Search.route) }
                        )
                    }
                    composable(Screen.Insights.route) {
                        InsightsScreen(viewModel, onNavigateToProject = { id ->
                            navController.navigate(Screen.ProjectDetail.route.replace("{projectId}", id.toString()))
                        })
                    }
                    composable(Screen.Graph.route) {
                        GraphScreen(viewModel, onNodeClick = onEditNode)
                    }
                    composable(Screen.Archive.route) { ArchiveScreen(viewModel, onEditNode) }
                }

                if (showCaptureSheet) {
                    CaptureSheet(
                        onDismiss = { showCaptureSheet = false },
                        onCapture = { text, type, projectId, areaId, isRec, recInt, remAt ->
                            when(type) {
                                "project" -> viewModel.addProject(text, "", areaId)
                                "area" -> viewModel.addArea(text)
                                else -> viewModel.addNode(text, "", type, projectId, areaId, isRec, recInt, remAt)
                            }
                            showCaptureSheet = false
                        },
                        projects = allProjects,
                        areas = allAreas
                    )
                }
            }
        }
    }
}
