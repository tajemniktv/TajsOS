/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.calendar

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class IcsEventBuilderTest {
    @Test
    fun testProcessLineNormal() {
        val builder = IcsEventBuilder()
        builder.processLine("UID:12345")
        builder.processLine("SUMMARY:Test Summary")
        builder.processLine("DESCRIPTION:Test Description")
        builder.processLine("LOCATION:Test Location")

        assertEquals("12345", builder.uid)
        assertEquals("Test Summary", builder.summary)
        assertEquals("Test Description", builder.description)
        assertEquals("Test Location", builder.location)
    }

    @Test
    fun testProcessLineWithEscapes() {
        val builder = IcsEventBuilder()
        builder.processLine("SUMMARY:Test\\, with comma")
        builder.processLine("DESCRIPTION:Line 1\\nLine 2")
        builder.processLine("LOCATION:Some\\;Place")

        assertEquals("Test, with comma", builder.summary)
        assertEquals("Line 1\nLine 2", builder.description)
        assertEquals("Some;Place", builder.location)
    }

    @Test
    fun testProcessLineWithTzid() {
        val builder = IcsEventBuilder()
        builder.processLine("DTSTART;TZID=Europe/London:20231024T100000")

        val startProp = builder.startProp
        assertTrue(startProp != null)
        assertEquals("20231024T100000", startProp.value)
        assertEquals("DTSTART;TZID=Europe/London", startProp.rawKey)
    }

    @Test
    fun testProcessLineInvalidFormat() {
        val builder = IcsEventBuilder()
        builder.processLine("INVALIDLINEWITHOUTCOLON")
        // Should ignore
        assertNull(builder.uid)
    }

    @Test
    fun testParseAllDayDate() {
        val builder = IcsEventBuilder()
        val instant = builder.parseAllDayDate("20231024")
        assertEquals(Instant.parse("2023-10-24T00:00:00Z"), instant)
    }

    /**
     * Regression test verifying that ISO date parsing respects local timezone (Europe/London)
     * across DST boundaries. This test uses 2023-10-24, which is before the DST shift to GMT
     * (DST ends on the last Sunday of October). The expected result correctly applies the BST
     * offset (UTC+1), converting local 10:00:00 BST to 09:00:00 UTC. This guards against
     * incorrect offset handling that could misapply timezone rules when local timezone parsing
     * was introduced.
     */
    @Test
    fun testParseIsoDateLocalTimeZone() {
        val builder = IcsEventBuilder()
        val prop = IcsDateProperty("20231024T100000", "DTSTART;TZID=Europe/London")
        val instant = builder.parseIsoDate(prop)
        // Europe/London is UTC+1 in October (BST) until last Sunday,
        // 2023-10-24 is Tuesday, so it's UTC+1.
        // Local 10:00:00 -> UTC 09:00:00
        assertEquals(Instant.parse("2023-10-24T09:00:00Z"), instant)
    }

    @Test
    fun testParseIsoDateUtc() {
        val builder = IcsEventBuilder()
        val prop = IcsDateProperty("20231024T100000Z", "DTSTART")
        val instant = builder.parseIsoDate(prop)
        assertEquals(Instant.parse("2023-10-24T10:00:00Z"), instant)
    }

    @Test
    fun testExtractTimeZone() {
        val builder = IcsEventBuilder()
        val tz = builder.extractTimeZone("DTSTART;TZID=Europe/London")
        assertEquals("Europe/London", tz.id)
    }

    @Test
    fun testExtractTimeZoneFallbackToSystem() {
        val builder = IcsEventBuilder()
        val tz = builder.extractTimeZone("DTSTART;TZID=Invalid/Timezone")
        assertEquals(
            kotlinx.datetime.TimeZone
                .currentSystemDefault()
                .id,
            tz.id,
        )
    }

    @Test
    fun testBuildMissingDtStart() {
        val builder = IcsEventBuilder()
        builder.processLine("SUMMARY:Missing start")
        assertNull(builder.build(1L))
    }

    @Test
    fun testBuildInvalidDtStartReturnsNull() {
        val builder = IcsEventBuilder()
        builder.processLine("DTSTART:123") // Too short, parseDate will return null
        assertNull(builder.build(1L))
    }

    @Test
    fun testBuildMissingDtEndFallsBackToDtStart() {
        val builder = IcsEventBuilder()
        builder.processLine("DTSTART:20231024") // All day
        val event = builder.build(1L)
        assertTrue(event != null)
        assertEquals(event.startAt, event.endAt)
    }

    @Test
    fun testBuildInvalidDtEndFallsBackToDtStart() {
        val builder = IcsEventBuilder()
        builder.processLine("DTSTART:20231024T100000Z")
        builder.processLine("DTEND:invalid")
        val event = builder.build(1L)
        assertTrue(event != null)
        assertEquals(event.startAt, event.endAt)
    }

    @Test
    fun testParseDateReturnsNullOnException() {
        val builder = IcsEventBuilder()
        builder.processLine("DTSTART:20231024T") // length < 15, parseIsoDate throws IllegalArgumentException
        assertNull(builder.build(1L))
    }
}
