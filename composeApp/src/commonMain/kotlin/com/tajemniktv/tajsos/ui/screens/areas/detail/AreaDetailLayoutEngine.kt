/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.areas.detail

/**
 * Builds an area detail layout plan based on the active surface.
 */
fun buildAreaDetailPlan(surface: AreaDetailSurface): AreaDetailPlan =
    when (surface)
    {
        AreaDetailSurface.MOBILE -> {
            AreaDetailPlan(
                primary =
                    listOf(
                        AreaDetailBlock("area_header"),
                        AreaDetailBlock("area_hero"),
                        AreaDetailBlock("area_tabs"),
                        AreaDetailBlock("area_content"),
                        AreaDetailBlock("area_sidebar"),
                    ),
            )
        }

        AreaDetailSurface.DESKTOP -> {
            AreaDetailPlan(
                primary =
                    listOf(
                        AreaDetailBlock("area_header"),
                        AreaDetailBlock("area_hero"),
                        AreaDetailBlock("area_tabs"),
                        AreaDetailBlock("area_content"),
                    ),
                secondary =
                    listOf(
                        AreaDetailBlock("area_sidebar"),
                    ),
            )
        }
    }
