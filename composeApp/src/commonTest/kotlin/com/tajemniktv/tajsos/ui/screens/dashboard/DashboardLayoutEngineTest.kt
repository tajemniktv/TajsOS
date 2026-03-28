/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.dashboard

import com.tajemniktv.tajsos.data.AppPack
import com.tajemniktv.tajsos.data.ModeEntity
import com.tajemniktv.tajsos.data.ModePreferenceEntity
import com.tajemniktv.tajsos.data.PackRegistry
import com.tajemniktv.tajsos.ui.DashboardUIState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DashboardLayoutEngineTest {
    @Test
    fun allMode_usesFullDashboardBlockCatalogEvenWhenPreferencesAreNarrow() {
        val allPackKeys = AppPack.entries.map { it.key }.toSet()
        val enabledPacks =
            PackRegistry(
                ownedPackKeys = allPackKeys,
                enabledPackKeys = allPackKeys,
            )
        val allMode =
            ModeEntity(
                id = 999,
                key = "ALL",
                name = "All",
            )
        val dashboardState =
            DashboardUIState(
                currentMode = allMode,
                modePreferences =
                    ModePreferenceEntity(
                        modeId = allMode.id,
                        dashboardBlocksJson = "[\"today_top_3\"]",
                    ),
            )

        val mobilePlan =
            buildDashboardLayoutPlan(
                surface = DashboardSurface.MOBILE,
                dashboardState = dashboardState,
                enabledPacks = enabledPacks,
            )
        val mobileIds = mobilePlan.primary.map { it.id }
        val allContentIds = allDashboardContentBlockIds()

        assertEquals("mode_controls", mobileIds.firstOrNull())
        allContentIds.forEach { blockId ->
            assertTrue(
                mobileIds.contains(blockId),
                "ALL mode should include dashboard block '$blockId' on mobile",
            )
        }
    }
}
