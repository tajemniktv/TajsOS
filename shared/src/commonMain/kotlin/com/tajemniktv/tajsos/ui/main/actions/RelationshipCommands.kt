/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.main.actions

import com.tajemniktv.tajsos.data.AppRepository
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.TemplateEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlin.time.Clock

/**
 * A specialized command dispatcher responsible for all interpersonal and CRM (Customer Relationship Management) operations.
 *
 * This class abstracts the logic required to log contact dates, schedule follow-ups, apply relationship-specific tags,
 * and seamlessly instantiate structured templates (e.g., meeting agendas or gift trackers) explicitly linked to people.
 *
 * @property repository The [AppRepository] used for direct database updates.
 * @property scope The [CoroutineScope] in which all asynchronous database operations execute.
 * @property currentTemplates A lambda supplier providing access to all registered user templates.
 * @property addNodeForResult A complex lambda function that creates a node and returns its inserted ID.
 * @property addRelation A lambda function injected to formally link two nodes together in the database.
 * @property updateNode A standard lambda function injected to trigger a core node update flow.
 * @property setTagOnNode A suspended lambda function injected to securely attach or detach string tags.
 */
class RelationshipCommands(
    private val repository: AppRepository,
    private val scope: CoroutineScope,
    private val currentTemplates: () -> List<TemplateEntity>,
    private val addNodeForResult: suspend (String, String, String, Long?, Long?, Boolean?) -> Long,
    private val addRelation: (Long, Long, String) -> Unit,
    private val updateNode: (NodeEntity) -> Unit,
    private val setTagOnNode: suspend (Long, String, Boolean) -> Unit,
) {
    /**
     * Marks the current moment as the last point of contact for a specific person.
     * Silently fails if the node is not of type "person".
     *
     * @param person The [NodeEntity] representing the person.
     */
    fun setPersonLastContactNow(person: NodeEntity) {
        if (person.type != "person") return
        updateNode(person.copy(lastContactAt = Clock.System.now().toEpochMilliseconds()))
    }

    /**
     * Schedules a formal follow-up date for a specific person by calculating a future epoch timestamp.
     * Silently fails if the node is not of type "person".
     *
     * @param person The [NodeEntity] representing the person.
     * @param days The number of days into the future to schedule the follow-up, or null to clear it.
     */
    fun setPersonFollowUpInDays(
        person: NodeEntity,
        days: Int?,
    ) {
        if (person.type != "person") return
        val followUpAt =
            if (days == null) {
                null
            } else {
                Clock.System
                    .now()
                    .plus(days.coerceIn(1, 365), DateTimeUnit.DAY, TimeZone.currentSystemDefault())
                    .toEpochMilliseconds()
            }
        updateNode(person.copy(dueAt = followUpAt))
    }

    fun setPersonImportantDate(
        person: NodeEntity,
        timestamp: Long?,
    ) {
        if (person.type != "person") return
        updateNode(person.copy(dueAt = timestamp))
    }

    fun setPersonSocialEnergyNotes(
        person: NodeEntity,
        notes: String?,
    ) {
        if (person.type != "person") return
        updateNode(person.copy(socialEnergyNotes = notes?.trim()?.ifBlank { null }))
    }

    fun setPersonRelationshipContext(
        person: NodeEntity,
        context: String?,
    ) {
        if (person.type != "person") return
        updateNode(person.copy(relationshipContext = context?.trim()?.ifBlank { null }))
    }

    fun markImportantRelationship(
        person: NodeEntity,
        important: Boolean,
    ) {
        if (person.type != "person") return
        scope.launch {
            setTagOnNode(person.id, "important_relationship", important)
        }
    }

    fun setPersonRelationshipType(
        person: NodeEntity,
        type: String?,
    ) {
        if (person.type != "person") return
        val supported = setOf("professor", "friend", "family")
        val normalized = type?.trim()?.lowercase()?.takeIf { it in supported }
        scope.launch {
            supported.forEach { tag -> setTagOnNode(person.id, tag, false) }
            if (normalized != null) setTagOnNode(person.id, normalized, true)
        }
    }

    fun linkPersonToNode(
        personId: Long,
        nodeId: Long,
    ) {
        addRelation(personId, nodeId, "RELATED_PERSON")
    }

    fun unlinkPersonFromNode(
        personId: Long,
        nodeId: Long,
    ) {
        scope.launch {
            val relation =
                repository.getRelationsForNode(personId).first().firstOrNull {
                    it.relationType == "RELATED_PERSON" &&
                        (
                            (it.fromNodeId == personId && it.toNodeId == nodeId) ||
                                (it.toNodeId == personId && it.fromNodeId == nodeId)
                        )
                } ?: return@launch
            repository.deleteRelation(relation)
        }
    }

    fun createReplyNeededForPerson(
        personId: Long,
        title: String,
        content: String = "",
    ) {
        scope.launch {
            val nodeId =
                addNodeForResult(
                    title.ifBlank { "Reply needed" },
                    content,
                    "open_loop",
                    null,
                    null,
                    false,
                )
            val node = repository.getNodeById(nodeId) ?: return@launch
            repository.updateNode(
                node.copy(
                    openLoopType = "reply_needed",
                    updatedAt = Clock.System.now().toEpochMilliseconds(),
                ),
            )
            addRelation(personId, nodeId, "RELATED_PERSON")
        }
    }

    fun createSharedPlanForPerson(
        personId: Long,
        title: String,
        content: String = "",
    ) {
        scope.launch {
            val nodeId =
                addNodeForResult(
                    title.ifBlank { "Shared plan" },
                    content,
                    "task",
                    null,
                    null,
                    false,
                )
            setTagOnNode(nodeId, "shared_plan", true)
            addRelation(personId, nodeId, "RELATED_PERSON")
        }
    }

    fun createAskAboutNextTimeNote(
        personId: Long,
        prompt: String,
    ) {
        scope.launch {
            val nodeId =
                addNodeForResult(
                    "Ask next time: ${prompt.ifBlank { "Topic" }}",
                    prompt,
                    "note",
                    null,
                    null,
                    false,
                )
            val note = repository.getNodeById(nodeId) ?: return@launch
            repository.updateNode(
                note.copy(
                    noteType = "ask_next_time",
                    updatedAt = Clock.System.now().toEpochMilliseconds(),
                ),
            )
            setTagOnNode(nodeId, "ask_next_time", true)
            addRelation(personId, nodeId, "RELATED_PERSON")
        }
    }

    fun addPlace(
        title: String,
        campus: Boolean = false,
        home: Boolean = false,
    ) {
        scope.launch {
            val placeId =
                addNodeForResult(
                    title.ifBlank { "Place" },
                    "",
                    "place",
                    null,
                    null,
                    false,
                )
            val placeNode = repository.getNodeById(placeId) ?: return@launch
            repository.updateNode(
                placeNode.copy(
                    locationContext =
                        when
                            {
                                campus -> "on_campus"
                                home -> "at_home"
                                else -> "out_of_home"
                            },
                    updatedAt = Clock.System.now().toEpochMilliseconds(),
                ),
            )
            if (campus) setTagOnNode(placeId, "campus", true)
            if (home) setTagOnNode(placeId, "home", true)
        }
    }

    fun linkNodeToPlace(
        nodeId: Long,
        placeId: Long,
    ) {
        addRelation(placeId, nodeId, "PLACE_CONTEXT")
    }

    fun unlinkNodeFromPlace(
        nodeId: Long,
        placeId: Long,
    ) {
        scope.launch {
            val relation =
                repository.getRelationsForNode(placeId).first().firstOrNull {
                    it.relationType == "PLACE_CONTEXT" &&
                        (
                            (it.fromNodeId == placeId && it.toNodeId == nodeId) ||
                                (it.fromNodeId == nodeId && it.toNodeId == placeId)
                        )
                } ?: return@launch
            repository.deleteRelation(relation)
        }
    }

    fun createWhatToBringList(
        title: String,
        placeId: Long? = null,
    ) {
        createLogisticsList(
            title = title.ifBlank { "What to bring" },
            tag = "what_to_bring",
            noteType = "logistics",
            placeId = placeId,
        )
    }

    fun createPackingList(title: String) {
        createLogisticsList(
            title = title.ifBlank { "Packing list" },
            tag = "packing_list",
            noteType = "logistics",
        )
    }

    fun createLeaveHomeChecklist(title: String = "Leave-home checklist") {
        createLogisticsList(
            title = title,
            tag = "leave_home_checklist",
            noteType = "logistics",
            type = "protocol",
        )
    }

    fun createDontForgetSet(title: String) {
        createLogisticsList(
            title = title.ifBlank { "Don't forget set" },
            tag = "dont_forget_set",
            noteType = "logistics",
        )
    }

    fun createEventPreparationList(title: String) {
        createLogisticsList(
            title = title.ifBlank { "Event prep list" },
            tag = "event_prep",
            noteType = "logistics",
        )
    }

    fun createClassBringList(title: String) {
        createLogisticsList(
            title = title.ifBlank { "Class bring list" },
            tag = "class_bring",
            noteType = "logistics",
        )
    }

    fun ensureTravelPackTemplate() {
        scope.launch {
            val exists =
                currentTemplates().any { it.name.contains("travel pack", ignoreCase = true) }
            if (!exists) {
                repository.insertTemplate(
                    TemplateEntity(
                        name = "Travel Pack Template",
                        nodeType = "note",
                        defaultTitle = "Travel pack - [Trip]",
                        defaultContent =
                            """
                            - IDs / documents
                            - Wallet / cards / cash
                            - Phone / charger / powerbank
                            - Medications
                            - Clothes / hygiene
                            - Special gear
                            - Don't forget items
                            """.trimIndent(),
                    ),
                )
            }
        }
    }

    fun addPhysicalLogisticsNote(
        title: String,
        content: String,
    ) {
        scope.launch {
            val noteId =
                addNodeForResult(
                    title.ifBlank { "Physical logistics note" },
                    content,
                    "note",
                    null,
                    null,
                    false,
                )
            val note = repository.getNodeById(noteId) ?: return@launch
            repository.updateNode(
                note.copy(
                    noteType = "logistics",
                    updatedAt = Clock.System.now().toEpochMilliseconds(),
                ),
            )
            setTagOnNode(noteId, "logistics", true)
        }
    }

    fun addPersonalRule(
        title: String,
        content: String = "",
        categoryTag: String,
    ) {
        val validPrefix = categoryTag.trim().lowercase()
        if (!validPrefix.startsWith("rule_")) return
        scope.launch {
            val nodeId =
                addNodeForResult(
                    title.ifBlank { validPrefix.removePrefix("rule_").replace("_", " ") },
                    content,
                    "rule",
                    null,
                    null,
                    false,
                )
            val node = repository.getNodeById(nodeId) ?: return@launch
            repository.updateNode(
                node.copy(
                    noteType = "rule",
                    updatedAt = Clock.System.now().toEpochMilliseconds(),
                ),
            )
            setTagOnNode(nodeId, validPrefix, true)
            setTagOnNode(nodeId, "operating_principle", true)
        }
    }

    fun pinOperatingPrinciple(
        node: NodeEntity,
        pinned: Boolean,
    ) {
        if (node.type !in setOf("rule", "principle", "note")) return
        updateNode(node.copy(isPinned = pinned))
    }

    fun linkPrincipleToPlaybook(
        principleId: Long,
        playbookNodeId: Long,
    ) {
        addRelation(principleId, playbookNodeId, "PRINCIPLE_FOR_PLAYBOOK")
    }

    fun unlinkPrincipleFromPlaybook(
        principleId: Long,
        playbookNodeId: Long,
    ) {
        scope.launch {
            val relation =
                repository.getRelationsForNode(principleId).first().firstOrNull {
                    it.relationType in
                        setOf(
                            "PRINCIPLE_FOR_PLAYBOOK",
                            "PLAYBOOK_SUPPORTS_PRINCIPLE",
                        ) &&
                        (
                            (it.fromNodeId == principleId && it.toNodeId == playbookNodeId) ||
                                (it.fromNodeId == playbookNodeId && it.toNodeId == principleId)
                        )
                } ?: return@launch
            repository.deleteRelation(relation)
        }
    }

    /**
     * Adds a new vault entry to the database.
     * Performance optimization: uses case-insensitive comparison to avoid string allocations when parsing type.
     */
    fun addVaultEntry(
        categoryTag: String,
        title: String,
        content: String = "",
        asType: String = "note",
        dueAt: Long? = null,
    ) {
        scope.launch {
            val cleanTag = categoryTag.trim().lowercase()
            val cleanAsType = asType.trim()
            val type =
                when
                {
                    cleanAsType.equals("record", ignoreCase = true) -> "record"
                    cleanAsType.equals("task", ignoreCase = true) || cleanAsType.equals("maintenance", ignoreCase = true) -> "task"
                    else -> "note"
                }
            val nodeId =
                addNodeForResult(
                    title.ifBlank { "Vault entry" },
                    content,
                    type,
                    null,
                    null,
                    false,
                )
            val node = repository.getNodeById(nodeId) ?: return@launch
            repository.updateNode(
                node.copy(
                    dueAt = dueAt ?: node.dueAt,
                    noteType = if (type == "note") "reference" else node.noteType,
                    updatedAt = Clock.System.now().toEpochMilliseconds(),
                ),
            )
            setTagOnNode(nodeId, cleanTag, true)
        }
    }

    fun createApplicationStatusEntry(
        title: String,
        status: String,
        dueAt: Long? = null,
    ) {
        val normalizedStatus = status.trim().ifBlank { "pending" }
        addVaultEntry(
            categoryTag = "process_tracking",
            title = title.ifBlank { "Application status" },
            content = "Status: $normalizedStatus",
            dueAt = dueAt,
        )
    }

    fun markMustFindLater(
        node: NodeEntity,
        enabled: Boolean,
    ) {
        scope.launch {
            setTagOnNode(node.id, "must_find_later", enabled)
            updateNode(node.copy(isPinned = enabled || node.isPinned))
        }
    }

    private fun createLogisticsList(
        title: String,
        tag: String,
        noteType: String,
        placeId: Long? = null,
        type: String = "note",
    ) {
        scope.launch {
            val nodeId =
                addNodeForResult(
                    title,
                    "",
                    type,
                    null,
                    null,
                    false,
                )
            val node = repository.getNodeById(nodeId) ?: return@launch
            repository.updateNode(
                node.copy(
                    noteType = noteType,
                    updatedAt = Clock.System.now().toEpochMilliseconds(),
                ),
            )
            setTagOnNode(nodeId, tag, true)
            if (placeId != null) addRelation(placeId, nodeId, "PLACE_CONTEXT")
        }
    }
}
