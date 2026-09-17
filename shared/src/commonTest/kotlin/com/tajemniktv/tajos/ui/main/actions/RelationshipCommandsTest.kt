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
import com.tajemniktv.tajos.data.NodeTagEntity
import com.tajemniktv.tajos.data.RelationEntity
import com.tajemniktv.tajos.data.TagEntity
import com.tajemniktv.tajos.data.TemplateEntity
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





    @Test
    fun testSetPersonLastContactNow() = runTest {
        val scope = this
        val repo = buildTestRepository()
        val commands = setupTestRelationshipCommands(scope, repo)

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
        val repo = buildTestRepository()
        val commands = setupTestRelationshipCommands(scope, repo)

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
        val repo = buildTestRepository()
        val commands = setupTestRelationshipCommands(scope, repo)

        val personId = repo.insertNode(NodeEntity(type = "person", title = "Bob"))

        commands.createReplyNeededForPerson(personId, "Bob")
        testScheduler.advanceUntilIdle()

        val nodes = repo.getAllNodes().first()
        val replyNodes = nodes.filter { it.node.type == "open_loop" && it.node.title.contains("Bob") }
        assertEquals(1, replyNodes.size, "Should create exactly 1 reply needed node")
        assertEquals("reply_needed", replyNodes.first().node.openLoopType)

        val relations = repo.getRelationsForNode(replyNodes.first().node.id).first()
        val relatedPersonIds = relations.filter { it.relationType == "RELATED_PERSON" }.map { it.fromNodeId }.toSet()
        assertEquals(setOf(personId), relatedPersonIds)
        val relation =
            relations.firstOrNull {
                it.relationType == "RELATED_PERSON" &&
                    it.fromNodeId == personId &&
                    it.toNodeId == replyNodes.first().node.id
            }
        assertNotNull(relation)
    }

    @Test
    fun testAddPlace() = runTest {
        val scope = TestScope(testScheduler)
        val repo = buildTestRepository()
        val commands = setupTestRelationshipCommands(scope, repo)

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
        val repo = buildTestRepository()
        val commands = setupTestRelationshipCommands(scope, repo)

        commands.createLeaveHomeChecklist()
        testScheduler.advanceUntilIdle()

        val nodes = repo.getAllNodes().first()
        val checkListNodes = nodes.filter { it.node.type == "protocol" && it.node.noteType == "logistics" }
        assertEquals(1, checkListNodes.size)
    }

    @Test
    fun testAddVaultEntry() = runTest {
        val scope = TestScope(testScheduler)
        val repo = buildTestRepository()
        val commands = setupTestRelationshipCommands(scope, repo)

        commands.addVaultEntry("receipt", "Laptop receipt", asType = "note")
        testScheduler.advanceUntilIdle()

        val nodes = repo.getAllNodes().first()
        val vaultNodes = nodes.filter { it.node.title == "Laptop receipt" }
        assertEquals(1, vaultNodes.size)
        assertEquals("note", vaultNodes.first().node.type)
        assertEquals("reference", vaultNodes.first().node.noteType)
    }

}



internal fun setupTestRelationshipCommands(
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
                repo.attachTagToNode(nodeId, tagId)
            }
        }
    )
}
