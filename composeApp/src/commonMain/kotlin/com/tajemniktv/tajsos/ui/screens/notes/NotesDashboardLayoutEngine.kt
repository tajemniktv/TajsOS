/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.notes

fun buildNotesDashboardPlan(
    surface: com.tajemniktv.tajsos.ui.screens.notes.NotesDashboardSurface,
): com.tajemniktv.tajsos.ui.screens.notes.NotesDashboardPlan =
    when (surface)
    {
        _root_ide_package_.com.tajemniktv.tajsos.ui.screens.notes.NotesDashboardSurface.MOBILE -> {
            _root_ide_package_.com.tajemniktv.tajsos.ui.screens.notes.NotesDashboardPlan(
                primary =
                    listOf(
                        _root_ide_package_.com.tajemniktv.tajsos.ui.screens.notes
                            .NotesDashboardBlock("notes_main"),
                    ),
            )
        }

        _root_ide_package_.com.tajemniktv.tajsos.ui.screens.notes.NotesDashboardSurface.DESKTOP -> {
            _root_ide_package_.com.tajemniktv.tajsos.ui.screens.notes.NotesDashboardPlan(
                primary =
                    listOf(
                        _root_ide_package_.com.tajemniktv.tajsos.ui.screens.notes
                            .NotesDashboardBlock("notes_main"),
                    ),
            )
        }
    }
