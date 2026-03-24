/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController(viewModel: com.tajemniktv.tajsos.ui.MainViewModel) = ComposeUIViewController { App(viewModel = viewModel) }