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
    fun canUseMode_returnsCorrectPermissions() {
        val noPacks = PackRegistry(emptySet(), emptySet())
        val studentPack = PackRegistry(emptySet(), setOf(AppPack.STUDENT.key))
        val financePack = PackRegistry(emptySet(), setOf(AppPack.FINANCE.key))
        val maintenancePack = PackRegistry(emptySet(), setOf(AppPack.MAINTENANCE.key))
        val protocolsPack = PackRegistry(emptySet(), setOf(AppPack.PROTOCOLS.key))

        // STUDY
        assertFalse(noPacks.canUseMode("STUDY"))
        assertTrue(studentPack.canUseMode("STUDY"))
        assertTrue(studentPack.canUseMode("study")) // Case sensitivity

        // ADMIN
        assertFalse(noPacks.canUseMode("ADMIN"))
        assertTrue(financePack.canUseMode("ADMIN"))
        assertTrue(maintenancePack.canUseMode("ADMIN"))

        // ERRAND
        assertFalse(noPacks.canUseMode("ERRAND"))
        assertTrue(maintenancePack.canUseMode("ERRAND"))

        // SHUTDOWN
        assertFalse(noPacks.canUseMode("SHUTDOWN"))
        assertTrue(protocolsPack.canUseMode("SHUTDOWN"))

        // Unknown modes
        assertTrue(noPacks.canUseMode("UNKNOWN"))
        assertTrue(noPacks.canUseMode(""))
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

    @Test
    fun packRegistry_reportsMissingDependencies() {
        // AppPack.FINANCE has a dependency on "maintenance"
        val registry =
            PackRegistry(
                ownedPackKeys = setOf(AppPack.FINANCE.key, AppPack.MAINTENANCE.key),
                enabledPackKeys = setOf(AppPack.FINANCE.key), // Only FINANCE enabled, missing MAINTENANCE
            )

        val missing = registry.validateEnabledPacks()

        // Expected string format: "<packKey>:missing:<depKey>"
        val expectedError = "${AppPack.FINANCE.key}:missing:${AppPack.MAINTENANCE.key}"

        assertTrue(missing.contains(expectedError))
        assertTrue(missing.size == 1)
    }

    @Test
    /**
     * Verifies that [PackRegistry.isOwned] returns true only for packs present in `ownedPackKeys`.
     */
    fun packRegistry_reportsOwnedPacks() {
        val registry =
            PackRegistry(
                ownedPackKeys = setOf(AppPack.STUDENT.key, AppPack.CREATOR.key),
                enabledPackKeys = emptySet(),
            )
        assertTrue(registry.isOwned(AppPack.STUDENT))
        assertTrue(registry.isOwned(AppPack.CREATOR))
        assertFalse(registry.isOwned(AppPack.FINANCE))
        assertFalse(registry.isOwned(AppPack.MAINTENANCE))
    }

    @Test
    fun isDependencySatisfied_returnsTrueWhenNoDependencies() {
        val registry = PackRegistry(ownedPackKeys = emptySet(), enabledPackKeys = emptySet())
        assertTrue(registry.isDependencySatisfied(AppPack.STUDENT))
    }

    @Test
    fun isDependencySatisfied_returnsTrueWhenDependenciesMet() {
        val registry =
            PackRegistry(
                ownedPackKeys = emptySet(),
                enabledPackKeys = setOf(AppPack.MAINTENANCE.key),
            )
        assertTrue(registry.isDependencySatisfied(AppPack.FINANCE))
    }

    @Test
    fun isDependencySatisfied_returnsFalseWhenDependenciesNotMet() {
        val registry = PackRegistry(ownedPackKeys = emptySet(), enabledPackKeys = emptySet())
        assertFalse(registry.isDependencySatisfied(AppPack.FINANCE))
    }

}
