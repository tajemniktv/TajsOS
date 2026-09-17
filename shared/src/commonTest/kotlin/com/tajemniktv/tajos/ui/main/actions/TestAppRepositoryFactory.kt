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
