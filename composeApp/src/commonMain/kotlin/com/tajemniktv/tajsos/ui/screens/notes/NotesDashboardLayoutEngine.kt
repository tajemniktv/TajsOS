/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.notes

fun buildNotesDashboardPlan(
    surface: com.tajemniktv.tajsos.ui.screens.notes.NotesDashboardSurface,
): com.tajemniktv.tajsos.ui.screens.notes.NotesDashboardPlan =
    when (surface)
    {
        com.tajemniktv.tajsos.ui.screens.notes.NotesDashboardSurface.MOBILE -> {
            com.tajemniktv.tajsos.ui.screens.notes.NotesDashboardPlan(
                primary =
                    listOf(
                        com.tajemniktv.tajsos.ui.screens.notes
                            .NotesDashboardBlock("notes_main"),
                    ),
            )
        }

        com.tajemniktv.tajsos.ui.screens.notes.NotesDashboardSurface.DESKTOP -> {
            com.tajemniktv.tajsos.ui.screens.notes.NotesDashboardPlan(
                primary =
                    listOf(
                        com.tajemniktv.tajsos.ui.screens.notes
                            .NotesDashboardBlock("notes_main"),
                    ),
            )
        }
    }
