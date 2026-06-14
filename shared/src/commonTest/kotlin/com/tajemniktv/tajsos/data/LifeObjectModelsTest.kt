/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests the new typed LifeOS object helpers used by capture and triage flows.
 */
class LifeObjectModelsTest {
    @Test
    fun parseCapturedText_usesFirstLineAsTitle() {
        val parsed =
            parseCapturedText(
                """
                Call the doctor
                Ask about recurring headaches
                Bring last bloodwork results
                """.trimIndent(),
            )

        assertEquals("Call the doctor", parsed.title)
        assertEquals(
            "Ask about recurring headaches\nBring last bloodwork results",
            parsed.content,
        )
    }

    @Test
    fun parseCapturedText_trimsLeadingBlankLines() {
        val parsed = parseCapturedText("\n\n   Brain dump idea   \n\n")

        assertEquals("Brain dump idea", parsed.title)
        assertEquals("", parsed.content)
    }

    @Test
    fun legacyNodeTypeToItemKind_collapsesLegacySprawlIntoCoreKinds() {
        assertEquals(ItemKind.TASK, legacyNodeTypeToItemKind("task"))
        assertEquals(ItemKind.TASK, legacyNodeTypeToItemKind("open_loop"))
        assertEquals(ItemKind.TASK, legacyNodeTypeToItemKind("maintenance"))
        assertEquals(ItemKind.NOTE, legacyNodeTypeToItemKind("idea"))
        assertEquals(ItemKind.NOTE, legacyNodeTypeToItemKind("resource"))
        assertEquals(ItemKind.RECORD, legacyNodeTypeToItemKind("record"))
        assertEquals(ItemKind.PROJECT, legacyNodeTypeToItemKind("project"))
        assertEquals(ItemKind.AREA, legacyNodeTypeToItemKind("area"))
    }

    @Test
    fun defaultInboxState_keepsProjectsAndAreasOutOfInboxByDefault() {
        assertTrue(ItemKind.TASK.defaultInboxState())
        assertTrue(ItemKind.NOTE.defaultInboxState())
        assertTrue(ItemKind.RECORD.defaultInboxState())
        assertFalse(ItemKind.PROJECT.defaultInboxState())
        assertFalse(ItemKind.AREA.defaultInboxState())
    }

    @Test
    fun itemHelpers_groupLegacyTypesIntoTaskAndKnowledgeLenses() {
        val maintenanceNode = NodeEntity(type = "maintenance", title = "Pay electricity bill")
        val resourceNode = NodeEntity(type = "resource", title = "CBT worksheet")
        val recordNode = NodeEntity(type = "record", title = "Therapy reflection")

        assertTrue(maintenanceNode.isTaskItem())
        assertEquals(TaskState.ACTIVE, maintenanceNode.taskStateOrNull())
        assertTrue(resourceNode.isNoteItem())
        assertTrue(resourceNode.isKnowledgeItem())
        assertTrue(recordNode.isRecordItem())
        assertTrue(recordNode.isKnowledgeItem())
    }

    @Test
    fun projectStateFromNodeStatus_mapsLegacyCompletionValues() {
        val projectNode = NodeEntity(type = "project", title = "Move apartments", projectStatus = "done")
        val areaNode = NodeEntity(type = "area", title = "Health")

        assertEquals(ProjectState.COMPLETED, projectNode.projectStateOrNull())
        assertEquals(ItemKind.AREA, areaNode.itemKindOrNull())
        assertTrue(areaNode.isAreaItem())
    }

    @Test
    fun matchesItemFilter_collapses_legacy_subtypes_for_search() {
        val ideaNode = NodeEntity(type = "idea", title = "Concept")
        val openLoopNode = NodeEntity(type = "open_loop", title = "Call insurance")
        val recordNode = NodeEntity(type = "record", title = "Session log")

        assertTrue(ideaNode.matchesItemFilter("note"))
        assertTrue(openLoopNode.matchesItemFilter("task"))
        assertTrue(recordNode.matchesItemFilter("record"))
        assertFalse(recordNode.matchesItemFilter("note"))
    }

    @Test
    fun decisionSupportHelpers_collapse_decisions_and_pending_decision_loops_into_task_work() {
        val decisionNode =
            NodeEntity(
                type = "decision",
                title = "Choose therapist",
                decisionStatus = "pending",
            )
        val pendingDecisionLoop =
            NodeEntity(
                type = "open_loop",
                title = "Need to decide on lease",
                openLoopType = "pending_decision",
            )
        val resolvedDecision =
            NodeEntity(
                type = "decision",
                title = "Pick laptop",
                status = "done",
                decisionStatus = "decided",
            )

        assertTrue(decisionNode.isDecisionSupportItem())
        assertTrue(pendingDecisionLoop.isDecisionSupportItem())
        assertFalse(decisionNode.isResolvedDecisionSupportItem())
        assertTrue(resolvedDecision.isResolvedDecisionSupportItem())
    }

    @Test
    fun anchorHelpers_keep_relationships_and_places_as_support_semantics_without_new_core_kinds() {
        val relationshipAnchor =
            NodeEntity(
                type = "person",
                title = "Alice",
                relationshipContext = "Important friend from university",
            )
        val placeAnchor =
            NodeEntity(
                type = "place",
                title = "Main Campus",
                locationContext = "on_campus",
            )
        val taskWithLocation =
            NodeEntity(
                type = "task",
                title = "Pick up package",
                locationContext = "out_of_home",
            )

        assertTrue(relationshipAnchor.isRelationshipAnchor())
        assertTrue(placeAnchor.isPlaceAnchor())
        assertFalse(taskWithLocation.isPlaceAnchor())
        assertEquals(ItemKind.TASK, taskWithLocation.itemKindOrNull())
    }

    @Test
    fun matchesItemFilter_handles_all_cases_including_null_and_unknown() {
        val taskNode = NodeEntity(type = "task", title = "T")
        val projectNode = NodeEntity(type = "project", title = "P")
        val areaNode = NodeEntity(type = "area", title = "A")
        val unknownNode = NodeEntity(type = "unknown_type", title = "U")

        assertTrue(taskNode.matchesItemFilter(null))
        assertTrue(projectNode.matchesItemFilter("project"))
        assertTrue(areaNode.matchesItemFilter("area"))
        assertTrue(unknownNode.matchesItemFilter("unknown_type"))
        assertFalse(unknownNode.matchesItemFilter("task"))
    }

    @Test
    fun projectStateFromNodeStatus_handles_archived_and_other_states() {
        assertEquals(ProjectState.ARCHIVED, projectStateFromNodeStatus("archived"))
        assertEquals(ProjectState.COMPLETED, projectStateFromNodeStatus("done"))
        assertEquals(ProjectState.COMPLETED, projectStateFromNodeStatus("completed"))
        assertEquals(ProjectState.ACTIVE, projectStateFromNodeStatus("active"))
        assertEquals(null, projectStateFromNodeStatus(null as String?))
    }

    @Test
    fun taskStateOrNull_handles_non_task_items() {
        val nonTaskNode = NodeEntity(type = "note", title = "N", status = "active")
        val taskNodeNullStatus = NodeEntity(type = "task", title = "T", status = "some_random")
        val taskNodeNoneStatus = NodeEntity(type = "task", title = "T", status = "null")

        assertEquals(null, nonTaskNode.taskStateOrNull())
        assertEquals(null, taskNodeNullStatus.taskStateOrNull())
        assertEquals(null, taskNodeNoneStatus.taskStateOrNull())
    }
}
