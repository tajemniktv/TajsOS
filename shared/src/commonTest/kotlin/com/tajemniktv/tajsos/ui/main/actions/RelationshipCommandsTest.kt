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
import com.tajemniktv.tajsos.data.NodeTagEntity
import com.tajemniktv.tajsos.data.RelationEntity
import com.tajemniktv.tajsos.data.TagEntity
import com.tajemniktv.tajsos.data.TemplateEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class RelationshipCommandsTest {

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

    private fun setupCommands(
        scope: TestScope,
        repo: AppRepository,
        currentTemplates: List<TemplateEntity> = emptyList()
    ): RelationshipCommands {
        return RelationshipCommands(
            repository = repo,
            scope = scope,
            currentTemplates = { currentTemplates },
            addNodeForResult = { title, content, type, projectId, areaId, inboxState ->
                val node = NodeEntity(
                    type = type,
                    title = title,
                    content = content,
                    projectId = projectId,
                    areaId = areaId,
                    inboxState = inboxState ?: false
                )
                repo.insertNode(node)
            },
            addRelation = { from, to, type ->
                scope.launch {
                    repo.insertRelation(RelationEntity(fromNodeId = from, toNodeId = to, relationType = type))
                }
            },
            updateNode = { node ->
                scope.launch {
                    repo.updateNode(node)
                }
            },
            setTagOnNode = { nodeId, tagName, _ ->
                scope.launch {
                    val tagId = repo.insertTag(TagEntity(name = tagName, normalizedName = tagName.lowercase()))
                    repo.attachTagToNode(nodeId = nodeId, tagId = tagId)
                }
            }
        )
    }

    @Test
    fun testSetPersonLastContactNow() = runTest {
        val scope = TestScope(testScheduler)
        val repo = buildRepository()
        val commands = setupCommands(scope, repo)

        val personNode = NodeEntity(type = "person", title = "Alice")
        val nodeId = repo.insertNode(personNode)
        val initialNode = repo.getNodeById(nodeId)!!

        commands.setPersonLastContactNow(initialNode)
        testScheduler.advanceUntilIdle()

        val updatedNode = repo.getNodeById(nodeId)!!
        assertNotNull(updatedNode.lastContactAt)
    }

    @Test
    fun testSetPersonFollowUpInDays() = runTest {
        val scope = TestScope(testScheduler)
        val repo = buildRepository()
        val commands = setupCommands(scope, repo)

        val personNode = NodeEntity(type = "person", title = "Alice")
        val nodeId = repo.insertNode(personNode)
        val initialNode = repo.getNodeById(nodeId)!!

        commands.setPersonFollowUpInDays(initialNode, 7)
        testScheduler.advanceUntilIdle()

        val updatedNode = repo.getNodeById(nodeId)!!
        assertNotNull(updatedNode.dueAt)
    }

    @Test
    fun testCreateReplyNeededForPerson() = runTest {
        val scope = TestScope(testScheduler)
        val repo = buildRepository()
        val commands = setupCommands(scope, repo)

        val personNode = NodeEntity(id = 1L, type = "person", title = "Bob")
        repo.insertNode(personNode)

        commands.createReplyNeededForPerson(1L, "Bob")
        testScheduler.advanceUntilIdle()

        val nodes = repo.getAllNodes().first()
        val replyNodes = nodes.filter { it.node.type == "open_loop" && it.node.title.contains("Bob") }
        assertEquals(1, replyNodes.size, "Should create exactly 1 reply needed node")

        val relations = repo.getAllRelations().first()
        val relation = relations.firstOrNull { it.fromNodeId == 1L && it.relationType == "RELATED_PERSON" }
        assertNotNull(relation)
        assertEquals(replyNodes.first().node.id, relation!!.toNodeId)
    }

    @Test
    fun testAddPlace() = runTest {
        val scope = TestScope(testScheduler)
        val repo = buildRepository()
        val commands = setupCommands(scope, repo)

        commands.addPlace("Coffee Shop", campus = false, home = false)
        testScheduler.advanceUntilIdle()

        val nodes = repo.getAllNodes().first()
        val placeNodes = nodes.filter { it.node.type == "place" }
        assertEquals(1, placeNodes.size)
        assertEquals("Coffee Shop", placeNodes.first().node.title)
        assertEquals("out_of_home", placeNodes.first().node.locationContext)
    }

    @Test
    fun testCreateLeaveHomeChecklist() = runTest {
        val scope = TestScope(testScheduler)
        val repo = buildRepository()
        val commands = setupCommands(scope, repo)

        commands.createLeaveHomeChecklist()
        testScheduler.advanceUntilIdle()

        val nodes = repo.getAllNodes().first()
        val checkListNodes = nodes.filter { it.node.type == "protocol" && it.node.noteType == "logistics" }
        assertEquals(1, checkListNodes.size)
    }

    @Test
    fun testAddVaultEntry() = runTest {
        val scope = TestScope(testScheduler)
        val repo = buildRepository()
        val commands = setupCommands(scope, repo)

        commands.addVaultEntry("receipt", "Laptop receipt", asType = "note")
        testScheduler.advanceUntilIdle()

        val nodes = repo.getAllNodes().first()
        val vaultNodes = nodes.filter { it.node.title == "Laptop receipt" }
        assertEquals(1, vaultNodes.size)
        assertEquals("note", vaultNodes.first().node.type)
        assertEquals("reference", vaultNodes.first().node.noteType)
    }

}
