/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.platform

import android.content.ClipData
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.text.AnnotatedString

@OptIn(ExperimentalComposeUiApi::class)
actual fun AnnotatedString.toClipEntry(): ClipEntry {
    val clipData = ClipData.newPlainText(this.text, this.text)
    return clipData.toClipEntry()
}
