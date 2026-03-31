/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.rules

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.main.state.PersonalRulesSnapshot
import com.tajemniktv.tajsos.ui.main.state.PlaybookSnapshot

/**
 * Defines the supported surfaces for rules dashboard layout planning.
 */
enum class RulesDashboardSurface {
    MOBILE,
    DESKTOP,
}

/**
 * Identifies a logical rules dashboard block.
 */
data class RulesDashboardBlock(
    val id: String,
)

/**
 * Structured layout plan for the rules dashboard screen.
 */
data class RulesDashboardPlan(
    val primary: List<RulesDashboardBlock> = emptyList(),
)

/**
 * Shared state and actions for rules dashboard block renderers.
 */
data class RulesDashboardContext(
    val viewModel: MainViewModel,
    val snapshot: PersonalRulesSnapshot,
    val playbookSnapshot: PlaybookSnapshot,
    val ruleTitle: String,
    val ruleContent: String,
    val selectedRuleTag: String,
    val onRuleTitleChange: (String) -> Unit,
    val onRuleContentChange: (String) -> Unit,
    val onRuleTagChange: (String) -> Unit,
    val onEditNode: (Long) -> Unit,
    val onSaveRule: () -> Unit,
)

/**
 * Functional interface for rendering a rules dashboard block.
 */
typealias RulesDashboardBlockRenderer = @Composable (RulesDashboardContext) -> Unit
