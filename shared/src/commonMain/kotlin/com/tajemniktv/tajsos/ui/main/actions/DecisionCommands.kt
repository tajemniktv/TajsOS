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

/**
 * A command dispatcher responsible for handling all operations related to "decision" type nodes.
 *
 * This class abstracts the logic for linking people to decisions, managing available choices (options),
 * executing choices, and converting decisions into actionable projects or tasks.
 *
 * @property repository The [AppRepository] used for direct database access and entity updates.
 * @property scope The [CoroutineScope] in which all asynchronous database operations are launched.
 * @property addRelation A lambda function injected to create bidirectional relationships between two nodes.
 * @property updateNode A lambda function injected to trigger a standard node update flow.
 */
class DecisionCommands(
    private val repository: AppRepository,
    private val scope: CoroutineScope,
    private val addRelation: (Long, Long, String) -> Unit,
    private val updateNode: (NodeEntity) -> Unit,
) {
    /**
     * Creates a bidirectional relation between a specific decision node and a person node.
     *
     * @param decisionId The unique numeric ID of the decision node.
     * @param personId The unique numeric ID of the person node.
     */
    fun linkDecisionToPerson(
        decisionId: Long,
        personId: Long,
    ) {
        addRelation(decisionId, personId, "RELATED_PERSON")
    }

    /**
     * Finds and deletes the "RELATED_PERSON" bidirectional relation between a decision and a person.
     *
     * @param decisionId The unique numeric ID of the decision node.
     * @param personId The unique numeric ID of the person node.
     */
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

    /**
     * Sets or clears a scheduled date to revisit a pending decision.
     *
     * This method will silently fail if the provided node is not of type "decision".
     *
     * @param node The [NodeEntity] representing the decision to update.
     * @param revisitAt The epoch timestamp when the decision should be revisited, or null to clear it.
     */
    fun setDecisionRevisit(
        node: NodeEntity,
        revisitAt: Long?,
    ) {
        if (node.type != "decision") return
        updateNode(node.copy(decisionRevisitAt = revisitAt))
    }

    /**
     * Creates and attaches a new option (choice) to an existing decision node.
     *
     * @param nodeId The unique numeric ID of the parent decision node.
     * @param title The primary string label for the new option.
     * @param description An optional string providing more context or pros/cons for the option.
     */
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

    /**
     * Updates an existing decision option in the database.
     *
     * @param option The [DecisionOptionEntity] containing the updated values.
     */
    fun updateDecisionOption(option: DecisionOptionEntity) {
        scope.launch {
            repository.updateDecisionOption(option)
        }
    }

    /**
     * Permanently deletes a decision option from the database.
     *
     * @param option The [DecisionOptionEntity] to be removed.
     */
    fun deleteDecisionOption(option: DecisionOptionEntity) {
        scope.launch {
            repository.deleteDecisionOption(option)
        }
    }

    /**
     * Finalizes a decision, marking it as completed with a specified outcome and optionally selecting a winning option.
     *
     * @param nodeId The unique numeric ID of the decision node being finalized.
     * @param outcome A string detailing the final reasoning or result of the decision.
     * @param selectedOptionId The unique numeric ID of the [DecisionOptionEntity] that was chosen, if applicable.
     */
    fun decideOn(
        nodeId: Long,
        outcome: String,
        selectedOptionId: Long? = null,
    ) {
        scope.launch {
            repository.decideOn(nodeId, outcome, selectedOptionId)
        }
    }

    /**
     * Promotes a decision node to a fully-fledged project node, migrating its properties.
     *
     * @param nodeId The unique numeric ID of the decision node to convert.
     */
    fun convertDecisionToProject(nodeId: Long) {
        scope.launch {
            repository.convertDecisionToProject(nodeId)
        }
    }

    /**
     * Demotes or converts a decision node directly into a standard task node.
     *
     * @param nodeId The unique numeric ID of the decision node to convert.
     */
    fun convertDecisionToTask(nodeId: Long) {
        scope.launch {
            repository.convertDecisionToTask(nodeId)
        }
    }
}
