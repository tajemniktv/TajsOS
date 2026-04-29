/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.relationships

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.cards.PersonRelationshipCard
import com.tajemniktv.tajsos.ui.components.common.EmptyState
import com.tajemniktv.tajsos.ui.components.screen.ScreenScaffold
import com.tajemniktv.tajsos.ui.components.screen.ScreenScrollBehavior
import com.tajemniktv.tajsos.ui.main.state.RelationshipSnapshot
import com.tajemniktv.tajsos.ui.screens.GroupedOpenLoopSection
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme

/**
 * Central relationships entry point that collects system state and coordinates layout.
 *
 * @param viewModel Source of relationships state.
 * @param onEditNode Node edit callback.
 * @param onNavigate Navigation callback.
 */
@Composable
fun RelationshipsRoute(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
    onNavigate: (String) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val surface =
            if (maxWidth > 900.dp) RelationshipsDashboardSurface.DESKTOP else RelationshipsDashboardSurface.MOBILE
        val plan = remember(surface) { buildRelationshipsDashboardPlan(surface) }
        val context =
            remember(viewModel, onEditNode) { RelationshipsDashboardContext(viewModel, onEditNode) }

        RelationshipsScreen(
            context = context,
            plan = plan,
            onNavigate = onNavigate,
        )
    }
}

/**
 * Stateless relationships screen content.
 *
 * @param context Relationships dashboard context.
 * @param plan Relationships dashboard plan.
 * @param onNavigate Navigation callback.
 */
@Composable
fun RelationshipsScreen(
    context: RelationshipsDashboardContext,
    plan: RelationshipsDashboardPlan,
    onNavigate: (String) -> Unit,
) {
    ScreenScaffold(
        screen = Screen.Relationships,
        onNavigate = onNavigate,
        scrollBehavior = ScreenScrollBehavior.None,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            plan.primary.forEach { block ->
                item(key = block.id) {
                    RelationshipsDashboardBlocks.resolve(block.id)?.invoke(context)
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
internal fun PeopleLayer(
    viewModel: MainViewModel,
    snapshot: RelationshipSnapshot,
    onEditNode: (Long) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TajsOSTheme.CardSurface,
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
    ) {
        Column(
            modifier = Modifier.padding(TajsOSTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "RELATIONSHIP HEALTH",
                style = MaterialTheme.typography.labelSmall,
                color = TajsOSTheme.Primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "People ${snapshot.people.size} • Follow-up ${snapshot.followUpNeeded.size} • Reply queue ${snapshot.replyQueue.size}",
                style = MaterialTheme.typography.bodySmall,
                color = TajsOSTheme.Muted,
            )
            snapshot.gentlePrompt?.let { prompt ->
                Text(
                    prompt,
                    style = MaterialTheme.typography.bodySmall,
                    color = TajsOSTheme.Accent,
                )
            }
        }
    }

    if (snapshot.people.isEmpty()) {
        EmptyState("No relationship anchors yet. Add someone you track to start building shared life context.")
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)) {
        if (snapshot.importantRelationships.isNotEmpty()) {
            GroupedOpenLoopSection(
                title = "IMPORTANT RELATIONSHIPS",
                items = snapshot.importantRelationships.map { it.person.node.title },
            )
        }

        if (snapshot.upcomingImportantDates.isNotEmpty()) {
            GroupedOpenLoopSection(
                title = "IMPORTANT DATES",
                items =
                    snapshot.upcomingImportantDates.map { item ->
                        "${item.person.node.title} • ${item.followUpDueInDays ?: 0}d"
                    },
            )
        }

        if (snapshot.replyQueue.isNotEmpty()) {
            GroupedOpenLoopSection(
                title = "REPLY QUEUE",
                items = snapshot.replyQueue.map { it.node.title },
            )
        }

        if (snapshot.sharedPlans.isNotEmpty()) {
            GroupedOpenLoopSection(
                title = "SHARED PLANS",
                items = snapshot.sharedPlans.map { it.node.title },
            )
        }

        if (snapshot.professors.isNotEmpty()) {
            GroupedOpenLoopSection(
                title = "PROFESSOR / ACADEMIC CONTACTS",
                items = snapshot.professors.map { it.person.node.title },
            )
        }

        if (snapshot.friendsAndFamily.isNotEmpty()) {
            GroupedOpenLoopSection(
                title = "FRIEND / FAMILY FOLLOW-UPS",
                items = snapshot.friendsAndFamily.map { it.person.node.title },
            )
        }

        snapshot.people.forEach { person ->
            PersonRelationshipCard(
                item = person,
                viewModel = viewModel,
                onEditNode = onEditNode,
                groupedSection = ::GroupedOpenLoopSection,
            )
        }
    }
}

