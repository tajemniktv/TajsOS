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

internal fun buildTestRepository(): AppRepository {
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
