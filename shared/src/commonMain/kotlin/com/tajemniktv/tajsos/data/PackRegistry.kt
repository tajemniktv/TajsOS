/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

/**
 * Represents the available feature packs in TajsOS.
 *
 * @property key The unique identifier string for the pack.
 * @property isFree Indicates whether the pack is available for free or requires purchase/subscription.
 * @property dependencies A set of pack keys that must be enabled for this pack to function. Defaults to an empty set.
 */
enum class AppPack(
    val key: String,
    val isFree: Boolean,
    val dependencies: Set<String> = emptySet(),
) {
    STUDENT("student", isFree = false),
    CREATOR("creator", isFree = false),
    FINANCE("finance", isFree = false, dependencies = setOf("maintenance")),
    PEOPLE("people", isFree = false),
    MAINTENANCE("maintenance", isFree = true),
    PROTOCOLS("protocols", isFree = true),
    ;

    companion object {
        val defaultFreePackKeys: Set<String> =
            entries.filter { it.isFree }.map { it.key }.toSet()
    }
}

/**
 * Manages the state of owned and enabled feature packs for the user.
 *
 * @property ownedPackKeys The set of pack keys that the user currently owns.
 * @property enabledPackKeys The set of pack keys that the user has currently enabled.
 */
data class PackRegistry(
    val ownedPackKeys: Set<String>,
    val enabledPackKeys: Set<String>,
) {
    /**
     * Checks if the user owns a specific feature pack.
     *
     * @param pack The [AppPack] to check ownership for.
     * @return `true` if the user owns the pack, `false` otherwise.
     */
    fun isOwned(pack: AppPack): Boolean = ownedPackKeys.contains(pack.key)

    /**
     * Checks if a specific feature pack is currently enabled by the user.
     *
     * @param pack The [AppPack] to check enablement for.
     * @return `true` if the pack is enabled, `false` otherwise.
     */
    fun isEnabled(pack: AppPack): Boolean = enabledPackKeys.contains(pack.key)

    /**
     * Verifies if all dependencies required by a specific feature pack are currently enabled.
     *
     * @param pack The [AppPack] whose dependencies are to be checked.
     * @return `true` if all dependencies are enabled, `false` if one or more dependencies are missing.
     */
    fun isDependencySatisfied(pack: AppPack): Boolean = enabledPackKeys.containsAll(pack.dependencies)

    /**
     * Determines if a specific operating mode can be used based on the currently enabled packs.
     *
     * @param modeKey The string identifier of the operating mode (e.g., "STUDY", "ADMIN").
     * @return `true` if the necessary packs for the mode are enabled, `false` otherwise. Defaults to `true` for unrecognized modes.
     */
    fun canUseMode(modeKey: String): Boolean =
        when (modeKey.uppercase())
        {
            "STUDY" -> isEnabled(AppPack.STUDENT)
            "ADMIN" -> isEnabled(AppPack.FINANCE) || isEnabled(AppPack.MAINTENANCE)
            "ERRAND" -> isEnabled(AppPack.MAINTENANCE)
            "SHUTDOWN" -> isEnabled(AppPack.PROTOCOLS)
            else -> true
        }

    /**
     * Validates the currently enabled packs against their dependencies.
     *
     * @return A set of string messages indicating missing dependencies for enabled and owned packs. Returns an empty set if all dependencies are satisfied.
     */
    fun validateEnabledPacks(): Set<String> =
        AppPack.entries
            .filter { enabledPackKeys.contains(it.key) && ownedPackKeys.contains(it.key) }
            .flatMap { pack ->
                pack.dependencies
                    .filterNot { dep -> enabledPackKeys.contains(dep) }
                    .map { dep -> "${pack.key}:missing:$dep" }
            }.toSet()
}
