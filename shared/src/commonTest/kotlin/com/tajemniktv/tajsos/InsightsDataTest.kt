/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos

import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.ui.InsightsData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for InsightsData data class and related constructs used by MainViewModel.
 * InsightsData is used as the output type of the insights StateFlow.
 */
class InsightsDataTest {
    // --- Default values ---

    @Test
    fun insightsData_defaultWeeklyCaptures_isZero() {
        val data = InsightsData()
        assertEquals(0, data.weeklyCaptures)
    }

    @Test
    fun insightsData_defaultWeeklyCompletions_isZero() {
        val data = InsightsData()
        assertEquals(0, data.weeklyCompletions)
    }

    @Test
    fun insightsData_defaultWeeklyFocusHours_isZero() {
        val data = InsightsData()
        assertEquals(0.0, data.weeklyFocusHours)
    }

    @Test
    fun insightsData_defaultBestFocusHour_isMinusOne() {
        val data = InsightsData()
        assertEquals(-1, data.bestFocusHour)
    }

    @Test
    fun insightsData_defaultAvgMood_isZero() {
        val data = InsightsData()
        assertEquals(0.0, data.avgMood)
    }

    @Test
    fun insightsData_defaultAvgEnergy_isZero() {
        val data = InsightsData()
        assertEquals(0.0, data.avgEnergy)
    }

    @Test
    fun insightsData_defaultAvgFocus_isZero() {
        val data = InsightsData()
        assertEquals(0.0, data.avgFocus)
    }

    @Test
    fun insightsData_defaultNeglectedProjects_isEmpty() {
        val data = InsightsData()
        assertTrue(data.neglectedProjects.isEmpty())
    }

    // --- Custom value construction ---

    @Test
    fun insightsData_customValues_areStoredCorrectly() {
        val project = NodeEntity(id = 1L, type = "project", title = "Old Project")
        val data =
            InsightsData(
                weeklyCaptures = 10,
                weeklyCompletions = 5,
                weeklyFocusHours = 3.5,
                bestFocusHour = 14,
                avgMood = 4.2,
                avgEnergy = 3.8,
                avgFocus = 4.0,
                neglectedProjects = listOf(project),
            )

        assertEquals(10, data.weeklyCaptures)
        assertEquals(5, data.weeklyCompletions)
        assertEquals(3.5, data.weeklyFocusHours)
        assertEquals(14, data.bestFocusHour)
        assertEquals(4.2, data.avgMood)
        assertEquals(3.8, data.avgEnergy)
        assertEquals(4.0, data.avgFocus)
        assertEquals(1, data.neglectedProjects.size)
        assertEquals("Old Project", data.neglectedProjects.first().title)
    }

    // --- Data class equality ---

    @Test
    fun insightsData_equalityWorks() {
        val a = InsightsData(weeklyCaptures = 3, weeklyCompletions = 1)
        val b = InsightsData(weeklyCaptures = 3, weeklyCompletions = 1)
        assertEquals(a, b)
    }

    @Test
    fun insightsData_copyWorks() {
        val original = InsightsData(weeklyCaptures = 5)
        val copy = original.copy(weeklyCaptures = 10)
        assertEquals(10, copy.weeklyCaptures)
        assertEquals(0, copy.weeklyCompletions) // unchanged default
    }

    // --- Boundary: negative bestFocusHour means no data ---

    @Test
    fun insightsData_bestFocusHourMinusOne_indicatesNoSessions() {
        val data = InsightsData()
        assertTrue(data.bestFocusHour < 0)
    }

    // --- Regression: bestFocusHour valid hours are 0-23 ---

    @Test
    fun insightsData_bestFocusHour_canRepresentMidnight() {
        val data = InsightsData(bestFocusHour = 0)
        assertEquals(0, data.bestFocusHour)
    }

    @Test
    fun insightsData_bestFocusHour_canRepresentLatestHour() {
        val data = InsightsData(bestFocusHour = 23)
        assertEquals(23, data.bestFocusHour)
    }
}
