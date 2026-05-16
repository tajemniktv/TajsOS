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
import kotlin.test.assertNull
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




    private data class CommandsConfig(
        val repo: AppRepository,
        val scope: TestScope,
        val currentNodes: List<NodeWithPin> = emptyList(),
        val protocolTemplates: List<TransitionProtocolTemplate> = emptyList(),
        val playbookTemplates: List<PlaybookTemplate> = emptyList()
    )


    private suspend fun assertSingleProtocolNode(repo: AppRepository, expectedTitle: String): NodeEntity {
        val savedNodes = repo.getAllNodes().first()
        assertEquals(1, savedNodes.size)
        val node = savedNodes.first().node
        assertEquals("protocol", node.type)
        assertEquals(expectedTitle, node.title)
        return node
    }


    private fun setupTestEnvironment(
        scope: TestScope,
        protocolTemplates: List<TransitionProtocolTemplate> = emptyList(),
        playbookTemplates: List<PlaybookTemplate> = emptyList()
    ): Pair<AppRepository, ProtocolCommands> {
        val repo = buildRepository()
        val config = CommandsConfig(
            repo = repo,
            scope = scope,
            protocolTemplates = protocolTemplates,
            playbookTemplates = playbookTemplates
        )
        return Pair(repo, createCommands(config))
    }

    private fun createCommands(config: CommandsConfig): ProtocolCommands {
        return ProtocolCommands(
            repository = config.repo,
            scope = config.scope,
            currentNodes = { config.currentNodes },
            currentTags = { emptyList() },
            protocolTemplates = { config.protocolTemplates },
            playbookTemplates = { config.playbookTemplates }
        )
    }



    @Test
    fun testTriggerProtocolCreatesNewNode() = runTest {
        val scope = TestScope(testScheduler)
        val (repo, commands) = setupTestEnvironment(scope)

        commands.triggerProtocol("morning_startup", "test")

        testScheduler.advanceUntilIdle()

        val node = assertSingleProtocolNode(repo, "morning_startup")

        val history = repo.getAllProtocolHistory().first()
        assertEquals(setOf(node.id), history.map { it.protocolNodeId }.toSet())
        assertEquals("Triggered from test", history.first().notes)
    }

    @Test
    fun testApplyProtocolTemplateCreatesNewNode() = runTest {
        val scope = TestScope(testScheduler)
        val template = TransitionProtocolTemplate(
            key = "morning_startup",
            label = "Morning Startup",
            checklist = listOf("Wake up", "Drink water")
        )
        val (repo, commands) = setupTestEnvironment(scope, protocolTemplates = listOf(template))

        commands.applyProtocolTemplate("Morning Startup")
        testScheduler.advanceUntilIdle()

        val node = assertSingleProtocolNode(repo, "Morning Startup")
        assertTrue(node.content.contains("- [ ] Wake up"))
    }

    @Test
    fun testApplyPlaybookTemplateCreatesNodeAndTags() = runTest {
        val scope = TestScope(testScheduler)
        val template = PlaybookTemplate(
            key = "focus_mode",
            label = "Focus Mode",
            checklist = listOf("Clear desk", "Start timer"),
            recommendedModeKey = "WORK"
        )
        val (repo, commands) = setupTestEnvironment(scope, playbookTemplates = listOf(template))

        commands.applyPlaybookTemplate("Focus Mode")
        testScheduler.advanceUntilIdle()

        val node = assertSingleProtocolNode(repo, "Focus Mode")
        assertEquals("playbook|mode=WORK", node.relationshipContext)
    }

    @Test
    fun testToggleProtocolChecklistStep() = runTest {
        val scope = TestScope(testScheduler)
        val (repo, commands) = setupTestEnvironment(scope)

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

    @Test
    fun saveCustomPlaybook_createsNodeCorrectly() = runTest {
        val scope = TestScope(testScheduler)
        val (repo, commands) = setupTestEnvironment(scope)

        // Invalid inputs
        commands.saveCustomPlaybook("   ", listOf("step 1"))
        commands.saveCustomPlaybook("valid", emptyList())
        commands.saveCustomPlaybook("valid", listOf("   ", ""))
        testScheduler.advanceUntilIdle()

        var savedNodes = repo.getAllNodes().first()
        assertEquals(0, savedNodes.size)

        // Valid inputs
        commands.saveCustomPlaybook("My Playbook", listOf("Step A", "Step B"), modeKey = "STUDY", areaId = 42L)
        testScheduler.advanceUntilIdle()

        val node = assertSingleProtocolNode(repo, "My Playbook")
        assertEquals(42L, node.areaId)
        assertEquals("playbook|mode=STUDY", node.relationshipContext)
        assertTrue(node.content.contains("- [ ] Step A"))

        val tagsOnNode = repo.getTagsForNode(node.id).first()
        assertTrue(tagsOnNode.any { it.normalizedName == "playbook" })
        assertTrue(tagsOnNode.any { it.normalizedName == "mode_study" })
    }


    @Test
    fun setPlaybookLinks_updatesNodeCorrectly() = runTest {
        val scope = TestScope(testScheduler)
        val (repo, commands) = setupTestEnvironment(scope)

        val protocolNode = NodeEntity(id = 1, type = "protocol", title = "P")
        val taskNode = NodeEntity(id = 2, type = "task", title = "T")
        repo.insertNode(protocolNode)
        repo.insertNode(taskNode)

        // Invalid type ignores update
        var updatedTask: NodeEntity? = null
        commands.setPlaybookModeLink(taskNode, "STUDY") { updatedTask = it }
        assertNull(updatedTask)

        var updatedProtocol: NodeEntity? = null
        commands.setPlaybookModeLink(protocolNode, "STUDY") { updatedProtocol = it }
        assertNotNull(updatedProtocol)
        assertEquals("playbook|mode=STUDY", updatedProtocol.relationshipContext)

        var updatedProtocolArea: NodeEntity? = null
        commands.setPlaybookAreaLink(protocolNode, 99L) { updatedProtocolArea = it }
        assertNotNull(updatedProtocolArea)
        assertEquals(99L, updatedProtocolArea.areaId)
    }

}
