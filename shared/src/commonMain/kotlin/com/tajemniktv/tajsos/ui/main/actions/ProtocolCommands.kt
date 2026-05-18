/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.main.actions

import com.tajemniktv.tajsos.data.AppRepository
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.ProtocolHistoryEntity
import com.tajemniktv.tajsos.data.TagEntity
import com.tajemniktv.tajsos.ui.main.calculators.buildPlaybookRelationshipContext
import com.tajemniktv.tajsos.ui.main.calculators.buildProtocolChecklistContent
import com.tajemniktv.tajsos.ui.main.calculators.findPlaybookTemplate
import com.tajemniktv.tajsos.ui.main.calculators.findProtocolTemplate
import com.tajemniktv.tajsos.ui.main.calculators.normalizeProtocolLabel
import com.tajemniktv.tajsos.ui.main.state.PlaybookTemplate
import com.tajemniktv.tajsos.ui.main.state.TransitionProtocolTemplate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.time.Clock

/**
 * A dedicated command dispatcher responsible for managing structured protocols and actionable playbooks.
 *
 * This class isolates the database and business logic required to instantiate, track, trigger,
 * and conclude system-defined protocols (e.g., "morning_startup") or custom playbooks.
 *
 * @property repository The [AppRepository] used for direct database updates.
 * @property scope The [CoroutineScope] in which all asynchronous database operations execute.
 * @property currentNodes A lambda supplier providing real-time access to the list of all active nodes.
 * @property currentTags A lambda supplier providing real-time access to the list of all tags.
 * @property protocolTemplates A lambda supplier providing available [TransitionProtocolTemplate] blueprints.
 * @property playbookTemplates A lambda supplier providing available [PlaybookTemplate] blueprints.
 */
class ProtocolCommands(
    private val repository: AppRepository,
    private val scope: CoroutineScope,
    private val currentNodes: () -> List<NodeWithPin>,
    private val currentTags: () -> List<TagEntity>,
    private val protocolTemplates: () -> List<TransitionProtocolTemplate>,
    private val playbookTemplates: () -> List<PlaybookTemplate>,
) {

    companion object {
        /** Cached regex for replacing non-alphanumeric characters with underscores to prevent recompilation. */
        private val nonAlphaNumericUnderscoreRegex = Regex("[^a-z0-9]+")
    }

    /**
     * Executes or instantiates a specific transition protocol based on its label.
     *
     * If the protocol node does not exist, a new protocol node is created from the best-matching [TransitionProtocolTemplate].
     * If the node already exists, a history row is recorded for the new trigger without altering the existing node metadata or content.
     *
     * @param protocolLabel The string identifier or title of the protocol to trigger.
     * @param source The structural context or UI location where the trigger originated (defaults to "dashboard").
     */
    fun triggerProtocol(
        protocolLabel: String,
        source: String = "dashboard",
    ) {
        scope.launch {
            val normalized = protocolLabel.trim()
            val template = findProtocolTemplate(protocolTemplates(), normalized)
            val existing =
                currentNodes()
                    .firstOrNull {
                        it.node.type == "protocol" &&
                            normalizeProtocolLabel(it.node.title) ==
                            normalizeProtocolLabel(
                                normalized,
                            )
                    }?.node

            val protocolNodeId =
                if (existing != null) {
                    if (existing.content.isBlank() && template != null) {
                        repository.updateNode(
                            existing.copy(
                                content = buildProtocolChecklistContent(template),
                                updatedAt = Clock.System.now().toEpochMilliseconds(),
                            ),
                        )
                    }
                    existing.id
                } else {
                    repository.insertNode(
                        NodeEntity(
                            type = "protocol",
                            title = template?.label ?: normalized,
                            content =
                                template?.let { buildProtocolChecklistContent(it) }
                                    ?: "Operational protocol trigger: $normalized",
                            inboxState = false,
                        ),
                    )
                }

            repository.insertProtocolHistory(
                ProtocolHistoryEntity(
                    protocolNodeId = protocolNodeId,
                    notes = "Triggered from $source",
                ),
            )
        }
    }

    fun applyProtocolTemplate(protocolLabel: String) {
        scope.launch {
            val template = findProtocolTemplate(protocolTemplates(), protocolLabel) ?: return@launch
            val existing =
                currentNodes()
                    .firstOrNull {
                        it.node.type == "protocol" &&
                            normalizeProtocolLabel(it.node.title) ==
                            normalizeProtocolLabel(
                                template.label,
                            )
                    }?.node
            if (existing != null) {
                repository.updateNode(
                    existing.copy(
                        title = template.label,
                        content = buildProtocolChecklistContent(template),
                        status = "active",
                        inboxState = false,
                        updatedAt = Clock.System.now().toEpochMilliseconds(),
                    ),
                )
            } else {
                repository.insertNode(
                    NodeEntity(
                        type = "protocol",
                        title = template.label,
                        content = buildProtocolChecklistContent(template),
                        inboxState = false,
                    ),
                )
            }
        }
    }

    fun applyPlaybookTemplate(
        playbookLabel: String,
        modeKey: String? = null,
        areaId: Long? = null,
    ) {
        scope.launch {
            val template = findPlaybookTemplate(playbookTemplates(), playbookLabel) ?: return@launch
            val resolvedModeKey = modeKey ?: template.recommendedModeKey
            val existing =
                currentNodes()
                    .firstOrNull {
                        it.node.type == "protocol" &&
                            normalizeProtocolLabel(it.node.title) ==
                            normalizeProtocolLabel(
                                template.label,
                            )
                    }?.node
            val relationshipContext = buildPlaybookRelationshipContext(resolvedModeKey)
            if (existing != null) {
                repository.updateNode(
                    existing.copy(
                        title = template.label,
                        content =
                            buildProtocolChecklistContent(
                                TransitionProtocolTemplate(
                                    template.key,
                                    template.label,
                                    template.checklist,
                                ),
                            ),
                        areaId = areaId ?: existing.areaId,
                        relationshipContext = relationshipContext,
                        status = "active",
                        inboxState = false,
                        updatedAt = Clock.System.now().toEpochMilliseconds(),
                    ),
                )
                setTagOnNode(existing.id, "playbook", true)
            } else {
                val playbookId =
                    repository.insertNode(
                        NodeEntity(
                            type = "protocol",
                            title = template.label,
                            content =
                                buildProtocolChecklistContent(
                                    TransitionProtocolTemplate(
                                        template.key,
                                        template.label,
                                        template.checklist,
                                    ),
                                ),
                            areaId = areaId,
                            relationshipContext = relationshipContext,
                            inboxState = false,
                        ),
                    )
                setTagOnNode(playbookId, "playbook", true)
            }
        }
    }

    fun saveCustomPlaybook(
        label: String,
        checklistLines: List<String>,
        modeKey: String? = null,
        areaId: Long? = null,
    ) {
        val cleanLabel = label.trim().ifBlank { return }
        val cleanChecklist = checklistLines.mapNotNull { it.trim().ifBlank { null } }
        if (cleanChecklist.isEmpty()) return
        scope.launch {
            val nodeId =
                repository.insertNode(
                    NodeEntity(
                        type = "protocol",
                        title = cleanLabel,
                        content =
                            buildProtocolChecklistContent(
                                TransitionProtocolTemplate(
                                    key =
                                        cleanLabel
                                            .lowercase()
                                            .replace(nonAlphaNumericUnderscoreRegex, "_")
                                            .trim('_'),
                                    label = cleanLabel,
                                    checklist = cleanChecklist,
                                ),
                            ),
                        areaId = areaId,
                        relationshipContext = buildPlaybookRelationshipContext(modeKey),
                        inboxState = false,
                    ),
                )
            setTagOnNode(nodeId, "playbook", true)
            if (modeKey != null) setTagOnNode(nodeId, "mode_${modeKey.lowercase()}", true)
        }
    }

    fun setPlaybookModeLink(
        playbookNode: NodeEntity,
        modeKey: String?,
        updateNode: (NodeEntity) -> Unit,
    ) {
        if (playbookNode.type != "protocol") return
        updateNode(playbookNode.copy(relationshipContext = buildPlaybookRelationshipContext(modeKey)))
    }

    fun setPlaybookAreaLink(
        playbookNode: NodeEntity,
        areaId: Long?,
        updateNode: (NodeEntity) -> Unit,
    ) {
        if (playbookNode.type != "protocol") return
        updateNode(playbookNode.copy(areaId = areaId))
    }

    fun toggleProtocolChecklistStep(
        protocolNode: NodeEntity,
        checklistIndex: Int,
        checked: Boolean,
    ) {
        if (protocolNode.type != "protocol") return
        scope.launch {
            val lines = protocolNode.content.lines().toMutableList()
            val checklistLineIndexes =
                lines
                    .withIndex()
                    .filter { (_, line) ->
                        line.trimStart().startsWith("- [ ] ") ||
                            line
                                .trimStart()
                                .startsWith("- [x] ")
                    }.map { it.index }
            val targetLine = checklistLineIndexes.getOrNull(checklistIndex) ?: return@launch
            val original = lines[targetLine].trimStart()
            val replacement =
                when
                    {
                        checked && original.startsWith("- [ ] ") -> {
                            original.replaceFirst(
                                "- [ ] ",
                                "- [x] ",
                            )
                        }

                        !checked && original.startsWith("- [x] ") -> {
                            original.replaceFirst(
                                "- [x] ",
                                "- [ ] ",
                            )
                        }

                        else -> {
                            original
                        }
                    }
            lines[targetLine] = replacement
            repository.updateNode(
                protocolNode.copy(
                    content = lines.joinToString("\n"),
                    updatedAt = Clock.System.now().toEpochMilliseconds(),
                ),
            )
        }
    }

    private suspend fun setTagOnNode(
        nodeId: Long,
        tagName: String,
        enabled: Boolean,
    ) {
        val normalized = tagName.trim().lowercase()
        val existingTag = currentTags().firstOrNull { it.normalizedName == normalized }
        val tagId =
            existingTag?.id
                ?: repository.insertTag(
                    TagEntity(
                        name = tagName.trim(),
                        normalizedName = normalized,
                    ),
                )
        if (enabled) {
            repository.attachTagToNode(nodeId, tagId)
        } else {
            repository.detachTagFromNode(nodeId, tagId)
        }
    }
}
