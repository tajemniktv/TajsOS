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

@Serializable
data class NodeMetadataEnvelope(
    val schemaVersion: Int = 1,
    val student: StudentMetadata? = null,
    val finance: FinanceMetadata? = null,
    val people: PeopleMetadata? = null,
    val creator: CreatorMetadata? = null,
)

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

@Serializable
data class FinanceMetadata(
    val entryType: String? = null,
    val amount: Double? = null,
    val currency: String? = null,
    val monthBucket: String? = null,
)

@Serializable
data class PeopleMetadata(
    val personId: String? = null,
    val relationshipType: String? = null,
    val importantDateEpochMs: Long? = null,
    val followUpEpochMs: Long? = null,
)

@Serializable
data class CreatorMetadata(
    val ideaStage: String? = null,
    val releaseTag: String? = null,
    val repositoryRef: String? = null,
)

fun NodeEntity.metadataEnvelopeOrNull(): NodeMetadataEnvelope? =
    metadataJson?.takeIf { it.isNotBlank() }?.let {
        runCatching {
            nodeMetadataJson.decodeFromString<NodeMetadataEnvelope>(it)
        }.getOrNull()
    }

fun NodeEntity.withMetadataEnvelope(envelope: NodeMetadataEnvelope?): NodeEntity =
    copy(
        metadataJson = envelope?.let { nodeMetadataJson.encodeToString(it) },
    )
