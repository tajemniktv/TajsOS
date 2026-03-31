/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.notes

fun buildNotesDashboardPlan(surface: NotesDashboardSurface): NotesDashboardPlan =
    when (surface)
    {
        NotesDashboardSurface.MOBILE -> {
            NotesDashboardPlan(
                primary =
                    listOf(
                        NotesDashboardBlock("notes_main"),
                    ),
            )
        }

        NotesDashboardSurface.DESKTOP -> {
            NotesDashboardPlan(
                primary =
                    listOf(
                        NotesDashboardBlock("notes_main"),
                    ),
            )
        }
    }
