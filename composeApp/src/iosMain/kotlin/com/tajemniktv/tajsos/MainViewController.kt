/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos

import androidx.compose.ui.window.ComposeUIViewController
import com.tajemniktv.tajsos.data.createDatabase
import com.tajemniktv.tajsos.data.createDataStore
import com.tajemniktv.tajsos.di.SharedModule

fun MainViewController(): platform.UIKit.UIViewController {
    val database = createDatabase()
    val dataStore = createDataStore()
    val sharedModule = SharedModule(database, dataStore)
    val viewModel = sharedModule.createViewModel()

    return ComposeUIViewController {
        App(viewModel = viewModel)
    }
}
