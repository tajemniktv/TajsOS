/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.platform

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.text.AnnotatedString

@OptIn(ExperimentalComposeUiApi::class)
actual fun AnnotatedString.toClipEntry(): ClipEntry = ClipEntry.withPlainText(this.text)
