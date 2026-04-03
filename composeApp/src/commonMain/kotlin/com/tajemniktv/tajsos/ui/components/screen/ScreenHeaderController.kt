/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.screen

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Explicit bridge between screen routes and the persistent app shell header.
 */
@Stable
class ScreenHeaderController {
    var model by mutableStateOf(ScreenHeaderModel())
        private set

    fun update(model: ScreenHeaderModel) {
        this.model = model
    }

    fun clear() {
        model = ScreenHeaderModel()
    }
}

/**
 * Compact screen-level metadata rendered inside the shell header.
 */
data class ScreenHeaderModel(
    val title: String? = null,
    val subtitle: String? = null,
    val actions: (@Composable RowScope.() -> Unit)? = null,
    val toolbar: (@Composable () -> Unit)? = null,
)

@Composable
fun rememberScreenHeaderController(): ScreenHeaderController = remember { ScreenHeaderController() }

/**
 * Publishes the current screen header model to the persistent shell.
 */
@Composable
fun BindScreenHeader(
    controller: ScreenHeaderController,
    model: ScreenHeaderModel,
) {
    SideEffect {
        controller.update(model)
    }

    DisposableEffect(controller) {
        onDispose {
            controller.clear()
        }
    }
}
