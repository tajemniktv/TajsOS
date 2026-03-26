/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui

import com.tajemniktv.tajsos.data.AppRepository
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.RelationEntity
import com.tajemniktv.tajsos.data.TagEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MainNodeSupport(
    private val repository: AppRepository,
    private val scope: CoroutineScope,
    private val currentNodes: () -> List<NodeWithPin>,
    private val currentTags: () -> List<TagEntity>,
) {
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
