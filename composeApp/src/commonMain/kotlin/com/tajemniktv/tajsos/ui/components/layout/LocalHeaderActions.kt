/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.layout

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

val LocalHeaderActions = compositionLocalOf<@Composable RowScope.() -> Unit> { { } }
