/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.ui.platform

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.text.AnnotatedString
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/** Checks desktop transfer bytes without reading or writing the user's system clipboard. */
class ClipboardUtilsTest {
    @OptIn(ExperimentalComposeUiApi::class)
    @Test
    fun privateBackupTransferPreservesFullUnicodePayload() {
        val json = "{\"body\":\"" + "Zażółć — 日本語 🌱\n".repeat(900) + "\"}"
        val clip = AnnotatedString(json).toClipEntry(label = "Synthetic backup", sensitive = true)
        val transferable = assertIs<Transferable>(clip.nativeClipEntry)
        assertEquals(json, transferable.getTransferData(DataFlavor.stringFlavor))
    }
}
