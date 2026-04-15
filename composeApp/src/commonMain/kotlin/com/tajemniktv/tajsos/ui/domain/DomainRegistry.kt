/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.domain

import androidx.compose.ui.graphics.vector.ImageVector
import com.tajemniktv.tajsos.domain.DomainKind
import com.tajemniktv.tajsos.ui.Screen
import org.jetbrains.compose.resources.StringResource

/**
 * Built-in domain definition used by navigation and domain modules.
 */
data class DomainDefinition(
    val kind: DomainKind,
    val screen: Screen,
    val capabilities: Set<String> = emptySet(),
) {
    val route: String get() = screen.route
    val label: StringResource get() = screen.label
    val icon: ImageVector get() = screen.icon
}

/**
 * Registry of first-class LifeOS domains.
 */
object DomainRegistry {
    val definitions: List<DomainDefinition> =
        listOf(
            DomainDefinition(
                kind = DomainKind.FINANCES,
                screen = Screen.Finances,
                capabilities = setOf("maintenance", "liquidity", "renewals"),
            ),
            DomainDefinition(
                kind = DomainKind.HEALTH,
                screen = Screen.Health,
                capabilities = setOf("medications", "appointments", "care"),
            ),
            DomainDefinition(
                kind = DomainKind.EDUCATION,
                screen = Screen.Education,
                capabilities = setOf("learning", "courses", "study_sessions"),
            ),
            DomainDefinition(
                kind = DomainKind.RELATIONSHIPS,
                screen = Screen.Relationships,
                capabilities = setOf("followups", "crm", "shared_plans"),
            ),
        )

    val screens: List<Screen> = definitions.map { it.screen }

    fun byKind(kind: DomainKind): DomainDefinition? = definitions.find { it.kind == kind }

    fun byRoute(route: String?): DomainDefinition? {
        if (route == null) return null
        val base = route.substringBefore('/').substringBefore('?')
        return definitions.find { it.route.substringBefore('/') == base }
    }
}
