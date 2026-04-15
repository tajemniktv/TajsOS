/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.platform

import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.text.AnnotatedString

/**
 * Creates a [ClipEntry] from an [AnnotatedString] in a way that works across platforms.
 * This is a workaround for the current lack of a common constructor for ClipEntry in Compose Multiplatform.
 */
expect fun AnnotatedString.toClipEntry(): ClipEntry
