/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.layout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import com.tajemniktv.tajsos.data.ModeEntity
import com.tajemniktv.tajsos.data.PackRegistry
import com.tajemniktv.tajsos.data.UserProfile
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun AppLayout(
    isDesktop: Boolean,
    currentDestination: NavDestination?,
    onNavigate: (Screen) -> Unit,
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
    var showMainSidebar by rememberSaveable { mutableStateOf(false) }
    val currentScreen = Screen.fromRoute(currentDestination?.route)
    val useContextualSidebar =
        currentScreen != null &&
            currentScreen != Screen.Dashboard &&
            !showMainSidebar

    if (isDesktop) {
        Row(modifier = Modifier.fillMaxSize().background(TactileTheme.Background)) {
            Surface(
                modifier = Modifier.width(TactileTheme.SidebarWidth).fillMaxHeight(),
                color = TactileTheme.SidebarBackground,
                border = BorderStroke(1.dp, TactileTheme.Border),
            ) {
                SidebarContent(
                    currentDestination = currentDestination,
                    onNavigate = onNavigate,
                    onNewEntry = onNewEntry,
                    currentMode = currentMode,
                    allModes = allModes,
                    packRegistry = packRegistry,
                    userProfile = userProfile,
                    onModeSelect = onModeSelect,
                    useContextualSidebar = useContextualSidebar,
                    onBackToMainSidebar = { showMainSidebar = true },
                    onNavigateFromSidebar = { showMainSidebar = false },
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                content()
            }
        }
    } else {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = TactileTheme.SidebarBackground,
                    drawerShape = RoundedCornerShape(0.dp),
                    modifier = Modifier.width(TactileTheme.SidebarWidth),
                ) {
                    SidebarContent(
                        currentDestination = currentDestination,
                        onNavigate = { screen ->
                            onNavigate(screen)
                            showMainSidebar = false
                            scope.launch { drawerState.close() }
                        },
                        onNewEntry = onNewEntry,
                        currentMode = currentMode,
                        allModes = allModes,
                        packRegistry = packRegistry,
                        userProfile = userProfile,
                        onModeSelect = onModeSelect,
                        useContextualSidebar = useContextualSidebar,
                        onBackToMainSidebar = { showMainSidebar = true },
                        onNavigateFromSidebar = { showMainSidebar = false },
                    )
                }
            },
        ) {
            content()
        }
    }
}
