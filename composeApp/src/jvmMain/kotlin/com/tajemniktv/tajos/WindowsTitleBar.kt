/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos

import androidx.compose.ui.graphics.Color
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.Structure
import com.sun.jna.win32.StdCallLibrary
import com.sun.jna.ptr.IntByReference
import org.slf4j.LoggerFactory
import java.awt.Window
import kotlin.math.roundToInt

private const val DWMWA_USE_IMMERSIVE_DARK_MODE = 20
private const val DWMWA_CAPTION_COLOR = 35
private const val DWMWA_TEXT_COLOR = 36
private const val DWMWA_SYSTEMBACKDROP_TYPE = 38
private const val DWMWA_REDIRECTIONBITMAP_ALPHA = 39
private const val DWMSBT_NONE = 1
private const val DWMSBT_TRANSIENTWINDOW = 3
private const val DWMWA_COLOR_DEFAULT = -1

private val titleBarLogger = LoggerFactory.getLogger("TajsOS.WindowChrome")

/** The small DWM surface used to theme the native caption without replacing its window controls. */
private interface DwmApi : StdCallLibrary {
    fun DwmSetWindowAttribute(hwnd: Pointer, attribute: Int, value: IntByReference, valueSize: Int): Int
    fun DwmExtendFrameIntoClientArea(hwnd: Pointer, margins: TitleBarMargins): Int
}

/** Native frame insets: a negative value extends DWM composition over the client area. */
@Structure.FieldOrder("left", "right", "top", "bottom")
class TitleBarMargins() : Structure() {
    @JvmField var left = 0
    @JvmField var right = 0
    @JvmField var top = 0
    @JvmField var bottom = 0

    constructor(extend: Boolean) : this() {
        if (extend) left = -1
        if (extend) right = -1
        if (extend) top = -1
        if (extend) bottom = -1
    }
}

/**
 * Matches the Windows title bar to the current app palette while retaining native resize and Snap.
 * Glass mode requests a system backdrop only for the native frame; Compose keeps its opaque canvas.
 * Unsupported Windows versions and non-Windows hosts keep their system-provided title bar.
 */
internal fun styleNativeTitleBar(
    window: Window,
    background: Color,
    foreground: Color,
    darkTheme: Boolean,
    glassEnabled: Boolean,
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
        setAttribute(DWMWA_SYSTEMBACKDROP_TYPE, if (glassEnabled) DWMSBT_TRANSIENTWINDOW else DWMSBT_NONE)
        val frameResult = dwm.DwmExtendFrameIntoClientArea(hwnd, TitleBarMargins(extend = glassEnabled))
        if (frameResult != 0) {
            titleBarLogger.debug("DWM title-bar frame extension was not accepted: {}", frameResult)
        }
        // An explicit caption fill covers Acrylic even when DWM accepts the backdrop request.
        setAttribute(DWMWA_CAPTION_COLOR, if (glassEnabled) DWMWA_COLOR_DEFAULT else background.toColorRef())
        setAttribute(DWMWA_TEXT_COLOR, foreground.toColorRef())
    }.onFailure { error ->
        titleBarLogger.debug("Native title-bar styling is unavailable", error)
    }
}

/** Lets DWM consume premultiplied alpha from a supported Windows 11 client surface. */
internal fun enableNativeClientAlpha(window: Window): Boolean =
    runCatching {
        val dwm = Native.load("dwmapi", DwmApi::class.java)
        val result =
            dwm.DwmSetWindowAttribute(
                Native.getWindowPointer(window),
                DWMWA_REDIRECTIONBITMAP_ALPHA,
                IntByReference(1),
                Int.SIZE_BYTES,
            )
        if (result != 0) titleBarLogger.debug("DWM client alpha was not accepted: {}", result)
        result == 0
    }.getOrElse { error ->
        titleBarLogger.debug("DWM client alpha is unavailable", error)
        false
    }

/** Converts an opaque Compose color to the Win32 COLORREF byte order (0x00BBGGRR). */
private fun Color.toColorRef(): Int =
    (red.times(255).roundToInt() and 0xFF) or
        ((green.times(255).roundToInt() and 0xFF) shl 8) or
        ((blue.times(255).roundToInt() and 0xFF) shl 16)
