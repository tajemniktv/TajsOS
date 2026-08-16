package com.tajemniktv.tajsos.ui.main.calculators

import kotlin.test.Test
import kotlin.test.assertEquals

class MainSnapshotCalculatorsInsightsTest {
    @Test
    fun testEmptyData() {
        val insights = calculateInsights(emptyList(), emptyList(), emptyList(), emptyList())
        assertEquals(0, insights.avgSessionMinutes)
        assertEquals(0, insights.bestFocusHour)
        assertEquals(0, insights.mostProductiveHour)
    }
}
