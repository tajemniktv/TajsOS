/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.main.actions

import com.tajemniktv.tajsos.data.AppRepository
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.RelationEntity
import com.tajemniktv.tajsos.data.TagEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * A specialized support class providing robust text-parsing and metadata-linking features
 * that sit outside the bounds of core node updates, but are intimately tied to node processing.
 *
 * It parses wiki-style internal links (e.g. `[[Title]]`) inside node content to automatically
 * create relational "MENTION" links between nodes, and handles the logic to safely apply
 * or remove normalized tags from specific nodes.
 *
 * @property repository The [AppRepository] used for direct database access and entity updates.
 * @property scope The [CoroutineScope] in which all asynchronous database operations are launched.
 * @property currentNodes A lambda supplier providing real-time access to the list of all nodes.
 * @property currentTags A lambda supplier providing real-time access to the list of all tags.
 */
class MainNodeSupport(
    private val repository: AppRepository,
    private val scope: CoroutineScope,
    private val currentNodes: () -> List<NodeWithPin>,
    private val currentTags: () -> List<TagEntity>,
) {
    /**
     * Inspects a node's primary content field, parses out explicitly defined internal links
     * structured like `[[Target Title]]` or `[[Target Title|Display Text]]`, and attempts
     * to automatically generate formal "MENTION" database relations.
     *
     * @param nodeId The unique numeric ID of the node to parse.
     */
    fun parseInternalLinks(nodeId: Long) {
        scope.launch {
            repository.getNodeById(nodeId)?.let { node ->
                val regex = Regex("\\[\\[(.*?)\\]\\]")
                val matches =
                    regex
                        .findAll(node.content)
                        .map { match ->
                            val fullMatch = match.groupValues[1]
                            if (fullMatch.contains("|")) fullMatch.split("|")[0] else fullMatch
                        }.toList()

                if (matches.isNotEmpty()) {
                    val nodes = currentNodes()
                    for (match in matches) {
                        nodes
                            .find { it.node.title.equals(match.trim(), ignoreCase = true) }
                            ?.let { target ->
                                repository.insertRelation(
                                    RelationEntity(
                                        fromNodeId = nodeId,
                                        toNodeId = target.node.id,
                                        relationType = "MENTION",
                                    ),
                                )
                            }
                    }
                }
            }
        }
    }

    /**
     * Updates the formal tagging state of a specific node, safely inserting missing tags into the
     * central database if they don't already exist.
     *
     * @param nodeId The unique numeric ID of the node receiving the tag update.
     * @param tagName The raw, un-normalized string name of the tag.
     * @param enabled If true, attaches the tag to the node. If false, detaches the tag.
     */
    suspend fun setTagOnNode(
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
