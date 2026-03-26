/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val nodeMetadataJson =
    Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

/**
 * Envelope for modular, typed metadata attached to [NodeEntity].
 *
 * This structure allows TajsOS to attach domain-specific data (like student grades,
 * or financial records) to the core Node model without bloating the primary SQL schema.
 * It is serialized to JSON and stored in `NodeEntity.metadataJson`.
 *
 * @property schemaVersion Used for future migrations if the envelope structure changes significantly.
 */
@Serializable
data class NodeMetadataEnvelope(
    val schemaVersion: Int = 1,
    val student: StudentMetadata? = null,
    val finance: FinanceMetadata? = null,
    val people: PeopleMetadata? = null,
    val creator: CreatorMetadata? = null,
)

/**
 * Metadata pack for student-related nodes (courses, assignments, exams).
 */
@Serializable
data class StudentMetadata(
    val courseId: String? = null,
    val courseName: String? = null,
    val assignmentType: String? = null,
    val weight: Double? = null,
    val gradePercent: Double? = null,
    val semester: String? = null,
    val topic: String? = null,
    val readingProgressPercent: Int? = null,
    val masteryPercent: Int? = null,
    val flashcardCandidate: Boolean = false,
    val revisitBeforeExam: Boolean = false,
)

/**
 * Metadata pack for financial tracking (expenses, income, budgets).
 */
@Serializable
data class FinanceMetadata(
    val entryType: String? = null,
    val amount: Double? = null,
    val currency: String? = null,
    val monthBucket: String? = null,
)

/**
 * Metadata pack for relationship and CRM features.
 */
@Serializable
data class PeopleMetadata(
    val personId: String? = null,
    val relationshipType: String? = null,
    val importantDateEpochMs: Long? = null,
    val followUpEpochMs: Long? = null,
)

/**
 * Metadata pack for content creation, development, and publishing pipelines.
 */
@Serializable
data class CreatorMetadata(
    val ideaStage: String? = null,
    val releaseTag: String? = null,
    val repositoryRef: String? = null,
)

/**
 * Safely deserializes the `metadataJson` field into a [NodeMetadataEnvelope].
 *
 * It silently ignores parsing errors or unknown keys (via [nodeMetadataJson] configuration),
 * returning null if the payload is malformed or empty. This ensures that corrupt metadata
 * does not crash the UI.
 */
fun NodeEntity.metadataEnvelopeOrNull(): NodeMetadataEnvelope? =
    metadataJson?.takeIf { it.isNotBlank() }?.let {
        runCatching {
            nodeMetadataJson.decodeFromString<NodeMetadataEnvelope>(it)
        }.getOrNull()
    }

/**
 * Returns a copy of the current [NodeEntity] with its `metadataJson` field updated
 * to reflect the provided [envelope].
 *
 * If [envelope] is null, the resulting JSON string will be null (clearing the metadata).
 */
fun NodeEntity.withMetadataEnvelope(envelope: NodeMetadataEnvelope?): NodeEntity =
    copy(
        metadataJson = envelope?.let { nodeMetadataJson.encodeToString(it) },
    )
