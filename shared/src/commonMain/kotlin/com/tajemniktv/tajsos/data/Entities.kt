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
    val type: String, // task, note, project, area, resource, idea
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
    // Reminders & Recurrence
    val reminderAt: Long? = null,
    val isRecurring: Boolean = false,
    val recurringInterval: String? = null, // daily, weekly, monthly
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
    val sleepScore: Float? = null,
    val tookMeds: Boolean = false,
    val symptomNote: String = "",
    val source: String = "manual", // manual, inferred, reminder
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
    val relationType: String, // RELATED, MENTION, DEPENDS_ON, BELONGS_TO, REFERENCE, DERIVED_FROM
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
    val eventType: String, // ITEM_CREATED, ITEM_COMPLETED, ITEM_ARCHIVED, SESSION_STARTED, SESSION_ENDED, TODAY_ASSIGNED, ITEM_LINKED, CHECKIN_CREATED
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
) {
    val isPinnedToToday: Boolean get() = pin != null
}
