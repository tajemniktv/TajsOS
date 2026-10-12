/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.ui.screens.settings

import com.tajemniktv.tajos.data.DataImportResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Exercises explicit transfer actions, overlapping requests, failure recovery, and cancellation. */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsDataTransferTest {
    @Test
    fun exportRetainsCompleteJsonAndCopiesOnlyOnRequest() = runTest {
        val payload = "Synthetic payload ".repeat(1_000)
        val copies = mutableListOf<String>()
        val transfer = SettingsDataTransfer({ "nodes" }, { payload }, { DataImportResult.InvalidFormat }, { copies += it })
        transfer.export(fullBundle = true)
        assertEquals(payload, transfer.state.value.exportJson)
        assertTrue(copies.isEmpty())
        transfer.copyExport()
        assertEquals(listOf(payload), copies)
        assertEquals(DataTransferFeedback.Copied, transfer.state.value.feedback)
        transfer.dismissExport()
        assertNull(transfer.state.value.exportJson)
    }

    @Test
    fun overlappingImportsAndExportsDoNotRepeatWritesOrReplacePendingResult() = runTest {
        val pending = CompletableDeferred<DataImportResult>()
        val imports = mutableListOf<String>()
        var exports = 0
        val transfer = SettingsDataTransfer(
            { exports++; "nodes" }, { exports++; "bundle" },
            { imports += it; pending.await() }, {},
        )
        val operation = launch { transfer.importPayload("synthetic input") }
        runCurrent()
        assertTrue(transfer.state.value.busy)
        transfer.importPayload("duplicate")
        transfer.export(fullBundle = true)
        assertEquals(listOf("synthetic input"), imports)
        assertEquals(0, exports)
        pending.complete(DataImportResult.Legacy(2))
        operation.join()
        assertFalse(transfer.state.value.busy)
        assertEquals(DataTransferFeedback.Imported(DataImportResult.Legacy(2)), transfer.state.value.feedback)
    }

    @Test
    fun clipboardFailureRetainsPayloadAndPermitsExplicitRetry() = runTest {
        var fail = true
        val copies = mutableListOf<String>()
        val transfer = SettingsDataTransfer(
            { "synthetic JSON" }, { "bundle" }, { DataImportResult.InvalidFormat },
            { if (fail) error("synthetic private diagnostic") else copies += it },
        )
        transfer.export(fullBundle = false)
        transfer.copyExport()
        assertEquals(DataTransferFeedback.Failed, transfer.state.value.feedback)
        assertEquals("synthetic JSON", transfer.state.value.exportJson)
        assertFalse(transfer.state.value.busy)
        fail = false
        transfer.copyExport()
        assertEquals(listOf("synthetic JSON"), copies)
    }

    @Test
    fun exportFailureDoesNotProducePayloadOrLeaveControlsBusy() = runTest {
        val transfer = SettingsDataTransfer({ error("synthetic export failure") }, { "bundle" }, { DataImportResult.InvalidFormat }, {})
        transfer.export(fullBundle = false)
        assertEquals(DataTransferFeedback.Failed, transfer.state.value.feedback)
        assertFalse(transfer.state.value.busy)
        assertNull(transfer.state.value.exportJson)
        transfer.export(fullBundle = true)
        assertEquals("bundle", transfer.state.value.exportJson)
        assertNull(transfer.state.value.feedback)
    }

    @Test
    fun failedReplacementExportCannotCopyThePreviousSnapshot() = runTest {
        var fail = false
        val copies = mutableListOf<String>()
        val transfer = SettingsDataTransfer(
            { if (fail) error("Synthetic failure") else "Old snapshot" },
            { "Fresh snapshot" }, { DataImportResult.InvalidFormat }, { copies += it },
        )
        transfer.export(fullBundle = false)
        assertEquals("Old snapshot", transfer.state.value.exportJson)
        fail = true
        transfer.export(fullBundle = false)
        assertNull(transfer.state.value.exportJson)
        assertEquals(DataTransferFeedback.Failed, transfer.state.value.feedback)
        transfer.copyExport()
        assertTrue(copies.isEmpty())
        transfer.export(fullBundle = true)
        transfer.copyExport()
        assertEquals(listOf("Fresh snapshot"), copies)
    }

    @Test
    fun cancellationPropagatesAndReleasesBusyState() = runTest {
        val transfer = SettingsDataTransfer({ throw CancellationException("cancel") }, { "bundle" }, { DataImportResult.InvalidFormat }, {})
        assertFailsWith<CancellationException> { transfer.export(fullBundle = false) }
        assertFalse(transfer.state.value.busy)
        assertNull(transfer.state.value.feedback)
    }

    @Test
    fun overlappingCopyAndCancelledClipboardWorkNeverReportFalseSuccess() = runTest {
        val pending = CompletableDeferred<Unit>()
        var attempts = 0
        val copies = mutableListOf<String>()
        val transfer = SettingsDataTransfer(
            { "complete synthetic export" }, { "bundle" }, { error("Import must not run during copy") },
            { attempts++; pending.await(); copies += it },
        )
        transfer.export(fullBundle = false)
        val copy = launch { transfer.copyExport() }
        runCurrent()
        transfer.copyExport()
        transfer.importPayload("overlapping import")
        transfer.export(fullBundle = true)
        transfer.dismissExport()
        assertEquals(1, attempts)
        assertTrue(copies.isEmpty())
        assertEquals("complete synthetic export", transfer.state.value.exportJson)
        copy.cancelAndJoin()
        assertFalse(transfer.state.value.busy)
        assertNull(transfer.state.value.feedback)
        pending.complete(Unit)
        transfer.copyExport()
        assertEquals(listOf("complete synthetic export"), copies)
        assertEquals(DataTransferFeedback.Copied, transfer.state.value.feedback)
    }

    @Test
    fun importerExceptionDoesNotLeakDiagnosticsOrTriggerImplicitRetry() = runTest {
        val submitted = mutableListOf<String>()
        var fail = true
        val transfer = SettingsDataTransfer(
            { "nodes" }, { "bundle" },
            { submitted += it; if (fail) error("LOCAL_SECRET_DIAGNOSTIC") else DataImportResult.Legacy(3) },
            { error("Import must never write the clipboard") },
        )
        val payload = "synthetic input retained by the route"
        transfer.importPayload(payload)
        assertEquals(listOf(payload), submitted)
        assertEquals(DataTransferFeedback.Failed, transfer.state.value.feedback)
        assertFalse(transfer.state.value.busy)
        fail = false
        transfer.importPayload(payload)
        assertEquals(listOf(payload, payload), submitted)
        assertEquals(DataTransferFeedback.Imported(DataImportResult.Legacy(3)), transfer.state.value.feedback)
    }
}
