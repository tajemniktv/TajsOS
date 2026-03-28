/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.screens.profile.ProfileRoute

/**
 * Compatibility entry point that delegates profile rendering to the profile feature route.
 */
@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onPickAvatar: (() -> Unit)? = null,
    pickedAvatarRef: String? = null,
    onAvatarPickedConsumed: () -> Unit = {},
) {
    ProfileRoute(
        viewModel = viewModel,
        onPickAvatar = onPickAvatar,
        pickedAvatarRef = pickedAvatarRef,
        onAvatarPickedConsumed = onAvatarPickedConsumed,
    )
}

