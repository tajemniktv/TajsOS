/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import com.tajemniktv.tajsos.domain.DomainKind
import kotlin.coroutines.cancellation.CancellationException
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
    /** Student domain metadata if the user has the Student pack enabled. */
    val student: StudentMetadata? = null,
    /** Financial domain metadata if the user has the Finance pack enabled. */
    val finance: FinanceMetadata? = null,
    /** People domain metadata if the user has the People pack enabled. */
    val people: PeopleMetadata? = null,
    /** Creator domain metadata if the user has the Creator pack enabled. */
    val creator: CreatorMetadata? = null,
    /** Area domain associations for aggregation and filtering. */
    val area: AreaMetadata? = null,
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
 * Optional area-level metadata.
 *
 * Areas remain generic containers; this metadata provides optional domain association
 * for filtering and read-model aggregation only.
 */
@Serializable
data class AreaMetadata(
    val associatedDomains: Set<DomainKind> = emptySet(),
)

/**
 * Executes [block] and returns its decoded value, or `null` when a non-cancellation exception is thrown.
 *
 * This helper is intended for resilient parsing paths where malformed payloads should not crash the app.
 * [CancellationException] is always rethrown so coroutine cancellation is propagated and not swallowed.
 */
inline fun <T> safeDecode(block: () -> T): T? =
    try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }

/**
 * Safely decodes the underlying JSON string (`metadataJson`) into a typed [NodeMetadataEnvelope].
 *
 * Returns `null` if the string is empty, malformed, or if parsing fails.
 */
fun NodeEntity.metadataEnvelopeOrNull(): NodeMetadataEnvelope? =
    metadataJson?.takeIf { it.isNotBlank() }?.let {
        safeDecode { nodeMetadataJson.decodeFromString<NodeMetadataEnvelope>(it) }
    }

/**
 * Returns a copy of the current [NodeEntity] with its `metadataJson` field updated
 * to reflect the provided [envelope].
 *
 * If [envelope] is null, the resulting JSON string will be null (clearing the metadata).
 * Uses the pre-configured [nodeMetadataJson] to serialize the structure.
 */
fun NodeEntity.withMetadataEnvelope(envelope: NodeMetadataEnvelope?): NodeEntity =
    copy(
        metadataJson = envelope?.let { nodeMetadataJson.encodeToString(it) },
    )

/**
 * Reads optional typed [AreaMetadata] attached to this node.
 */
fun NodeEntity.areaMetadataOrNull(): AreaMetadata? = metadataEnvelopeOrNull()?.area

/**
 * Returns true when this node is associated with the provided [domain].
 */
fun NodeEntity.isAssociatedWithDomain(domain: DomainKind): Boolean = areaMetadataOrNull()?.associatedDomains?.contains(domain) == true

/**
 * Returns a copy with [domain] added to area-domain associations.
 */
fun NodeEntity.withAssociatedDomain(domain: DomainKind): NodeEntity {
    val existingEnvelope = metadataEnvelopeOrNull() ?: NodeMetadataEnvelope()
    val existingArea = existingEnvelope.area ?: AreaMetadata()
    val updatedArea =
        existingArea.copy(
            associatedDomains = existingArea.associatedDomains + domain,
        )
    return withMetadataEnvelope(existingEnvelope.copy(area = updatedArea))
}

/**
 * Returns a copy with [domain] removed from area-domain associations.
 */
fun NodeEntity.withoutAssociatedDomain(domain: DomainKind): NodeEntity {
    val existingEnvelope = metadataEnvelopeOrNull() ?: return this
    val existingArea = existingEnvelope.area ?: return this
    val updatedDomains = existingArea.associatedDomains - domain
    val updatedArea = existingArea.copy(associatedDomains = updatedDomains)
    return withMetadataEnvelope(existingEnvelope.copy(area = updatedArea))
}
