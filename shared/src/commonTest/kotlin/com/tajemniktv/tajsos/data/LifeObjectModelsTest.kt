/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests the new typed LifeOS object helpers used by capture and triage flows.
 */
class LifeObjectModelsTest {
    @Test
    fun parseCapturedText_usesFirstLineAsTitle() {
        val parsed =
            parseCapturedText(
                """
                Call the doctor
                Ask about recurring headaches
                Bring last bloodwork results
                """.trimIndent(),
            )

        assertEquals("Call the doctor", parsed.title)
        assertEquals(
            "Ask about recurring headaches\nBring last bloodwork results",
            parsed.content,
        )
    }

    @Test
    fun parseCapturedText_trimsLeadingBlankLines() {
        val parsed = parseCapturedText("\n\n   Brain dump idea   \n\n")

        assertEquals("Brain dump idea", parsed.title)
        assertEquals("", parsed.content)
    }

    @Test
    fun legacyNodeTypeToItemKind_collapsesLegacySprawlIntoCoreKinds() {
        assertEquals(ItemKind.TASK, legacyNodeTypeToItemKind("task"))
        assertEquals(ItemKind.TASK, legacyNodeTypeToItemKind("open_loop"))
        assertEquals(ItemKind.TASK, legacyNodeTypeToItemKind("maintenance"))
        assertEquals(ItemKind.NOTE, legacyNodeTypeToItemKind("idea"))
        assertEquals(ItemKind.NOTE, legacyNodeTypeToItemKind("resource"))
        assertEquals(ItemKind.RECORD, legacyNodeTypeToItemKind("record"))
        assertEquals(ItemKind.PROJECT, legacyNodeTypeToItemKind("project"))
        assertEquals(ItemKind.AREA, legacyNodeTypeToItemKind("area"))
    }

    @Test
    fun defaultInboxState_keepsProjectsAndAreasOutOfInboxByDefault() {
        assertTrue(ItemKind.TASK.defaultInboxState())
        assertTrue(ItemKind.NOTE.defaultInboxState())
        assertTrue(ItemKind.RECORD.defaultInboxState())
        assertFalse(ItemKind.PROJECT.defaultInboxState())
        assertFalse(ItemKind.AREA.defaultInboxState())
    }
}
