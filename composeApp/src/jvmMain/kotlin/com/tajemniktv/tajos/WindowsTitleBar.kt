/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos

import androidx.compose.ui.graphics.Color
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.win32.StdCallLibrary
import com.sun.jna.ptr.IntByReference
import org.slf4j.LoggerFactory
import java.awt.Window
import kotlin.math.roundToInt

private const val DWMWA_USE_IMMERSIVE_DARK_MODE = 20
private const val DWMWA_CAPTION_COLOR = 35
private const val DWMWA_TEXT_COLOR = 36

private val titleBarLogger = LoggerFactory.getLogger("TajsOS.WindowChrome")

/** The small DWM surface used to theme the native caption without replacing its window controls. */
private interface DwmApi : StdCallLibrary {
    fun DwmSetWindowAttribute(hwnd: Pointer, attribute: Int, value: IntByReference, valueSize: Int): Int
}

/**
 * Matches the Windows title bar to the current app palette while retaining native resize and Snap.
 * Unsupported Windows versions and non-Windows hosts keep their system-provided title bar.
 */
internal fun styleNativeTitleBar(
    window: Window,
    background: Color,
    foreground: Color,
    darkTheme: Boolean,
) {
    if (!System.getProperty("os.name").startsWith("Windows", ignoreCase = true)) return

    runCatching {
        val dwm = Native.load("dwmapi", DwmApi::class.java)
        val hwnd = Native.getWindowPointer(window)
        fun setAttribute(attribute: Int, value: Int) {
            val result = dwm.DwmSetWindowAttribute(hwnd, attribute, IntByReference(value), Int.SIZE_BYTES)
            if (result != 0) {
                titleBarLogger.debug("DWM title-bar attribute {} was not accepted: {}", attribute, result)
            }
        }

        setAttribute(DWMWA_USE_IMMERSIVE_DARK_MODE, if (darkTheme) 1 else 0)
        setAttribute(DWMWA_CAPTION_COLOR, background.toColorRef())
        setAttribute(DWMWA_TEXT_COLOR, foreground.toColorRef())
    }.onFailure { error ->
        titleBarLogger.debug("Native title-bar styling is unavailable", error)
    }
}

/** Converts an opaque Compose color to the Win32 COLORREF byte order (0x00BBGGRR). */
private fun Color.toColorRef(): Int =
    (red.times(255).roundToInt() and 0xFF) or
        ((green.times(255).roundToInt() and 0xFF) shl 8) or
        ((blue.times(255).roundToInt() and 0xFF) shl 16)
