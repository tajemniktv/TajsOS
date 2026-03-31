/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.templates

/**
 * Builds a templates dashboard layout plan based on the active surface.
 */
fun buildTemplatesDashboardPlan(surface: TemplatesDashboardSurface): TemplatesDashboardPlan =
    when (surface)
    {
        TemplatesDashboardSurface.MOBILE -> {
            TemplatesDashboardPlan(
                primary =
                    listOf(
                        TemplatesDashboardBlock("templates_list"),
                    ),
            )
        }

        TemplatesDashboardSurface.DESKTOP -> {
            TemplatesDashboardPlan(
                primary =
                    listOf(
                        TemplatesDashboardBlock("templates_list"),
                    ),
            )
        }
    }
