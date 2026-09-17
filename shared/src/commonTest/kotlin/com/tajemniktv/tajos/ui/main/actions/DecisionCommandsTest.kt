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
