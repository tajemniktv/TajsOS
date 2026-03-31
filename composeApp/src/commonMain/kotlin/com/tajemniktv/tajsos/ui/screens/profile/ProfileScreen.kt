/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.profile

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.ui.MainViewModel

/**
 * Compatibility entry point that delegates profile rendering to the profile feature route.
 *
 * @param viewModel The main ViewModel.
 * @param onPickAvatar Optional callback to request a platform avatar picker.
 * @param pickedAvatarRef Optional URI of a picked avatar.
 * @param onAvatarPickConsume Callback invoked when an avatar pick has been consumed.
 */
@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onPickAvatar: (() -> Unit)? = null,
    pickedAvatarRef: String? = null,
    onAvatarPickConsume: () -> Unit = {},
) {
    ProfileRoute(
        viewModel = viewModel,
        onPickAvatar = onPickAvatar,
        pickedAvatarRef = pickedAvatarRef,
        onAvatarPickedConsumed = onAvatarPickConsume,
    )
}
