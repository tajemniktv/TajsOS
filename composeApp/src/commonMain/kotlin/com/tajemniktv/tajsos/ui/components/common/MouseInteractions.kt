/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.common

import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.changedToDownIgnoreConsumed
import androidx.compose.ui.input.pointer.isBackPressed
import androidx.compose.ui.input.pointer.isForwardPressed
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.isTertiaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.DpOffset

/**
 * Stores open/close state and pointer anchor for cursor-positioned context menus.
 */
@Stable
class MouseContextMenuState {
    var isExpanded by mutableStateOf(false)
    var anchor by mutableStateOf(Offset.Zero)

    fun showAt(position: Offset) {
        anchor = position
        isExpanded = true
    }

    fun dismiss() {
        isExpanded = false
    }
}

/**
 * Remembers [MouseContextMenuState] across recompositions.
 */
@Composable
fun rememberMouseContextMenuState(): MouseContextMenuState = remember { MouseContextMenuState() }

/**
 * Renders a cursor-anchored dropdown menu in the current local coordinate space.
 */
@Composable
fun MouseContextMenu(
    state: MouseContextMenuState,
    content: @Composable ColumnScope.() -> Unit,
) {
    val density = LocalDensity.current
    val offset =
        with(density) {
            DpOffset(state.anchor.x.toDp(), state.anchor.y.toDp())
        }
    DropdownMenu(
        expanded = state.isExpanded,
        onDismissRequest = state::dismiss,
        offset = offset,
        content = content,
    )
}

/**
 * Convenience host that lets callers place [MouseContextMenu] near a clickable surface.
 */
@Composable
fun MouseContextMenuHost(
    state: MouseContextMenuState,
    modifier: Modifier = Modifier,
    menuContent: @Composable ColumnScope.() -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier) {
        content()
        MouseContextMenu(state = state, content = menuContent)
    }
}

/**
 * Adds support for non-primary mouse buttons and optional fallback behavior.
 *
 * Events are observed at [PointerEventPass.Final] to avoid fighting child handlers.
 */
fun Modifier.mouseButtons(
    enabled: Boolean = true,
    onSecondaryClick: (() -> Unit)? = null,
    onSecondaryClickAt: ((Offset) -> Unit)? = null,
    onMiddleClick: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
    onForwardClick: (() -> Unit)? = null,
    middleClickFallbackToPrimary: (() -> Unit)? = null,
): Modifier =
    if (!enabled) {
        this
    } else {
        pointerInput(
            enabled,
            onSecondaryClick,
            onSecondaryClickAt,
            onMiddleClick,
            onBackClick,
            onForwardClick,
            middleClickFallbackToPrimary,
        ) {
            awaitEachGesture {
                handleMouseButtonPress(
                    onSecondaryClick = onSecondaryClick,
                    onSecondaryClickAt = onSecondaryClickAt,
                    onMiddleClick = onMiddleClick,
                    onBackClick = onBackClick,
                    onForwardClick = onForwardClick,
                    middleClickFallbackToPrimary = middleClickFallbackToPrimary,
                )
            }
        }
    }

private suspend fun AwaitPointerEventScope.handleMouseButtonPress(
    onSecondaryClick: (() -> Unit)?,
    onSecondaryClickAt: ((Offset) -> Unit)?,
    onMiddleClick: (() -> Unit)?,
    onBackClick: (() -> Unit)?,
    onForwardClick: (() -> Unit)?,
    middleClickFallbackToPrimary: (() -> Unit)?,
) {
    val event = awaitPointerEvent()
    if (event.changes.all { it.isConsumed }) return
    val downChange = event.changes.firstOrNull { it.changedToDownIgnoreConsumed() } ?: return
    val buttons = event.buttons
    val consumed =
        when {
            buttons.isSecondaryPressed && (onSecondaryClick != null || onSecondaryClickAt != null) -> {
                onSecondaryClickAt?.invoke(downChange.position)
                onSecondaryClick?.invoke()
                true
            }

            buttons.isTertiaryPressed -> {
                (onMiddleClick ?: middleClickFallbackToPrimary)?.invoke()
                onMiddleClick != null || middleClickFallbackToPrimary != null
            }

            buttons.isBackPressed && onBackClick != null -> {
                onBackClick()
                true
            }

            buttons.isForwardPressed && onForwardClick != null -> {
                onForwardClick()
                true
            }

            else -> {
                false
            }
        }

    if (consumed) {
        event.changes.forEach { it.consume() }
    }
}

/**
 * Unified clickable modifier with optional long-click and explicit mouse button handlers.
 *
 * Primary/touch/keyboard activation remains handled by [combinedClickable].
 */
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.mouseClickable(
    enabled: Boolean = true,
    onClick: (() -> Unit)?,
    onLongClick: (() -> Unit)? = null,
    role: Role? = null,
    interactionSource: MutableInteractionSource? = null,
    indication: Indication? = null,
    onSecondaryClick: (() -> Unit)? = null,
    onSecondaryClickAt: ((Offset) -> Unit)? = null,
    onMiddleClick: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
    onForwardClick: (() -> Unit)? = null,
    middleClickFallbackToPrimary: Boolean = false,
): Modifier =
    composed {
        val clickableModifier =
            if (onClick != null || onLongClick != null) {
                this.combinedClickable(
                    enabled = enabled,
                    role = role,
                    interactionSource =
                        interactionSource
                            ?: remember { MutableInteractionSource() },
                    indication = indication ?: LocalIndication.current,
                    onClick = { onClick?.invoke() },
                    onLongClick = onLongClick,
                ).pointerHoverIcon(PointerIcon.Hand)
            } else {
                this
            }

        clickableModifier.mouseButtons(
            enabled = enabled,
            onSecondaryClick = onSecondaryClick,
            onSecondaryClickAt = onSecondaryClickAt,
            onMiddleClick = onMiddleClick,
            onBackClick = onBackClick,
            onForwardClick = onForwardClick,
            middleClickFallbackToPrimary = if (middleClickFallbackToPrimary) onClick else null,
        )
    }
