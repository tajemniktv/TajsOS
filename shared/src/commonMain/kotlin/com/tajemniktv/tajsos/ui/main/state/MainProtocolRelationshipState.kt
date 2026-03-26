/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui

import com.tajemniktv.tajsos.data.NodeWithPin

/**
 * A data class representing a structural template used to bootstrap transition protocols.
 *
 * @param key The unique string identifier for the template (e.g., "morning_startup").
 * @param label The human-readable title or label of the template.
 * @param checklist A list of string tasks forming the default steps of the protocol.
 */
data class TransitionProtocolTemplate(
    val key: String,
    val label: String,
    val checklist: List<String>,
)

/**
 * A data class wrapping a specific instantiated transition protocol node, tracking its state and usage.
 *
 * @param node The [NodeWithPin] representing the instantiated protocol.
 * @param checklistDone The number of currently checked off items within the protocol.
 * @param checklistTotal The total number of checklist items defined within the protocol.
 * @param triggerCount The number of times this protocol has been executed historically.
 * @param lastTriggeredAt The epoch timestamp indicating when this protocol was last executed.
 */
data class TransitionProtocolItem(
    val node: NodeWithPin,
    val checklistDone: Int,
    val checklistTotal: Int,
    val triggerCount: Int,
    val lastTriggeredAt: Long? = null,
)

/**
 * A data class representing a single historical execution record of a transition protocol.
 *
 * @param historyId The unique ID of the historical record.
 * @param protocolNodeId The ID of the [NodeWithPin] that was executed.
 * @param protocolLabel The name or title of the protocol at the time of execution.
 * @param executedAt The epoch timestamp when the execution was logged.
 * @param notes Optional notes or reflections recorded during the execution.
 */
data class ProtocolHistoryItem(
    val historyId: Long,
    val protocolNodeId: Long,
    val protocolLabel: String,
    val executedAt: Long,
    val notes: String? = null,
)

/**
 * A snapshot encompassing all currently active and available transition protocols, along with templates and contextual recommendations.
 *
 * @param protocols A list of instantiated [TransitionProtocolItem] objects active in the system.
 * @param templates A list of available [TransitionProtocolTemplate] blueprints for creation.
 * @param recommendedLabel An optional system-generated suggestion indicating which protocol should be run right now based on time or context.
 */
data class TransitionProtocolsSnapshot(
    val protocols: List<TransitionProtocolItem> = emptyList(),
    val templates: List<TransitionProtocolTemplate> = emptyList(),
    val recommendedLabel: String? = null,
)

/**
 * A data class tracking the status and necessary follow-up actions for a specific tracked person entity.
 *
 * @param person The [NodeWithPin] representing the person or CRM entry.
 * @param relationshipType A string defining the nature of the relationship (e.g., "Friend", "Professor").
 * @param daysSinceLastContact The calculated number of days since the user last interacted with this person.
 * @param followUpDueInDays The calculated number of days remaining until a scheduled follow-up is due.
 * @param isImportant A boolean flag denoting if the relationship is marked as critical or close.
 * @param linkedItemsCount The total number of nodes (tasks, notes) explicitly linked to this person.
 * @param pendingReplyCount The number of open loops indicating the user is waiting for a reply from this person.
 * @param sharedPlansCount The number of active tasks or projects collaboratively shared with this person.
 * @param askAboutNextTimeCount The number of specific "Ask about X" notes linked to this person.
 */
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

/**
 * A snapshot aggregating all relationship-related nodes, segmenting them by urgency and category.
 *
 * @param people A general list of all [RelationshipStatusItem] entities currently tracked.
 * @param importantRelationships A subset list of [RelationshipStatusItem] entities explicitly marked as important.
 * @param followUpNeeded A subset list of [RelationshipStatusItem] entities requiring immediate contact or follow-up.
 * @param upcomingImportantDates A subset list of [RelationshipStatusItem] entities with imminent birthdays or anniversaries.
 * @param replyQueue A list of open loop nodes indicating the user is waiting for an external reply.
 * @param sharedPlans A list of actionable nodes linked collaboratively to specific tracked people.
 * @param professors A subset list of [RelationshipStatusItem] entities categorized academically.
 * @param friendsAndFamily A subset list of [RelationshipStatusItem] entities categorized personally.
 * @param gentlePrompt An optional system-generated reminder to reach out to a specific neglected connection.
 */
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

/**
 * A data class representing a standard structural template for a specific behavioral or functional Playbook.
 *
 * @param key The unique string identifier for the template (e.g., "exam_prep").
 * @param label The human-readable title or label of the template.
 * @param checklist A list of string tasks forming the default steps of the playbook.
 * @param recommendedModeKey An optional key hinting which focus mode this playbook pairs best with.
 */
data class PlaybookTemplate(
    val key: String,
    val label: String,
    val checklist: List<String>,
    val recommendedModeKey: String? = null,
)

/**
 * A data class wrapping an active, instantiated Playbook node and tracking its execution metrics.
 *
 * @param node The [NodeWithPin] representing the instantiated playbook.
 * @param checklistDone The number of currently checked off steps within the playbook.
 * @param checklistTotal The total number of steps defined within the playbook.
 * @param triggerCount The total historical times this playbook has been executed.
 * @param linkedModeKey An optional key linking this specific playbook to a focus mode.
 * @param linkedAreaId An optional ID linking this specific playbook to an Area of Responsibility.
 * @param isCustom A boolean indicating whether this playbook was built from scratch rather than a [PlaybookTemplate].
 */
data class PlaybookItem(
    val node: NodeWithPin,
    val checklistDone: Int,
    val checklistTotal: Int,
    val triggerCount: Int,
    val linkedModeKey: String? = null,
    val linkedAreaId: Long? = null,
    val isCustom: Boolean = false,
)

/**
 * A snapshot grouping all available Playbook templates and actively running Playbook instances.
 *
 * @param playbooks A list of all actively instantiated [PlaybookItem] entities in the system.
 * @param templates A list of all available [PlaybookTemplate] blueprints for creation.
 * @param suggestedPlaybookLabel An optional system-generated string suggesting a specific playbook based on context or load.
 */
data class PlaybookSnapshot(
    val playbooks: List<PlaybookItem> = emptyList(),
    val templates: List<PlaybookTemplate> = emptyList(),
    val suggestedPlaybookLabel: String? = null,
)
