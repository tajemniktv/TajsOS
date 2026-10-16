/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.ui.screens.relationships

@Suppress("UNUSED_PARAMETER")
fun buildRelationshipsDashboardPlan(surface: RelationshipsDashboardSurface): RelationshipsDashboardPlan =
    RelationshipsDashboardPlan(
        primary =
            listOf(
                RelationshipsDashboardBlock("relationships_main"),
            ),
    )
