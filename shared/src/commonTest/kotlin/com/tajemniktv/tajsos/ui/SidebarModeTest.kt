package com.tajemniktv.tajsos.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class SidebarModeTest {

    @Test
    fun testSidebarModeValues() {
        val modes = SidebarMode.entries
        assertEquals(3, modes.size)
        assertEquals(SidebarMode.EXPANDED, modes[0])
        assertEquals(SidebarMode.COLLAPSED, modes[1])
        assertEquals(SidebarMode.HOVER_EXPAND, modes[2])
    }
}
