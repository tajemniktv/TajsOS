/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.sidebar.sub

import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.sidebar.SidebarSection

/**
 * Resolves dedicated contextual sidebar section sets for main/root screens.
 */
internal fun subSidebarSectionsFor(screen: Screen): List<SidebarSection>? =
    when (screen) {
        Screen.Dashboard -> dashboardSubSidebarSections()
        Screen.Inbox -> inboxSubSidebarSections()
        Screen.Search -> searchSubSidebarSections()
        Screen.Today -> todaySubSidebarSections()
        Screen.Tasks -> tasksSubSidebarSections()
        Screen.Focus -> focusSubSidebarSections()
        Screen.Decisions -> decisionsSubSidebarSections()
        Screen.OpenLoops -> openLoopsSubSidebarSections()
        Screen.Calendar -> calendarSubSidebarSections()
        Screen.Projects -> projectsSubSidebarSections()
        Screen.Areas -> areasSubSidebarSections()
        Screen.Protocols -> protocolsSubSidebarSections()
        Screen.TimeArchitecture -> timeArchitectureSubSidebarSections()
        Screen.Places -> placesSubSidebarSections()
        Screen.Notes -> notesSubSidebarSections()
        Screen.Vaults -> vaultsSubSidebarSections()
        Screen.Rules -> rulesSubSidebarSections()
        Screen.Track -> trackSubSidebarSections()
        Screen.Insights -> insightsSubSidebarSections()
        Screen.Capacity -> capacitySubSidebarSections()
        Screen.Identity -> identitySubSidebarSections()
        Screen.Graph -> graphSubSidebarSections()
        Screen.Review -> reviewSubSidebarSections()
        Screen.Finances -> financesSubSidebarSections()
        Screen.Health -> healthSubSidebarSections()
        Screen.Relationships -> relationshipsSubSidebarSections()
        Screen.Education -> educationSubSidebarSections()
        Screen.Archive -> archiveSubSidebarSections()
        Screen.Settings -> settingsSubSidebarSections()
        Screen.Profile -> profileSubSidebarSections()
        else -> null
    }