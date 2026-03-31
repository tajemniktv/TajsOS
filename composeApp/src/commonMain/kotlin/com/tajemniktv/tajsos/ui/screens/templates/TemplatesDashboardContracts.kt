/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.templates

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.data.TemplateEntity
import com.tajemniktv.tajsos.ui.MainViewModel

/**
 * Defines the supported surfaces for templates dashboard layout planning.
 */
enum class TemplatesDashboardSurface {
    MOBILE,
    DESKTOP,
}

/**
 * Identifies a logical templates dashboard block.
 */
data class TemplatesDashboardBlock(
    val id: String,
)

/**
 * Structured layout plan for the templates dashboard screen.
 */
data class TemplatesDashboardPlan(
    val primary: List<TemplatesDashboardBlock> = emptyList(),
)

/**
 * Shared state and actions for templates dashboard block renderers.
 */
data class TemplatesDashboardContext(
    val viewModel: MainViewModel,
    val templates: List<TemplateEntity>,
    val onAddTemplate: (String, String) -> Unit,
    val onDeleteTemplate: (TemplateEntity) -> Unit,
    val onShowAddDialog: () -> Unit,
)

/**
 * Functional interface for rendering a templates dashboard block.
 */
typealias TemplatesDashboardBlockRenderer = @Composable (TemplatesDashboardContext) -> Unit
