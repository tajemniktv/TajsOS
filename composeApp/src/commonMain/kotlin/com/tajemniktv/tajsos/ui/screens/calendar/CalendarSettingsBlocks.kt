/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.calendar

import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tajemniktv.tajsos.data.CalendarProviderEntity
import com.tajemniktv.tajsos.ui.components.common.EmptyState
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.archive_delete
import tajsos.composeapp.generated.resources.cal_settings_add
import tajsos.composeapp.generated.resources.cal_settings_empty
import tajsos.composeapp.generated.resources.cal_settings_title

object CalendarSettingsBlockRegistry {
    private val renderers: Map<String, CalendarSettingsBlockRenderer> =
        mapOf(
            "cal_settings_header" to ::renderCalendarSettingsHeader,
            "cal_settings_list" to ::renderCalendarSettingsList,
        )

    fun resolve(id: String): CalendarSettingsBlockRenderer? = renderers[id]
}

@Composable
private fun renderCalendarSettingsHeader(context: CalendarSettingsContext) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(Res.string.cal_settings_title),
            style = MaterialTheme.typography.displaySmall,
            color = TajsOSTheme.Text,
        )
        IconButton(onClick = context.onShowAddDialog, modifier = Modifier.size(48.dp).pointerHoverIcon(PointerIcon.Hand)) {
            Icon(
                Icons.Default.Add,
                contentDescription = stringResource(Res.string.cal_settings_add),
                tint = TajsOSTheme.Primary,
            )
        }
    }
}

@Composable
private fun renderCalendarSettingsList(context: CalendarSettingsContext) {
    if (context.providers.isEmpty()) {
        EmptyState(message = stringResource(Res.string.cal_settings_empty)) {
            Spacer(modifier = Modifier.height(TajsOSTheme.SpacingMd))
            Button(onClick = context.onShowAddDialog) {
                Text(stringResource(Res.string.cal_settings_add))
            }
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)) {
            items(context.providers, key = { it.id }) { provider ->
                ProviderRow(
                    provider = provider,
                    onDelete = { context.onDeleteProvider(provider) },
                )
            }
        }
    }
}

@Composable
private fun ProviderRow(
    provider: CalendarProviderEntity,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TajsOSTheme.CardSurface),
    ) {
        Row(
            modifier = Modifier.padding(TajsOSTheme.SpacingMd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    provider.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TajsOSTheme.Text,
                )
                Text(
                    provider.type,
                    style = MaterialTheme.typography.labelSmall,
                    color = TajsOSTheme.Muted,
                )
                val url = provider.url
                if (url != null) {
                    Text(
                        url,
                        style = MaterialTheme.typography.bodySmall,
                        color = TajsOSTheme.Muted,
                        maxLines = 1,
                    )
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(48.dp).pointerHoverIcon(PointerIcon.Hand)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(Res.string.archive_delete),
                    tint = TajsOSTheme.Error,
                )
            }
        }
    }
}
