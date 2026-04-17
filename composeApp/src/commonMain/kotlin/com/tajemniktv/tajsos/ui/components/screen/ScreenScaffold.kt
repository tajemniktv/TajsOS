/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme

/**
 * Shared page-level width policies for screen scaffolds.
 */
sealed interface ScreenContentWidth {
    data object Full : ScreenContentWidth

    data object Wide : ScreenContentWidth

    data object Readable : ScreenContentWidth

    data class Fixed(
        val maxWidth: Dp,
    ) : ScreenContentWidth
}

/**
 * Shared outer scroll modes for screen scaffolds.
 */
enum class ScreenScrollBehavior {
    BodyScroll,
    PaneScroll,
    None,
}

/**
 * Standard single-screen template with optional page header and shell-header integration.
 */
@Composable
fun ScreenScaffold(
    screen: Screen? = null,
    onNavigate: ((String) -> Unit)? = null,
    screenHeaderController: ScreenHeaderController? = LocalScreenHeaderController.current,
    screenHeader: ScreenHeaderModel? = null,
    modifier: Modifier = Modifier,
    contentWidth: ScreenContentWidth = ScreenContentWidth.Full,
    scrollBehavior: ScreenScrollBehavior = ScreenScrollBehavior.BodyScroll,
    contentPadding: PaddingValues = ScreenScaffoldDefaults.contentPadding(),
    backgroundColor: Color = Color.Transparent,
    backgroundBrush: Brush? = null,
    title: (@Composable () -> Unit)? = null,
    subtitle: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    toolbar: (@Composable () -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val breadcrumbs = if (screen != null) {
        screenBreadcrumbs(screen) { breadcrumb ->
            if (onNavigate == null || breadcrumb.route.contains("{")) {
                null
            } else {
                { onNavigate(breadcrumb.route) }
            }
        }
    } else {
        emptyList()
    }

    val finalHeaderModel =
        remember(screenHeader, breadcrumbs, screen) {
            screenHeader ?: if (screen != null) {
                ScreenHeaderModel(
                    breadcrumbs = breadcrumbs,
                    title = null,
                )
            } else {
                null
            }
        }

    if (screenHeaderController != null && finalHeaderModel != null) {
        BindScreenHeader(
            controller = screenHeaderController,
            model = finalHeaderModel,
        )
    }

    val outerModifier =
        modifier
            .fillMaxSize()
            .then(if (backgroundBrush != null) Modifier.background(backgroundBrush) else Modifier)
            .then(if (backgroundBrush == null) Modifier.background(backgroundColor) else Modifier)

    Box(modifier = outerModifier) {
        val constrainedModifier =
            Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .then(contentWidthModifier(contentWidth))
                .padding(contentPadding)

        if (scrollBehavior == ScreenScrollBehavior.BodyScroll) {
            val scrollState = rememberScrollState()
            Column(
                modifier = constrainedModifier.verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(ScreenScaffoldDefaults.sectionSpacing),
            ) {
                ScreenScaffoldHeader(
                    title = title,
                    subtitle = subtitle,
                    actions = actions,
                    toolbar = toolbar,
                )
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    content = content,
                )
            }
        } else {
            Column(
                modifier = constrainedModifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(ScreenScaffoldDefaults.sectionSpacing),
            ) {
                ScreenScaffoldHeader(
                    title = title,
                    subtitle = subtitle,
                    actions = actions,
                    toolbar = toolbar,
                )
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f, fill = scrollBehavior == ScreenScrollBehavior.None),
                    content = content,
                )
            }
        }
    }
}

/**
 * Convenience version of [ScreenScaffold] that defaults to [ScreenScrollBehavior.BodyScroll].
 */
@Composable
fun ScrollableScreenScaffold(
    screen: Screen? = null,
    onNavigate: ((String) -> Unit)? = null,
    screenHeaderController: ScreenHeaderController? = LocalScreenHeaderController.current,
    screenHeader: ScreenHeaderModel? = null,
    modifier: Modifier = Modifier,
    contentWidth: ScreenContentWidth = ScreenContentWidth.Full,
    contentPadding: PaddingValues = ScreenScaffoldDefaults.contentPadding(),
    backgroundColor: Color = Color.Transparent,
    backgroundBrush: Brush? = null,
    title: (@Composable () -> Unit)? = null,
    subtitle: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    toolbar: (@Composable () -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    ScreenScaffold(
        screen = screen,
        onNavigate = onNavigate,
        screenHeaderController = screenHeaderController,
        screenHeader = screenHeader,
        modifier = modifier,
        contentWidth = contentWidth,
        scrollBehavior = ScreenScrollBehavior.BodyScroll,
        contentPadding = contentPadding,
        backgroundColor = backgroundColor,
        backgroundBrush = backgroundBrush,
        title = title,
        subtitle = subtitle,
        actions = actions,
        toolbar = toolbar,
        content = content,
    )
}

/**
 * Reusable split-layout scaffold for detail and dual-pane screens.
 */
@Composable
fun SplitScreenScaffold(
    isSplitLayout: Boolean,
    screen: Screen? = null,
    onNavigate: ((String) -> Unit)? = null,
    screenHeaderController: ScreenHeaderController? = LocalScreenHeaderController.current,
    screenHeader: ScreenHeaderModel? = null,
    modifier: Modifier = Modifier,
    contentWidth: ScreenContentWidth = ScreenContentWidth.Full,
    scrollBehavior: ScreenScrollBehavior = ScreenScrollBehavior.PaneScroll,
    contentPadding: PaddingValues = ScreenScaffoldDefaults.contentPadding(),
    backgroundColor: Color = Color.Transparent,
    backgroundBrush: Brush? = null,
    title: (@Composable () -> Unit)? = null,
    subtitle: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    toolbar: (@Composable () -> Unit)? = null,
    header: (@Composable () -> Unit)? = null,
    secondaryWidth: Dp = 320.dp,
    primaryWeight: Float = 1f,
    secondaryWeight: Float = 0f,
    primary: @Composable BoxScope.() -> Unit,
    secondary: (@Composable BoxScope.() -> Unit)? = null,
) {
    val breadcrumbs = if (screen != null) {
        screenBreadcrumbs(screen) { breadcrumb ->
            if (onNavigate == null || breadcrumb.route.contains("{")) {
                null
            } else {
                { onNavigate(breadcrumb.route) }
            }
        }
    } else {
        emptyList()
    }

    val finalHeaderModel =
        remember(screenHeader, breadcrumbs, screen) {
            screenHeader ?: if (screen != null) {
                ScreenHeaderModel(
                    breadcrumbs = breadcrumbs,
                    title = null,
                )
            } else {
                null
            }
        }

    if (screenHeaderController != null && finalHeaderModel != null) {
        BindScreenHeader(
            controller = screenHeaderController,
            model = finalHeaderModel,
        )
    }

    val outerModifier =
        modifier
            .fillMaxSize()
            .then(if (backgroundBrush != null) Modifier.background(backgroundBrush) else Modifier)
            .then(if (backgroundBrush == null) Modifier.background(backgroundColor) else Modifier)

    Box(modifier = outerModifier) {
        val constrainedModifier =
            Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .then(contentWidthModifier(contentWidth))
                .padding(contentPadding)

        if (!isSplitLayout) {
            val scrollState = rememberScrollState()
            Column(
                modifier =
                    constrainedModifier.then(
                        if (scrollBehavior == ScreenScrollBehavior.None) {
                            Modifier.fillMaxSize()
                        } else {
                            Modifier.verticalScroll(scrollState)
                        },
                    ),
                verticalArrangement = Arrangement.spacedBy(ScreenScaffoldDefaults.sectionSpacing),
            ) {
                ScreenScaffoldHeader(
                    title = title,
                    subtitle = subtitle,
                    actions = actions,
                    toolbar = toolbar,
                )
                header?.invoke()
                Box(modifier = Modifier.fillMaxWidth(), content = primary)
                if (secondary != null) {
                    Box(modifier = Modifier.fillMaxWidth(), content = secondary)
                }
            }
            return
        }

        Column(
            modifier = constrainedModifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(ScreenScaffoldDefaults.sectionSpacing),
        ) {
            ScreenScaffoldHeader(
                title = title,
                subtitle = subtitle,
                actions = actions,
                toolbar = toolbar,
            )
            header?.invoke()
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f, fill = true),
                horizontalArrangement = Arrangement.spacedBy(ScreenScaffoldDefaults.paneSpacing),
            ) {
                SplitPane(
                    modifier = Modifier.weight(primaryWeight),
                    scrollBehavior = scrollBehavior,
                    content = primary,
                )
                if (secondary != null) {
                    val secondaryModifier =
                        if (secondaryWeight > 0f) {
                            Modifier.weight(secondaryWeight)
                        } else {
                            Modifier.widthIn(max = secondaryWidth).fillMaxWidth()
                        }
                    SplitPane(
                        modifier = secondaryModifier,
                        scrollBehavior = scrollBehavior,
                        content = secondary,
                    )
                }
            }
        }
    }
}

private object ScreenScaffoldDefaults {
    val sectionSpacing = TajsOSTheme.SpacingMd
    val paneSpacing = TajsOSTheme.SpacingMd

    @Composable
    fun contentPadding(): PaddingValues =
        PaddingValues(
            start = TajsOSTheme.SpacingMd,
            top = TajsOSTheme.SpacingMd,
            end = TajsOSTheme.SpacingMd,
            bottom = TajsOSTheme.SpacingLg,
        )
}

@Composable
private fun ScreenScaffoldHeader(
    title: (@Composable () -> Unit)?,
    subtitle: (@Composable () -> Unit)?,
    actions: (@Composable RowScope.() -> Unit)?,
    toolbar: (@Composable () -> Unit)?,
) {
    if (title == null && subtitle == null && actions == null && toolbar == null) {
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
    ) {
        if (title != null || subtitle != null || actions != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd),
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    title?.invoke()
                    subtitle?.invoke()
                }
                if (actions != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        content = actions,
                    )
                }
            }
        }

        toolbar?.invoke()
    }
}

@Composable
private fun SplitPane(
    modifier: Modifier,
    scrollBehavior: ScreenScrollBehavior,
    content: @Composable BoxScope.() -> Unit,
) {
    if (scrollBehavior == ScreenScrollBehavior.PaneScroll) {
        val scrollState = rememberScrollState()
        Box(
            modifier = modifier.verticalScroll(scrollState),
            content = content,
        )
    } else {
        Box(
            modifier = modifier,
            content = content,
        )
    }
}

private fun contentWidthModifier(contentWidth: ScreenContentWidth): Modifier =
    when (contentWidth) {
        ScreenContentWidth.Full -> Modifier
        ScreenContentWidth.Wide -> Modifier.widthIn(max = 1440.dp)
        ScreenContentWidth.Readable -> Modifier.widthIn(max = 1040.dp)
        is ScreenContentWidth.Fixed -> Modifier.widthIn(max = contentWidth.maxWidth)
    }

@Composable
fun ScreenTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        color = TajsOSTheme.Text,
    )
}

@Composable
fun ScreenSubtitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = TajsOSTheme.Muted,
    )
}
