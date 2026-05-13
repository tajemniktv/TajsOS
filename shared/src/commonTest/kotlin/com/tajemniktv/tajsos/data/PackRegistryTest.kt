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
        assertTrue(studentPack.canUseMode("StUdY"))

        // ADMIN
        assertFalse(noPacks.canUseMode("ADMIN"))
        assertTrue(financePack.canUseMode("ADMIN"))
        assertTrue(financePack.canUseMode("admin"))
        assertTrue(maintenancePack.canUseMode("ADMIN"))
        assertTrue(maintenancePack.canUseMode("AdMiN"))

        // ERRAND
        assertFalse(noPacks.canUseMode("ERRAND"))
        assertTrue(maintenancePack.canUseMode("ERRAND"))
        assertTrue(maintenancePack.canUseMode("errand"))

        // SHUTDOWN
        assertFalse(noPacks.canUseMode("SHUTDOWN"))
        assertTrue(protocolsPack.canUseMode("SHUTDOWN"))
        assertTrue(protocolsPack.canUseMode("shutdown"))

        // Unknown modes
        assertTrue(noPacks.canUseMode("UNKNOWN"))
        assertTrue(noPacks.canUseMode("unknown"))
        assertTrue(noPacks.canUseMode(""))
    }

    @Test
    fun isDependencySatisfied_returnsCorrectStatus() {
        // AppPack.FINANCE requires "maintenance"
        val registryWithDep = PackRegistry(emptySet(), setOf(AppPack.MAINTENANCE.key))
        val registryWithoutDep = PackRegistry(emptySet(), emptySet())

        assertTrue(registryWithDep.isDependencySatisfied(AppPack.FINANCE))
        assertFalse(registryWithoutDep.isDependencySatisfied(AppPack.FINANCE))

        // AppPack.STUDENT requires nothing
        assertTrue(registryWithoutDep.isDependencySatisfied(AppPack.STUDENT))
    }

    @Test
    fun isOwned_returnsCorrectStatus() {
        val registry = PackRegistry(setOf(AppPack.STUDENT.key), emptySet())
        assertTrue(registry.isOwned(AppPack.STUDENT))
        assertFalse(registry.isOwned(AppPack.FINANCE))
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
}
