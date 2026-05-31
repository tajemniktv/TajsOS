/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import com.tajemniktv.tajsos.domain.DomainKind
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

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
                        semester = "2026-S"
                    ),
                creator = CreatorMetadata(ideaStage = "exploring")
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

    @Test
    fun areaDomainAssociation_roundTripsThroughMetadataEnvelope() {
        val node = NodeEntity(type = "area", title = "University")
        val encoded =
            node.withMetadataEnvelope(
                NodeMetadataEnvelope(
                    area =
                        AreaMetadata(
                            associatedDomains = setOf(DomainKind.EDUCATION, DomainKind.HEALTH)
                        )
                )
            )
        val decoded = encoded.areaMetadataOrNull()

        assertNotNull(decoded)
        assertEquals(
            setOf(DomainKind.EDUCATION, DomainKind.HEALTH),
            decoded.associatedDomains
        )
    }

    @Test
    fun associatedDomainHelpers_addAndRemoveDomainSafely() {
        val area = NodeEntity(type = "area", title = "Apartment")
        val withDomain = area.withAssociatedDomain(DomainKind.HEALTH)
        assertTrue(withDomain.isAssociatedWithDomain(DomainKind.HEALTH))

        val withoutDomain = withDomain.withoutAssociatedDomain(DomainKind.HEALTH)
        assertFalse(withoutDomain.isAssociatedWithDomain(DomainKind.HEALTH))
    }

    @Test
    fun safeDecode_catchesGenericExceptions() {
        val result = safeDecode<String> { throw IllegalArgumentException("Parse failed") }
        assertNull(result, "safeDecode should swallow generic exceptions and return null")
    }

    @Test
    fun safeDecode_rethrowsCancellationException() {
        assertFailsWith<CancellationException>(
            message = "safeDecode must rethrow CancellationException to avoid breaking coroutines",
            block = { safeDecode<String> { throw CancellationException() } }
        )
    }
}
