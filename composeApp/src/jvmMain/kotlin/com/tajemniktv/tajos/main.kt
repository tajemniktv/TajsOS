/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.window.application
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.emptyPreferences
import com.tajemniktv.tajos.data.DesktopWindowStartupMode
import com.tajemniktv.tajos.data.PreferencesRepository
import com.tajemniktv.tajos.data.createDatabase
import com.tajemniktv.tajos.utils.AppDirs

import com.tajemniktv.tajos.di.SharedModule
import com.tajemniktv.tajos.ui.theme.PrimaryPurple
import com.tajemniktv.tajos.ui.theme.buildTajsOSColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.app_icon
import tajsos.composeapp.generated.resources.next_step
import tajsos.composeapp.generated.resources.untitled
import java.awt.FileDialog
import java.awt.Frame
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import javax.swing.SwingUtilities

fun main() =
    application {
        // Basic setup for Desktop
        val database = createDatabase()

        // Simple DataStore setup for Desktop
        val dataStore =
            PreferenceDataStoreFactory.create(
                corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
                produceFile = { File(AppDirs.getAppDataDir(), "tajsos.preferences_pb") },
            )

        val sharedModule = SharedModule(database, dataStore)
        val preferencesRepository = sharedModule.preferencesRepository
        // The Skiko renderer is process-wide and must be selected before creating a window.
        // Preserve an explicit user override and use software only when native glass is requested.
        if (System.getProperty("skiko.renderApi").isNullOrBlank() &&
            supportsNativeClientAlpha() &&
            runBlocking { preferencesRepository.isGlassmorphismEnabled.first() }
        ) {
            System.setProperty("skiko.renderApi", "SOFTWARE")
        }
        val persistedWindowPlacement =
            runBlocking {
                preferencesRepository.desktopWindowPlacement.first()
            }
        val desktopWindowStartupMode =
            runBlocking {
                preferencesRepository.desktopWindowStartupMode.first()
            }
        val windowState =
            rememberWindowState(
                position = persistedWindowPlacement.toWindowPosition(),
                size = persistedWindowPlacement.toWindowSize(),
                placement = resolveWindowPlacement(desktopWindowStartupMode, persistedWindowPlacement.isMaximized),
            )
        val viewModel =
            sharedModule.createViewModel(
                nextStepFallbackLabel = runBlocking { getString(Res.string.next_step) },
                untitledFallbackLabel = runBlocking { getString(Res.string.untitled) },
            )

        Window(
            onCloseRequest = {
                runBlocking {
                    preferencesRepository.updateDesktopWindowPlacement(windowState.toDesktopWindowPlacement())
                }
                exitApplication()
            },
            title = "TajsOS",
            icon = painterResource(Res.drawable.app_icon),
            state = windowState,
        ) {
            val avatarPickResult = remember { mutableStateOf<String?>(null) }
            val nativeBackdropAvailable = remember { mutableStateOf(false) }
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            val isGlassmorphismEnabled by viewModel.isGlassmorphismEnabled.collectAsState()
            val windowPalette = buildTajsOSColors(isDarkTheme, PrimaryPurple)
            DisposableEffect(window) {
                SwingUtilities.invokeLater {
                    nativeBackdropAvailable.value = enableNativeDesktopTransparency(window)
                }
                onDispose { }
            }
            DisposableEffect(window, isDarkTheme, isGlassmorphismEnabled) {
                val applyChrome = {
                    styleNativeTitleBar(
                        window,
                        windowPalette.surfaceHigh,
                        windowPalette.text,
                        isDarkTheme,
                        isGlassmorphismEnabled,
                    )
                }
                val listener =
                    object : WindowAdapter() {
                        override fun windowActivated(event: WindowEvent) {
                            applyChrome()
                        }
                    }
                window.addWindowListener(listener)
                SwingUtilities.invokeLater(applyChrome)
                onDispose { window.removeWindowListener(listener) }
            }
            LaunchedEffect(windowState) {
                snapshotFlow { windowState.toDesktopWindowPlacement() }
                    .distinctUntilChanged()
                    .collectLatest { placement ->
                        delay(300)
                        preferencesRepository.updateDesktopWindowPlacement(placement)
                    }
            }
            App(
                viewModel = viewModel,
                nativeDesktopBackdropAvailable = nativeBackdropAvailable.value,
                onPickAvatar = {
                    val dialog = FileDialog(null as Frame?, "Select Avatar Image", FileDialog.LOAD)
                    dialog.isVisible = true
                    val file = dialog.file
                    val directory = dialog.directory
                    avatarPickResult.value =
                        if (file != null && directory != null) {
                            File(directory, file).absolutePath
                        } else {
                            null
                        }
                },
                avatarPickResult = avatarPickResult.value,
                onAvatarPickConsume = { avatarPickResult.value = null },
            )
        }
    }

/**
 * Maps persisted values into a [WindowPosition] with platform fallback.
 */
private fun PreferencesRepository.DesktopWindowPlacement.toWindowPosition(): WindowPosition {
    val persistedX = xDp ?: return WindowPosition.PlatformDefault
    val persistedY = yDp ?: return WindowPosition.PlatformDefault
    return WindowPosition(persistedX.dp, persistedY.dp)
}

/**
 * Maps persisted values into a startup [DpSize].
 */
private fun PreferencesRepository.DesktopWindowPlacement.toWindowSize(): DpSize {
    val persistedWidth = widthDp ?: return DpSize(1280.dp, 800.dp)
    val persistedHeight = heightDp ?: return DpSize(1280.dp, 800.dp)
    return DpSize(persistedWidth.dp, persistedHeight.dp)
}

/**
 * Captures current window placement in a persistable representation.
 */
private fun androidx.compose.ui.window.WindowState.toDesktopWindowPlacement():
    PreferencesRepository.DesktopWindowPlacement {
    val resolvedPosition =
        when (val currentPosition = position) {
            WindowPosition.PlatformDefault -> null
            is WindowPosition.Absolute -> currentPosition
            else -> null
        }
    val widthDp = size.width.toPersistedDpOrNull()
    val heightDp = size.height.toPersistedDpOrNull()
    return PreferencesRepository.DesktopWindowPlacement(
        xDp = resolvedPosition?.x?.toPersistedDpOrNull(),
        yDp = resolvedPosition?.y?.toPersistedDpOrNull(),
        widthDp = widthDp,
        heightDp = heightDp,
        isMaximized = placement == WindowPlacement.Maximized,
    )
}

/**
 * Converts [Dp] to integer dp if specified and finite.
 */
private fun Dp.toPersistedDpOrNull(): Int? {
    val rawValue = value
    return if (this == Dp.Unspecified || !rawValue.isFinite()) {
        null
    } else {
        rawValue.toInt()
    }
}


/**
 * Resolves [WindowPlacement] based on startup mode and persistence.
 */
private fun resolveWindowPlacement(
    startupMode: DesktopWindowStartupMode,
    isMaximized: Boolean,
): WindowPlacement {
    return when (startupMode) {
        DesktopWindowStartupMode.ALWAYS_MAXIMIZED -> WindowPlacement.Maximized
        DesktopWindowStartupMode.RESTORE_LAST ->
            if (isMaximized) {
                WindowPlacement.Maximized
            } else {
                WindowPlacement.Floating
            }
    }
}
