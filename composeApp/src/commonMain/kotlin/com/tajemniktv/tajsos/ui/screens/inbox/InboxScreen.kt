/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.inbox

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.cards.NodeCard
import com.tajemniktv.tajsos.ui.components.common.EmptyState
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*

/**
 * Displays the inbox UI with a quick-capture input, type filters, recent entries, and per-item actions.
 *
 * Delegates node operations (add, update status, pin/unpin, archive, mark processed) to the provided ViewModel.
 *
 * @param onEditNode Callback invoked with a node ID when the user requests to edit that node.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val surface =
            if (maxWidth >
                900.dp
            ) {
                InboxDashboardSurface.DESKTOP
            } else {
                InboxDashboardSurface.MOBILE
            }
        val plan =
            remember(surface) {
                buildInboxDashboardPlan(
                    surface,
                )
            }
        val context =
            remember(viewModel, onEditNode) {
                InboxDashboardContext(
                    viewModel,
                    onEditNode,
                )
            }
        Column(modifier = Modifier.fillMaxSize()) {
            plan.primary.forEach { block ->
                InboxDashboardBlockRegistry
                    .resolve(block.id)
                    ?.invoke(context)
            }
        }
    }
}
