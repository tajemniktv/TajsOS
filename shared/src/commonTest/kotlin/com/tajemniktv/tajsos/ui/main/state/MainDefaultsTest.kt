/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MainDefaultsTest {
    @Test
    fun suggestedAreas_areGenericAndExcludeBuiltInDomainNames() {
        val normalized = suggestedAreaTitles.map { it.trim().lowercase() }

        assertFalse(normalized.contains("finances"))
        assertFalse(normalized.contains("health"))
        assertFalse(normalized.contains("education"))
        assertFalse(normalized.contains("relationships"))
        assertFalse(normalized.contains("money"))

        assertTrue(normalized.contains("university"))
        assertTrue(normalized.contains("apartment"))
    }
}
