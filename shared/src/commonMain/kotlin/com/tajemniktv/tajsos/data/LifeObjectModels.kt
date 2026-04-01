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
 */
@Serializable
enum class ItemKind(
    val storageKey: String,
) {
    TASK("task"),
    NOTE("note"),
    RECORD("record"),
    PROJECT("project"),
    AREA("area"),
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
 */
@Serializable
enum class TaskState(
    val storageKey: String,
) {
    ACTIVE("active"),
    DONE("done"),
    ON_HOLD("on_hold"),
    SOMEDAY("someday"),
    BLOCKED("blocked"),
    ARCHIVED("archived"),
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
    ACTIVE("active"),
    ON_HOLD("on_hold"),
    SOMEDAY("someday"),
    COMPLETED("completed"),
    ARCHIVED("archived"),
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
    GENERAL("general"),
    KNOWLEDGE("knowledge"),
    REFERENCE("reference"),
    REFLECTION("reflection"),
    JOURNAL("journal"),
    LOGISTICS("logistics"),
    CONCEPT("concept"),
    MEETING("meeting"),
    READING("reading"),
    EVERGREEN("evergreen"),
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
    ACTIVE("active"),
    RAW("raw"),
    HIGHLIGHTED("highlighted"),
    DISTILLED("distilled"),
    TAKEAWAY("takeaway"),
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
    GENERAL("general"),
    JOURNAL("journal"),
    REFLECTION("reflection"),
    HEALTH_LOG("health_log"),
    CONTACT_LOG("contact_log"),
    SESSION_LOG("session_log"),
    EVENT_LOG("event_log"),
    SYMPTOM_LOG("symptom_log"),
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
    ACTIVE("active"),
    STABLE("stable"),
    NEGLECTED("neglected"),
    OVERLOADED("overloaded"),
    ON_FIRE("on_fire"),
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
    START("start"),
    DUE("due"),
    REMINDER("reminder"),
    EVENT("event"),
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
    MARKDOWN("markdown"),
    PLAIN_TEXT("plain_text"),
    BLOCKS_JSON("blocks_json"),
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
    RELATED("RELATED"),
    MENTION("MENTION"),
    DEPENDS_ON("DEPENDS_ON"),
    BELONGS_TO("BELONGS_TO"),
    REFERENCE("REFERENCE"),
    DERIVED_FROM("DERIVED_FROM"),
    INSPIRED_BY("INSPIRED_BY"),
    RELATED_PERSON("RELATED_PERSON"),
    PLACE_CONTEXT("PLACE_CONTEXT"),
    TOPIC_LINK("TOPIC_LINK"),
    PAPER_REFERENCE("PAPER_REFERENCE"),
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
    NOW("now"),
    PLAN("plan"),
    OPERATE("operate"),
    KNOWLEDGE("knowledge"),
    REVIEW("review"),
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
    LIST("list"),
    TABLE("table"),
    BOARD("board"),
    MATRIX("matrix"),
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
    TITLE("title"),
    STATUS("status"),
    KIND("kind"),
    AREA("area"),
    PROJECT("project"),
    DOMAIN("domain"),
    DUE_DATE("due_date"),
    START_DATE("start_date"),
    OCCURRED_AT("occurred_at"),
    CREATED_AT("created_at"),
    UPDATED_AT("updated_at"),
    TAG("tag"),
    PINNED("pinned"),
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
    COUNT("count"),
    ESTIMATED_MINUTES("estimated_minutes"),
    COMPLETED_COUNT("completed_count"),
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
    EQUALS("equals"),
    NOT_EQUALS("not_equals"),
    IN_SET("in_set"),
    CONTAINS("contains"),
    BEFORE("before"),
    AFTER("after"),
    IS_TRUE("is_true"),
    IS_FALSE("is_false"),
    IS_NULL("is_null"),
    IS_NOT_NULL("is_not_null"),
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
    STRING("string"),
    NUMBER("number"),
    BOOLEAN("boolean"),
    DATE("date"),
    ENUM("enum"),
    REFERENCE("reference"),
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
    ASCENDING("asc"),
    DESCENDING("desc"),
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

fun NodeWithPin.isTaskItem(): Boolean = node.isTaskItem()

fun NodeWithPin.isKnowledgeItem(): Boolean = node.isKnowledgeItem()

fun NodeWithPin.isAreaItem(): Boolean = node.isAreaItem()

fun NodeWithPin.isRelationshipAnchor(): Boolean = node.isRelationshipAnchor()

fun NodeWithPin.isPlaceAnchor(): Boolean = node.isPlaceAnchor()

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
 */
@Serializable
data class ProjectFacet(
    val state: ProjectState,
    val purpose: String? = null,
    val isFrozen: Boolean = false,
)

/**
 * Typed record facet returned by repository read models.
 */
@Serializable
data class RecordFacet(
    val kind: RecordKind,
    val occurredAt: Long,
)

/**
 * Typed area facet returned by repository read models.
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
