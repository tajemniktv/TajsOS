/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.ui.main.actions

import com.tajemniktv.tajos.data.AppRepository
import com.tajemniktv.tajos.data.FakeAttachmentDao
import com.tajemniktv.tajos.data.FakeCalendarEventDao
import com.tajemniktv.tajos.data.FakeCalendarProviderDao
import com.tajemniktv.tajos.data.FakeDecisionDao
import com.tajemniktv.tajos.data.FakeEventLogDao
import com.tajemniktv.tajos.data.FakeFocusSessionDao
import com.tajemniktv.tajos.data.FakeMedicationDao
import com.tajemniktv.tajos.data.FakeModeDao
import com.tajemniktv.tajos.data.FakeNodeDao
import com.tajemniktv.tajos.data.FakeNodeSnapshotDao
import com.tajemniktv.tajos.data.FakeProtocolDao
import com.tajemniktv.tajos.data.FakeRelationDao
import com.tajemniktv.tajos.data.FakeReviewDao
import com.tajemniktv.tajos.data.FakeTagDao
import com.tajemniktv.tajos.data.FakeTemplateDao
import com.tajemniktv.tajos.data.FakeTrackDao
import com.tajemniktv.tajos.data.FakeUserDao
import com.tajemniktv.tajos.data.NodeEntity
import com.tajemniktv.tajos.data.DecisionOptionEntity
import com.tajemniktv.tajos.data.RelationEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertNotNull
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DecisionCommandsTest {
    private fun TestScope.commands(repo: AppRepository) =
        DecisionCommands(repo, this, { _, _, _ -> }, {})

    private fun createFakeRepo(): AppRepository {
        return AppRepository(
            nodeDao = FakeNodeDao(),
            focusSessionDao = FakeFocusSessionDao(),
            trackDao = FakeTrackDao(),
            relationDao = FakeRelationDao(),
            tagDao = FakeTagDao(),
            eventLogDao = FakeEventLogDao(),
            attachmentDao = FakeAttachmentDao(),
            templateDao = FakeTemplateDao(),
            nodeSnapshotDao = FakeNodeSnapshotDao(),
            reviewDao = FakeReviewDao(),
            calendarProviderDao = FakeCalendarProviderDao(),
            calendarEventDao = FakeCalendarEventDao(),
            modeDao = FakeModeDao(),
            protocolDao = FakeProtocolDao(),
            decisionDao = FakeDecisionDao(),
            userDao = FakeUserDao(),
            medicationDao = FakeMedicationDao(),
        )
    }

    @Test
    fun setDecisionRevisit_ignoresNonDecisionNodes() {
        var updatedNode: NodeEntity? = null
        val commands =
            DecisionCommands(
                repository = createFakeRepo(),
                scope = TestScope(),
                addRelation = { _, _, _ -> },
                updateNode = { updatedNode = it },
            )

        val taskNode = NodeEntity(id = 1L, type = "task", title = "Task")
        commands.setDecisionRevisit(taskNode, 1000L)

        assertNull(updatedNode, "Expected updateNode not to be called for non-decision nodes")
    }

    @Test
    fun setDecisionRevisit_updatesDecisionNodes() {
        var updatedNode: NodeEntity? = null
        val commands =
            DecisionCommands(
                repository = createFakeRepo(),
                scope = TestScope(),
                addRelation = { _, _, _ -> },
                updateNode = { updatedNode = it },
            )

        val decisionNode = NodeEntity(id = 1L, type = "decision", title = "Decision")
        commands.setDecisionRevisit(decisionNode, 1000L)

        assertEquals(1000L, updatedNode?.decisionRevisitAt, "Expected decisionRevisitAt to be updated")
    }

    @Test
    fun linkDecisionToPersonCallsRelationOwnerOnce() = runTest {
        val calls = mutableListOf<Triple<Long, Long, String>>()
        val commands = DecisionCommands(createFakeRepo(), this, { from, to, kind -> calls += Triple(from, to, kind) }, {})
        commands.linkDecisionToPerson(11, 22)
        assertEquals(listOf(Triple(11L, 22L, "RELATED_PERSON")), calls)
    }

    @Test
    fun unlinkRemovesOnlyMatchingPersonRelationInEitherDirection() = runTest {
        for (reversed in listOf(false, true)) {
            val repo = createFakeRepo()
            val matching = RelationEntity(id = 1, fromNodeId = if (reversed) 22 else 11, toNodeId = if (reversed) 11 else 22, relationType = "RELATED_PERSON")
            val otherType = matching.copy(id = 2, relationType = "DERIVED_FROM")
            val otherPerson = RelationEntity(id = 3, fromNodeId = 11, toNodeId = 33, relationType = "RELATED_PERSON")
            listOf(matching, otherType, otherPerson).forEach { repo.insertRelation(it) }
            commands(repo).unlinkDecisionFromPerson(11, 22)
            advanceUntilIdle()
            assertEquals(listOf(otherType, otherPerson), repo.getRelationsForNode(11).first())
        }
    }

    @Test
    fun unlinkMissingRelationDoesNotDeleteOtherRelations() = runTest {
        val repo = createFakeRepo()
        val relation = RelationEntity(fromNodeId = 11, toNodeId = 33, relationType = "RELATED_PERSON")
        repo.insertRelation(relation)
        val before = repo.getRelationsForNode(11).first()
        commands(repo).unlinkDecisionFromPerson(11, 22)
        commands(repo).unlinkDecisionFromPerson(999, 22)
        advanceUntilIdle()
        assertEquals(before, repo.getRelationsForNode(11).first())
    }

    @Test
    fun optionsAreAddedUpdatedAndDeletedWithoutTouchingOtherDecisions() = runTest {
        val repo = createFakeRepo()
        val commands = commands(repo)
        commands.addDecisionOption(11, "First", "Reason")
        commands.addDecisionOption(22, "Other")
        advanceUntilIdle()
        val option = repo.getOptionsForDecision(11).first().single()
        assertEquals("First", option.title)
        assertEquals("Reason", option.description)
        val updated = option.copy(title = "Revised", prosJson = "[\"Pro\"]", isSelected = true)
        commands.updateDecisionOption(updated)
        advanceUntilIdle()
        assertEquals(listOf(updated), repo.getOptionsForDecision(11).first())
        commands.deleteDecisionOption(updated)
        advanceUntilIdle()
        assertTrue(repo.getOptionsForDecision(11).first().isEmpty())
        assertEquals("Other", repo.getOptionsForDecision(22).first().single().title)
    }

    @Test
    fun decidingPersistsOutcomeAndReplacesSelectionOnlyWithinThisDecision() = runTest {
        val repo = createFakeRepo()
        repo.insertNode(NodeEntity(id = 11, title = "Choose", type = "decision"))
        val oldId = repo.insertDecisionOption(DecisionOptionEntity(decisionNodeId = 11, title = "Old", isSelected = true))
        val newId = repo.insertDecisionOption(DecisionOptionEntity(decisionNodeId = 11, title = "New"))
        repo.insertDecisionOption(DecisionOptionEntity(decisionNodeId = 22, title = "Unrelated", isSelected = true))
        val commands = commands(repo)
        commands.decideOn(11, "Chosen reason", newId)
        advanceUntilIdle()
        val node = assertNotNull(repo.getNodeById(11))
        assertEquals("done", node.status)
        assertEquals("decided", node.decisionStatus)
        assertEquals("Chosen reason", node.decisionOutcome)
        assertNotNull(node.completedAt)
        assertFalse(node.inboxState)
        assertEquals(listOf(newId), repo.getOptionsForDecision(11).first().filter { it.isSelected }.map { it.id })
        assertTrue(repo.getOptionsForDecision(22).first().single().isSelected)
        commands.decideOn(11, "No predefined choice")
        advanceUntilIdle()
        assertTrue(repo.getOptionsForDecision(11).first().none { it.isSelected })
        assertEquals(setOf(oldId, newId), repo.getOptionsForDecision(11).first().map { it.id }.toSet())
    }

    @Test
    fun conversionsCreateLinkedOutcomesAndLeaveOriginalDecisionIntact() = runTest {
        for (project in listOf(false, true)) {
            val repo = createFakeRepo()
            val decision = NodeEntity(id = 11, title = "Choose", type = "decision", content = "Context", decisionOutcome = "Outcome", areaId = 33, projectId = 44)
            repo.insertNode(decision)
            val commands = commands(repo)
            if (project) commands.convertDecisionToProject(11) else commands.convertDecisionToTask(11)
            advanceUntilIdle()
            val derived = repo.getAllNodes().first().single { it.node.id != 11L }.node
            assertEquals(if (project) "project" else "task", derived.type)
            assertEquals(if (project) "Action Plan: Choose" else "Follow-up: Choose", derived.title)
            assertEquals(if (project) "Derived from decision: Outcome" else "Outcome: Outcome\n\nContext", derived.content)
            assertEquals(33L, derived.areaId)
            assertEquals(if (project) null else 44L, derived.projectId)
            val relation = repo.getRelationsForNode(11).first().single { it.relationType == "DERIVED_FROM" }
            assertEquals(Triple(11L, derived.id, "DERIVED_FROM"), Triple(relation.fromNodeId, relation.toNodeId, relation.relationType))
            assertEquals(decision, repo.getNodeById(11))
        }
    }

    @Test
    fun missingDecisionsDoNotCreateNodesRelationsOrChangeOptions() = runTest {
        val repo = createFakeRepo()
        val commands = commands(repo)
        commands.decideOn(999, "Missing")
        commands.convertDecisionToProject(999)
        commands.convertDecisionToTask(999)
        advanceUntilIdle()
        assertTrue(repo.getAllNodes().first().isEmpty())
        assertTrue(repo.getRelationsForNode(999).first().isEmpty())
        assertTrue(repo.getOptionsForDecision(999).first().isEmpty())
    }
}
