/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.ui.screens.archive

@Suppress("UNUSED_PARAMETER")
fun buildArchiveDashboardPlan(surface: ArchiveDashboardSurface): ArchiveDashboardPlan =
    ArchiveDashboardPlan(
        primary =
            listOf(
                ArchiveDashboardBlock("archive_main"),
            ),
    )
