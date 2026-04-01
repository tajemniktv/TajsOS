/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import com.tajemniktv.tajsos.domain.DomainKind

/**
 * Maps a persisted task facet row into a typed read model.
 */
fun TaskFacetEntity.toModel(): TaskFacet =
    TaskFacet(
        state = TaskState.fromStorageKey(state) ?: TaskState.ACTIVE,
        energyLevel = energyLevel,
        friction = friction,
        nextStep = nextStep,
        estimatedMinutes = estimatedMinutes,
        completionNote = completionNote,
        completedAt = completedAt,
        isRecurring = isRecurring,
        recurringInterval = recurringInterval,
    )

/**
 * Maps a persisted note facet row into a typed read model.
 */
fun NoteFacetEntity.toModel(): NoteFacet =
    NoteFacet(
        kind = NoteKind.fromStorageKey(kind) ?: NoteKind.GENERAL,
        state = NoteState.fromStorageKey(state) ?: NoteState.ACTIVE,
        sourceTitle = sourceTitle,
        sourceAuthor = sourceAuthor,
        lastReviewedAt = lastReviewedAt,
    )

/**
 * Maps a persisted project facet row into a typed read model.
 */
fun ProjectFacetEntity.toModel(): ProjectFacet =
    ProjectFacet(
        state = ProjectState.fromStorageKey(state) ?: ProjectState.ACTIVE,
        purpose = purpose,
        isFrozen = isFrozen,
    )

/**
 * Maps a persisted record facet row into a typed read model.
 */
fun RecordFacetEntity.toModel(): RecordFacet =
    RecordFacet(
        kind = RecordKind.fromStorageKey(kind) ?: RecordKind.GENERAL,
        occurredAt = occurredAt,
    )

/**
 * Maps a persisted area facet row into a typed read model.
 */
fun AreaFacetEntity.toModel(): AreaFacet =
    AreaFacet(
        healthStatus = AreaHealthStatus.fromStorageKey(healthStatus) ?: AreaHealthStatus.STABLE,
        standardOfCare = standardOfCare,
        vision = vision,
    )

/**
 * Maps a persisted schedule row into a typed read model.
 */
fun ScheduleEntryEntity.toModel(): ScheduleEntry =
    ScheduleEntry(
        id = id,
        itemId = itemId,
        kind = ScheduleEntryKind.fromStorageKey(kind) ?: ScheduleEntryKind.EVENT,
        scheduledAt = scheduledAt,
        localDateEpochDay = localDateEpochDay,
        timezoneId = timezoneId,
        isAllDay = isAllDay,
        endAt = endAt,
        recurrenceRule = recurrenceRule,
        note = note,
        completedAt = completedAt,
    )

/**
 * Maps a persisted domain assignment into a typed read model.
 */
fun ItemDomainEntity.toModel(): DomainAssignment? =
    DomainKind.entries
        .firstOrNull { it.name == domainKey }
        ?.let { domain ->
            DomainAssignment(
                domain = domain,
                isPrimary = isPrimary,
                assignedAt = assignedAt,
            )
        }

/**
 * Maps a persisted document row into a typed read model.
 */
fun RichContentDocumentEntity.toModel(): RichContentDocument =
    RichContentDocument(
        itemId = itemId,
        format = RichContentFormat.fromStorageKey(format) ?: RichContentFormat.MARKDOWN,
        body = body,
        structuredContentJson = structuredContentJson,
        schemaVersion = schemaVersion,
        updatedAt = updatedAt,
    )

/**
 * Maps a persisted saved-view row set into a typed read model.
 */
fun SavedViewEntity.toDefinition(
    sourceKinds: List<SavedViewSourceKindEntity>,
    filters: List<SavedViewFilterEntity>,
    sorts: List<SavedViewSortEntity>,
    visibleFields: List<SavedViewVisibleFieldEntity>,
): SavedViewDefinition =
    SavedViewDefinition(
        id = id,
        name = name,
        description = description,
        lens = SavedViewLens.fromStorageKey(lens) ?: SavedViewLens.OPERATE,
        layout = SavedViewLayout.fromStorageKey(layout) ?: SavedViewLayout.LIST,
        sourceKinds =
            sourceKinds.mapNotNull { ItemKind.fromStorageKey(it.itemKind) }
                .toSet(),
        filters =
            filters
                .sortedBy { it.position }
                .mapNotNull { filter ->
                    val fieldKey = SavedViewFieldKey.fromStorageKey(filter.fieldKey) ?: return@mapNotNull null
                    val operator = SavedViewFilterOperator.fromStorageKey(filter.operatorKey) ?: return@mapNotNull null
                    SavedViewFilter(
                        fieldKey = fieldKey,
                        operator = operator,
                        value = filter.value,
                        valueType =
                            SavedViewValueType.fromStorageKey(filter.valueType)
                                ?: SavedViewValueType.STRING,
                    )
                },
        sorts =
            sorts
                .sortedBy { it.position }
                .mapNotNull { sort ->
                    val fieldKey = SavedViewFieldKey.fromStorageKey(sort.fieldKey) ?: return@mapNotNull null
                    SavedViewSort(
                        fieldKey = fieldKey,
                        direction =
                            SavedViewSortDirection.fromStorageKey(sort.direction)
                                ?: SavedViewSortDirection.ASCENDING,
                    )
                },
        visibleFields =
            visibleFields
                .sortedBy { it.position }
                .mapNotNull { field -> SavedViewFieldKey.fromStorageKey(field.fieldKey) },
        rowDimension = SavedViewFieldKey.fromStorageKey(rowDimension),
        columnDimension = SavedViewFieldKey.fromStorageKey(columnDimension),
        measure = SavedViewMeasure.fromStorageKey(measure),
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
