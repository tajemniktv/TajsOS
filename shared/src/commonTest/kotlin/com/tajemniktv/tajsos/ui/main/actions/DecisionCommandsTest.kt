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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class DecisionCommandsTest {
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
}
