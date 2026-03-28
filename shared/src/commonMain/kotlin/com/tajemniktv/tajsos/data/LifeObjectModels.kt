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
    when (type) {
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
 * ideas/resources/open loops continue to resolve through the smaller primary object set.
 */
fun NodeEntity.matchesItemFilter(filter: String?): Boolean =
    when (filter) {
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
    when (value) {
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
    when (this) {
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
