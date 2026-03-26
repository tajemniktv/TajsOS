/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui

import com.tajemniktv.tajsos.data.NodeWithPin

data class TransitionProtocolTemplate(
    val key: String,
    val label: String,
    val checklist: List<String>,
)

data class TransitionProtocolItem(
    val node: NodeWithPin,
    val checklistDone: Int,
    val checklistTotal: Int,
    val triggerCount: Int,
    val lastTriggeredAt: Long? = null,
)

data class ProtocolHistoryItem(
    val historyId: Long,
    val protocolNodeId: Long,
    val protocolLabel: String,
    val executedAt: Long,
    val notes: String? = null,
)

data class TransitionProtocolsSnapshot(
    val protocols: List<TransitionProtocolItem> = emptyList(),
    val templates: List<TransitionProtocolTemplate> = emptyList(),
    val recommendedLabel: String? = null,
)

data class RelationshipStatusItem(
    val person: NodeWithPin,
    val relationshipType: String? = null,
    val daysSinceLastContact: Int? = null,
    val followUpDueInDays: Int? = null,
    val isImportant: Boolean = false,
    val linkedItemsCount: Int = 0,
    val pendingReplyCount: Int = 0,
    val sharedPlansCount: Int = 0,
    val askAboutNextTimeCount: Int = 0,
)

data class RelationshipSnapshot(
    val people: List<RelationshipStatusItem> = emptyList(),
    val importantRelationships: List<RelationshipStatusItem> = emptyList(),
    val followUpNeeded: List<RelationshipStatusItem> = emptyList(),
    val upcomingImportantDates: List<RelationshipStatusItem> = emptyList(),
    val replyQueue: List<NodeWithPin> = emptyList(),
    val sharedPlans: List<NodeWithPin> = emptyList(),
    val professors: List<RelationshipStatusItem> = emptyList(),
    val friendsAndFamily: List<RelationshipStatusItem> = emptyList(),
    val gentlePrompt: String? = null,
)

data class PlaybookTemplate(
    val key: String,
    val label: String,
    val checklist: List<String>,
    val recommendedModeKey: String? = null,
)

data class PlaybookItem(
    val node: NodeWithPin,
    val checklistDone: Int,
    val checklistTotal: Int,
    val triggerCount: Int,
    val linkedModeKey: String? = null,
    val linkedAreaId: Long? = null,
    val isCustom: Boolean = false,
)

data class PlaybookSnapshot(
    val playbooks: List<PlaybookItem> = emptyList(),
    val templates: List<PlaybookTemplate> = emptyList(),
    val suggestedPlaybookLabel: String? = null,
)
