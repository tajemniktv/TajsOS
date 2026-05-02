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
    RELATIONSHIPS,
}
