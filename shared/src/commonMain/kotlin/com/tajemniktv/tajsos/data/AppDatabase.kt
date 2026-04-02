/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

/**
 * Main Room database for TajsOS.
 *
 * This database handles all persistent data including nodes (tasks, notes, etc.),
 * relationships, tracking, and external integrations like calendars.
 */
@Database(
    entities = [
        NodeEntity::class,
        InboxEntryEntity::class,
        TaskFacetEntity::class,
        NoteFacetEntity::class,
        ProjectFacetEntity::class,
        AreaFacetEntity::class,
        RecordFacetEntity::class,
        ItemDomainEntity::class,
        RichContentDocumentEntity::class,
        ScheduleEntryEntity::class,
        SavedViewEntity::class,
        SavedViewSourceKindEntity::class,
        SavedViewFilterEntity::class,
        SavedViewSortEntity::class,
        SavedViewVisibleFieldEntity::class,
        TodayPinEntity::class,
        FocusSessionEntity::class,
        TrackEntryEntity::class,
        RelationEntity::class,
        TagEntity::class,
        NodeTagEntity::class,
        EventLogEntity::class,
        AttachmentEntity::class,
        TemplateEntity::class,
        CalendarProviderEntity::class,
        CalendarEventEntity::class,
        NodeSnapshotEntity::class,
        ReviewEntity::class,
        ModeEntity::class,
        ModePreferenceEntity::class,
        ModeAreaFilterEntity::class,
        ModeTypeFilterEntity::class,
        ModeUsageLogEntity::class,
        ProtocolHistoryEntity::class,
        DecisionOptionEntity::class,
        UserEntity::class,
        MedicationEntity::class,
        TrackMedicationJoinEntity::class,
    ],
    version = 32,
    exportSchema = false,
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    /**
     * Provides access to [NodeEntity] operations, representing the core data model.
     */
    abstract fun nodeDao(): NodeDao

    /**
     * Stores raw capture entries awaiting triage into typed life objects.
     */
    abstract fun inboxEntryDao(): InboxEntryDao

    /**
     * Stores task-specific execution data.
     */
    abstract fun taskFacetDao(): TaskFacetDao

    /**
     * Stores note-specific knowledge-state data.
     */
    abstract fun noteFacetDao(): NoteFacetDao

    /**
     * Stores project-specific coordination data.
     */
    abstract fun projectFacetDao(): ProjectFacetDao

    /**
     * Stores area-specific stewardship data.
     */
    abstract fun areaFacetDao(): AreaFacetDao

    /**
     * Stores record-specific chronology data.
     */
    abstract fun recordFacetDao(): RecordFacetDao

    /**
     * Stores lens-style domain assignments across life objects.
     */
    abstract fun itemDomainDao(): ItemDomainDao

    /**
     * Stores optional rich-content documents attached to life objects.
     */
    abstract fun richContentDocumentDao(): RichContentDocumentDao

    /**
     * Stores attachable schedule and reminder data.
     */
    abstract fun scheduleEntryDao(): ScheduleEntryDao

    /**
     * Stores persisted list/table/board/matrix projections over shared life objects.
     */
    abstract fun savedViewDao(): SavedViewDao

    /**
     * Manages focus session persistence and statistics.
     */
    abstract fun focusSessionDao(): FocusSessionDao

    /**
     * Handles daily activity and progress tracking.
     */
    abstract fun trackDao(): TrackDao

    /**
     * Manages [RelationEntity] records, linking different nodes together.
     */
    abstract fun relationDao(): RelationDao

    /**
     * Handles [TagEntity] and [NodeTagEntity] operations for categorizing nodes.
     */
    abstract fun tagDao(): TagDao

    /**
     * Provides access to the system event logs for audit and sync purposes.
     */
    abstract fun eventLogDao(): EventLogDao

    /**
     * Manages file attachments and external links associated with nodes.
     */
    abstract fun attachmentDao(): AttachmentDao

    /**
     * Handles node templates for quick creation of pre-defined structures.
     */
    abstract fun templateDao(): TemplateDao

    /**
     * Manages external calendar source configurations.
     */
    abstract fun calendarProviderDao(): CalendarProviderDao

    /**
     * Handles cached calendar events synced from external providers.
     */
    abstract fun calendarEventDao(): CalendarEventDao

    /**
     * Stores historical versions of nodes.
     */
    abstract fun nodeSnapshotDao(): NodeSnapshotDao

    /**
     * Manages formal review sessions.
     */
    abstract fun reviewDao(): ReviewDao

    /**
     * Manages Operating Mode configurations and logs.
     */
    abstract fun modeDao(): ModeDao

    /**
     * Tracks execution history of routines and playbooks.
     */
    abstract fun protocolDao(): ProtocolDao

    /**
     * Manages options for decisions.
     */
    abstract fun decisionDao(): DecisionDao

    /**
     * Manages user profile data.
     */
    abstract fun userDao(): UserDao

    /**
     * Manages medications and tracking.
     */
    abstract fun medicationDao(): MedicationDao
}

/**
 * Constructor for [AppDatabase] generated by Room.
 */
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    /**
     * Initializes the [AppDatabase].
     */
    override fun initialize(): AppDatabase
}

/**
 * Configures and builds the [AppDatabase] instance.
 *
 * @param builder The platform-specific Room database builder.
 * @return A fully initialized [AppDatabase] instance.
 */
fun getDatabaseBuilder(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase =
    builder
        .build()
