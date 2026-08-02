package com.tajemniktv.tajsos.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class SidebarModeTest {
    @Test
    fun testSidebarModeValues() {
        assertEquals("EXPANDED", SidebarMode.EXPANDED.name)
        assertEquals("COLLAPSED", SidebarMode.COLLAPSED.name)
        assertEquals("HOVER_EXPAND", SidebarMode.HOVER_EXPAND.name)
        assertEquals(3, SidebarMode.values().size)
    }
}
