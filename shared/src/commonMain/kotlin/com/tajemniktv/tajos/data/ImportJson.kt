/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable

/** Legacy node-only export contract; its JSON shape remains unchanged. */
@Serializable
data class ExportData(val version: Int, val nodes: List<NodeEntity>)

/** Typed import feedback so each UI can localize results without parsing diagnostic text. */
sealed interface DataImportResult {
    data class Bundle(val report: ImportReport) : DataImportResult
    data class Legacy(val nodes: Int) : DataImportResult
    data class UnsupportedSchema(val version: Int) : DataImportResult
    data object EmptyPayload : DataImportResult
    data object InvalidFormat : DataImportResult
}

/** Decodes supported local exports before invoking the existing transactional restore contract. */
suspend fun AppRepository.importJson(payload: String): DataImportResult {
    val content = payload.trim()
    if (content.isEmpty()) return DataImportResult.EmptyPayload
    val bundle = safeDecode { Json.decodeFromString<ExportBundle>(content) }
    if (bundle != null) {
        if (bundle.schemaVersion !in 1..EXPORT_SCHEMA_VERSION) {
            return DataImportResult.UnsupportedSchema(bundle.schemaVersion)
        }
        return DataImportResult.Bundle(importBundle(bundle))
    }
    val legacy = safeDecode { Json.decodeFromString<ExportData>(content) }
        ?: return DataImportResult.InvalidFormat
    return DataImportResult.Legacy(importLegacyNodes(legacy.nodes))
}
