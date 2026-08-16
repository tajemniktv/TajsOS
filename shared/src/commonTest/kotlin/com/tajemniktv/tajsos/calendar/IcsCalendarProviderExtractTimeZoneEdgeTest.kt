package com.tajemniktv.tajsos.calendar

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.TimeZone

class IcsCalendarProviderExtractTimeZoneEdgeTest {

    @Test
    fun testExtractTimeZoneEdgeCases() {
        val builder = IcsEventBuilder()
        val defaultTzId = TimeZone.currentSystemDefault().id

        // Case 1: Empty TZID value should safely fall back to system default
        // because TimeZone.of("") throws IllegalArgumentException
        val tzEmpty = builder.extractTimeZone("DTSTART;TZID=")
        assertEquals(defaultTzId, tzEmpty.id)

        // Case 2: Multiple TZID parameters in a single key (malformed)
        // should extract the first valid one
        val tzMultiple = builder.extractTimeZone("DTSTART;TZID=Europe/Paris;TZID=Europe/London")
        assertEquals("Europe/Paris", tzMultiple.id)

        // Case 3: No TZID match at all should fall back
        val tzNone = builder.extractTimeZone("DTSTART;VALUE=DATE")
        assertEquals(defaultTzId, tzNone.id)

        // Case 4: TZID with spaces
        // TimeZone.of("  Europe/Paris  ") will throw IllegalArgumentException, should fallback
        val tzSpaces = builder.extractTimeZone("DTSTART;TZID=  Europe/Paris  ")
        assertEquals(defaultTzId, tzSpaces.id)

        // Case 5: TZID without the equals sign (won't match regex)
        val tzNoEquals = builder.extractTimeZone("DTSTART;TZIDEurope/Paris")
        assertEquals(defaultTzId, tzNoEquals.id)

        // Case 6: Trailing characters after valid TZID
        // It matches "Europe/London" because it is captured before the semicolon
        val tzTrailingChars = builder.extractTimeZone("DTSTART;TZID=Europe/London;VALUE=DATE-TIME")
        assertEquals("Europe/London", tzTrailingChars.id)
    }
}
