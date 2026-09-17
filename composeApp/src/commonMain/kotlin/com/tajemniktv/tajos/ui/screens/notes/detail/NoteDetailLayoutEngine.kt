/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.ui.screens.notes.detail

import com.tajemniktv.tajos.data.isDecisionSupportItem
import com.tajemniktv.tajos.data.isNoteItem
import com.tajemniktv.tajos.data.isTaskItem

/**
 * Builds a note detail layout plan based on the active surface and node state.
 */
fun buildNoteDetailPlan(
    surface: NoteDetailSurface,
    context: NoteDetailContext,
): NoteDetailPlan {
    val node = context.node
    val primary = mutableListOf<NoteDetailBlock>()

    primary.add(NoteDetailBlock("note_header"))
    primary.add(NoteDetailBlock("note_action_button"))
    primary.add(NoteDetailBlock("note_relationship_inspector"))
    primary.add(NoteDetailBlock("note_status_card"))

    if (node.isDecisionSupportItem()) {
        primary.add(NoteDetailBlock("note_decision_content"))
    }

    primary.add(NoteDetailBlock("note_info_grid"))

    if (node.isTaskItem()) {
        primary.add(NoteDetailBlock("note_task_metadata"))
    }

    val showMediaMetadata =
        node.isNoteItem() &&
            (node.mediaType != null || node.rating != null)
    if (showMediaMetadata) {
        primary.add(NoteDetailBlock("note_resource_metadata"))
    }

    if (context.tags.isNotEmpty()) {
        primary.add(NoteDetailBlock("note_context_graph"))
    }

    primary.add(NoteDetailBlock("note_cadence"))

    if (node.type == "task") {
        primary.add(NoteDetailBlock("note_aware_planning"))
    }

    primary.add(NoteDetailBlock("note_organization"))
    primary.add(NoteDetailBlock("note_attachments"))

    if (node.isNoteItem()) {
        primary.add(NoteDetailBlock("note_knowledge_config"))
    }

    primary.add(NoteDetailBlock("note_content_editor"))

    return when (surface) {
        NoteDetailSurface.MOBILE -> NoteDetailPlan(primary = primary)
        NoteDetailSurface.DESKTOP -> NoteDetailPlan(primary = primary) // Desktop currently uses the same or Workspace
    }
}
