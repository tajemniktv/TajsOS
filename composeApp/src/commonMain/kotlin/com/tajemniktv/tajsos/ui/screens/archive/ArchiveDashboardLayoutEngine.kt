/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.archive

fun buildArchiveDashboardPlan(surface: ArchiveDashboardSurface): ArchiveDashboardPlan =
    when (surface)
    {
        ArchiveDashboardSurface.MOBILE -> {
            ArchiveDashboardPlan(
                primary =
                    listOf(
                        ArchiveDashboardBlock("archive_main"),
                    ),
            )
        }

        ArchiveDashboardSurface.DESKTOP -> {
            ArchiveDashboardPlan(
                primary =
                    listOf(
                        ArchiveDashboardBlock("archive_main"),
                    ),
            )
        }
    }
