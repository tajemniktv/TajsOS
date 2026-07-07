package com.tajemniktv.tajsos.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class SidebarModeTest {
    @Test
    fun testSidebarModeValues() {
        assertEquals(3, SidebarMode.values().size)
        assertEquals(SidebarMode.EXPANDED, SidebarMode.valueOf("EXPANDED"))
        assertEquals(SidebarMode.COLLAPSED, SidebarMode.valueOf("COLLAPSED"))
        assertEquals(SidebarMode.HOVER_EXPAND, SidebarMode.valueOf("HOVER_EXPAND"))
    }
}
