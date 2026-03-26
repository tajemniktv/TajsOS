/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class NodeMetadataTest {
    @Test
    fun metadataEnvelope_roundTripsThroughNodeEntity() {
        val node = NodeEntity(type = "task", title = "Exam prep")
        val envelope =
            NodeMetadataEnvelope(
                student =
                    StudentMetadata(
                        courseId = "psy-101",
                        assignmentType = "exam",
                        semester = "2026-S",
                    ),
                creator = CreatorMetadata(ideaStage = "exploring"),
            )

        val encoded = node.withMetadataEnvelope(envelope)
        val decoded = encoded.metadataEnvelopeOrNull()

        assertNotNull(decoded)
        assertEquals("psy-101", decoded.student?.courseId)
        assertEquals("exploring", decoded.creator?.ideaStage)
    }

    @Test
    fun metadataEnvelope_invalidJsonReturnsNull() {
        val node = NodeEntity(type = "note", title = "x", metadataJson = "{bad")
        assertNull(node.metadataEnvelopeOrNull())
    }
}
