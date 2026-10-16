/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.ui.screens.templates

/**
 * Builds a templates dashboard layout plan based on the active surface.
 */
@Suppress("UNUSED_PARAMETER")
fun buildTemplatesDashboardPlan(surface: TemplatesDashboardSurface): TemplatesDashboardPlan =
    TemplatesDashboardPlan(
        primary =
            listOf(
                TemplatesDashboardBlock("templates_list"),
            ),
    )
