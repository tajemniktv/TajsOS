/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.ui.main.state

import com.tajemniktv.tajos.data.NodeEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Nullable text means the persisted value owns that field. Unsaved text survives route changes. */
data class NoteTextDraft(
    val title: String? = null,
    val content: String? = null,
    val titleDirty: Boolean = false,
    val contentDirty: Boolean = false,
    val saving: Boolean = false,
    val failed: Boolean = false,
)

/** Coalesces each note's edits, with one writer per ID and explicit retry after failure. */
class NoteEditController(
    private val scope: CoroutineScope,
    private val save: suspend (Long, String?, String?) -> Unit,
) {
    private val mutableDrafts = MutableStateFlow<Map<Long, NoteTextDraft>>(emptyMap())
    val drafts = mutableDrafts.asStateFlow()

    fun updateTitle(nodeId: Long, title: String) {
        edit(nodeId) { it.copy(title = title, titleDirty = true, failed = false) }
    }

    fun updateContent(nodeId: Long, content: String) {
        edit(nodeId) { it.copy(content = content, contentDirty = true, failed = false) }
    }

    private fun edit(nodeId: Long, change: (NoteTextDraft) -> NoteTextDraft) {
        mutableDrafts.update { it + (nodeId to change(it[nodeId] ?: NoteTextDraft())) }
        startSave(nodeId)
    }

    fun retry(nodeId: Long) {
        mutableDrafts.update { state -> state[nodeId]?.let { state + (nodeId to it.copy(failed = false)) } ?: state }
        startSave(nodeId)
    }

    /** Keep optimistic text until the observed Room row acknowledges it, without masking other fields. */
    fun observe(node: NodeEntity) {
        mutableDrafts.update { state ->
            val draft = state[node.id] ?: return@update state
            val updated = draft.copy(
                title = if (!draft.titleDirty && draft.title == node.title) null else draft.title,
                content = if (!draft.contentDirty && draft.content == node.content) null else draft.content,
            )
            if (updated.title == null && updated.content == null && !updated.saving && !updated.failed) state - node.id
            else state + (node.id to updated)
        }
    }

    private fun startSave(nodeId: Long) {
        while (true) {
            val state = mutableDrafts.value
            val draft = state[nodeId] ?: return
            if (draft.saving || draft.failed || (!draft.titleDirty && !draft.contentDirty)) return
            if (mutableDrafts.compareAndSet(state, state + (nodeId to draft.copy(saving = true)))) break
        }
        scope.launch {
            var cancelled = false
            try {
                while (true) {
                    val pending = mutableDrafts.value[nodeId] ?: break
                    if (!pending.titleDirty && !pending.contentDirty) break
                    save(nodeId, pending.title.takeIf { pending.titleDirty }, pending.content.takeIf { pending.contentDirty })
                    mutableDrafts.update { state ->
                        val current = state[nodeId] ?: return@update state
                        state + (nodeId to current.copy(
                            titleDirty = current.titleDirty && (!pending.titleDirty || current.title != pending.title),
                            contentDirty = current.contentDirty && (!pending.contentDirty || current.content != pending.content),
                        ))
                    }
                }
            } catch (exception: CancellationException) {
                cancelled = true
                throw exception
            } catch (_: Exception) {
                mutableDrafts.update { state -> state[nodeId]?.let { state + (nodeId to it.copy(failed = true)) } ?: state }
            } finally {
                mutableDrafts.update { state -> state[nodeId]?.let { state + (nodeId to it.copy(saving = false)) } ?: state }
                if (!cancelled && scope.isActive) startSave(nodeId)
            }
        }
    }
}
