/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import androidx.compose.runtime.Immutable

/**
 * Represents the available feature packs in TajsOS.
 *
 * @property key The unique identifier string for the pack.
 * @property displayName Pre-computed localized or display-ready label for UI performance.
 * @property isFree Indicates whether the pack is available for free or requires purchase/subscription.
 * @property dependencies A set of pack keys that must be enabled for this pack to function. Defaults to an empty set.
 */
enum class AppPack(
    val key: String,
    val displayName: String,
    val isFree: Boolean,
    val dependencies: Set<String> = emptySet(),
) {
    STUDENT("student", "Student", isFree = false),
    CREATOR("creator", "Creator", isFree = false),
    FINANCE("finance", "Finance", isFree = false, dependencies = setOf("maintenance")),
    PEOPLE("people", "People", isFree = false),
    MAINTENANCE("maintenance", "Maintenance", isFree = true),
    PROTOCOLS("protocols", "Protocols", isFree = true),
    ;

    companion object {
        val defaultFreePackKeys: Set<String> =
            entries.mapNotNullTo(mutableSetOf()) { if (it.isFree) it.key else null }
    }
}

/**
 * Manages the state of owned and enabled feature packs for the user.
 *
 * @property ownedPackKeys The set of pack keys that the user currently owns.
 * @property enabledPackKeys The set of pack keys that the user has currently enabled.
 */
@Immutable
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
     * Checks whether all dependencies required by the given pack are enabled.
     *
     * @param pack The AppPack whose dependencies to check.
     * @return `true` if all dependencies are enabled, `false` otherwise.
     */
    fun isDependencySatisfied(pack: AppPack): Boolean = enabledPackKeys.containsAll(pack.dependencies)

    /**
     * Determines whether the specified operating mode is permitted by the currently enabled packs.
     *
     * Mode requirements:
     * - "STUDY" requires the STUDENT pack.
     * - "ADMIN" requires the FINANCE pack or the MAINTENANCE pack.
     * - "ERRAND" requires the MAINTENANCE pack.
     * - "SHUTDOWN" requires the PROTOCOLS pack.
     * - Any other mode is permitted.
     *
     * @param modeKey The mode identifier (comparison is case-insensitive).
     * @return `true` if the required packs for the mode are enabled, `false` otherwise.
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
     * Finds missing dependencies for enabled packs that the user owns.
     *
     * Checks only packs that are both enabled and owned and reports each dependency
     * that is not enabled.
     *
     * @return `Set<String>` of messages formatted as "<packKey>:missing:<depKey>" for each missing dependency; empty set if none.
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
