/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.notes

import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.RelationEntity
import com.tajemniktv.tajsos.data.isTaskItem

/**
 * Local domain grouping used by the Notes workspace left-rail filters.
 *
 * @property displayName Pre-computed capitalized string representation used for UI rendering,
 * avoiding dynamic string allocations during Compose recompositions.
 */
enum class NotesDomain(val displayName: String) {
    PERSONAL("Personal"),
    STUDY("Study"),
    WORK("Work"),
    HEALTH("Health"),
}

/**
 * Left-rail filter buckets for shaping the notes list.
 */
enum class NotesListFilter {
    ALL,
    PINNED,
    RECENT,
    FAVORITES,
    ARCHIVE,
}

/**
 * Left-rail sorting options for notes.
 */
enum class NotesSortOrder {
    UPDATED,
    CREATED,
    ALPHABETICAL,
}

/**
 * UI-facing note item used by the Notes workspace.
 *
 * This model intentionally keeps note-centric attributes local to the screen, allowing gradual
 * backend alignment without overloading core entities.
 */
data class NotesWorkspaceItem(
    val id: Long,
    val title: String,
    val content: String,
    val preview: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isPinned: Boolean,
    val isFavorite: Boolean,
    val isArchived: Boolean,
    val domain: NotesDomain,
    val tags: List<String>,
    val linkedTaskIds: List<Long>,
    val attachmentsSummary: List<String>,
    val source: NodeEntity,
)

/**
 * Maps a note node from storage into a workspace UI item.
 */
fun NodeWithPin.toNotesWorkspaceItem(
    linkedTaskIds: List<Long>,
    inferredDomain: NotesDomain,
): NotesWorkspaceItem =
    NotesWorkspaceItem(
        id = node.id,
        title = node.title,
        content = node.content,
        preview =
            node.content
                .trim()
                .replace('\n', ' ')
                .take(120),
        createdAt = node.createdAt,
        updatedAt = node.updatedAt,
        isPinned = node.isSticky || isPinnedToToday,
        isFavorite = node.isPinned,
        isArchived = node.status == "archived",
        domain = inferredDomain,
        tags = tags.map { it.name },
        linkedTaskIds = linkedTaskIds,
        attachmentsSummary = emptyList(),
        source = node,
    )

/**
 * Infers a lightweight domain for the notes workspace based on available metadata.
 */
fun inferNotesDomain(node: NodeWithPin): NotesDomain {
    val searchable =
        buildString {
            append(node.node.title)
            append(' ')
            append(node.node.content.take(180))
            append(' ')
            append(node.tags.joinToString(" ") { it.name })
        }.lowercase()
    return when {
        searchable.contains("study") || searchable.contains("exam") || searchable.contains("course") -> {
            NotesDomain.STUDY
        }

        searchable.contains("work") || searchable.contains("client") || searchable.contains("meeting") -> {
            NotesDomain.WORK
        }

        searchable.contains("health") || searchable.contains("med") || searchable.contains("symptom") -> {
            NotesDomain.HEALTH
        }

        else -> {
            NotesDomain.PERSONAL
        }
    }
}

/**
 * Builds an index of linked task IDs for each node based on relation edges.
 */
fun buildLinkedTaskIndex(
    relations: List<RelationEntity>,
    nodesById: Map<Long, NodeEntity>,
): Map<Long, List<Long>> {
    val linked = mutableMapOf<Long, MutableSet<Long>>()
    relations.forEach { relation ->
        val from = relation.fromNodeId
        val to = relation.toNodeId
        if (nodesById[to]?.isTaskItem() == true) {
            linked.getOrPut(from) { mutableSetOf() }.add(to)
        }
        if (nodesById[from]?.isTaskItem() == true) {
            linked.getOrPut(to) { mutableSetOf() }.add(from)
        }
    }
    return linked.mapValues { it.value.toList() }
}
