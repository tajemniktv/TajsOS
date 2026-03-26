/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui

import com.tajemniktv.tajsos.data.AppRepository
import com.tajemniktv.tajsos.data.DecisionOptionEntity
import com.tajemniktv.tajsos.data.NodeEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DecisionCommands(
    private val repository: AppRepository,
    private val scope: CoroutineScope,
    private val addRelation: (Long, Long, String) -> Unit,
    private val updateNode: (NodeEntity) -> Unit,
) {
    fun linkDecisionToPerson(
        decisionId: Long,
        personId: Long,
    ) {
        addRelation(decisionId, personId, "RELATED_PERSON")
    }

    fun unlinkDecisionFromPerson(
        decisionId: Long,
        personId: Long,
    ) {
        scope.launch {
            val relation =
                repository.getRelationsForNode(decisionId).first().firstOrNull {
                    it.relationType == "RELATED_PERSON" &&
                        (
                            (it.fromNodeId == decisionId && it.toNodeId == personId) ||
                                (it.fromNodeId == personId && it.toNodeId == decisionId)
                        )
                } ?: return@launch
            repository.deleteRelation(relation)
        }
    }

    fun setDecisionRevisit(
        node: NodeEntity,
        revisitAt: Long?,
    ) {
        if (node.type != "decision") return
        updateNode(node.copy(decisionRevisitAt = revisitAt))
    }

    fun addDecisionOption(
        nodeId: Long,
        title: String,
        description: String? = null,
    ) {
        scope.launch {
            repository.insertDecisionOption(
                DecisionOptionEntity(
                    decisionNodeId = nodeId,
                    title = title,
                    description = description,
                ),
            )
        }
    }

    fun updateDecisionOption(option: DecisionOptionEntity) {
        scope.launch {
            repository.updateDecisionOption(option)
        }
    }

    fun deleteDecisionOption(option: DecisionOptionEntity) {
        scope.launch {
            repository.deleteDecisionOption(option)
        }
    }

    fun decideOn(
        nodeId: Long,
        outcome: String,
        selectedOptionId: Long? = null,
    ) {
        scope.launch {
            repository.decideOn(nodeId, outcome, selectedOptionId)
        }
    }

    fun convertDecisionToProject(nodeId: Long) {
        scope.launch {
            repository.convertDecisionToProject(nodeId)
        }
    }

    fun convertDecisionToTask(nodeId: Long) {
        scope.launch {
            repository.convertDecisionToTask(nodeId)
        }
    }
}
