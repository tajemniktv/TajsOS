/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.relationships

fun buildRelationshipsDashboardPlan(surface: RelationshipsDashboardSurface): RelationshipsDashboardPlan =
    when (surface)
    {
        RelationshipsDashboardSurface.MOBILE -> {
            RelationshipsDashboardPlan(
                primary =
                    listOf(
                        RelationshipsDashboardBlock("relationships_main"),
                    ),
            )
        }

        RelationshipsDashboardSurface.DESKTOP -> {
            RelationshipsDashboardPlan(
                primary =
                    listOf(
                        RelationshipsDashboardBlock("relationships_main"),
                    ),
            )
        }
    }
