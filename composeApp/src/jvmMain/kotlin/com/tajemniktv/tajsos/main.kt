/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.tajemniktv.tajsos.data.createDatabase
import com.tajemniktv.tajsos.di.SharedModule
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import org.jetbrains.compose.resources.painterResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.app_icon

fun main() =
    application {
        // Basic setup for Desktop
        val database = createDatabase()

        // Simple DataStore setup for Desktop
        val dataStore =
            PreferenceDataStoreFactory.create(
                produceFile = { File(System.getProperty("user.home"), "tajsos.preferences_pb") },
            )

        val sharedModule = SharedModule(database, dataStore)
        val viewModel = sharedModule.createViewModel()

        Window(
            onCloseRequest = ::exitApplication,
            title = "TajsOS",
            icon = painterResource(Res.drawable.app_icon),
        ) {
            val avatarPickResult = remember { mutableStateOf<String?>(null) }
            App(
                viewModel = viewModel,
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
                onAvatarPickConsumed = { avatarPickResult.value = null },
            )
        }
    }
