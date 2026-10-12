/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.ui.platform

import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.text.AnnotatedString

/**
 * Creates a [ClipEntry] from an [AnnotatedString] in a way that works across platforms.
 * This is a workaround for the current lack of a common constructor for ClipEntry in Compose Multiplatform.
 * [label] is an optional non-secret localized description. [sensitive] suppresses system previews
 * on platforms that support sensitive clipboard metadata; it does not restrict paste access.
 */
expect fun AnnotatedString.toClipEntry(label: String? = null, sensitive: Boolean = false): ClipEntry
