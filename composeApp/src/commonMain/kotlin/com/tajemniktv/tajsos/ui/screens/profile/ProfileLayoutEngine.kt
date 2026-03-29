/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.profile

private val mobileDefaults =
    listOf(
        "identity_header",
        "signature_panel",
        "identity_module",
        "contact_module",
        "about_module",
        "medications_module",
    )

private val desktopPrimaryDefaults =
    listOf(
        "identity_header",
        "signature_panel",
        "identity_module",
        "contact_module",
        "about_module",
        "medications_module",
    )

/**
 * Builds the profile dashboard plan.
 */
fun buildProfileDashboardPlan(surface: ProfileDashboardSurface): ProfileDashboardPlan =
    when (surface)
    {
        ProfileDashboardSurface.MOBILE -> {
            ProfileDashboardPlan(primary = mobileDefaults.map { ProfileDashboardBlock(it) })
        }

        ProfileDashboardSurface.DESKTOP -> {
            ProfileDashboardPlan(primary = desktopPrimaryDefaults.map { ProfileDashboardBlock(it) })
        }
    }
