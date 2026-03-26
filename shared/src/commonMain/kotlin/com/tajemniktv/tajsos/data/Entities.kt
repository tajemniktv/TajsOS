/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlinx.serialization.Serializable

/**
 * NodeEntity is the central entity in TajsOS, representing everything from
 * tasks and notes to projects and areas.
 */
@Entity(tableName = "nodes")
@Serializable
data class NodeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /**
     * Task, note, project, area, resource, idea
     */
    val type: String,
    val title: String,
    val content: String = "",
    val status: String = "active", // active, done, archived, on_hold, someday, blocked
    val isPinned: Boolean = false,
    val parentNodeId: Long? = null,
    val projectId: Long? = null,
    val areaId: Long? = null,
    val color: Int? = null, // Used for projects and areas
    val icon: String? = null, // Used for projects and areas
    val source: String = "manual", // manual, import, template, system
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
    // Energy & Friction (Roadmap Section 3)
    val energyLevel: Int? = null, // 1=low, 2=medium, 3=high
    val friction: String? = null, // easy, annoying, mentally_heavy, unclear
    val nextSmallestStep: String? = null,
    val estimatedMinutes: Int? = null,
    val postponeCount: Int = 0,
    val completionNote: String? = null,
    val isHardDeadline: Boolean = false,
    // Project/Area specific (Roadmap Section 4)
    val projectWhy: String? = null,
    val isFrozen: Boolean = false,
    val projectStatus: String? = null, // active, slowing_down, neglected, exploratory, on_hold
    // Reminders & Recurrence
    val reminderAt: Long? = null,
    val isRecurring: Boolean = false,
    val recurringInterval: String? = null, // daily, weekly, monthly
    // Note-specific (Roadmap Section 5)
    val noteType: String? = null, // thought, lecture, research, idea, reflection, bug, concept, evergreen, meeting, reading, journal
    val noteState: String? = null, // raw, highlighted, distilled, takeaway
    // Media/Resource specific (Roadmap Section 11)
    val mediaType: String? = null, // book, article, podcast, video, link
    val rating: Int? = null, // 1-5 stars
    val author: String? = null,
    val publisher: String? = null,
    // LifeOS Features (Open Loops, Decisions, Maintenance, Relationships, Contexts, Health)
    /**
     * open_loop, decision, maintenance, person, place, protocol, rule, principle, vault, document
     */
    val openLoopType: String? = null, // reply_needed, waiting_for, pending_decision, must_check_later, follow_up, unresolved_problem
    val openLoopStalenessAt: Long? = null,
    val decisionStatus: String? = null, // pending, decided, expired, parked
    val decisionCategory: String? = null, // tiny, major
    val decisionRevisitAt: Long? = null,
    val decisionOutcome: String? = null,
    val decisionInfoMissing: String? = null,
    val decisionDifficultBecause: String? = null,
    val decisionEasierIf: String? = null,
    val maintenanceType: String? = null, // med_refill, prescription, appointment, bill, subscription, renewal, form, cleaning, backup
    val maintenanceInterval: String? = null, // manual string representation or JSON
    val maintenanceOverdueAt: Long? = null,
    val lastContactAt: Long? = null,
    val socialEnergyNotes: String? = null,
    val relationshipContext: String? = null,
    val locationContext: String? = null, // at_home, on_campus, out_of_home
    val energyContext: String? = null, // low_energy, high_focus, brain_works, emotionally_wrecked
    val deviceContext: String? = null, // laptop_required, phone_okay, needs_internet
    val socialContext: String? = null, // needs_privacy, commute_friendly
    val timeWindowContext: String? = null, // 10_minute, waiting_room
    val areaHealthStatus: String? = null, // active, neglected, overloaded, stable, on_fire
    // Typed metadata envelope for optional packs and future schema evolution.
    val metadataJson: String? = null,
)

/**
 * ModeEntity defines an Operating Mode profile.
 */
@Entity(tableName = "modes")
@Serializable
data class ModeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val key: String, // COMMAND, FOCUS, RECOVERY, STUDY, ADMIN, ERRAND, SHUTDOWN
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
    val activationSource: String? = null, // manual, auto, suggestion
    val contextJson: String? = null,
)

/**
 * ProtocolHistoryEntity tracks when transition protocols are executed.
 */
@Entity(tableName = "protocol_history")
@Serializable
data class ProtocolHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val protocolNodeId: Long, // Linked to NodeEntity of type 'protocol'
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
    val decisionNodeId: Long, // Linked to NodeEntity of type 'decision'
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
    val date: String, // YYYY-MM-DD
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
    val sessionType: String = "FOCUS", // FOCUS, REVIEW, WRITING, STUDY, FREEFORM
    val startedAt: Long,
    val endedAt: Long? = null,
    val durationSec: Int = 0,
    val interrupted: Boolean = false,
    val completed: Boolean = true,
    val note: String? = null,
)

/**
 * TrackEntryEntity handles daily micro check-ins.
 */
@Entity(tableName = "track_entries")
@Serializable
data class TrackEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // YYYY-MM-DD
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
    val source: String = "manual", // manual, inferred, reminder
    // LifeOS Status Tracking
    val loadScore: Int? = null, // 0-100
    val fragmentationScore: Int? = null, // 0-100
)

/**
 * UserEntity stores basic user profile information.
 */
@Entity(tableName = "users")
@Serializable
data class UserEntity(
    @PrimaryKey val id: Long = 1, // Singleton for now
    val name: String = "OPERATOR",
    val avatarUrl: String? = null,
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
    val brandNames: String = "", // Comma separated list of brands
    val dosage: String? = null,
    val takeAtHour: Int? = null, // 0-23
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
 * RelationEntity links items to other items or entities.
 */
@Entity(tableName = "relations")
@Serializable
data class RelationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fromNodeId: Long,
    val toNodeId: Long,
    val relationType: String, // RELATED, MENTION, DEPENDS_ON, BELONGS_TO, REFERENCE, DERIVED_FROM, INSPIRED_BY
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
    val eventType: String, // NODE_CREATED, NODE_COMPLETED, NODE_ARCHIVED, SESSION_STARTED, SESSION_ENDED, TODAY_ASSIGNED, NODE_LINKED, CHECKIN_CREATED, NODE_FROZEN, NODE_UNFROZEN
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
@Entity(tableName = "attachments")
@Serializable
data class AttachmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nodeId: Long,
    val assetType: String, // URL, IMAGE, FILE, AUDIO
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
data class TemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val nodeType: String, // task, note, project
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
    val type: String, // daily, weekly, monthly
    val date: String, // YYYY-MM-DD
    val completedAt: Long =
        kotlin.time.Clock.System
            .now()
            .toEpochMilliseconds(),
    val resultNodeId: Long? = null, // Linked note containing the review content
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
    val type: String, // GOOGLE, OUTLOOK, ICS, TAJSOS
    val accountEmail: String? = null,
    val url: String? = null, // For ICS
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
 * NodeWithPin is a "POJO" used by Room to perform a JOIN.
 */
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
    val isPinnedToToday: Boolean get() = pin != null
}
