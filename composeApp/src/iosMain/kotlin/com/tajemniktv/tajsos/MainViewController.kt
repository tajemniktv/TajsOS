/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos

import androidx.compose.ui.window.ComposeUIViewController
import com.tajemniktv.tajsos.data.createDatabase
import com.tajemniktv.tajsos.data.createDataStore
import com.tajemniktv.tajsos.di.SharedModule

/**
 * Global, app-wide shared module for iOS.
 *
 * This is intentionally a file-level singleton shared across all MainViewController instances.
 * On iOS, there is typically one app instance, so this global sharing is safe and appropriate.
 * The module is never explicitly disposed as it lives for the entire application lifetime.
 */
private val sharedModule by lazy { SharedModule(createDatabase(), createDataStore()) }

/**
* Creates the iOS root [platform.UIKit.UIViewController] hosting the Compose app.
*
* The controller uses shared dependencies from a lazily initialized module.
*
* @return Root Compose-backed UIKit view controller.
*/
fun MainViewController(): platform.UIKit.UIViewController {
    val viewModel = sharedModule.createViewModel()

    return ComposeUIViewController {
        App(viewModel = viewModel)
    }
}
