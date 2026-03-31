/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.rules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme

@Composable
fun RulesScreen(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
) {
    val personalRulesSnapshot by viewModel.personalRulesSnapshot.collectAsState()
    val playbookSnapshot by viewModel.playbookSnapshot.collectAsState()

    var ruleTitle by remember { mutableStateOf("") }
    var ruleContent by remember { mutableStateOf("") }
    var selectedRuleTag by remember { mutableStateOf("rule_foundational") }

    val context =
        RulesDashboardContext(
            viewModel = viewModel,
            snapshot = personalRulesSnapshot,
            playbookSnapshot = playbookSnapshot,
            ruleTitle = ruleTitle,
            ruleContent = ruleContent,
            selectedRuleTag = selectedRuleTag,
            onRuleTitleChange = { ruleTitle = it },
            onRuleContentChange = { ruleContent = it },
            onRuleTagChange = { selectedRuleTag = it },
            onEditNode = onEditNode,
            onSaveRule = {
                viewModel.addPersonalRule(
                    title = ruleTitle,
                    content = ruleContent,
                    categoryTag = selectedRuleTag,
                )
                ruleTitle = ""
                ruleContent = ""
            },
        )

    val surface = RulesDashboardSurface.MOBILE // Default for now
    val plan = remember(surface) { buildRulesDashboardPlan(surface) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(TajsOSTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd)
    ) {
        plan.primary.forEach { block ->
            item(key = block.id) {
                RulesDashboardBlockRegistry.resolve(block.id)?.invoke(context)
            }
        }
    }
}
