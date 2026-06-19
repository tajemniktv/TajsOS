/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.notes

import com.tajemniktv.tajsos.ui.components.TactileOutlinedTextField
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.components.common.EmptyState
import com.tajemniktv.tajsos.ui.components.common.MouseContextMenuHost
import com.tajemniktv.tajsos.ui.components.common.mouseClickable
import com.tajemniktv.tajsos.ui.components.common.rememberMouseContextMenuState
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.common_open
import kotlin.time.Instant

/**
 * Notes workspace left rail with creation, search, filtering, sorting, and note list navigation.
 */
@Composable
fun NotesListRail(
    notes: List<NotesWorkspaceItem>,
    selectedNoteId: Long?,
    searchQuery: String,
    activeFilter: NotesListFilter,
    activeDomain: NotesDomain?,
    sortOrder: NotesSortOrder,
    onCreateNote: () -> Unit,
    onSearchChange: (String) -> Unit,
    onFilterChange: (NotesListFilter) -> Unit,
    onDomainChange: (NotesDomain?) -> Unit,
    onSortOrderChange: (NotesSortOrder) -> Unit,
    onSelectNote: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = TajsOSTheme.SurfaceLow,
        shape = RoundedCornerShape(TajsOSTheme.RadiusLg),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(TajsOSTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
        ) {
            Button(
                onClick = onCreateNote,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("New note")
            }
            TactileOutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                placeholder = { Text("Search notes...") },
            )
            NotesFilterRow(activeFilter = activeFilter, onFilterChange = onFilterChange)
            NotesDomainRow(activeDomain = activeDomain, onDomainChange = onDomainChange)
            NotesSortPicker(sortOrder = sortOrder, onSortOrderChange = onSortOrderChange)

            if (notes.isEmpty()) {
                val emptyLabel =
                    if (searchQuery.isNotBlank()) {
                        "No search results"
                    } else {
                        "No notes yet"
                    }
                EmptyState(
                    message = emptyLabel,
                    description = null,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(items = notes, key = { it.id }) { note ->
                        NotesListItem(
                            note = note,
                            selected = note.id == selectedNoteId,
                            onClick = { onSelectNote(note.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotesFilterRow(
    activeFilter: NotesListFilter,
    onFilterChange: (NotesListFilter) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState()),
    ) {
        listOf(
            NotesListFilter.ALL to "All",
            NotesListFilter.PINNED to "Pinned",
            NotesListFilter.RECENT to "Recent",
            NotesListFilter.FAVORITES to "Favorites",
            NotesListFilter.ARCHIVE to "Archive",
        ).forEach { (filter, label) ->
            NotesTokenButton(
                label = label,
                selected = activeFilter == filter,
                onClick = { onFilterChange(filter) },
            )
        }
    }
}

@Composable
private fun NotesDomainRow(
    activeDomain: NotesDomain?,
    onDomainChange: (NotesDomain?) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState()),
    ) {
        listOf(
            null to "Any",
            NotesDomain.PERSONAL to "Personal",
            NotesDomain.STUDY to "Study",
            NotesDomain.WORK to "Work",
            NotesDomain.HEALTH to "Health",
        ).forEach { (domain, label) ->
            NotesTokenButton(
                label = label,
                selected = activeDomain == domain,
                onClick = { onDomainChange(domain) },
            )
        }
    }
}

@Composable
private fun NotesSortPicker(
    sortOrder: NotesSortOrder,
    onSortOrderChange: (NotesSortOrder) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState()),
    ) {
        listOf(
            NotesSortOrder.UPDATED to "Updated",
            NotesSortOrder.CREATED to "Created",
            NotesSortOrder.ALPHABETICAL to "Alphabetical",
        ).forEach { (sort, label) ->
            NotesTokenButton(
                label = label,
                selected = sortOrder == sort,
                onClick = { onSortOrderChange(sort) },
            )
        }
    }
}

/**
 * Reusable note list row for left-rail note browsing.
 */
@Composable
fun NotesListItem(
    note: NotesWorkspaceItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contextMenuState = rememberMouseContextMenuState()
    val titleColor = if (selected) TajsOSTheme.Primary else TajsOSTheme.Text
    val surfaceColor =
        if (selected) {
            TajsOSTheme.Primary.copy(alpha = 0.16f)
        } else {
            TajsOSTheme.SurfaceHighest.copy(alpha = 0.55f)
        }
    MouseContextMenuHost(
        state = contextMenuState,
        modifier = modifier.fillMaxWidth(),
        menuContent = {
            DropdownMenuItem(
                text = { Text(text = stringResource(Res.string.common_open)) },
                onClick = {
                    contextMenuState.dismiss()
                    onClick()
                },
            )
        },
    ) {
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(TajsOSTheme.RadiusMd))
                    .mouseClickable(
                        onClick = onClick,
                        onSecondaryClickAt = { contextMenuState.showAt(it) },
                        middleClickFallbackToPrimary = true,
                    ),
            color = surfaceColor,
            shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = note.title.ifBlank { "Untitled note" },
                        style = MaterialTheme.typography.titleSmall,
                        color = titleColor,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (note.isPinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = null,
                            tint = TajsOSTheme.Primary,
                            modifier = Modifier.padding(start = 4.dp).width(14.dp),
                        )
                    }
                    if (note.isFavorite) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = TajsOSTheme.Primary,
                            modifier = Modifier.padding(start = 4.dp).width(14.dp),
                        )
                    }
                }
                Text(
                    text = note.preview.ifBlank { "No content yet..." },
                    style = MaterialTheme.typography.bodySmall,
                    color = TajsOSTheme.Muted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${note.domain.displayName} • ${relativeDateLabel(note.updatedAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TajsOSTheme.Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun NotesTokenButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .clip(CircleShape)
                .background(
                    if (selected) {
                        TajsOSTheme.Primary.copy(alpha = 0.18f)
                    } else {
                        TajsOSTheme.SurfaceHighest.copy(alpha = 0.5f)
                    },
                ).mouseClickable(
                    onClick = onClick,
                    onSecondaryClick = onClick,
                    middleClickFallbackToPrimary = true,
                ).padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) TajsOSTheme.Primary else TajsOSTheme.Muted,
        )
    }
}

private fun relativeDateLabel(epochMillis: Long): String {
    val now =
        kotlin.time.Clock.System
            .now()
            .toEpochMilliseconds()
    val day = 24 * 60 * 60 * 1000L
    val diff = (now - epochMillis).coerceAtLeast(0L)
    return when {
        diff < day -> {
            "Today"
        }

        diff < day * 2 -> {
            "Yesterday"
        }

        diff < day * 7 -> {
            "${diff / day}d ago"
        }

        else -> {
            val dt =
                Instant
                    .fromEpochMilliseconds(epochMillis)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
            "${dt.date}"
        }
    }
}
