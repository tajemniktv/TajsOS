package com.tajemniktv.tajsos.data

import kotlin.test.Test
import kotlin.test.assertEquals

class StabilityTest {
    @Test
    fun testStableList() {
        val originalList = listOf("a", "b", "c")
        val stableList = originalList.toStableList()

        assertEquals(originalList, stableList.items)
    }
}
