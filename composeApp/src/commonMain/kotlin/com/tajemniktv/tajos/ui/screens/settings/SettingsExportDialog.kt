/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajos.data.DataImportResult
import com.tajemniktv.tajos.ui.theme.TajsOSTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.common_close
import tajsos.composeapp.generated.resources.settings_transfer_bundle_imported
import tajsos.composeapp.generated.resources.settings_transfer_copied
import tajsos.composeapp.generated.resources.settings_transfer_copy
import tajsos.composeapp.generated.resources.settings_transfer_empty
import tajsos.composeapp.generated.resources.settings_transfer_export_ready
import tajsos.composeapp.generated.resources.settings_transfer_failed
import tajsos.composeapp.generated.resources.settings_transfer_invalid
import tajsos.composeapp.generated.resources.settings_transfer_legacy_imported
import tajsos.composeapp.generated.resources.settings_transfer_preview
import tajsos.composeapp.generated.resources.settings_transfer_private
import tajsos.composeapp.generated.resources.settings_transfer_unsupported

/** Reviews a bounded preview while keeping the full JSON available to the explicit copy action. */
@Composable
internal fun SettingsExportDialog(
    state: SettingsDataTransferState,
    onCopy: () -> Unit,
    onDismiss: () -> Unit,
) {
    val payload = state.exportJson ?: return
    AlertDialog(
        onDismissRequest = { if (!state.busy) onDismiss() },
        title = { Text(stringResource(Res.string.settings_transfer_export_ready)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)) {
                Text(stringResource(Res.string.settings_transfer_private))
                if (payload.length > EXPORT_PREVIEW_LIMIT) {
                    Text(stringResource(Res.string.settings_transfer_preview, EXPORT_PREVIEW_LIMIT, payload.length))
                }
                SelectionContainer {
                    Text(
                        text = payload.take(EXPORT_PREVIEW_LIMIT),
                        modifier = Modifier.heightIn(max = 240.dp).verticalScroll(rememberScrollState()),
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    )
                }
                state.feedback?.let { Text(dataTransferFeedbackText(it)) }
            }
        },
        confirmButton = {
            TextButton(onClick = onCopy, enabled = !state.busy) {
                Text(stringResource(Res.string.settings_transfer_copy))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !state.busy) {
                Text(stringResource(Res.string.common_close))
            }
        },
    )
}

/** Localizes transfer outcomes at the UI boundary rather than storing rendered strings. */
@Composable
internal fun dataTransferFeedbackText(feedback: DataTransferFeedback): String = when (feedback) {
    DataTransferFeedback.Copied -> stringResource(Res.string.settings_transfer_copied)
    DataTransferFeedback.Failed -> stringResource(Res.string.settings_transfer_failed)
    is DataTransferFeedback.Imported -> when (val result = feedback.result) {
        is DataImportResult.Bundle -> stringResource(
            Res.string.settings_transfer_bundle_imported, result.report.nodes, result.report.relations, result.report.events,
        )
        is DataImportResult.Legacy -> stringResource(Res.string.settings_transfer_legacy_imported, result.nodes)
        is DataImportResult.UnsupportedSchema -> stringResource(Res.string.settings_transfer_unsupported, result.version)
        DataImportResult.EmptyPayload -> stringResource(Res.string.settings_transfer_empty)
        DataImportResult.InvalidFormat -> stringResource(Res.string.settings_transfer_invalid)
    }
}

private const val EXPORT_PREVIEW_LIMIT = 8_000
