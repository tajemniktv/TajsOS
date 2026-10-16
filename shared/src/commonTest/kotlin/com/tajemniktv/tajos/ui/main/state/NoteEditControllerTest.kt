/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.ui.main.state

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.runCurrent
import com.tajemniktv.tajos.data.NodeEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class NoteEditControllerTest {
    @Test fun coalescesEditsDuringSaveAndKeepsEachNoteIdSeparate() = runTest {
        val started = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        val saves = mutableListOf<Triple<Long, String?, String?>>()
        val controller = NoteEditController(backgroundScope) { id, title, body ->
            saves += Triple(id, title, body)
            if (saves.size == 1) { started.complete(Unit); release.await() }
        }
        controller.updateTitle(1, "Title")
        runCurrent(); started.await()
        controller.updateContent(1, "B")
        controller.updateContent(1, "Body")
        controller.updateTitle(2, "Other note")
        runCurrent()
        release.complete(Unit); runCurrent()
        assertEquals(listOf(Triple(1L, "Title", null), Triple(2L, "Other note", null), Triple(1L, null, "Body")), saves)
        controller.observe(NodeEntity(id = 1, type = "note", title = "Title", content = "Body"))
        assertNull(controller.drafts.value[1])
    }

    @Test fun failedSaveRetainsLatestDraftUntilExplicitRetryAndRoomAcknowledgement() = runTest {
        var fail = true
        var count = 0
        val controller = NoteEditController(backgroundScope) { _, _, _ -> count++; if (fail) error("Synthetic failure") }
        controller.updateContent(1, "Keep this text")
        runCurrent()
        assertTrue(controller.drafts.value.getValue(1).failed)
        assertEquals("Keep this text", controller.drafts.value.getValue(1).content)
        assertEquals(1, count)
        fail = false
        controller.retry(1); runCurrent()
        assertFalse(controller.drafts.value.getValue(1).failed)
        controller.observe(NodeEntity(id = 1, type = "note", title = "Note", content = "Old row"))
        assertEquals("Keep this text", controller.drafts.value.getValue(1).content)
        controller.observe(NodeEntity(id = 1, type = "note", title = "Note", content = "Keep this text"))
        assertNull(controller.drafts.value[1])
    }
}
