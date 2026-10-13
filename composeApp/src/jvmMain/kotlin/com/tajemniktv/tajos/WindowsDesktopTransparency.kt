/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos

import androidx.compose.ui.awt.ComposeWindow
import org.jetbrains.skiko.GraphicsApi
import org.jetbrains.skiko.SkiaLayer
import org.slf4j.LoggerFactory
import java.awt.Container
import javax.swing.JComponent

private val transparencyLogger = LoggerFactory.getLogger("TajsOS.WindowTransparency")

/**
 * Allows transparent Compose pixels to reveal DWM's window backdrop while keeping a decorated
 * HWND. Skiko's current Direct3D path presents an opaque client image, so only its software
 * renderers are eligible. Call after the window is realized and before exposing native glass UI.
 *
 * Keep Compose's original content pane: replacing it breaks Skiko's normal repaint path and leaves
 * state changes invisible until an external window repaint. Its non-opaque background is enough to
 * expose the software layer's alpha without taking ownership of the Swing component hierarchy.
 */
internal fun enableNativeDesktopTransparency(window: ComposeWindow): Boolean {
    if (!System.getProperty("os.name").startsWith("Windows", ignoreCase = true)) return false
    val layer = window.findSkiaLayer() ?: return false
    if (layer.renderApi != GraphicsApi.SOFTWARE_FAST && layer.renderApi != GraphicsApi.SOFTWARE_COMPAT) {
        transparencyLogger.debug("Native desktop transparency needs a software Skiko renderer, found {}", layer.renderApi)
        return false
    }
    if (!enableNativeClientAlpha(window)) return false

    val pane = window.contentPane as? JComponent ?: return false
    val originalBackground = pane.background
    val originalOpacity = pane.isOpaque
    return runCatching {
        layer.transparency = true
        pane.background = java.awt.Color(0, 0, 0, 0)
        pane.isOpaque = false
        window.repaint()
        true
    }.getOrElse { error ->
        layer.transparency = false
        pane.background = originalBackground
        pane.isOpaque = originalOpacity
        transparencyLogger.warn("Native desktop transparency setup failed; retaining in-app glass", error)
        false
    }
}

private fun Container.findSkiaLayer(): SkiaLayer? =
    components.firstNotNullOfOrNull { component ->
        when (component) {
            is SkiaLayer -> component
            is Container -> component.findSkiaLayer()
            else -> null
        }
    }
