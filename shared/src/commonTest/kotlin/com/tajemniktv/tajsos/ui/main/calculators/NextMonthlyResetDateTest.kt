package com.tajemniktv.tajsos.ui.main.calculators

import kotlin.test.Test
import kotlin.test.assertTrue

class NextMonthlyResetDateTest {
    @Test
    fun testNextMonthlyResetDate() {
        val result = nextMonthlyResetDate()
        assertTrue(result.matches(Regex("\\d{4}-\\d{2}-01")))
    }
}
