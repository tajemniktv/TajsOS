/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.sidebar.sub

import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.sidebar.SidebarSection
import com.tajemniktv.tajsos.ui.components.sidebar.SidebarItem

internal fun settingsSubSidebarSections(): List<SidebarSection> =
    listOf(
        SidebarSection(
            title = "User Profile",
            items =
                listOf(
                    SidebarItem("About", Screen.SettingsAbout),
                    SidebarItem("Health", Screen.SettingsHealth),
                ),
        ),
        SidebarSection(
            title = "System settings",
            items =
                listOf(
                    SidebarItem("Preferences", Screen.Settings),
                    SidebarItem("Calendar", Screen.CalendarSettings),
                    SidebarItem("Feature Packs", Screen.SettingsFeaturePacks),
                    SidebarItem("Data", Screen.SettingsData),
                ),
        ),
        SidebarSection(
            title = "Debug",
            items = listOf(SidebarItem("Debug", Screen.SettingsDebug)),
        ),
    )
