/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.ui.screens.notes

import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.notes_workspace_health
import tajsos.composeapp.generated.resources.notes_workspace_personal
import tajsos.composeapp.generated.resources.notes_workspace_retry
import tajsos.composeapp.generated.resources.notes_workspace_save_failed
import tajsos.composeapp.generated.resources.notes_workspace_saving
import tajsos.composeapp.generated.resources.notes_workspace_study
import tajsos.composeapp.generated.resources.notes_workspace_work

import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.tajemniktv.tajos.ui.main.state.NoteTextDraft
import com.tajemniktv.tajos.ui.theme.TajsOSTheme
import org.jetbrains.compose.resources.stringResource

/** Save failures leave the draft visible and provide an explicit retry. */
@Composable
fun NoteSaveStatus(draft: NoteTextDraft?, onRetry: () -> Unit) {
    when {
        draft?.failed == true -> Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(Res.string.notes_workspace_save_failed), modifier = Modifier.weight(1f), color = TajsOSTheme.Error)
            TextButton(onClick = onRetry) { Text(stringResource(Res.string.notes_workspace_retry)) }
        }
        draft?.saving == true -> Text(stringResource(Res.string.notes_workspace_saving), color = TajsOSTheme.Muted)
    }
}

@Composable
internal fun notesDomainLabel(domain: NotesDomain): String = stringResource(when (domain) {
    NotesDomain.PERSONAL -> Res.string.notes_workspace_personal
    NotesDomain.STUDY -> Res.string.notes_workspace_study
    NotesDomain.WORK -> Res.string.notes_workspace_work
    NotesDomain.HEALTH -> Res.string.notes_workspace_health
})
