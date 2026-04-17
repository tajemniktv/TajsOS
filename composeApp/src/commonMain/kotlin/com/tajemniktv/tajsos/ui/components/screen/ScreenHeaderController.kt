/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.screen

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.tajemniktv.tajsos.ui.Screen
import org.jetbrains.compose.resources.stringResource

/**
 * Explicit bridge between screen routes and the persistent app shell header.
 */
@Stable
class ScreenHeaderController {
    private var currentOwner: Any? by mutableStateOf(null)

    var model by mutableStateOf(ScreenHeaderModel())
        private set

    fun update(
        owner: Any,
        model: ScreenHeaderModel,
    ) {
        currentOwner = owner
        this.model = model
    }

    fun clear(owner: Any) {
        if (currentOwner == owner) {
            currentOwner = null
            model = ScreenHeaderModel()
        }
    }
}

val LocalScreenHeaderController = compositionLocalOf<ScreenHeaderController?> { null }

data class ScreenHeaderBreadcrumb(
    val label: String,
    val onClick: (() -> Unit)? = null,
)

/**
 * Compact screen-level metadata rendered inside the shell header.
 */
data class ScreenHeaderModel(
    val breadcrumbs: List<ScreenHeaderBreadcrumb> = emptyList(),
    val title: String? = null,
    val subtitle: String? = null,
    val actions: (@Composable RowScope.() -> Unit)? = null,
    val toolbar: (@Composable () -> Unit)? = null,
)

@Composable
fun rememberScreenHeaderController(): ScreenHeaderController = remember { ScreenHeaderController() }

@Composable
fun screenBreadcrumbs(
    screen: Screen,
    onScreenClick: ((Screen) -> (() -> Unit)?)? = null,
): List<ScreenHeaderBreadcrumb> {
    val trail = remember(screen) { screen.breadcrumbTrail() }
    return trail.mapIndexed { index, breadcrumbScreen ->
        val click =
            if (index < trail.lastIndex) {
                onScreenClick?.invoke(breadcrumbScreen)
            } else {
                null
            }
        ScreenHeaderBreadcrumb(
            label = stringResource(breadcrumbScreen.label),
            onClick = click,
        )
    }
}

/**
 * Publishes the current screen header model to the persistent shell.
 */
@Composable
fun BindScreenHeader(
    controller: ScreenHeaderController,
    model: ScreenHeaderModel,
) {
    val owner = remember { Any() }

    SideEffect {
        controller.update(owner = owner, model = model)
    }

    DisposableEffect(controller, owner) {
        onDispose {
            controller.clear(owner)
        }
    }
}
