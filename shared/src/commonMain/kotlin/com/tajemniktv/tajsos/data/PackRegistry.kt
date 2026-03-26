/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

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

data class PackRegistry(
    val ownedPackKeys: Set<String>,
    val enabledPackKeys: Set<String>,
) {
    fun isOwned(pack: AppPack): Boolean = ownedPackKeys.contains(pack.key)

    fun isEnabled(pack: AppPack): Boolean = enabledPackKeys.contains(pack.key)

    fun isDependencySatisfied(pack: AppPack): Boolean = enabledPackKeys.containsAll(pack.dependencies)

    fun canUseMode(modeKey: String): Boolean =
        when (modeKey.uppercase())
        {
            "STUDY" -> isEnabled(AppPack.STUDENT)
            "ADMIN" -> isEnabled(AppPack.FINANCE) || isEnabled(AppPack.MAINTENANCE)
            "ERRAND" -> isEnabled(AppPack.MAINTENANCE)
            "SHUTDOWN" -> isEnabled(AppPack.PROTOCOLS)
            else -> true
        }

    fun validateEnabledPacks(): Set<String> =
        AppPack.entries
            .filter { enabledPackKeys.contains(it.key) && ownedPackKeys.contains(it.key) }
            .flatMap { pack ->
                pack.dependencies
                    .filterNot { dep -> enabledPackKeys.contains(dep) }
                    .map { dep -> "${pack.key}:missing:$dep" }
            }.toSet()
}
