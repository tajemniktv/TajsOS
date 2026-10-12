/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Version emitted by the node-only JSON exporter. Historical supported versions remain readable. */
const val LEGACY_EXPORT_VERSION = 2

private val legacyExportVersions = 1..LEGACY_EXPORT_VERSION
private val versionProbeJson = Json { ignoreUnknownKeys = true }

/** Decodes only top-level version metadata; unknown tables are skipped without building a JSON tree. */
@Serializable
private data class ImportVersionMetadata(
    val schemaVersion: Int? = null,
    @SerialName("version") val legacyVersion: Int? = null,
) {
    /** Resolves the relevant format boundary without treating bundle metadata as a legacy export. */
    fun unsupportedVersion(): Int? {
        val declaredVersion = schemaVersion ?: legacyVersion ?: return null
        val supportedVersions = if (schemaVersion != null) 1..EXPORT_SCHEMA_VERSION else legacyExportVersions
        return declaredVersion.takeUnless { it in supportedVersions }
    }
}

/** Legacy node-only export contract; its JSON shape remains unchanged. */
@Serializable
data class ExportData(val version: Int, val nodes: List<NodeEntity>)

/** Typed import feedback so each UI can localize results without parsing diagnostic text. */
sealed interface DataImportResult {
    /** A full supported backup was restored; [report] describes the imported root rows. */
    data class Bundle(val report: ImportReport) : DataImportResult
    /** A supported node-only export was restored, with node-derived companion state hydrated. */
    data class Legacy(val nodes: Int) : DataImportResult
    /** The declared bundle or legacy version is unsupported; no writes were attempted. */
    data class UnsupportedSchema(val version: Int) : DataImportResult
    /** The submitted text contained no JSON; no writes were attempted. */
    data object EmptyPayload : DataImportResult
    /** The payload does not strictly match a supported export contract; no writes were attempted. */
    data object InvalidFormat : DataImportResult
}

/** Checks the bundle version before strict decoding, then invokes transactional restore for supported exports. */
suspend fun AppRepository.importJson(payload: String): DataImportResult {
    val content = payload.trim()
    if (content.isEmpty()) return DataImportResult.EmptyPayload
    val metadata = safeDecode { versionProbeJson.decodeFromString<ImportVersionMetadata>(content) }
        ?: return DataImportResult.InvalidFormat
    metadata.unsupportedVersion()?.let { return DataImportResult.UnsupportedSchema(it) }
    val bundle = safeDecode { Json.decodeFromString<ExportBundle>(content) }
    if (bundle != null) {
        return DataImportResult.Bundle(importBundle(bundle))
    }
    val legacy = safeDecode { Json.decodeFromString<ExportData>(content) }
        ?: return DataImportResult.InvalidFormat
    return DataImportResult.Legacy(importLegacyNodes(legacy.nodes))
}
