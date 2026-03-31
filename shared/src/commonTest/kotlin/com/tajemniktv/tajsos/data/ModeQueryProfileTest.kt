/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ModeQueryProfileTest {
    @Test
    fun buildModeQueryProfile_mapsPreferencesAndFilters() {
        val preference =
            ModePreferenceEntity(
                modeId = 42,
                showInbox = false,
                sortStrategy = "DUE_FIRST",
                defaultQuickActionsJson = "[\"quick_capture\",\"start_focus\"]",
                dashboardBlocksJson = "[\"today_top_3\",\"resume\"]",
                suggestionProfileJson = "[\"switch_to_recovery\"]",
            )

        val profile =
            buildModeQueryProfile(
                preference = preference,
                areaFilters = listOf(ModeAreaFilterEntity(modeId = 42, areaId = 8)),
                typeFilters =
                    listOf(
                        ModeTypeFilterEntity(modeId = 42, nodeType = "task"),
                        ModeTypeFilterEntity(modeId = 42, nodeType = "note", include = false),
                    ),
            )

        assertEquals(42, profile.modeId)
        assertEquals(false, profile.visibility.showInbox)
        assertEquals("DUE_FIRST", profile.filtering.sortStrategy)
        assertTrue(profile.filtering.includeAreaIds.contains(8))
        assertTrue(profile.filtering.includeTypes.contains("task"))
        assertEquals(listOf("quick_capture", "start_focus"), profile.actions.quickActions)
        assertEquals(listOf("today_top_3", "resume"), profile.dashboardBlocks)
        assertEquals(listOf("switch_to_recovery"), profile.suggestions.suggestionKeys)
    }
}
