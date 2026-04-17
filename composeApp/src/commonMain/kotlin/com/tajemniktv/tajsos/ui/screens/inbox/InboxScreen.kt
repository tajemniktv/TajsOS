/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.inbox

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.screen.ScreenScaffold
import com.tajemniktv.tajsos.ui.components.screen.ScreenScrollBehavior

/**
 * Central inbox entry point that collects system state and coordinates layout.
 *
 * @param viewModel Source of inbox state.
 * @param onEditNode Node edit callback.
 * @param onNavigate Navigation callback.
 */
@Composable
fun InboxRoute(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
    onNavigate: (String) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val surface =
            if (maxWidth > 900.dp) InboxDashboardSurface.DESKTOP else InboxDashboardSurface.MOBILE
        val plan = remember(surface) { buildInboxDashboardPlan(surface) }
        val context =
            remember(viewModel, onEditNode) { InboxDashboardContext(viewModel, onEditNode) }

        InboxScreen(
            context = context,
            plan = plan,
            onNavigate = onNavigate,
        )
    }
}

/**
 * Stateless inbox screen content.
 *
 * @param context Inbox dashboard context.
 * @param plan Inbox dashboard plan.
 * @param onNavigate Navigation callback.
 */
@Composable
fun InboxScreen(
    context: InboxDashboardContext,
    plan: InboxDashboardPlan,
    onNavigate: (String) -> Unit,
) {
    ScreenScaffold(
        screen = Screen.Inbox,
        onNavigate = onNavigate,
        scrollBehavior = ScreenScrollBehavior.BodyScroll,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement =
                androidx.compose.foundation.layout.Arrangement
                    .spacedBy(16.dp),
        ) {
            plan.primary.forEach { block ->
                InboxDashboardBlockRegistry.resolve(block.id)?.invoke(context)
            }
        }
    }
}
