/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.domain

import androidx.compose.ui.graphics.vector.ImageVector
import com.tajemniktv.tajsos.domain.DomainKind
import com.tajemniktv.tajsos.ui.Screen
import org.jetbrains.compose.resources.StringResource

/**
 * Represents a built-in domain used by navigation and domain modules.
 * This class represents a product-level lens over shared system data,
 * providing the required metadata (route, label, icon) for UI presentation.
 *
 * @property kind The [DomainKind] enum value representing the specific domain.
 * @property screen The [Screen] associated with this domain, determining navigation route and visual presentation.
 * @property capabilities An optional set of capability strings defining specific features available within this domain.
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
 *
 * Provides lazy initialization and lookup for the predefined [DomainDefinition]s
 * supported by TajsOS. It is utilized across the application to render domain-specific
 * navigation options and resolve domains based on routes or [DomainKind].
 */
object DomainRegistry {
    val definitions: List<DomainDefinition> by lazy {
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
    }

    /**
     * A lazily initialized list of all [Screen]s associated with the defined domains.
     */
    val screens: List<Screen> by lazy { definitions.map { it.screen } }

    /**
     * Retrieves the [DomainDefinition] corresponding to the given [DomainKind].
     *
     * @param kind The [DomainKind] to search for.
     * @return The matching [DomainDefinition], or null if no such domain is defined.
     */
    fun byKind(kind: DomainKind): DomainDefinition? = definitions.find { it.kind == kind }

    /**
     * Retrieves the [DomainDefinition] whose base route matches the provided route string.
     * Extracts the base route from the given string by stripping out sub-paths and query parameters.
     *
     * @param route The full navigation route string, or null.
     * @return The matching [DomainDefinition], or null if the route does not match any domain.
     */
    fun byRoute(route: String?): DomainDefinition? {
        if (route == null) return null
        val base = route.substringBefore('/').substringBefore('?')
        return definitions.find { it.route.substringBefore('/') == base }
    }
}
