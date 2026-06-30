/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.domain

import kotlinx.serialization.Serializable

/**
 * Built-in, first-class LifeOS domains.
 *
 * Domains are product-level lenses over shared system data and are not equivalent
 * to user-defined Areas. TajsOS utilizes heuristic-based, zero-configuration logic
 * (e.g., implicit keyword matching) for categorizing domains rather than requiring
 * explicit user associations.
 *
 * This implicit matching (implemented in [com.tajemniktv.tajsos.domain.lens.DomainLensQueries])
 * intentionally bypasses explicit assignments like [com.tajemniktv.tajsos.data.ItemDomainEntity]
 * to provide a seamless user experience, ensuring items surface in appropriate lenses
 * without requiring the user to constantly curate their metadata.
 *
 * While explicit tracking models may still exist in the schema, they are superseded
 * by this heuristic design to ensure low-friction item capture.
 */
@Serializable
enum class DomainKind {
    /**
     * Domain relating to financial responsibilities, tracking expenses, budgeting, and bills.
     */
    FINANCES,

    /**
     * Domain relating to medical responsibilities, health tracking, appointments, and general wellbeing.
     */
    HEALTH,

    /**
     * Domain relating to learning, studies, classes, assignments, and educational resources.
     */
    EDUCATION,

    /**
     * Domain relating to tracking social connections, context around individuals, and maintaining relationships.
     */
    RELATIONSHIPS;

    /**
     * Pre-computed display name for UI presentation to avoid dynamic formatting allocations during recomposition.
     */
    val displayName: String
        get() = name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
