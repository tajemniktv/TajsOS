/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.areas

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Suppress("TestMethodWithoutAssertion")
class AreasNavigationTest {
    @Test
    fun areaClick_alwaysRoutesToAreaDetail() {
        val route = routeForAreaDetail(42L)
        assertEquals("area/42", route)
        assertTrue(!route.startsWith("finances"))
        assertTrue(!route.startsWith("health"))
        assertTrue(!route.startsWith("education"))
        assertTrue(!route.startsWith("relationships"))
    }
}
