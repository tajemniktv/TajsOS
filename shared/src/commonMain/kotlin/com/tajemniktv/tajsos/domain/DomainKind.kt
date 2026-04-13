/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.domain

import kotlinx.serialization.Serializable

/**
 * Built-in, first-class LifeOS domains.
 *
 * Domains are product-level lenses over shared system data and are not equivalent
 * to user-defined Areas.
 */
@Serializable
enum class DomainKind {
    FINANCES,
    HEALTH,
    EDUCATION,
    RELATIONSHIPS,
}
