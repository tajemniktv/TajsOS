/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import androidx.compose.runtime.Immutable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlinx.serialization.Serializable

/**
 * NodeEntity is the central polymorphic entity in TajsOS, acting as the primary system object.
 * It represents everything from tasks and notes to projects, records, and areas.
 *
 * Architectural Note: This is an overloaded legacy surface. While it supports generic string types
 * (`type`, `status`), new domain behavior should prefer typed models and companion structures
 * (e.g. `ItemKind`) instead of relying purely on this table to prevent string-state sprawl.
 */
@Entity(
    tableName = "nodes",
    indices = [
        Index(value = ["type", "status"]),
        Index(value = ["projectId", "status"]),
        Index(value = ["areaId", "status"]),
        Index(value = ["dueAt"]),
        Index(value = ["startAt"]),
        Index(value = ["updatedAt"]),
    ],
)
@Serializable
@Immutable
/**
 * @property id The unique identifier for the node.
 * @property type The legacy string-based type of the node.
 * @property title The main display title of the node.
 * @property content The main textual body of the node.
 * @property status The execution lifecycle state (e.g. active, done).
 * @property isPinned Indicates if the node is currently pinned.
 * @property parentNodeId Optional ID of the parent node for hierarchy.
 * @property projectId Optional ID linking this node to a project.
 * @property areaId Optional ID linking this node to an area.
 * @property color Optional UI color integer associated with this node.
 * @property icon Optional string identifier for an icon representing this node.
 * @property source The origin source of the node (e.g., manual, system).
 * @property inboxState Indicates if the node is currently held in the inbox.
 * @property sortOrder An optional integer to manually rank or order nodes.
 * @property contextScreen An optional string representing the UI screen context.
 * @property isSticky Indicates if the node should stick to the top of lists.
 * @property dueAt Optional explicit deadline for the node (epoch ms).
 * @property startAt Optional explicit start time for the node (epoch ms).
 * @property completedAt Optional timestamp for when the node was marked done (epoch ms).
 * @property archivedAt Optional timestamp for when the node was archived (epoch ms).
 * @property deletedAt Optional timestamp for when the node was soft-deleted (epoch ms).
 * @property createdAt The timestamp when the node was originally created (epoch ms).
 * @property updatedAt The timestamp when the node was most recently modified (epoch ms).
 * @property energyLevel Optional rating of energy required (e.g., 1=low, 3=high).
 * @property friction Optional descriptor of the cognitive or emotional friction involved.
 * @property nextSmallestStep An optional description of the immediate next action to take.
 * @property estimatedMinutes Optional estimated duration in minutes to complete.
 * @property postponeCount Tracks how many times the item's deadline has been delayed.
 * @property completionNote Optional reflection or note added when marking as complete.
 * @property isHardDeadline Indicates if the due date is inflexible.
 * @property projectWhy Optional project-level statement of purpose.
 * @property isFrozen Indicates if the project or node is currently suspended/frozen.
 * @property projectStatus Optional specific project lifecycle string (e.g., active, on_hold).
 * @property reminderAt Optional timestamp for a scheduled alert or reminder (epoch ms).
 * @property isRecurring Indicates if the node represents a repeating pattern or task.
 * @property recurringInterval Optional interval string (e.g. daily, weekly) if recurring.
 * @property noteType Optional categorical string for knowledge items (e.g., reflection, journal).
 * @property noteState Optional state of the knowledge item (e.g., raw, distilled).
 * @property mediaType Optional identifier for the type of media (e.g., book, video).
 * @property rating Optional subjective score rating (e.g., 1-5).
 * @property author Optional creator or author of a media/reference node.
 * @property publisher Optional publisher or source entity for a reference node.
 * @property openLoopType Optional identifier indicating the type of unresolved loop.
 * @property openLoopStalenessAt Optional timestamp indicating when an open loop became stale.
 * @property decisionStatus Optional state of a decision node (e.g., pending, decided).
 * @property decisionCategory Optional size or scope string for a decision.
 * @property decisionRevisitAt Optional timestamp to review a past decision.
 * @property decisionOutcome Optional recorded result or conclusion of a decision.
 * @property decisionInfoMissing Optional notes on what data is preventing a decision.
 * @property decisionDifficultBecause Optional notes explaining the friction of the decision.
 * @property decisionEasierIf Optional thoughts on what would simplify the decision.
 * @property maintenanceType Optional category string for a maintenance or routine responsibility.
 * @property maintenanceInterval Optional text or JSON describing the maintenance frequency.
 * @property maintenanceOverdueAt Optional timestamp when a maintenance task became past due.
 * @property lastContactAt Optional timestamp of the last interaction for a relationship node.
 * @property socialEnergyNotes Optional notes regarding the social dynamic or energy.
 * @property relationshipContext Optional string describing the context of the relationship.
 * @property locationContext Optional environmental requirement string (e.g., at_home).
 * @property energyContext Optional cognitive requirement string (e.g., low_energy).
 * @property deviceContext Optional hardware requirement string (e.g., laptop_required).
 * @property socialContext Optional social requirement string (e.g., needs_privacy).
 * @property timeWindowContext Optional duration requirement string (e.g., 10_minute).
 * @property areaHealthStatus Optional health descriptor for an area (e.g., stable, overloaded).
 * @property metadataJson Optional serialized envelope for extended typed facets and domain mappings.
 */
data class NodeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /**
     * Legacy storage type mirrored by current UI.
     * New work should prefer [ItemKind] plus typed facets.
     */
    val type: String,
    val title: String,
    val content: String = "",
    /**
     * active, done, archived, on_hold, someday, blocked
     */
    val status: String = "active",
    val isPinned: Boolean = false,
    val parentNodeId: Long? = null,
    val projectId: Long? = null,
    val areaId: Long? = null,
    /**
     * Used for projects and areas.
     */
    val color: Int? = null,
    /**
     * Used for projects and areas.
     */
    val icon: String? = null,
    /**
     * manual, import, template, system
     */
    val source: String = "manual",
    val inboxState: Boolean = true,
    val sortOrder: Int? = null,
    val contextScreen: String? = null,
    val isSticky: Boolean = false,
    val dueAt: Long? = null,
    val startAt: Long? = null,
    val completedAt: Long? = null,
    val archivedAt: Long? = null,
    val deletedAt: Long? = null,
    val createdAt: Long =
        kotlin.time.Clock.System
            .now()
            .toEpochMilliseconds(),
    val updatedAt: Long =
        kotlin.time.Clock.System
            .now()
            .toEpochMilliseconds(),
    /**
     * Energy & Friction (Roadmap Section 3). 1=low, 2=medium, 3=high.
     */
    val energyLevel: Int? = null,
    /**
     * easy, annoying, mentally_heavy, unclear
     */
    val friction: String? = null,
    val nextSmallestStep: String? = null,
    val estimatedMinutes: Int? = null,
    val postponeCount: Int = 0,
    val completionNote: String? = null,
    val isHardDeadline: Boolean = false,
    /**
     * Project/Area specific (Roadmap Section 4).
     */
    val projectWhy: String? = null,
    val isFrozen: Boolean = false,
    /**
     * active, slowing_down, neglected, exploratory, on_hold
     */
    val projectStatus: String? = null,
    /**
     * Reminders & recurrence.
     */
    val reminderAt: Long? = null,
    val isRecurring: Boolean = false,
    /**
     * daily, weekly, monthly
     */
    val recurringInterval: String? = null,
    /**
     * Note-specific (Roadmap Section 5): thought, lecture, research, rough note,
     * reflection, bug, concept, evergreen, meeting, reading, journal.
     */
    val noteType: String? = null,
    /**
     * raw, highlighted, distilled, takeaway
     */
    val noteState: String? = null,
    /**
     * Media/Resource specific (Roadmap Section 11): book, article, podcast, video, link.
     */
    val mediaType: String? = null,
    /**
     * 1-5 stars
     */
    val rating: Int? = null,
    val author: String? = null,
    val publisher: String? = null,
    /**
     * LifeOS feature type (Open Loops, Decisions, Maintenance, Relationships, Contexts, Health):
     * unresolved work, decision support, maintenance, relationship anchor, place anchor, routine, rule, principle, reference, document.
     * For open loops: reply_needed, waiting_for, pending_decision, must_check_later,
     * follow_up, unresolved_problem.
     */
    val openLoopType: String? = null,
    val openLoopStalenessAt: Long? = null,
    /**
     * pending, decided, expired, parked
     */
    val decisionStatus: String? = null,
    /**
     * tiny, major
     */
    val decisionCategory: String? = null,
    val decisionRevisitAt: Long? = null,
    val decisionOutcome: String? = null,
    val decisionInfoMissing: String? = null,
    val decisionDifficultBecause: String? = null,
    val decisionEasierIf: String? = null,
    /**
     * med_refill, prescription, appointment, bill, subscription, renewal, form, cleaning, backup
     */
    val maintenanceType: String? = null,
    /**
     * Manual string representation or JSON.
     */
    val maintenanceInterval: String? = null,
    val maintenanceOverdueAt: Long? = null,
    val lastContactAt: Long? = null,
    val socialEnergyNotes: String? = null,
    val relationshipContext: String? = null,
    /**
     * at_home, on_campus, out_of_home
     */
    val locationContext: String? = null,
    /**
     * low_energy, high_focus, brain_works, emotionally_wrecked
     */
    val energyContext: String? = null,
    /**
     * laptop_required, phone_okay, needs_internet
     */
    val deviceContext: String? = null,
    /**
     * needs_privacy, commute_friendly
     */
    val socialContext: String? = null,
    /**
     * 10_minute, waiting_room
     */
    val timeWindowContext: String? = null,
    /**
     * active, neglected, overloaded, stable, on_fire
     */
    val areaHealthStatus: String? = null,
    /**
     * Typed metadata envelope for optional packs and future schema evolution.
     */
    val metadataJson: String? = null,
)

/**
 * ModeEntity defines an Operating Mode profile.
 *
 * @property id The unique primary key of the mode.
 * @property key The unique identifier for the mode (e.g., COMMAND, FOCUS, RECOVERY).
 * @property name The human-readable name of the mode.
 * @property description An optional explanation of the mode's purpose.
 * @property icon An optional string identifier for the mode's icon.
 * @property themeColor An optional integer representing the theme color for this mode.
 * @property isBuiltin Whether this mode is a built-in system default or user-created.
 * @property isEnabled Whether this mode is currently active and selectable.
 * @property sortOrder The display order of the mode in lists.
 * @property createdAt The timestamp (epoch milliseconds) when the mode was created.
 * @property updatedAt The timestamp (epoch milliseconds) when the mode was last updated.
 */
@Entity(tableName = "modes")
@Serializable
@Immutable
data class ModeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /**
     * COMMAND, FOCUS, RECOVERY, STUDY, ADMIN, ERRAND, SHUTDOWN
     */
    val key: String,
    val name: String,
    val description: String? = null,
    val icon: String? = null,
    val themeColor: Int? = null,
    val isBuiltin: Boolean = true,
    val isEnabled: Boolean = true,
    val sortOrder: Int = 0,
    val createdAt: Long =
        kotlin.time.Clock.System
            .now()
            .toEpochMilliseconds(),
    val updatedAt: Long =
        kotlin.time.Clock.System
            .now()
            .toEpochMilliseconds(),
)

/**
 * ModePreferenceEntity stores the behavioral configuration for a mode.
 *
 * @property id The unique primary key.
 * @property modeId The ID of the ModeEntity this preference is linked to.
 * @property showInbox Whether the inbox section is visible in this mode.
 * @property showStats Whether the statistics section is visible in this mode.
 * @property showNotes Whether the notes section is visible in this mode.
 * @property showResources Whether the resources section is visible in this mode.
 * @property showDeadlines Whether the deadlines section is visible in this mode.
 * @property showOpenLoops Whether the open loops section is visible in this mode.
 * @property maxVisibleTasks The maximum number of tasks to display in a list.
 * @property sortStrategy The identifier indicating how the resulting nodes should be sorted (e.g., "DEFAULT", "URGENCY").
 * @property quickActionsJson A JSON string defining the available quick actions.
 * @property defaultQuickActionsJson A JSON string defining the default quick actions for this mode.
 * @property dashboardBlocksJson A JSON string defining the modular dashboard components to render.
 * @property filterProfileJson A JSON string storing advanced filter configurations for this mode.
 * @property suggestionProfileJson A JSON string defining the AI or context-based suggestions relevant to this mode.
 */
@Entity(tableName = "mode_preferences")
@Serializable
data class ModePreferenceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val modeId: Long,
    val showInbox: Boolean = true,
    val showStats: Boolean = true,
    val showNotes: Boolean = true,
    val showResources: Boolean = true,
    val showDeadlines: Boolean = true,
    val showOpenLoops: Boolean = true,
    val maxVisibleTasks: Int = 10,
    val sortStrategy: String = "DEFAULT",
    val quickActionsJson: String? = null,
    val defaultQuickActionsJson: String? = null,
    val dashboardBlocksJson: String? = null,
    val filterProfileJson: String? = null,
    val suggestionProfileJson: String? = null,
)

/**
 * ModeAreaFilterEntity filters which areas are visible in a mode.
 */
@Entity(
    tableName = "mode_area_filters",
    primaryKeys = ["modeId", "areaId"],
)
@Serializable
data class ModeAreaFilterEntity(
    val modeId: Long,
    val areaId: Long,
    val include: Boolean = true,
)

/**
 * ModeTypeFilterEntity filters which node types are visible in a mode.
 */
@Entity(
    tableName = "mode_type_filters",
    primaryKeys = ["modeId", "nodeType"],
)
@Serializable
data class ModeTypeFilterEntity(
    val modeId: Long,
    val nodeType: String,
    val include: Boolean = true,
)

/**
 * ModeUsageLogEntity tracks mode transitions.
 */
@Entity(tableName = "mode_usage_logs")
@Serializable
data class ModeUsageLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val modeId: Long,
    val activatedAt: Long =
        kotlin.time.Clock.System
            .now()
            .toEpochMilliseconds(),
    val deactivatedAt: Long? = null,
    /**
     * manual, auto, suggestion
     */
    val activationSource: String? = null,
    val contextJson: String? = null,
)

/**
 * ProtocolHistoryEntity tracks when routines and playbooks are executed.
 */
@Entity(tableName = "protocol_history")
@Serializable
data class ProtocolHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /**
     * Linked to the routine/playbook node that was executed.
     */
    val protocolNodeId: Long,
    val executedAt: Long =
        kotlin.time.Clock.System
            .now()
            .toEpochMilliseconds(),
    val notes: String? = null,
    val completed: Boolean = true,
)

/**
 * DecisionOptionEntity stores options for a decision node.
 */
@Entity(tableName = "decision_options")
@Serializable
data class DecisionOptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /**
     * Linked to [NodeEntity] of type `decision`.
     */
    val decisionNodeId: Long,
    val title: String,
    val description: String? = null,
    val prosJson: String? = null,
    val consJson: String? = null,
    val isSelected: Boolean = false,
)

/**
 * TodayPinEntity represents an item that has been "shortlisted" for today.
 */
@Entity(tableName = "today_pins")
@Serializable
data class TodayPinEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nodeId: Long,
    /**
     * YYYY-MM-DD
     */
    val date: String,
    val position: Int,
    val selectedAt: Long =
        kotlin.time.Clock.System
            .now()
            .toEpochMilliseconds(),
)

/**
 * FocusSessionEntity logs the time spent on a specific node.
 */
@Entity(tableName = "focus_sessions")
@Serializable
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nodeId: Long,
    /**
     * FOCUS, REVIEW, WRITING, STUDY, FREEFORM
     */
    val sessionType: String = "FOCUS",
    val startedAt: Long,
    val endedAt: Long? = null,
    val durationSec: Int = 0,
    val interrupted: Boolean = false,
    val completed: Boolean = true,
    val note: String? = null,
)

/**
 * TrackEntryEntity handles daily micro check-ins.
 *
 * @property id The unique primary key of the track entry.
 * @property date The date of the track entry in YYYY-MM-DD format.
 * @property createdAt The timestamp (epoch milliseconds) when the entry was created.
 * @property moodScore An optional score representing the user's mood.
 * @property energyScore An optional score representing the user's energy level.
 * @property focusScore An optional score representing the user's focus level.
 * @property anxietyScore An optional score representing the user's anxiety level.
 * @property sleepScore An optional score representing the user's sleep quality.
 * @property tookMeds Indicates if the user took their tracked medications.
 * @property symptomNote A text note for any observed symptoms or health details.
 * @property source The origin of the entry (e.g., manual, inferred, reminder).
 * @property loadScore LifeOS status tracking representing overall cognitive or physical load (0-100).
 * @property fragmentationScore LifeOS status tracking representing task or context fragmentation (0-100).
 */
@Entity(tableName = "track_entries")
@Serializable
data class TrackEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /**
     * YYYY-MM-DD
     */
    val date: String,
    val createdAt: Long =
        kotlin.time.Clock.System
            .now()
            .toEpochMilliseconds(),
    val moodScore: Int? = null,
    val energyScore: Int? = null,
    val focusScore: Int? = null,
    val anxietyScore: Int? = null,
    val sleepScore: Float? = null,
    val tookMeds: Boolean = false,
    val symptomNote: String = "",
    /**
     * manual, inferred, reminder
     */
    val source: String = "manual",
    /**
     * LifeOS status tracking. 0-100.
     */
    val loadScore: Int? = null,
    /**
     * LifeOS status tracking. 0-100.
     */
    val fragmentationScore: Int? = null,
)

/**
 * UserEntity stores basic user profile information.
 */
@Entity(tableName = "users")
@Serializable
data class UserEntity(
    /**
     * Singleton for now.
     */
    @PrimaryKey val id: Long = 1,
    val firstName: String = "",
    val lastName: String = "",
    val nickname: String = "OPERATOR",
    val email: String = "",
    val avatarRef: String? = null,
    val bio: String = "",
    val phoneNumber: String = "",
    /**
     * ISO date string `YYYY-MM-DD`.
     */
    val birthDate: String = "",
    val city: String = "",
    val country: String = "",
    val timezone: String = "",
    val occupation: String = "",
    val website: String = "",
    val preferredGreeting: String = "",
    /**
     * Serialized [UserDisplayNameFormat] enum key.
     */
    val displayNameFormat: String = "NICKNAME",
    val createdAt: Long =
        kotlin.time.Clock.System
            .now()
            .toEpochMilliseconds(),
    val updatedAt: Long =
        kotlin.time.Clock.System
            .now()
            .toEpochMilliseconds(),
)

/**
 * MedicationEntity represents a drug or supplement the user tracks.
 */
@Entity(tableName = "medications")
@Serializable
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val substance: String,
    /**
     * Comma-separated list of brands.
     */
    val brandNames: String = "",
    val dosage: String? = null,
    /**
     * 0-23
     */
    val takeAtHour: Int? = null,
    val isOptional: Boolean = false,
    val isEnabled: Boolean = true,
    val createdAt: Long =
        kotlin.time.Clock.System
            .now()
            .toEpochMilliseconds(),
    val updatedAt: Long =
        kotlin.time.Clock.System
            .now()
            .toEpochMilliseconds(),
)

/**
 * TrackMedicationJoinEntity tracks whether a specific medication was taken for a specific track entry.
 */
@Entity(
    tableName = "track_medications",
    primaryKeys = ["trackEntryId", "medicationId"],
    indices = [Index(value = ["trackEntryId"]), Index(value = ["medicationId"])],
)
@Serializable
data class TrackMedicationJoinEntity(
    val trackEntryId: Long,
    val medicationId: Long,
    val wasTaken: Boolean = false,
)

/**
 * Represents a directed relationship graph edge between two nodes.
 *
 * This table serves as a first-class capability for creating the LifeOS graph network,
 * enabling bidirectional relationships, hierarchy representations, and knowledge linking.
 */
@Entity(
    tableName = "relations",
    indices = [
        Index(value = ["fromNodeId"]),
        Index(value = ["toNodeId"]),
        Index(value = ["relationType"]),
        Index(value = ["fromNodeId", "toNodeId", "relationType"], unique = true),
    ],
)
@Serializable
data class RelationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fromNodeId: Long,
    val toNodeId: Long,
    /**
     * RELATED, MENTION, DEPENDS_ON, BELONGS_TO, REFERENCE, DERIVED_FROM, INSPIRED_BY
     */
    val relationType: String,
    val createdAt: Long =
        kotlin.time.Clock.System
            .now()
            .toEpochMilliseconds(),
    val metadataJson: String? = null,
)

/**
 * TagEntity represents an optional label for organization.
 */
@Entity(tableName = "tags")
@Serializable
@Immutable
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val normalizedName: String,
    val color: Int? = null,
)

/**
 * ItemTagEntity is a join table for items and tags.
 */
@Entity(
    tableName = "node_tags",
    primaryKeys = ["nodeId", "tagId"],
    indices = [Index(value = ["tagId"])],
)
@Serializable
data class NodeTagEntity(
    val nodeId: Long,
    val tagId: Long,
)

/**
 * EventLogEntity provides passive logging for user actions.
 */
@Entity(tableName = "event_log")
@Serializable
data class EventLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /**
     * NODE_CREATED, NODE_COMPLETED, NODE_ARCHIVED, SESSION_STARTED, SESSION_ENDED,
     * TODAY_ASSIGNED, NODE_LINKED, CHECKIN_CREATED, NODE_FROZEN, NODE_UNFROZEN
     */
    val eventType: String,
    val nodeId: Long? = null,
    val relatedNodeId: Long? = null,
    val timestamp: Long =
        kotlin.time.Clock.System
            .now()
            .toEpochMilliseconds(),
    val payloadJson: String? = null,
)

/**
 * AttachmentEntity stores links or references to assets.
 */
@Entity(
    tableName = "attachments",
    indices = [Index(value = ["nodeId"])],
)
@Serializable
data class AttachmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nodeId: Long,
    /**
     * URL, IMAGE, FILE, AUDIO
     */
    val assetType: String,
    val uriOrPath: String,
    val mimeType: String? = null,
    val title: String? = null,
    val metadataJson: String? = null,
)

/**
 * TemplateEntity defines a reusable structure for new items.
 */
@Entity(tableName = "templates")
@Serializable
@Immutable
data class TemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /**
     * task, note, project
     */
    val nodeType: String,
    val defaultTitle: String? = null,
    val defaultContent: String? = null,
    val defaultMetadataJson: String? = null,
    val isEnabled: Boolean = true,
)

/**
 * ReviewEntity tracks formal reflection sessions.
 */
@Entity(tableName = "reviews")
@Serializable
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /**
     * daily, weekly, monthly
     */
    val type: String,
    /**
     * YYYY-MM-DD
     */
    val date: String,
    val completedAt: Long =
        kotlin.time.Clock.System
            .now()
            .toEpochMilliseconds(),
    /**
     * Linked note containing the review content.
     */
    val resultNodeId: Long? = null,
    val moodScore: Int? = null,
    val energyScore: Int? = null,
)

/**
 * CalendarProviderEntity represents an external or internal source of calendar events.
 */
@Entity(tableName = "calendar_providers")
@Serializable
data class CalendarProviderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /**
     * GOOGLE, OUTLOOK, ICS, TAJSOS
     */
    val type: String,
    val accountEmail: String? = null,
    /**
     * For ICS.
     */
    val url: String? = null,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val lastSyncedAt: Long? = null,
    val isEnabled: Boolean = true,
    val color: Int? = null,
)

/**
 * CalendarEventEntity represents a single event from an external calendar or a pure calendar event.
 */
@Entity(tableName = "calendar_events")
@Serializable
data class CalendarEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val providerId: Long,
    val externalId: String? = null,
    val title: String,
    val description: String? = null,
    val location: String? = null,
    val startAt: Long,
    val endAt: Long,
    val isAllDay: Boolean = false,
    val url: String? = null,
    val createdAt: Long =
        kotlin.time.Clock.System
            .now()
            .toEpochMilliseconds(),
    val updatedAt: Long =
        kotlin.time.Clock.System
            .now()
            .toEpochMilliseconds(),
)

/**
 * NodeSnapshotEntity stores historical versions of a node's content.
 */
@Entity(
    tableName = "node_snapshots",
    indices = [Index(value = ["nodeId"])],
)
@Serializable
@Immutable
data class NodeSnapshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nodeId: Long,
    val title: String,
    val content: String,
    val timestamp: Long =
        kotlin.time.Clock.System
            .now()
            .toEpochMilliseconds(),
)

/**
 * NodeWithPin is a "POJO" used by Room to perform a JOIN, creating a rich read-model.
 *
 * @property node The core entity data.
 * @property pin The pinning status, joining the node with any active pin for today.
 * @property tags The list of tags associated with the node, retrieved via a junction table.
 * @property snapshots The historical content snapshots associated with the node.
 */
@Immutable
data class NodeWithPin(
    @Embedded val node: NodeEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "nodeId",
    )
    val pin: TodayPinEntity?,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy =
            Junction(
                value = NodeTagEntity::class,
                parentColumn = "nodeId",
                entityColumn = "tagId",
            ),
    )
    val tags: List<TagEntity> = emptyList(),
    @Relation(
        parentColumn = "id",
        entityColumn = "nodeId",
    )
    val snapshots: List<NodeSnapshotEntity> = emptyList(),
) {
    /**
     * Helper to quickly determine if the node is currently pinned to today's view.
     */
    val isPinnedToToday: Boolean get() = pin != null
}
