/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import kotlinx.serialization.Serializable

/**
 * The small, product-level set of primary life objects supported by TajsOS.
 *
 * These kinds intentionally stay narrow so the product can build rich lenses
 * and workflows without exploding into a large flat list of peer object types.
 *
 * This enum maps directly to the underlying `type` string column in `NodeEntity`
 * (e.g., "task", "note", "record", "project", "area"). This guarantees type safety
 * when querying or filtering core object kinds throughout the app.
 */
@Serializable
enum class ItemKind(
    val storageKey: String,
) {
    /** Represents actionable work to be completed. */
    TASK("task"), // NON-NLS
    /** Represents durable knowledge or reference material. */
    NOTE("note"), // NON-NLS
    /** Represents a chronological log or reflection. */
    RECORD("record"), // NON-NLS
    /** Represents an outcome-bearing endeavor. */
    PROJECT("project"), // NON-NLS
    /** Represents an ongoing area of responsibility. */
    AREA("area"), // NON-NLS
    ;

    companion object {
        /**
         * Resolves a persisted storage key into an [ItemKind].
         */
        fun fromStorageKey(value: String?): ItemKind? = entries.firstOrNull { it.storageKey == value }
    }
}

/**
 * Canonical execution states for task-shaped work.
 *
 * This enum maps directly to the underlying `status` string column in `NodeEntity`
 * when the node represents an actionable task. It represents the task's lifecycle
 * phase, allowing UI layers to easily sort, filter, and display tasks in boards or lists.
 */
@Serializable
enum class TaskState(
    val storageKey: String,
) {
    ACTIVE("active"), // NON-NLS
    DONE("done"), // NON-NLS
    ON_HOLD("on_hold"), // NON-NLS
    SOMEDAY("someday"), // NON-NLS
    BLOCKED("blocked"), // NON-NLS
    ARCHIVED("archived"), // NON-NLS
    ;

    companion object {
        /**
         * Resolves a persisted storage key into a [TaskState].
         */
        fun fromStorageKey(value: String?): TaskState? = entries.firstOrNull { it.storageKey == value }
    }
}

/**
 * Canonical lifecycle states for outcome-bearing projects.
 */
@Serializable
enum class ProjectState(
    val storageKey: String,
) {
    ACTIVE("active"), // NON-NLS
    ON_HOLD("on_hold"), // NON-NLS
    SOMEDAY("someday"), // NON-NLS
    COMPLETED("completed"), // NON-NLS
    ARCHIVED("archived"), // NON-NLS
    ;

    companion object {
        /**
         * Resolves a persisted storage key into a [ProjectState].
         */
        fun fromStorageKey(value: String?): ProjectState? = entries.firstOrNull { it.storageKey == value }
    }
}

/**
 * Typed note flavors used by new LifeOS code.
 */
@Serializable
enum class NoteKind(
    val storageKey: String,
) {
    GENERAL("general"), // NON-NLS
    KNOWLEDGE("knowledge"), // NON-NLS
    REFERENCE("reference"), // NON-NLS
    REFLECTION("reflection"), // NON-NLS
    JOURNAL("journal"), // NON-NLS
    LOGISTICS("logistics"), // NON-NLS
    CONCEPT("concept"), // NON-NLS
    MEETING("meeting"), // NON-NLS
    READING("reading"), // NON-NLS
    EVERGREEN("evergreen"), // NON-NLS
    ;

    companion object {
        /**
         * Resolves a persisted storage key into a [NoteKind].
         */
        fun fromStorageKey(value: String?): NoteKind? = entries.firstOrNull { it.storageKey == value }
    }
}

/**
 * Typed note maturity states for longer-form knowledge work.
 */
@Serializable
enum class NoteState(
    val storageKey: String,
) {
    ACTIVE("active"), // NON-NLS
    RAW("raw"), // NON-NLS
    HIGHLIGHTED("highlighted"), // NON-NLS
    DISTILLED("distilled"), // NON-NLS
    TAKEAWAY("takeaway"), // NON-NLS
    ;

    companion object {
        /**
         * Resolves a persisted storage key into a [NoteState].
         */
        fun fromStorageKey(value: String?): NoteState? = entries.firstOrNull { it.storageKey == value }
    }
}

/**
 * Typed record flavors for chronological, lived experience data.
 */
@Serializable
enum class RecordKind(
    val storageKey: String,
) {
    GENERAL("general"), // NON-NLS
    JOURNAL("journal"), // NON-NLS
    REFLECTION("reflection"), // NON-NLS
    HEALTH_LOG("health_log"), // NON-NLS
    CONTACT_LOG("contact_log"), // NON-NLS
    SESSION_LOG("session_log"), // NON-NLS
    EVENT_LOG("event_log"), // NON-NLS
    SYMPTOM_LOG("symptom_log"), // NON-NLS
    ;

    companion object {
        /**
         * Resolves a persisted storage key into a [RecordKind].
         */
        fun fromStorageKey(value: String?): RecordKind? = entries.firstOrNull { it.storageKey == value }
    }
}

/**
 * Typed stewardship status for areas of responsibility.
 */
@Serializable
enum class AreaHealthStatus(
    val storageKey: String,
) {
    ACTIVE("active"), // NON-NLS
    STABLE("stable"), // NON-NLS
    NEGLECTED("neglected"), // NON-NLS
    OVERLOADED("overloaded"), // NON-NLS
    ON_FIRE("on_fire"), // NON-NLS
    ;

    companion object {
        /**
         * Resolves a persisted storage key into an [AreaHealthStatus].
         */
        fun fromStorageKey(value: String?): AreaHealthStatus? = entries.firstOrNull { it.storageKey == value }
    }
}

/**
 * Typed schedule layers that can attach time to any life object.
 */
@Serializable
enum class ScheduleEntryKind(
    val storageKey: String,
) {
    START("start"), // NON-NLS
    DUE("due"), // NON-NLS
    REMINDER("reminder"), // NON-NLS
    EVENT("event"), // NON-NLS
    ;

    companion object {
        /**
         * Resolves a persisted storage key into a [ScheduleEntryKind].
         */
        fun fromStorageKey(value: String?): ScheduleEntryKind? = entries.firstOrNull { it.storageKey == value }
    }
}

/**
 * Supported rich-content payload formats for life-object documents.
 */
@Serializable
enum class RichContentFormat(
    val storageKey: String,
) {
    MARKDOWN("markdown"), // NON-NLS
    PLAIN_TEXT("plain_text"), // NON-NLS
    BLOCKS_JSON("blocks_json"), // NON-NLS
    ;

    companion object {
        /**
         * Resolves a persisted storage key into a [RichContentFormat].
         */
        fun fromStorageKey(value: String?): RichContentFormat? = entries.firstOrNull { it.storageKey == value }
    }
}

/**
 * Typed relation vocabulary for current and future LifeOS graph links.
 */
@Serializable
enum class RelationKind(
    val storageKey: String,
) {
    RELATED("RELATED"), // NON-NLS
    MENTION("MENTION"), // NON-NLS
    DEPENDS_ON("DEPENDS_ON"), // NON-NLS
    BELONGS_TO("BELONGS_TO"), // NON-NLS
    REFERENCE("REFERENCE"), // NON-NLS
    DERIVED_FROM("DERIVED_FROM"), // NON-NLS
    INSPIRED_BY("INSPIRED_BY"), // NON-NLS
    RELATED_PERSON("RELATED_PERSON"), // NON-NLS
    PLACE_CONTEXT("PLACE_CONTEXT"), // NON-NLS
    TOPIC_LINK("TOPIC_LINK"), // NON-NLS
    PAPER_REFERENCE("PAPER_REFERENCE"), // NON-NLS
    ;

    companion object {
        /**
         * Resolves a persisted storage key into a [RelationKind].
         */
        fun fromStorageKey(value: String?): RelationKind? = entries.firstOrNull { it.storageKey == value }
    }
}

/**
 * Product lenses supported by saved local projections.
 */
@Serializable
enum class SavedViewLens(
    val storageKey: String,
) {
    /** Projection focused on immediate, pressing tasks and currently active states. */
    NOW("now"), // NON-NLS
    /** Projection focused on scheduling, future commitments, and time architecture. */
    PLAN("plan"), // NON-NLS
    /** Projection focused on executing tasks, protocols, and standard operating procedures. */
    OPERATE("operate"), // NON-NLS
    /** Projection focused on durable information, references, reflections, and records. */
    KNOWLEDGE("knowledge"), // NON-NLS
    /** Projection focused on reflecting on past performance, metrics, and logs. */
    REVIEW("review"), // NON-NLS
    ;

    companion object {
        /**
         * Resolves a persisted storage key into a [SavedViewLens].
         */
        fun fromStorageKey(value: String?): SavedViewLens? = entries.firstOrNull { it.storageKey == value }
    }
}

/**
 * Saved-view layouts supported by the current local model.
 */
@Serializable
enum class SavedViewLayout(
    val storageKey: String,
) {
    /** Displays items in a vertical, scrollable list. */
    LIST("list"), // NON-NLS
    /** Displays items in a structured, multi-column grid or table. */
    TABLE("table"), // NON-NLS
    /** Displays items categorized into columns (e.g., Kanban style). */
    BOARD("board"), // NON-NLS
    /** Displays items in a multi-dimensional matrix, often used for cross-referencing metrics. */
    MATRIX("matrix"), // NON-NLS
    ;

    companion object {
        /**
         * Resolves a persisted storage key into a [SavedViewLayout].
         */
        fun fromStorageKey(value: String?): SavedViewLayout? = entries.firstOrNull { it.storageKey == value }
    }
}

/**
 * Canonical field vocabulary for saved-view filters, grouping, and visible columns.
 */
@Serializable
enum class SavedViewFieldKey(
    val storageKey: String,
) {
    /** The primary title or name of the item. */
    TITLE("title"), // NON-NLS
    /** The active, completed, or archived status of the item. */
    STATUS("status"), // NON-NLS
    /** The fundamental type or kind of the item (e.g., task, note). */
    KIND("kind"), // NON-NLS
    /** The area of responsibility the item belongs to. */
    AREA("area"), // NON-NLS
    /** The project the item is associated with. */
    PROJECT("project"), // NON-NLS
    /** The life domain (e.g., finance, health) the item relates to. */
    DOMAIN("domain"), // NON-NLS
    /** The deadline or target completion date for the item. */
    DUE_DATE("due_date"), // NON-NLS
    /** The date work on the item is scheduled to begin. */
    START_DATE("start_date"), // NON-NLS
    /** The timestamp when an event or log occurred. */
    OCCURRED_AT("occurred_at"), // NON-NLS
    /** The timestamp when the item was originally created. */
    CREATED_AT("created_at"), // NON-NLS
    /** The timestamp of the last modification to the item. */
    UPDATED_AT("updated_at"), // NON-NLS
    /** A user-defined tag or label attached to the item. */
    TAG("tag"), // NON-NLS
    /** Whether the item is pinned or highlighted in the current view. */
    PINNED("pinned"), // NON-NLS
    ;

    companion object {
        /**
         * Resolves a persisted storage key into a [SavedViewFieldKey].
         */
        fun fromStorageKey(value: String?): SavedViewFieldKey? = entries.firstOrNull { it.storageKey == value }
    }
}

/**
 * Aggregations supported by matrix/table projections.
 */
@Serializable
enum class SavedViewMeasure(
    val storageKey: String,
) {
    /** The total raw count of items matching the criteria. */
    COUNT("count"), // NON-NLS
    /** The aggregated total of estimated minutes across matching items. */
    ESTIMATED_MINUTES("estimated_minutes"), // NON-NLS
    /** The count of items that have reached a completed state. */
    COMPLETED_COUNT("completed_count"), // NON-NLS
    ;

    companion object {
        /**
         * Resolves a persisted storage key into a [SavedViewMeasure].
         */
        fun fromStorageKey(value: String?): SavedViewMeasure? = entries.firstOrNull { it.storageKey == value }
    }
}

/**
 * Supported filter operators for saved local projections.
 */
@Serializable
enum class SavedViewFilterOperator(
    val storageKey: String,
) {
    /** Matches if the field exactly equals the filter value. */
    EQUALS("equals"), // NON-NLS
    /** Matches if the field does not equal the filter value. */
    NOT_EQUALS("not_equals"), // NON-NLS
    /** Matches if the field value is present within the provided set of values. */
    IN_SET("in_set"), // NON-NLS
    /** Matches if the field contains the filter value (typically used for text). */
    CONTAINS("contains"), // NON-NLS
    /** Matches if the field's date/time is before the filter value. */
    BEFORE("before"), // NON-NLS
    /** Matches if the field's date/time is after the filter value. */
    AFTER("after"), // NON-NLS
    /** Matches if the boolean field is true. */
    IS_TRUE("is_true"), // NON-NLS
    /** Matches if the boolean field is false. */
    IS_FALSE("is_false"), // NON-NLS
    /** Matches if the field does not hold any value. */
    IS_NULL("is_null"), // NON-NLS
    /** Matches if the field holds any value. */
    IS_NOT_NULL("is_not_null"), // NON-NLS
    ;

    companion object {
        /**
         * Resolves a persisted storage key into a [SavedViewFilterOperator].
         */
        fun fromStorageKey(value: String?): SavedViewFilterOperator? = entries.firstOrNull { it.storageKey == value }
    }
}

/**
 * Serialized value shapes for saved-view filters.
 */
@Serializable
enum class SavedViewValueType(
    val storageKey: String,
) {
    /** A plain text value. */
    STRING("string"), // NON-NLS
    /** A numeric value. */
    NUMBER("number"), // NON-NLS
    /** A true/false boolean value. */
    BOOLEAN("boolean"), // NON-NLS
    /** A date or timestamp value. */
    DATE("date"), // NON-NLS
    /** A value constrained to a specific set of predefined options. */
    ENUM("enum"), // NON-NLS
    /** A value that references another entity's unique identifier. */
    REFERENCE("reference"), // NON-NLS
    ;

    companion object {
        /**
         * Resolves a persisted storage key into a [SavedViewValueType].
         */
        fun fromStorageKey(value: String?): SavedViewValueType? = entries.firstOrNull { it.storageKey == value }
    }
}

/**
 * Supported sort directions for saved-view ordering.
 */
@Serializable
enum class SavedViewSortDirection(
    val storageKey: String,
) {
    /** Sorts from lowest to highest, or alphabetically A to Z. */
    ASCENDING("asc"), // NON-NLS
    /** Sorts from highest to lowest, or alphabetically Z to A. */
    DESCENDING("desc"), // NON-NLS
    ;

    companion object {
        /**
         * Resolves a persisted storage key into a [SavedViewSortDirection].
         */
        fun fromStorageKey(value: String?): SavedViewSortDirection? = entries.firstOrNull { it.storageKey == value }
    }
}

/**
 * Returns the default inbox posture for a newly created item kind.
 */
fun ItemKind.defaultInboxState(): Boolean = this != ItemKind.PROJECT && this != ItemKind.AREA

/**
 * Maps the current legacy node type vocabulary to the new core [ItemKind] set.
 *
 * This lets new code reason about the smaller LifeOS object model while legacy
 * screens and calculators continue to read `NodeEntity.type`.
 */
fun legacyNodeTypeToItemKind(type: String?): ItemKind? =
    when (type)
    {
        "task",
        "open_loop",
        "maintenance",
        "decision",
        "protocol",
        -> ItemKind.TASK

        "note",
        "idea",
        "resource",
        "rule",
        "principle",
        "vault",
        "document",
        -> ItemKind.NOTE

        "record" -> ItemKind.RECORD

        "project" -> ItemKind.PROJECT

        "area" -> ItemKind.AREA

        else -> null
    }

/**
 * Convenience projection from the current node surface into the new core item set.
 */
fun NodeEntity.itemKindOrNull(): ItemKind? = legacyNodeTypeToItemKind(type)

/**
 * Returns true when the node represents task-shaped work in the new model.
 */
fun NodeEntity.isTaskItem(): Boolean = itemKindOrNull() == ItemKind.TASK

/**
 * Returns true when the node represents durable knowledge or planning text.
 */
fun NodeEntity.isNoteItem(): Boolean = itemKindOrNull() == ItemKind.NOTE

/**
 * Returns true when the node represents chronological or reflective records.
 */
fun NodeEntity.isRecordItem(): Boolean = itemKindOrNull() == ItemKind.RECORD

/**
 * Returns true when the node belongs to the Knowledge lens.
 */
fun NodeEntity.isKnowledgeItem(): Boolean = isNoteItem() || isRecordItem()

/**
 * Returns true when the node represents an outcome-bearing project.
 */
fun NodeEntity.isProjectItem(): Boolean = itemKindOrNull() == ItemKind.PROJECT

/**
 * Returns true when the node represents an enduring area of responsibility.
 */
fun NodeEntity.isAreaItem(): Boolean = itemKindOrNull() == ItemKind.AREA

/**
 * Returns true when the node behaves as a tracked relationship anchor.
 */
fun NodeEntity.isRelationshipAnchor(): Boolean =
    type == "person" ||
        !relationshipContext.isNullOrBlank() ||
        !socialEnergyNotes.isNullOrBlank() ||
        lastContactAt != null ||
        tagsHintContainsRelationship()

/**
 * Returns true when the node behaves as a physical context or place anchor.
 */
fun NodeEntity.isPlaceAnchor(): Boolean = type == "place"

/**
 * Returns true when task-shaped work is specifically about making or revisiting a decision.
 */
fun NodeEntity.isDecisionSupportItem(): Boolean =
    isTaskItem() &&
        (
            type == "decision" ||
                openLoopType == "pending_decision" ||
                decisionStatus != null ||
                decisionCategory != null ||
                decisionRevisitAt != null ||
                !decisionInfoMissing.isNullOrBlank() ||
                !decisionDifficultBecause.isNullOrBlank() ||
                !decisionEasierIf.isNullOrBlank()
        )

/**
 * Returns true when decision-shaped work has already been resolved or logged.
 */
fun NodeEntity.isResolvedDecisionSupportItem(): Boolean =
    isDecisionSupportItem() &&
        (
            decisionStatus in setOf("decided", "expired") ||
                status == "done"
        )

/**
 * Convenience projections for node wrappers consumed by the UI.
 */
fun NodeWithPin.itemKindOrNull(): ItemKind? = node.itemKindOrNull()

/**
 * Returns true when the node represents task-shaped work in the new model.
 */
fun NodeWithPin.isTaskItem(): Boolean = node.isTaskItem()

/**
 * Returns true when the node belongs to the Knowledge lens.
 */
fun NodeWithPin.isKnowledgeItem(): Boolean = node.isKnowledgeItem()

/**
 * Returns true when the node represents an enduring area of responsibility.
 */
fun NodeWithPin.isAreaItem(): Boolean = node.isAreaItem()

/**
 * Returns true when the node behaves as a tracked relationship anchor.
 */
fun NodeWithPin.isRelationshipAnchor(): Boolean = node.isRelationshipAnchor()

/**
 * Returns true when the node behaves as a physical context or place anchor.
 */
fun NodeWithPin.isPlaceAnchor(): Boolean = node.isPlaceAnchor()

/**
 * Returns true when task-shaped work is specifically about making or revisiting a decision.
 */
fun NodeWithPin.isDecisionSupportItem(): Boolean = node.isDecisionSupportItem()

private fun NodeEntity.tagsHintContainsRelationship(): Boolean =
    listOfNotNull(relationshipContext, socialEnergyNotes)
        .any { text ->
            listOf("friend", "family", "professor", "relationship", "contact").any {
                text.contains(it, ignoreCase = true)
            }
        }

/**
 * Matches a UI-facing kind filter against the collapsed LifeOS object model.
 *
 * Search and navigation filters should prefer this helper so legacy subtypes like
 * Legacy idea/resource/unresolved-work variants continue to resolve through the smaller primary object set.
 */
fun NodeEntity.matchesItemFilter(filter: String?): Boolean =
    when (filter)
    {
        null -> true
        "task" -> isTaskItem()
        "note" -> isNoteItem()
        "record" -> isRecordItem()
        "project" -> isProjectItem()
        "area" -> isAreaItem()
        else -> type == filter
    }

/**
 * Resolves the canonical task state from current node status values.
 */
fun taskStateFromNodeStatus(value: String?): TaskState? = TaskState.fromStorageKey(value)

/**
 * Resolves the canonical project state from current node status values.
 */
fun projectStateFromNodeStatus(value: String?): ProjectState? =
    when (value)
    {
        "done",
        "completed",
        -> ProjectState.COMPLETED

        "archived" -> ProjectState.ARCHIVED

        else -> ProjectState.fromStorageKey(value)
    }

/**
 * Reads a typed task state from the legacy node surface when possible.
 */
fun NodeEntity.taskStateOrNull(): TaskState? = if (isTaskItem()) taskStateFromNodeStatus(status) else null

/**
 * Reads a typed project state from the legacy node surface when possible.
 */
fun NodeEntity.projectStateOrNull(): ProjectState? =
    if (isProjectItem()) {
        projectStateFromNodeStatus(projectStatus ?: status)
    } else {
        null
    }

/**
 * Converts a task state into the existing node status vocabulary.
 */
fun TaskState.toNodeStatus(): String = storageKey

/**
 * Converts a project state into the existing node status vocabulary.
 */
fun ProjectState.toNodeStatus(): String =
    when (this)
    {
        ProjectState.ACTIVE -> "active"
        ProjectState.ON_HOLD -> "on_hold"
        ProjectState.SOMEDAY -> "someday"
        ProjectState.COMPLETED -> "done"
        ProjectState.ARCHIVED -> "archived"
    }

/**
 * Parsed representation of raw captured text.
 *
 * Title is the first non-blank line; remaining lines become content.
 */
data class ParsedCaptureText(
    val title: String,
    val content: String,
)

/**
 * Typed task facet returned by repository read models.
 *
 * @property state The execution state of the task (e.g., ACTIVE, DONE).
 * @property energyLevel An optional integer representing the energy required to complete the task.
 * @property friction An optional text describing any resistance or blockers.
 * @property nextStep An optional concrete next action to move the task forward.
 * @property estimatedMinutes An optional estimated duration for the task in minutes.
 * @property completionNote An optional note added when the task is completed.
 * @property completedAt An optional timestamp (epoch milliseconds) of when the task was completed.
 * @property isRecurring Indicates if the task repeats.
 * @property recurringInterval The recurrence rule/interval string if the task is recurring.
 */
@Serializable
data class TaskFacet(
    val state: TaskState,
    val energyLevel: Int? = null,
    val friction: String? = null,
    val nextStep: String? = null,
    val estimatedMinutes: Int? = null,
    val completionNote: String? = null,
    val completedAt: Long? = null,
    val isRecurring: Boolean = false,
    val recurringInterval: String? = null,
)

/**
 * Typed note facet returned by repository read models.
 *
 * @property kind The category or type of the note (e.g., GENERAL, MEETING).
 * @property state The lifecycle state of the note.
 * @property sourceTitle An optional title of the source material this note references.
 * @property sourceAuthor An optional author of the source material.
 * @property lastReviewedAt An optional timestamp (epoch milliseconds) of when the note was last reviewed.
 */
@Serializable
data class NoteFacet(
    val kind: NoteKind,
    val state: NoteState,
    val sourceTitle: String? = null,
    val sourceAuthor: String? = null,
    val lastReviewedAt: Long? = null,
)

/**
 * Typed project facet returned by repository read models.
 *
 * @property state The execution state of the project (e.g., ACTIVE, COMPLETED).
 * @property purpose An optional description of the project's goal or desired outcome.
 * @property isFrozen Indicates if the project is currently suspended or on hold.
 */
@Serializable
data class ProjectFacet(
    val state: ProjectState,
    val purpose: String? = null,
    val isFrozen: Boolean = false,
)

/**
 * Typed record facet returned by repository read models.
 *
 * @property kind The specific category of the record (e.g., GENERAL, HEALTH).
 * @property occurredAt The timestamp (epoch milliseconds) when the recorded event occurred.
 */
@Serializable
data class RecordFacet(
    val kind: RecordKind,
    val occurredAt: Long,
)

/**
 * Typed area facet returned by repository read models.
 *
 * @property healthStatus The current status/health of the area (e.g., STABLE, ATTENTION_NEEDED).
 * @property standardOfCare An optional description defining what "good" looks like for this area.
 * @property vision An optional long-term vision or desired state for this area.
 */
@Serializable
data class AreaFacet(
    val healthStatus: AreaHealthStatus,
    val standardOfCare: String? = null,
    val vision: String? = null,
)

/**
 * Typed schedule entry returned by repository read models.
 */
@Serializable
data class ScheduleEntry(
    val id: Long,
    val itemId: Long,
    val kind: ScheduleEntryKind,
    val scheduledAt: Long,
    val localDateEpochDay: Int? = null,
    val timezoneId: String? = null,
    val isAllDay: Boolean = false,
    val endAt: Long? = null,
    val recurrenceRule: String? = null,
    val note: String? = null,
    val completedAt: Long? = null,
)

/**
 * Typed domain assignment returned by repository read models.
 */
@Serializable
data class DomainAssignment(
    val domain: com.tajemniktv.tajsos.domain.DomainKind,
    val isPrimary: Boolean = false,
    val assignedAt: Long,
)

/**
 * Typed rich-content document returned by repository read models.
 */
@Serializable
data class RichContentDocument(
    val itemId: Long,
    val format: RichContentFormat,
    val body: String,
    val structuredContentJson: String? = null,
    val schemaVersion: Int = 1,
    val updatedAt: Long,
)

/**
 * Typed saved-view filter returned by repository read models.
 */
@Serializable
data class SavedViewFilter(
    val fieldKey: SavedViewFieldKey,
    val operator: SavedViewFilterOperator,
    val value: String? = null,
    val valueType: SavedViewValueType = SavedViewValueType.STRING,
)

/**
 * Typed saved-view sort returned by repository read models.
 */
@Serializable
data class SavedViewSort(
    val fieldKey: SavedViewFieldKey,
    val direction: SavedViewSortDirection = SavedViewSortDirection.ASCENDING,
)

/**
 * Typed saved-view definition returned by repository read models.
 */
@Serializable
data class SavedViewDefinition(
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val lens: SavedViewLens = SavedViewLens.OPERATE,
    val layout: SavedViewLayout = SavedViewLayout.LIST,
    val sourceKinds: Set<ItemKind> = emptySet(),
    val filters: List<SavedViewFilter> = emptyList(),
    val sorts: List<SavedViewSort> = emptyList(),
    val visibleFields: List<SavedViewFieldKey> = emptyList(),
    val rowDimension: SavedViewFieldKey? = null,
    val columnDimension: SavedViewFieldKey? = null,
    val measure: SavedViewMeasure? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
)

/**
 * Aggregated local read model for a typed life object.
 */
@Serializable
data class LifeObjectAggregate(
    val node: NodeEntity,
    val task: TaskFacet? = null,
    val note: NoteFacet? = null,
    val record: RecordFacet? = null,
    val project: ProjectFacet? = null,
    val area: AreaFacet? = null,
    val schedule: List<ScheduleEntry> = emptyList(),
    val document: RichContentDocument? = null,
    val domains: List<DomainAssignment> = emptyList(),
    val tags: List<TagEntity> = emptyList(),
    val attachments: List<AttachmentEntity> = emptyList(),
    val relations: List<RelationEntity> = emptyList(),
) {
    /**
     * The canonical kind for the aggregate when the backing node maps cleanly into the spine.
     */
    val kind: ItemKind? get() = node.itemKindOrNull()
}

/**
 * Splits a raw capture into an item title plus optional body content.
 */
fun parseCapturedText(rawText: String): ParsedCaptureText {
    val lines =
        rawText
            .lines()
            .map { it.trim() }
            .dropWhile { it.isBlank() }

    if (lines.isEmpty()) {
        return ParsedCaptureText(title = "", content = "")
    }

    val title = lines.first()
    val content = lines.drop(1).joinToString("\n").trim()
    return ParsedCaptureText(title = title, content = content)
}
