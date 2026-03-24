/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved. 
 */

package com.tajemniktv.tajsos.ui.components.common

import androidx.compose.runtime.Composable

@Composable
fun RecoveryBasicsBlock(
    onMedsClick: () -> Unit,
    onHydrationClick: () -> Unit,
    onFoodClick: () -> Unit
) {
    com.tajemniktv.tajsos.ui.components.modes.RecoveryBasicsBlock(
        onMedsClick = onMedsClick,
        onHydrationClick = onHydrationClick,
        onFoodClick = onFoodClick
    )
}
