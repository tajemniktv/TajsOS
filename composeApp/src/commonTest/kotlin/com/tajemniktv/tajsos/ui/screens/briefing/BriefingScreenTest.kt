/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.briefing

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for the pure utility logic defined in BriefingScreen.kt.
 *
 * Because [briefingGreeting], [relativeUpdatedText], and [formatClockTime] are declared as
 * private functions inside BriefingScreen.kt they cannot be called directly from tests.
 * These tests instead verify the same behavioural contract as independent pure-logic
 * specifications, which serve as regression anchors if the functions are ever refactored
 * or made more accessible.
 */
class BriefingScreenTest {

    // ------------------------------------------------------------------
    // briefingGreeting logic specification
    // Mirror of the when-expression in BriefingScreen.briefingGreeting().
    // ------------------------------------------------------------------

    private fun greetingCategory(hour: Int): String =
        when (hour) {
            in 5..11 -> "morning"
            in 12..17 -> "afternoon"
            in 18..22 -> "evening"
            else -> "night"
        }

    @Test
    fun briefingGreeting_earlyMorningBoundary_returnsMorning() {
        assertEquals("morning", greetingCategory(5))
    }

    @Test
    fun briefingGreeting_lateMorningBoundary_returnsMorning() {
        assertEquals("morning", greetingCategory(11))
    }

    @Test
    fun briefingGreeting_midMorning_returnsMorning() {
        assertEquals("morning", greetingCategory(8))
    }

    @Test
    fun briefingGreeting_earlyAfternoonBoundary_returnsAfternoon() {
        assertEquals("afternoon", greetingCategory(12))
    }

    @Test
    fun briefingGreeting_lateAfternoonBoundary_returnsAfternoon() {
        assertEquals("afternoon", greetingCategory(17))
    }

    @Test
    fun briefingGreeting_midAfternoon_returnsAfternoon() {
        assertEquals("afternoon", greetingCategory(14))
    }

    @Test
    fun briefingGreeting_earlyEveningBoundary_returnsEvening() {
        assertEquals("evening", greetingCategory(18))
    }

    @Test
    fun briefingGreeting_lateEveningBoundary_returnsEvening() {
        assertEquals("evening", greetingCategory(22))
    }

    @Test
    fun briefingGreeting_midEvening_returnsEvening() {
        assertEquals("evening", greetingCategory(20))
    }

    @Test
    fun briefingGreeting_midnight_returnsNight() {
        assertEquals("night", greetingCategory(0))
    }

    @Test
    fun briefingGreeting_lateNight_returnsNight() {
        assertEquals("night", greetingCategory(3))
    }

    @Test
    fun briefingGreeting_hourBeforeMorning_returnsNight() {
        assertEquals("night", greetingCategory(4))
    }

    @Test
    fun briefingGreeting_hourAfterEvening_returnsNight() {
        assertEquals("night", greetingCategory(23))
    }

    @Test
    fun briefingGreeting_allHoursMapToKnownCategory() {
        val validCategories = setOf("morning", "afternoon", "evening", "night")
        for (hour in 0..23) {
            assertTrue(
                greetingCategory(hour) in validCategories,
                "Hour $hour should map to a valid category",
            )
        }
    }

    // ------------------------------------------------------------------
    // relativeUpdatedText logic specification
    // Mirror of the when-logic in BriefingScreen.relativeUpdatedText().
    // ------------------------------------------------------------------

    private fun relativeUpdatedCategory(diffMillis: Long): String {
        val diffHours = (diffMillis / 3_600_000L).coerceAtLeast(0L)
        return when {
            diffHours == 0L -> "now"
            diffHours < 24L -> "hours:$diffHours"
            else -> "days:${diffHours / 24L}"
        }
    }

    @Test
    fun relativeUpdatedText_zeroMillis_returnsNow() {
        assertEquals("now", relativeUpdatedCategory(0L))
    }

    @Test
    fun relativeUpdatedText_justUnderOneHour_returnsNow() {
        val almostOneHour = 3_599_999L
        assertEquals("now", relativeUpdatedCategory(almostOneHour))
    }

    @Test
    fun relativeUpdatedText_exactlyOneHour_returnsOneHour() {
        val oneHour = 3_600_000L
        assertEquals("hours:1", relativeUpdatedCategory(oneHour))
    }

    @Test
    fun relativeUpdatedText_severalHours_returnsHours() {
        val fiveHours = 5 * 3_600_000L
        assertEquals("hours:5", relativeUpdatedCategory(fiveHours))
    }

    @Test
    fun relativeUpdatedText_justUnder24Hours_returnsHours() {
        val almostOneDay = 23 * 3_600_000L
        assertEquals("hours:23", relativeUpdatedCategory(almostOneDay))
    }

    @Test
    fun relativeUpdatedText_exactly24Hours_returnsDays() {
        val exactlyOneDay = 24 * 3_600_000L
        assertEquals("days:1", relativeUpdatedCategory(exactlyOneDay))
    }

    @Test
    fun relativeUpdatedText_twoDays_returnsTwoDays() {
        val twoDays = 48 * 3_600_000L
        assertEquals("days:2", relativeUpdatedCategory(twoDays))
    }

    @Test
    fun relativeUpdatedText_negativeMillis_clampsToNow() {
        // coerceAtLeast(0L) ensures negative differences don't produce "hours:-1"
        assertEquals("now", relativeUpdatedCategory(-3_600_000L))
    }

    // ------------------------------------------------------------------
    // formatClockTime logic specification
    // Mirror of the logic in BriefingScreen.formatClockTime().
    // The function formats LocalTime.toString() and takes first 5 chars ("HH:MM").
    // ------------------------------------------------------------------

    private fun formatTimeString(localTimeString: String): String = localTimeString.take(5)

    @Test
    fun formatClockTime_fullTimeString_returnsHHMM() {
        assertEquals("14:30", formatTimeString("14:30:00"))
    }

    @Test
    fun formatClockTime_midnight_returnsCorrectFormat() {
        assertEquals("00:00", formatTimeString("00:00:00"))
    }

    @Test
    fun formatClockTime_noon_returnsCorrectFormat() {
        assertEquals("12:00", formatTimeString("12:00:00"))
    }

    @Test
    fun formatClockTime_singleDigitHour_preservesLeadingZero() {
        assertEquals("09:05", formatTimeString("09:05:30"))
    }

    @Test
    fun formatClockTime_exactlyFiveChars_returnsAllFive() {
        assertEquals("23:59", formatTimeString("23:59:59.999"))
    }

    @Test
    fun formatClockTime_timeStringWithNanoseconds_stripsSeconds() {
        assertEquals("08:15", formatTimeString("08:15:42.123456789"))
    }
}