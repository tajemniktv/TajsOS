/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui

import androidx.compose.runtime.compositionLocalOf

/**
 * Provides the [MainViewModel] to the Compose hierarchy.
 *
 * This allows screens and components to access global application state and orchestration
 * without explicit parameter forwarding through the UI tree.
 */
val LocalMainViewModel =
    compositionLocalOf<MainViewModel> {
        error("No MainViewModel provided")
    }
