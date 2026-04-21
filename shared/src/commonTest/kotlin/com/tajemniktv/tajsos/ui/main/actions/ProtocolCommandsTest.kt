/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.main.actions

import com.tajemniktv.tajsos.data.AppRepository
import com.tajemniktv.tajsos.data.FakeAttachmentDao
import com.tajemniktv.tajsos.data.FakeCalendarEventDao
import com.tajemniktv.tajsos.data.FakeCalendarProviderDao
import com.tajemniktv.tajsos.data.FakeDecisionDao
import com.tajemniktv.tajsos.data.FakeEventLogDao
import com.tajemniktv.tajsos.data.FakeFocusSessionDao
import com.tajemniktv.tajsos.data.FakeMedicationDao
import com.tajemniktv.tajsos.data.FakeModeDao
import com.tajemniktv.tajsos.data.FakeNodeDao
import com.tajemniktv.tajsos.data.FakeNodeSnapshotDao
import com.tajemniktv.tajsos.data.FakeProtocolDao
import com.tajemniktv.tajsos.data.FakeRelationDao
import com.tajemniktv.tajsos.data.FakeReviewDao
import com.tajemniktv.tajsos.data.FakeTagDao
import com.tajemniktv.tajsos.data.FakeTemplateDao
import com.tajemniktv.tajsos.data.FakeTrackDao
import com.tajemniktv.tajsos.data.FakeUserDao
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.TagEntity
import com.tajemniktv.tajsos.ui.main.state.PlaybookTemplate
import com.tajemniktv.tajsos.ui.main.state.TransitionProtocolTemplate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProtocolCommandsTest {

    private fun buildRepository(): AppRepository {
        return AppRepository(
            nodeDao = FakeNodeDao(),
            nodeSnapshotDao = FakeNodeSnapshotDao(),
            tagDao = FakeTagDao(),
            relationDao = FakeRelationDao(),
            attachmentDao = FakeAttachmentDao(),
            trackDao = FakeTrackDao(),
            eventLogDao = FakeEventLogDao(),
            templateDao = FakeTemplateDao(),
            modeDao = FakeModeDao(),
            userDao = FakeUserDao(),
            reviewDao = FakeReviewDao(),
            calendarProviderDao = FakeCalendarProviderDao(),
            calendarEventDao = FakeCalendarEventDao(),
            decisionDao = FakeDecisionDao(),
            protocolDao = FakeProtocolDao(),
            medicationDao = FakeMedicationDao(),
            focusSessionDao = FakeFocusSessionDao(),
        )
    }

    @Test
    fun testTriggerProtocolCreatesNewNode() = runTest {
        val repo = buildRepository()
        val scope = TestScope(testScheduler)

        var currentNodes = emptyList<NodeWithPin>()

        val commands = ProtocolCommands(
            repository = repo,
            scope = scope,
            currentNodes = { currentNodes },
            currentTags = { emptyList() },
            protocolTemplates = { emptyList() },
            playbookTemplates = { emptyList() }
        )

        commands.triggerProtocol("morning_startup", "test")

        testScheduler.advanceUntilIdle()

        val savedNodes = repo.getAllNodes().first()
        assertEquals(1, savedNodes.size)
        val node = savedNodes.first().node
        assertEquals("protocol", node.type)
        assertEquals("morning_startup", node.title)

        val history = repo.getAllProtocolHistory().first()
        assertEquals(setOf(node.id), history.map { it.protocolNodeId }.toSet())
        assertEquals("Triggered from test", history.first().notes)
    }

    @Test
    fun testApplyProtocolTemplateCreatesNewNode() = runTest {
        val repo = buildRepository()
        val scope = TestScope(testScheduler)

        val template = TransitionProtocolTemplate(
            key = "morning_startup",
            label = "Morning Startup",
            checklist = listOf("Wake up", "Drink water")
        )

        val commands = ProtocolCommands(
            repository = repo,
            scope = scope,
            currentNodes = { emptyList() },
            currentTags = { emptyList() },
            protocolTemplates = { listOf(template) },
            playbookTemplates = { emptyList() }
        )

        commands.applyProtocolTemplate("Morning Startup")
        testScheduler.advanceUntilIdle()

        val savedNodes = repo.getAllNodes().first()
        assertEquals(1, savedNodes.size)
        val node = savedNodes.first().node
        assertEquals("protocol", node.type)
        assertEquals("Morning Startup", node.title)
        assertTrue(node.content.contains("- [ ] Wake up"))
    }

    @Test
    fun testApplyPlaybookTemplateCreatesNodeAndTags() = runTest {
        val repo = buildRepository()
        val scope = TestScope(testScheduler)

        val template = PlaybookTemplate(
            key = "focus_mode",
            label = "Focus Mode",
            checklist = listOf("Clear desk", "Start timer"),
            recommendedModeKey = "WORK"
        )

        val commands = ProtocolCommands(
            repository = repo,
            scope = scope,
            currentNodes = { emptyList() },
            currentTags = { emptyList() },
            protocolTemplates = { emptyList() },
            playbookTemplates = { listOf(template) }
        )

        commands.applyPlaybookTemplate("Focus Mode")
        testScheduler.advanceUntilIdle()

        val savedNodes = repo.getAllNodes().first()
        assertEquals(1, savedNodes.size)
        val node = savedNodes.first().node
        assertEquals("protocol", node.type)
        assertEquals("Focus Mode", node.title)
        assertEquals("playbook|mode=WORK", node.relationshipContext)
    }

    @Test
    fun testToggleProtocolChecklistStep() = runTest {
        val repo = buildRepository()
        val scope = TestScope(testScheduler)

        val commands = ProtocolCommands(
            repository = repo,
            scope = scope,
            currentNodes = { emptyList() },
            currentTags = { emptyList() },
            protocolTemplates = { emptyList() },
            playbookTemplates = { emptyList() }
        )

        val initialContent = """
            ## TRANSITION CHECKLIST
            - [ ] Step 1
            - [x] Step 2
            - [ ] Step 3
        """.trimIndent()

        val node = NodeEntity(
            id = 1,
            type = "protocol",
            title = "Test Protocol",
            content = initialContent,
            inboxState = false
        )
        val nodeId = repo.insertNode(node)
        testScheduler.advanceUntilIdle()

        val insertedNode = repo.getNodeById(nodeId)!!
        commands.toggleProtocolChecklistStep(insertedNode, 0, true)
        testScheduler.advanceUntilIdle()

        val savedNodes1 = repo.getAllNodes().first()
        val updatedContent1 = savedNodes1.first().node.content
        assertTrue(updatedContent1.contains("- [x] Step 1"))

        val updatedNode1 = savedNodes1.first().node
        commands.toggleProtocolChecklistStep(updatedNode1, 1, false)
        testScheduler.advanceUntilIdle()

        val savedNodes2 = repo.getAllNodes().first()
        val updatedContent2 = savedNodes2.first().node.content
        assertTrue(updatedContent2.contains("- [ ] Step 2"))
    }
}
