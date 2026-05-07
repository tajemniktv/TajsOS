/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos

import androidx.compose.ui.window.ComposeUIViewController
import com.tajemniktv.tajsos.ui.MainViewModel

fun MainViewController(viewModel: MainViewModel) = ComposeUIViewController { App(viewModel = viewModel) }
