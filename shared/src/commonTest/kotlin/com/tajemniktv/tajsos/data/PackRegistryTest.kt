/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PackRegistryTest {
    @Test
    fun packRegistry_reportsEnabledPacks() {
        val registry =
            PackRegistry(
                ownedPackKeys = setOf(AppPack.STUDENT.key, AppPack.CREATOR.key),
                enabledPackKeys = setOf(AppPack.STUDENT.key, AppPack.CREATOR.key),
            )
        assertTrue(registry.isEnabled(AppPack.STUDENT))
        assertFalse(registry.isEnabled(AppPack.FINANCE))
    }

    @Test
    fun packRegistry_hasNoDependencyErrorsForCurrentPacks() {
        val all = AppPack.entries.map { it.key }.toSet()
        val registry =
            PackRegistry(
                ownedPackKeys = all,
                enabledPackKeys = all,
            )
        assertTrue(registry.validateEnabledPacks().isEmpty())
    }
}
