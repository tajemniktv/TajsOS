/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/** Ensures fake pin mutations notify existing node projections without a coincidental node edit. */
@OptIn(ExperimentalCoroutinesApi::class)
class FakeNodePinObservationTest {
    @Test
    fun pinAndUnpinUpdateObservedNodeProjection() = runTest {
        val dao = FakeNodeDao()
        val id = dao.insertNode(NodeEntity(title = "Synthetic task", type = "task"))
        var latest = emptyList<NodeWithPin>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            dao.getAllNodesWithPins().collect { latest = it }
        }
        assertNull(latest.single().pin)
        dao.pinToToday(TodayPinEntity(nodeId = id, date = "2026-10-12", position = 0))
        assertEquals(id, assertNotNull(latest.single().pin).nodeId)
        dao.unpinFromDate(id, "2026-10-12")
        assertNull(latest.single().pin)
    }
}
