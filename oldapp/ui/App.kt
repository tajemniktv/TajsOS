/*
 * Copyright (c) TajemnikTV 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui

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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tajemniktv.tajsos.ui.components.CaptureSheet
import com.tajemniktv.tajsos.ui.screens.*
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TajsOSApp(viewModel: com.tajemniktv.tajsos.ui.MainViewModel) {
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

    _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TajsOSTheme {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Surface,
                    drawerShape = RoundedCornerShape(
                        topEnd = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.RadiusMd,
                        bottomEnd = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.RadiusMd
                    )
                ) {
                    Spacer(Modifier.height(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingLg))
                    Text(
                        "TajsOS",
                        modifier = Modifier.padding(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingMd),
                        style = MaterialTheme.typography.titleLarge,
                        color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary
                    )
                    HorizontalDivider(color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Muted)

                    val navItems = listOf(
                        _root_ide_package_.com.tajemniktv.tajsos.ui.Screen.Dashboard,
                        _root_ide_package_.com.tajemniktv.tajsos.ui.Screen.Tasks,
                        _root_ide_package_.com.tajemniktv.tajsos.ui.Screen.Notes,
                        _root_ide_package_.com.tajemniktv.tajsos.ui.Screen.Today,
                        _root_ide_package_.com.tajemniktv.tajsos.ui.Screen.Focus,
                        _root_ide_package_.com.tajemniktv.tajsos.ui.Screen.Projects,
                        _root_ide_package_.com.tajemniktv.tajsos.ui.Screen.Areas,
                        _root_ide_package_.com.tajemniktv.tajsos.ui.Screen.Track,
                        _root_ide_package_.com.tajemniktv.tajsos.ui.Screen.Insights,
                        _root_ide_package_.com.tajemniktv.tajsos.ui.Screen.Settings
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
                                selectedContainerColor = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary.copy(
                                    alpha = 0.1f
                                ),
                                selectedIconColor = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary,
                                selectedTextColor = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary,
                                unselectedIconColor = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Muted,
                                unselectedTextColor = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Muted,
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
                                    tint = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary
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
                                        text = "E:${latestTrack.energy} M:${latestTrack.mood} F:${latestTrack.focus ?: "-"}",
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
                        shape = RoundedCornerShape(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.RadiusMd)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Capture")
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController,
                    startDestination = _root_ide_package_.com.tajemniktv.tajsos.ui.Screen.Dashboard.route,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable(_root_ide_package_.com.tajemniktv.tajsos.ui.Screen.Dashboard.route) {
                        _root_ide_package_.com.tajemniktv.tajsos.ui.screens.DashboardScreen(viewModel, onNavigateTo = { screen ->
                                                    navController.navigate(screen.route) {
                                                        popUpTo(navController.graph.findStartDestination().id) {
                                                            saveState = true
                                                        }
                                                        launchSingleTop = true
                                                        restoreState = true
                                                    }
                                                })
                    }
                    composable(_root_ide_package_.com.tajemniktv.tajsos.ui.Screen.Today.route) { _root_ide_package_.com.tajemniktv.tajsos.ui.screens.TodayScreen(viewModel) }
                    composable(_root_ide_package_.com.tajemniktv.tajsos.ui.Screen.Focus.route) { _root_ide_package_.com.tajemniktv.tajsos.ui.screens.FocusScreen(viewModel) }
                    composable(_root_ide_package_.com.tajemniktv.tajsos.ui.Screen.Track.route) { _root_ide_package_.com.tajemniktv.tajsos.ui.screens.TrackScreen(viewModel) }
                    composable(_root_ide_package_.com.tajemniktv.tajsos.ui.Screen.Tasks.route) { _root_ide_package_.com.tajemniktv.tajsos.ui.screens.TasksScreen(viewModel) }
                    composable(_root_ide_package_.com.tajemniktv.tajsos.ui.Screen.Notes.route) {
                        _root_ide_package_.com.tajemniktv.tajsos.ui.screens.NotesScreen(viewModel, onNoteClick = { noteId ->
                                                    navController.navigate(_root_ide_package_.com.tajemniktv.tajsos.ui.Screen.NoteDetail.route.replace("{noteId}", noteId.toString()))
                                                })
                    }
                    composable(_root_ide_package_.com.tajemniktv.tajsos.ui.Screen.Settings.route) { _root_ide_package_.com.tajemniktv.tajsos.ui.screens.SettingsScreen(viewModel) }
                    composable(_root_ide_package_.com.tajemniktv.tajsos.ui.Screen.Projects.route) {
                        _root_ide_package_.com.tajemniktv.tajsos.ui.screens.ProjectsScreen(viewModel, onNavigateTo = { route -> navController.navigate(route) })
                    }
                    composable(_root_ide_package_.com.tajemniktv.tajsos.ui.Screen.Areas.route) {
                        _root_ide_package_.com.tajemniktv.tajsos.ui.screens.AreasScreen(viewModel, onNavigateTo = { route -> navController.navigate(route) })
                    }
                    composable(_root_ide_package_.com.tajemniktv.tajsos.ui.Screen.ProjectDetail.route) { backStackEntry ->
                        val projectId =
                            backStackEntry.arguments?.getString("projectId")?.toLongOrNull() ?: -1L
                        _root_ide_package_.com.tajemniktv.tajsos.ui.screens.ProjectDetailScreen(viewModel, projectId)
                    }
                    composable(_root_ide_package_.com.tajemniktv.tajsos.ui.Screen.AreaDetail.route) { backStackEntry ->
                        val areaId =
                            backStackEntry.arguments?.getString("areaId")?.toLongOrNull() ?: -1L
                        _root_ide_package_.com.tajemniktv.tajsos.ui.screens.AreaDetailScreen(viewModel, areaId, onNavigateToProject = { id ->
                                                    navController.navigate(_root_ide_package_.com.tajemniktv.tajsos.ui.Screen.ProjectDetail.route.replace("{projectId}", id.toString()))
                                                })
                    }
                    composable(_root_ide_package_.com.tajemniktv.tajsos.ui.Screen.NoteDetail.route) { backStackEntry ->
                        val noteId =
                            backStackEntry.arguments?.getString("noteId")?.toLongOrNull() ?: -1L
                        _root_ide_package_.com.tajemniktv.tajsos.ui.screens.NoteDetailScreen(viewModel, noteId, onBack = { navController.popBackStack() })
                    }
                    composable(_root_ide_package_.com.tajemniktv.tajsos.ui.Screen.Insights.route) {
                        _root_ide_package_.com.tajemniktv.tajsos.ui.screens.InsightsScreen(viewModel, onNavigateToProject = { id ->
                                                    navController.navigate(_root_ide_package_.com.tajemniktv.tajsos.ui.Screen.ProjectDetail.route.replace("{projectId}", id.toString()))
                                                })
                    }
                }

                if (showCaptureSheet) {
                    _root_ide_package_.com.tajemniktv.tajsos.ui.components.CaptureSheet(
                                            onDismiss = { showCaptureSheet = false },
                                            onCapture = { text, type, projectId, areaId, isRec, recInt, remAt ->
                                                when(type) {
                                                    "project" -> viewModel.addProject(text, "", areaId)
                                                    "area" -> viewModel.addArea(text)
                                                    else -> viewModel.addItem(text, type, projectId, areaId, isRec, recInt, remAt)
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
