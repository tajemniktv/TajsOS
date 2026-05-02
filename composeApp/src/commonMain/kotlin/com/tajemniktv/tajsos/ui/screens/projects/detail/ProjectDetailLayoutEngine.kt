/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.projects.detail

/**
 * Builds a project detail layout plan based on the active surface.
 */
fun buildProjectDetailPlan(surface: ProjectDetailSurface): ProjectDetailPlan =
    when (surface) {
        ProjectDetailSurface.MOBILE -> {
            ProjectDetailPlan(
                primary =
                    listOf(
                        ProjectDetailBlock("project_header"),
                        ProjectDetailBlock("project_hero"),
                        ProjectDetailBlock("project_tabs"),
                        ProjectDetailBlock("project_content"),
                        ProjectDetailBlock("project_sidebar"),
                    ),
            )
        }

        ProjectDetailSurface.DESKTOP -> {
            ProjectDetailPlan(
                primary =
                    listOf(
                        ProjectDetailBlock("project_header"),
                        ProjectDetailBlock("project_hero"),
                        ProjectDetailBlock("project_tabs"),
                        ProjectDetailBlock("project_content"),
                    ),
                secondary =
                    listOf(
                        ProjectDetailBlock("project_sidebar"),
                    ),
            )
        }
    }
