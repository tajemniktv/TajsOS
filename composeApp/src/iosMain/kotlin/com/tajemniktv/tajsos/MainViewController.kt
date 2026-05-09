/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos

import androidx.compose.ui.window.ComposeUIViewController
import com.tajemniktv.tajsos.data.createDatabase
import com.tajemniktv.tajsos.data.createDataStore
import com.tajemniktv.tajsos.di.SharedModule

private val sharedModule by lazy { SharedModule(createDatabase(), createDataStore()) }

fun MainViewController(): platform.UIKit.UIViewController {
    val viewModel = sharedModule.createViewModel()

    return ComposeUIViewController {
        App(viewModel = viewModel)
    }
}
