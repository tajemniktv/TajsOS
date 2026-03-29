/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.track

/**
 * Builds a track dashboard layout plan based on the active surface.
 */
fun buildTrackDashboardPlan(surface: TrackDashboardSurface): TrackDashboardPlan =
    when (surface)
    {
        TrackDashboardSurface.MOBILE -> {
            TrackDashboardPlan(
                primary =
                    listOf(
                        TrackDashboardBlock("track_header"),
                        TrackDashboardBlock("track_energy"),
                        TrackDashboardBlock("track_affective"),
                        TrackDashboardBlock("track_cognitive"),
                        TrackDashboardBlock("track_tension"),
                        TrackDashboardBlock("track_recovery"),
                        TrackDashboardBlock("track_medication"),
                        TrackDashboardBlock("track_bio"),
                        TrackDashboardBlock("track_note"),
                        TrackDashboardBlock("track_save_button"),
                        TrackDashboardBlock("track_history_header"),
                        TrackDashboardBlock("track_history_list"),
                    ),
            )
        }

        TrackDashboardSurface.DESKTOP -> {
            TrackDashboardPlan(
                primary =
                    listOf(
                        TrackDashboardBlock("track_header"),
                        TrackDashboardBlock("track_energy"),
                        TrackDashboardBlock("track_affective"),
                        TrackDashboardBlock("track_cognitive"),
                        TrackDashboardBlock("track_tension"),
                        TrackDashboardBlock("track_recovery"),
                        TrackDashboardBlock("track_medication"),
                        TrackDashboardBlock("track_bio"),
                        TrackDashboardBlock("track_note"),
                        TrackDashboardBlock("track_save_button"),
                        TrackDashboardBlock("track_history_header"),
                        TrackDashboardBlock("track_history_list"),
                    ),
            )
        }
    }
