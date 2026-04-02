/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import androidx.compose.runtime.Immutable

/**
 * A wrapper for a standard [List] that is marked as [Immutable] for the Compose compiler.
 *
 * Standard [List] interfaces are often treated as unstable by Compose because their underlying
 * implementation might be mutable (e.g., [ArrayList]). This wrapper explicitly promises that
 * the content will not change, enabling better recomposition performance.
 */
@Immutable
data class StableList<T>(
    /**
     * The underlying list of items.
     */
    val items: List<T>,
)

/**
 * Convenience extension to wrap a [List] into a [StableList].
 */
fun <T> List<T>.toStableList(): StableList<T> = StableList(this)
