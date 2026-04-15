package com.tajemniktv.tajsos.ui.main.calculators

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MainProtocolHelpersTest {

    @Test
    fun testNormalizeProtocolLabel() {
        assertEquals("hello world", normalizeProtocolLabel("Hello World!"))
        assertEquals("hello world", normalizeProtocolLabel("  Hello... World  "))
        assertEquals("hello 123", normalizeProtocolLabel("Hello 123"))
        assertEquals("", normalizeProtocolLabel("   !!!   "))
    }

    @Test
    fun testBuildPlaybookRelationshipContext() {
        assertEquals("playbook", buildPlaybookRelationshipContext(null))
        assertEquals("playbook", buildPlaybookRelationshipContext(""))
        assertEquals("playbook", buildPlaybookRelationshipContext("   "))
        assertEquals("playbook|mode=WORK", buildPlaybookRelationshipContext("work"))
        assertEquals("playbook|mode=WORK", buildPlaybookRelationshipContext(" WORK "))
    }

    @Test
    fun testParsePlaybookModeKey() {
        assertNull(parsePlaybookModeKey(null))
        assertNull(parsePlaybookModeKey(""))
        assertNull(parsePlaybookModeKey("playbook"))
        assertNull(parsePlaybookModeKey("playbook|mode="))
        assertNull(parsePlaybookModeKey("playbook|mode=   "))

        assertEquals("WORK", parsePlaybookModeKey("playbook|mode=work"))
        assertEquals("WORK", parsePlaybookModeKey("playbook|mode=WORK"))
        assertEquals("WORK", parsePlaybookModeKey("playbook|mode= WORK "))
    }

    @Test
    fun testProtocolChecklistProgress() {
        val emptyContent = ""
        val (emptyDone, emptyTotal) = protocolChecklistProgress(emptyContent)
        assertEquals(0, emptyDone)
        assertEquals(0, emptyTotal)

        val oneDone = "- [x] task"
        val (oneD, oneT) = protocolChecklistProgress(oneDone)
        assertEquals(1, oneD)
        assertEquals(1, oneT)

        val multiple = """
            ## Checklist
            - [ ] Task 1
            - [x] Task 2
            - [ ] Task 3
            Some text
        """.trimIndent()
        val (multiDone, multiTotal) = protocolChecklistProgress(multiple)
        assertEquals(1, multiDone)
        assertEquals(3, multiTotal)
    }

    @Test
    fun testCalculateNextRecurringDate() {
        val epochMs = 1704067200000L // 2024-01-01 00:00:00 UTC

        // 1 day = 86400000 ms
        assertEquals(epochMs + 86400000L, calculateNextRecurringDate(epochMs, "daily"))
        assertEquals(epochMs + 86400000L, calculateNextRecurringDate(epochMs, "DAILY"))

        // 7 days = 604800000 ms
        assertEquals(epochMs + 604800000L, calculateNextRecurringDate(epochMs, "weekly"))

        // 30 days = 2592000000 ms
        assertEquals(epochMs + 2592000000L, calculateNextRecurringDate(epochMs, "monthly"))

        // fallback
        assertEquals(epochMs + 86400000L, calculateNextRecurringDate(epochMs, "unknown"))
    }
}
