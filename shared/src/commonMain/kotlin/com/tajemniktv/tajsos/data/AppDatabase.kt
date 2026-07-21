/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

/**
 * The primary Room database for the TajsOS application.
 *
 * This database stores the core life-object graph, including nodes, facets,
 * relations, and various system logs and preferences.
 */
/**
 * The main Room database for TajsOS.
 *
 * This database acts as the single source of truth for all structured and unstructured life objects,
 * views, scheduling facets, and relational graphs across all multiplatform targets.
 */
@Database(
    entities =
        [
            NodeEntity::class,
            NodeTagEntity::class,
            RelationEntity::class,
            TagEntity::class,
            FocusSessionEntity::class,
            TrackEntryEntity::class,
            TrackMedicationJoinEntity::class,
            EventLogEntity::class,
            AttachmentEntity::class,
            TemplateEntity::class,
            CalendarProviderEntity::class,
            CalendarEventEntity::class,
            NodeSnapshotEntity::class,
            ReviewEntity::class,
            TodayPinEntity::class,
            ModeEntity::class,
            ModePreferenceEntity::class,
            ModeAreaFilterEntity::class,
            ModeTypeFilterEntity::class,
            ModeUsageLogEntity::class,
            ProtocolHistoryEntity::class,
            DecisionOptionEntity::class,
            UserEntity::class,
            MedicationEntity::class,
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
        ],
    version = 33,
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    /** Data Access Object for managing core life-objects (Nodes). */
    abstract fun nodeDao(): NodeDao

    /** Data Access Object for raw system captures. */
    abstract fun inboxEntryDao(): InboxEntryDao

    /** Data Access Object for task-specific behavioral state. */
    abstract fun taskFacetDao(): TaskFacetDao

    /** Data Access Object for note-specific organizational state. */
    abstract fun noteFacetDao(): NoteFacetDao

    /** Data Access Object for project-specific lifecycle state. */
    abstract fun projectFacetDao(): ProjectFacetDao

    /** Data Access Object for area-specific state. */
    abstract fun areaFacetDao(): AreaFacetDao

    /** Data Access Object for record-specific temporal state. */
    abstract fun recordFacetDao(): RecordFacetDao

    /** Data Access Object for domain lens assignments. */
    abstract fun itemDomainDao(): ItemDomainDao

    /** Data Access Object for Markdown and structured documents. */
    abstract fun richContentDocumentDao(): RichContentDocumentDao

    /** Data Access Object for reminder and task scheduling. */
    abstract fun scheduleEntryDao(): ScheduleEntryDao

    /** Data Access Object for customized system views. */
    abstract fun savedViewDao(): SavedViewDao

    /** Data Access Object for time tracking and focus sessions. */
    abstract fun focusSessionDao(): FocusSessionDao

    /** Data Access Object for daily check-ins and metrics. */
    abstract fun trackDao(): TrackDao

    /** Data Access Object for linking life-objects. */
    abstract fun relationDao(): RelationDao

    /** Data Access Object for taxonomic categorization. */
    abstract fun tagDao(): TagDao

    /** Data Access Object for internal system audit logs. */
    abstract fun eventLogDao(): EventLogDao

    /** Data Access Object for file and URI references. */
    abstract fun attachmentDao(): AttachmentDao

    /** Data Access Object for content boilerplate. */
    abstract fun templateDao(): TemplateDao

    /** Data Access Object for external calendar sync configurations. */
    abstract fun calendarProviderDao(): CalendarProviderDao

    /** Data Access Object for mirrored external calendar events. */
    abstract fun calendarEventDao(): CalendarEventDao

    /** Data Access Object for point-in-time state preservation. */
    abstract fun nodeSnapshotDao(): NodeSnapshotDao

    /** Data Access Object for periodic review summaries. */
    abstract fun reviewDao(): ReviewDao

    /** Data Access Object for system-wide operating modes. */
    abstract fun modeDao(): ModeDao

    /** Data Access Object for step-by-step procedure history. */
    abstract fun protocolDao(): ProtocolDao

    /** Data Access Object for resolving choice-based items. */
    abstract fun decisionDao(): DecisionDao

    /** Data Access Object for operator profile persistence. */
    abstract fun userDao(): UserDao

    /** Data Access Object for health tracking. */
    abstract fun medicationDao(): MedicationDao
}

/** Expected constructor for platform-specific Room implementation. */
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    /** Factory method to initialize the database instance. */
    override fun initialize(): AppDatabase
}

/**
 * Configures a Room builder with standard TajsOS driver settings.
 *
 * @param builder The base Room database builder.
 */
fun getDatabaseBuilder(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase =
    builder
        .setDriver(
            androidx.sqlite.driver.bundled
                .BundledSQLiteDriver(),
        ).build()
