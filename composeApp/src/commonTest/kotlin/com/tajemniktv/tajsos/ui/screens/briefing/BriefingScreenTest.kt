/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.briefing

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for the pure utility functions extracted from BriefingScreen.kt.
 *
 * These tests cover [briefingPeriodForHour], [relativeHourDiff], and [formatClockTime]
 * without requiring a Compose runtime.
 */
class BriefingScreenTest {

    // ── briefingPeriodForHour ─────────────────────────────────────────────────

    @Test
    fun briefingPeriodForHour_hour5_returnsMorning() {
        assertEquals("morning", briefingPeriodForHour(5))
    }

    @Test
    fun briefingPeriodForHour_hour11_returnsMorning() {
        assertEquals("morning", briefingPeriodForHour(11))
    }

    @Test
    fun briefingPeriodForHour_hour12_returnsAfternoon() {
        assertEquals("afternoon", briefingPeriodForHour(12))
    }

    @Test
    fun briefingPeriodForHour_hour17_returnsAfternoon() {
        assertEquals("afternoon", briefingPeriodForHour(17))
    }

    @Test
    fun briefingPeriodForHour_hour18_returnsEvening() {
        assertEquals("evening", briefingPeriodForHour(18))
    }

    @Test
    fun briefingPeriodForHour_hour22_returnsEvening() {
        assertEquals("evening", briefingPeriodForHour(22))
    }

    @Test
    fun briefingPeriodForHour_hour23_returnsNight() {
        assertEquals("night", briefingPeriodForHour(23))
    }

    @Test
    fun briefingPeriodForHour_hour0_returnsNight() {
        assertEquals("night", briefingPeriodForHour(0))
    }

    @Test
    fun briefingPeriodForHour_hour4_returnsNight() {
        assertEquals("night", briefingPeriodForHour(4))
    }

    @Test
    fun briefingPeriodForHour_allMorningHoursReturnMorning() {
        (5..11).forEach { hour ->
            assertEquals("morning", briefingPeriodForHour(hour), "Expected morning for hour=$hour")
        }
    }

    @Test
    fun briefingPeriodForHour_allAfternoonHoursReturnAfternoon() {
        (12..17).forEach { hour ->
            assertEquals("afternoon", briefingPeriodForHour(hour), "Expected afternoon for hour=$hour")
        }
    }

    @Test
    fun briefingPeriodForHour_allEveningHoursReturnEvening() {
        (18..22).forEach { hour ->
            assertEquals("evening", briefingPeriodForHour(hour), "Expected evening for hour=$hour")
        }
    }

    @Test
    fun briefingPeriodForHour_nightHoursReturnNight() {
        val nightHours = (0..4) + listOf(23)
        nightHours.forEach { hour ->
            assertEquals("night", briefingPeriodForHour(hour), "Expected night for hour=$hour")
        }
    }

    // ── relativeHourDiff ──────────────────────────────────────────────────────

    @Test
    fun relativeHourDiff_sameTimestamp_returnsZero() {
        val now = 1_000_000_000L
        assertEquals(0L, relativeHourDiff(now, now))
    }

    @Test
    fun relativeHourDiff_updatedAtInFuture_returnsZero() {
        val now = 1_000_000_000L
        val future = now + 3_600_001L
        assertEquals(0L, relativeHourDiff(now, future))
    }

    @Test
    fun relativeHourDiff_exactly1HourAgo_returns1() {
        val now = 3_600_000L
        val updatedAt = 0L
        assertEquals(1L, relativeHourDiff(now, updatedAt))
    }

    @Test
    fun relativeHourDiff_almostOneHour_returnsZero() {
        val now = 3_599_999L
        val updatedAt = 0L
        assertEquals(0L, relativeHourDiff(now, updatedAt))
    }

    @Test
    fun relativeHourDiff_23hoursAgo_returns23() {
        val hourMs = 3_600_000L
        val now = 23 * hourMs
        val updatedAt = 0L
        assertEquals(23L, relativeHourDiff(now, updatedAt))
    }

    @Test
    fun relativeHourDiff_24hoursAgo_returns24() {
        val hourMs = 3_600_000L
        val now = 24 * hourMs
        val updatedAt = 0L
        assertEquals(24L, relativeHourDiff(now, updatedAt))
    }

    @Test
    fun relativeHourDiff_48hoursAgo_returns48() {
        val hourMs = 3_600_000L
        val now = 48 * hourMs
        val updatedAt = 0L
        assertEquals(48L, relativeHourDiff(now, updatedAt))
    }

    @Test
    fun relativeHourDiff_oneMinuteAgo_returnsZero() {
        val now = 60_000L
        val updatedAt = 0L
        assertEquals(0L, relativeHourDiff(now, updatedAt))
    }

    // ── formatClockTime ───────────────────────────────────────────────────────

    @Test
    fun formatClockTime_outputHasCorrectLength() {
        // Any epoch ms should produce exactly "HH:MM" = 5 chars
        val result = formatClockTime(0L)
        assertEquals(5, result.length, "formatClockTime output should be exactly 5 characters")
    }

    @Test
    fun formatClockTime_outputMatchesHHMMPattern() {
        // Verify that the output matches HH:MM format (two digits, colon, two digits)
        val result = formatClockTime(0L)
        val pattern = Regex("""\d{2}:\d{2}""")
        assertTrue(pattern.matches(result), "formatClockTime should return HH:MM format, got: $result")
    }

    @Test
    fun formatClockTime_hourAndMinuteAreValid() {
        val result = formatClockTime(0L)
        val parts = result.split(":")
        assertEquals(2, parts.size)
        val hours = parts[0].toInt()
        val minutes = parts[1].toInt()
        assertTrue(hours in 0..23, "Hour should be 0-23, got $hours")
        assertTrue(minutes in 0..59, "Minute should be 0-59, got $minutes")
    }

    @Test
    fun formatClockTime_largeTimestampProducesValidOutput() {
        // Use a known large timestamp and verify format consistency
        val epochMs = 1_700_000_000_000L // ~Nov 2023
        val result = formatClockTime(epochMs)
        assertEquals(5, result.length)
        val pattern = Regex("""\d{2}:\d{2}""")
        assertTrue(pattern.matches(result), "formatClockTime should return HH:MM format for large timestamps")
    }
}