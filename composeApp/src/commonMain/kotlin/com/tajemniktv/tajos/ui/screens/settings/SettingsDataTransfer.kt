/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.ui.screens.settings

import com.tajemniktv.tajos.data.DataImportResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Local transfer state; payloads remain in memory until the user explicitly copies them. */
data class SettingsDataTransferState(
    val busy: Boolean = false,
    val exportJson: String? = null,
    val feedback: DataTransferFeedback? = null,
)

/** UI feedback that does not expose raw exception details or imported secrets. */
sealed interface DataTransferFeedback {
    /** A typed import result is available for localized feedback without retaining rendered text. */
    data class Imported(val result: DataImportResult) : DataTransferFeedback
    /** The explicit copy operation completed for the full retained JSON payload. */
    data object Copied : DataTransferFeedback
    /** The requested operation failed; diagnostic details and secrets are intentionally omitted. */
    data object Failed : DataTransferFeedback
}

/** Coordinates one export, import, or explicit clipboard write at a time for Settings. */
class SettingsDataTransfer(
    private val exportNodes: suspend () -> String,
    private val exportBundle: suspend () -> String,
    private val importJson: suspend (String) -> DataImportResult,
    private val copyJson: suspend (String) -> Unit,
) {
    private val mutableState = MutableStateFlow(SettingsDataTransferState())
    /** Read-only observation of the current operation, in-memory export, and typed feedback. */
    val state: StateFlow<SettingsDataTransferState> = mutableState.asStateFlow()

    /** Replaces prior JSON before exporting, so failure cannot offer an obsolete snapshot for copy. */
    suspend fun export(fullBundle: Boolean) = runOperation {
        mutableState.update { it.copy(exportJson = null) }
        val payload = if (fullBundle) exportBundle() else exportNodes()
        mutableState.update { it.copy(exportJson = payload) }
    }

    /** Reports the importer result and leaves the submitted text available for correction. */
    suspend fun importPayload(payload: String) = runOperation {
        val result = importJson(payload)
        mutableState.update { it.copy(feedback = DataTransferFeedback.Imported(result)) }
    }

    /** Copies the full retained payload, even when the on-screen preview is truncated. */
    suspend fun copyExport() = runOperation {
        val payload = mutableState.value.exportJson ?: return@runOperation
        copyJson(payload)
        mutableState.update { it.copy(feedback = DataTransferFeedback.Copied) }
    }

    /** Releases the export payload when its review dialog closes. */
    fun dismissExport() {
        mutableState.update { if (it.busy) it else it.copy(exportJson = null) }
    }

    private suspend fun runOperation(block: suspend () -> Unit) {
        val previous = mutableState.value
        if (previous.busy || !mutableState.compareAndSet(previous, previous.copy(busy = true, feedback = null))) return
        try {
            block()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            mutableState.update { it.copy(feedback = DataTransferFeedback.Failed) }
        } finally {
            mutableState.update { it.copy(busy = false) }
        }
    }
}
