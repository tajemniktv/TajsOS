/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.layout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.ModeEntity
import com.tajemniktv.tajsos.data.UserProfile
import com.tajemniktv.tajsos.data.resolveDisplayName
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.SidebarMode
import com.tajemniktv.tajsos.ui.components.common.GlassMaterial
import com.tajemniktv.tajsos.ui.components.common.MouseContextMenuHost
import com.tajemniktv.tajsos.ui.components.common.glassChrome
import com.tajemniktv.tajsos.ui.components.common.glassContainerColor
import com.tajemniktv.tajsos.ui.components.common.mouseButtons
import com.tajemniktv.tajsos.ui.components.common.mouseClickable
import com.tajemniktv.tajsos.ui.components.common.rememberMouseContextMenuState
import com.tajemniktv.tajsos.ui.screens.tasks.TasksTab
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.common_collapse
import tajsos.composeapp.generated.resources.common_expand
import tajsos.composeapp.generated.resources.common_no_active_mode
import tajsos.composeapp.generated.resources.common_open
import tajsos.composeapp.generated.resources.sidebar_brand
import tajsos.composeapp.generated.resources.sidebar_new_entry
import kotlin.math.roundToInt

private const val MinExpandedSidebarWidthDp = 220f
private const val MaxExpandedSidebarWidthDp = 360f
private const val DefaultExpandedSidebarWidthDp = 236
private val CollapsedSidebarWidth = 92.dp

/**
 * Persistent app sidebar with explicit behavior modes and inline expandable root sections.
 */
@Composable
fun AppSidebar(
    shellState: AppShellState,
    menuGroups: List<Pair<StringResource, List<Screen>>>,
    currentRootScreen: Screen?,
    currentScreen: Screen?,
    activeTasksTab: TasksTab,
    currentMode: ModeEntity?,
    userProfile: UserProfile,
    onNavigate: (Screen) -> Unit,
    onNavigateToTasksTab: (TasksTab) -> Unit,
    onNewEntry: () -> Unit,
    onNavigateToProfile: () -> Unit,
    expandedWidthDp: Int = DefaultExpandedSidebarWidthDp,
    resizeEnabled: Boolean = false,
    onExpandedWidthCommit: ((Int) -> Unit)? = null,
    forceExpandedPresentation: Boolean = false,
    useFixedWidth: Boolean = true,
    applyGlass: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val hoverInteraction = remember { MutableInteractionSource() }
    val isHovered by hoverInteraction.collectIsHoveredAsState()

    LaunchedEffect(isHovered, shellState.sidebarMode, forceExpandedPresentation) {
        if (forceExpandedPresentation || shellState.sidebarMode != SidebarMode.HOVER_EXPAND) {
            shellState.hoverExpanded = false
            return@LaunchedEffect
        }

        if (isHovered) {
            delay(150)
            shellState.hoverExpanded = true
        } else {
            delay(250)
            shellState.hoverExpanded = false
        }
    }

    val showExpandedContent = forceExpandedPresentation || shellState.isSidebarExpandedPresentation
    var liveExpandedWidthDp by remember { mutableFloatStateOf(expandedWidthDp.toFloat()) }
    var expandedFlyoutRoute by remember { mutableStateOf<String?>(null) }
    val density = LocalDensity.current

    LaunchedEffect(expandedWidthDp) {
        liveExpandedWidthDp =
            expandedWidthDp.toFloat().coerceIn(MinExpandedSidebarWidthDp, MaxExpandedSidebarWidthDp)
    }

    LaunchedEffect(showExpandedContent) {
        if (showExpandedContent) {
            expandedFlyoutRoute = null
        }
    }

    LaunchedEffect(showExpandedContent, currentRootScreen?.route) {
        if (!showExpandedContent) return@LaunchedEffect
        val activeRoot = currentRootScreen ?: return@LaunchedEffect
        if (sidebarChildren(activeRoot).isNotEmpty()) {
            shellState.setRootExpanded(activeRoot.route, true)
        }
    }

    val sidebarWidth by animateDpAsState(
        targetValue = if (showExpandedContent) liveExpandedWidthDp.dp else CollapsedSidebarWidth,
        label = "sidebarWidth",
    )

    val canResizeDesktopSidebar =
        resizeEnabled &&
            useFixedWidth &&
            !forceExpandedPresentation &&
            shellState.sidebarMode == SidebarMode.EXPANDED &&
            showExpandedContent

    Surface(
        modifier =
            modifier
                .then(
                    if (useFixedWidth) {
                        Modifier.width(sidebarWidth)
                    } else {
                        Modifier.fillMaxWidth()
                    },
                ).fillMaxHeight()
                .then(
                    if (applyGlass) {
                        Modifier.glassChrome(
                            shape = RoundedCornerShape(0.dp),
                            material = GlassMaterial.THICK,
                        )
                    } else {
                        Modifier
                    },
                ).hoverable(
                    interactionSource = hoverInteraction,
                    enabled = !forceExpandedPresentation && shellState.sidebarMode == SidebarMode.HOVER_EXPAND,
                ),
        color =
            if (applyGlass) {
                glassContainerColor(TajsOSTheme.SurfaceLowest)
            } else {
                TajsOSTheme.SurfaceLowest
            },
        shape = RoundedCornerShape(0.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                SidebarLogoHeader(showExpandedContent = showExpandedContent)

                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 12.dp),
                ) {
                    menuGroups.forEach { (groupTitle, screens) ->
                        if (showExpandedContent) {
                            Text(
                                text = stringResource(groupTitle).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = TajsOSTheme.Muted,
                                modifier =
                                    Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 8.dp,
                                    ),
                            )
                        } else {
                            Spacer(Modifier.height(10.dp))
                        }

                        screens.forEach { rootScreen ->
                            val isActiveRoot = currentRootScreen?.route == rootScreen.route
                            ExpandableNavSection(
                                screen = rootScreen,
                                currentScreen = currentScreen,
                                currentRootScreen = currentRootScreen,
                                isExpandedPresentation = showExpandedContent,
                                isActiveRoot = isActiveRoot,
                                isExpandedRoot = shellState.isRootExpanded(rootScreen.route),
                                isFlyoutExpanded = expandedFlyoutRoute == rootScreen.route,
                                activeTasksTab = activeTasksTab,
                                onRootNavigate = { onNavigate(rootScreen) },
                                onRootExpandToggle = {
                                    shellState.toggleRootExpanded(rootScreen.route)
                                },
                                onExpandFlyout = {
                                    expandedFlyoutRoute = rootScreen.route
                                },
                                onDismissFlyout = {
                                    if (expandedFlyoutRoute == rootScreen.route) {
                                        expandedFlyoutRoute = null
                                    }
                                },
                                onChildNavigate = { child ->
                                    navigateFromSidebar(
                                        screen = child,
                                        onNavigate = onNavigate,
                                        onNavigateToTasksTab = onNavigateToTasksTab,
                                    )
                                },
                            )
                        }
                    }
                }

                SidebarBottomActions(
                    showExpandedContent = showExpandedContent,
                    currentMode = currentMode,
                    userProfile = userProfile,
                    onNewEntry = onNewEntry,
                    onNavigateToProfile = onNavigateToProfile,
                )
            }

            if (canResizeDesktopSidebar) {
                onExpandedWidthCommit?.let { commitSidebarWidth ->
                    val resizeInteraction = remember { MutableInteractionSource() }
                    val isResizeHovered by resizeInteraction.collectIsHoveredAsState()
                    Box(
                        modifier =
                            Modifier
                                .align(Alignment.CenterEnd)
                                .fillMaxHeight()
                                .width(10.dp)
                                .hoverable(interactionSource = resizeInteraction)
                                .draggable(
                                    orientation = Orientation.Horizontal,
                                    state =
                                        rememberDraggableState { deltaPx ->
                                            val deltaDp = with(density) { deltaPx.toDp().value }
                                            liveExpandedWidthDp =
                                                (liveExpandedWidthDp + deltaDp)
                                                    .coerceIn(
                                                        MinExpandedSidebarWidthDp,
                                                        MaxExpandedSidebarWidthDp,
                                                    )
                                        },
                                    onDragStopped = {
                                        commitSidebarWidth(liveExpandedWidthDp.roundToInt())
                                    },
                                ),
                    ) {
                        if (isResizeHovered) {
                            Surface(
                                modifier =
                                    Modifier
                                        .align(Alignment.Center)
                                        .width(2.dp)
                                        .height(48.dp),
                                color = TajsOSTheme.Primary.copy(alpha = 0.5f),
                                tonalElevation = 0.dp,
                                shadowElevation = 0.dp,
                            ) {}
                        }
                    }
                }
            }
        }
    }
}

/**
 * Sidebar logo/identity section pinned at the top.
 */
@Composable
fun SidebarLogoHeader(showExpandedContent: Boolean) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        TBoxIcon(tintColor = TajsOSTheme.Primary)
        AnimatedVisibility(
            visible = showExpandedContent,
            enter = fadeIn() + slideInHorizontally(initialOffsetX = { -it / 2 }),
            exit = fadeOut() + slideOutHorizontally(targetOffsetX = { -it / 2 }),
        ) {
            Text(
                text = stringResource(Res.string.sidebar_brand),
                style = MaterialTheme.typography.titleMedium,
                color = TajsOSTheme.Text,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

/**
 * Root item with optional inline child destinations shown in the same sidebar column.
 */
@Composable
fun ExpandableNavSection(
    screen: Screen,
    currentScreen: Screen?,
    currentRootScreen: Screen?,
    isExpandedPresentation: Boolean,
    isActiveRoot: Boolean,
    isExpandedRoot: Boolean,
    isFlyoutExpanded: Boolean,
    activeTasksTab: TasksTab,
    onRootNavigate: () -> Unit,
    onRootExpandToggle: () -> Unit,
    onExpandFlyout: () -> Unit,
    onDismissFlyout: () -> Unit,
    onChildNavigate: (Screen) -> Unit,
) {
    val rootLabel = stringResource(screen.label)
    val children = sidebarChildren(screen)
    val expandable = children.isNotEmpty()
    val activeChildRoutes =
        children.mapNotNull { child ->
            if (isChildActive(screen, child, currentScreen, currentRootScreen, activeTasksTab)) {
                child.route
            } else {
                null
            }
        }
    val hasActiveChild = activeChildRoutes.isNotEmpty()
    val isActiveBranch = isActiveRoot || hasActiveChild
    val showChildrenInline = isExpandedPresentation && expandable && isExpandedRoot
    val contextMenuState = rememberMouseContextMenuState()
    val rootClickAction =
        when {
            !expandable -> onRootNavigate
            isExpandedPresentation -> onRootNavigate
            else -> onExpandFlyout
        }

    MouseContextMenuHost(
        state = contextMenuState,
        modifier = Modifier.fillMaxWidth(),
        menuContent = {
            DropdownMenuItem(
                text = { Text(text = rootLabel) },
                onClick = {
                    contextMenuState.dismiss()
                    onRootNavigate()
                },
            )
            if (expandable) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text =
                                if (isExpandedPresentation) {
                                    stringResource(if (showChildrenInline) Res.string.common_collapse else Res.string.common_expand)
                                } else {
                                    stringResource(Res.string.common_open)
                                },
                        )
                    },
                    onClick = {
                        contextMenuState.dismiss()
                        if (isExpandedPresentation) {
                            onRootExpandToggle()
                        } else {
                            onExpandFlyout()
                        }
                    },
                )
            }
        },
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            SidebarTooltip(
                enabled = !isExpandedPresentation && !isFlyoutExpanded,
                text = rootLabel,
            ) {
                Surface(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 2.dp)
                            .mouseClickable(
                                onClick = rootClickAction,
                                onSecondaryClickAt = { contextMenuState.showAt(it) },
                                middleClickFallbackToPrimary = true,
                            ),
                    shape = RoundedCornerShape(10.dp),
                    color =
                        if (isActiveBranch) {
                            TajsOSTheme.Primary.copy(alpha = 0.17f)
                        } else {
                            Color.Transparent
                        },
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                ) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = if (isExpandedPresentation) 12.dp else 10.dp,
                                    vertical = if (isExpandedPresentation) 10.dp else 9.dp,
                                ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .size(20.dp),
                        ) {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = rootLabel,
                                tint = if (isActiveBranch) TajsOSTheme.Primary else TajsOSTheme.Muted,
                                modifier = Modifier.align(Alignment.Center),
                            )
                            if (!isExpandedPresentation && expandable) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isActiveBranch) TajsOSTheme.Primary else TajsOSTheme.Muted,
                                    modifier =
                                        Modifier
                                            .size(6.dp)
                                            .align(Alignment.BottomEnd),
                                ) {}
                            }
                        }
                        AnimatedVisibility(
                            visible = isExpandedPresentation,
                            enter = fadeIn(),
                            exit = fadeOut(),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = rootLabel,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isActiveBranch) TajsOSTheme.Text else TajsOSTheme.Muted,
                                    fontWeight = if (isActiveBranch) FontWeight.SemiBold else FontWeight.Normal,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                if (expandable) {
                                    IconButton(
                                        onClick = onRootExpandToggle,
                                        modifier = Modifier.size(28.dp),
                                    ) {
                                        Icon(
                                            imageVector =
                                                if (showChildrenInline) {
                                                    Icons.Default.ArrowDropDown
                                                } else {
                                                    Icons.AutoMirrored.Filled.ArrowRight
                                                },
                                            contentDescription = null,
                                            tint = TajsOSTheme.Muted,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (!isExpandedPresentation && expandable) {
                DropdownMenu(
                    expanded = isFlyoutExpanded,
                    onDismissRequest = onDismissFlyout,
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = rootLabel,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = null,
                            )
                        },
                        onClick = {
                            onDismissFlyout()
                            onRootNavigate()
                        },
                    )
                    HorizontalDivider(color = TajsOSTheme.GhostBorder)
                    children.forEach { child ->
                        val isActiveChild =
                            isChildActive(
                                root = screen,
                                child = child,
                                currentScreen = currentScreen,
                                currentRootScreen = currentRootScreen,
                                activeTasksTab = activeTasksTab,
                            )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(child.label),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = sidebarScreenIcon(child),
                                    contentDescription = null,
                                )
                            },
                            trailingIcon = {
                                if (isActiveChild) {
                                    Icon(
                                        imageVector = Icons.Default.FiberManualRecord,
                                        contentDescription = null,
                                        tint = TajsOSTheme.Primary,
                                        modifier = Modifier.size(10.dp),
                                    )
                                }
                            },
                            onClick = {
                                onDismissFlyout()
                                onChildNavigate(child)
                            },
                        )
                    }
                }
            }
        }

        AnimatedVisibility(visible = showChildrenInline, enter = fadeIn(), exit = fadeOut()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(start = 34.dp, end = 10.dp, top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                children.forEach { child ->
                    val childContextMenuState = rememberMouseContextMenuState()
                    val isActiveChild =
                        isChildActive(
                            root = screen,
                            child = child,
                            currentScreen = currentScreen,
                            currentRootScreen = currentRootScreen,
                            activeTasksTab = activeTasksTab,
                        )
                    MouseContextMenuHost(
                        state = childContextMenuState,
                        modifier = Modifier.fillMaxWidth(),
                        menuContent = {
                            DropdownMenuItem(
                                text = { Text(text = stringResource(Res.string.common_open)) },
                                onClick = {
                                    childContextMenuState.dismiss()
                                    onChildNavigate(child)
                                },
                            )
                        },
                    ) {
                        Surface(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .mouseClickable(
                                        onClick = { onChildNavigate(child) },
                                        onSecondaryClickAt = { childContextMenuState.showAt(it) },
                                        middleClickFallbackToPrimary = true,
                                    ),
                            color =
                                if (isActiveChild) {
                                    TajsOSTheme.Primary.copy(alpha = 0.12f)
                                } else {
                                    Color.Transparent
                                },
                            shape = RoundedCornerShape(8.dp),
                            tonalElevation = 0.dp,
                            shadowElevation = 0.dp,
                        ) {
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    imageVector = sidebarScreenIcon(child),
                                    contentDescription = null,
                                    tint =
                                        if (isActiveChild) {
                                            TajsOSTheme.Primary
                                        } else {
                                            TajsOSTheme.Muted.copy(alpha = 0.75f)
                                        },
                                    modifier = Modifier.size(14.dp),
                                )
                                Text(
                                    text = stringResource(child.label),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isActiveChild) TajsOSTheme.Text else TajsOSTheme.Muted,
                                    fontWeight = if (isActiveChild) FontWeight.SemiBold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Bottom sidebar cluster with primary action and account/profile area.
 */
@Composable
fun SidebarBottomActions(
    showExpandedContent: Boolean,
    currentMode: ModeEntity?,
    userProfile: UserProfile,
    onNewEntry: () -> Unit,
    onNavigateToProfile: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
    ) {
        NewEntryButton(
            expanded = showExpandedContent,
            onClick = onNewEntry,
        )
        Spacer(Modifier.height(8.dp))
        UserProfileSidebarSection(
            expanded = showExpandedContent,
            userProfile = userProfile,
            currentMode = currentMode,
            onClick = onNavigateToProfile,
        )
    }
}

/**
 * Primary shell action button shown above the profile section.
 */
@Composable
fun NewEntryButton(
    expanded: Boolean,
    onClick: () -> Unit,
) {
    val label = stringResource(Res.string.sidebar_new_entry)
    SidebarTooltip(enabled = !expanded, text = label) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
            color = TajsOSTheme.Primary,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(if (expanded) 46.dp else 42.dp)
                    .mouseButtons(
                        onSecondaryClick = onClick,
                        onMiddleClick = onClick,
                    ),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (expanded) Arrangement.Start else Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = label,
                    tint = TajsOSTheme.Background,
                )
                AnimatedVisibility(visible = expanded, enter = fadeIn(), exit = fadeOut()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            color = TajsOSTheme.Background,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Bottom-anchored user account section in sidebar.
 */
@Composable
fun UserProfileSidebarSection(
    expanded: Boolean,
    userProfile: UserProfile,
    currentMode: ModeEntity?,
    onClick: () -> Unit,
) {
    val displayName = userProfile.resolveDisplayName()
    val modeName = currentMode?.name ?: stringResource(Res.string.common_no_active_mode)
    val initials = profileInitials(userProfile)

    SidebarTooltip(enabled = !expanded, text = displayName) {
        Surface(
            onClick = onClick,
            modifier =
                Modifier.fillMaxWidth().mouseButtons(
                    onSecondaryClick = onClick,
                    onMiddleClick = onClick,
                ),
            shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
            color = TajsOSTheme.CardNestedSurface,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color =
                        currentMode?.themeColor?.let { Color(it).copy(alpha = 0.25f) }
                            ?: TajsOSTheme.SurfaceHigh,
                    modifier = Modifier.size(32.dp),
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        if (initials.isBlank()) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = TajsOSTheme.Muted,
                                modifier = Modifier.size(18.dp),
                            )
                        } else {
                            Text(
                                text = initials,
                                color = TajsOSTheme.Text,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                maxLines = 1,
                            )
                        }
                    }
                }
                AnimatedVisibility(visible = expanded, enter = fadeIn(), exit = fadeOut()) {
                    Column {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TajsOSTheme.Text,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.FiberManualRecord,
                                contentDescription = null,
                                tint = TajsOSTheme.Success,
                                modifier = Modifier.size(9.dp),
                            )
                            Text(
                                text = modeName,
                                style = MaterialTheme.typography.labelSmall,
                                color = TajsOSTheme.Muted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SidebarTooltip(
    enabled: Boolean,
    text: String,
    content: @Composable () -> Unit,
) {
    if (!enabled) {
        content()
        return
    }

    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = {
            PlainTooltip {
                Text(text)
            }
        },
        state = rememberTooltipState(),
    ) {
        content()
    }
}

private fun profileInitials(profile: UserProfile): String {
    val first =
        profile.firstName
            .trim()
            .firstOrNull()
            ?.uppercaseChar()
            ?.toString()
            .orEmpty()
    val last =
        profile.lastName
            .trim()
            .firstOrNull()
            ?.uppercaseChar()
            ?.toString()
            .orEmpty()
    val initials = first + last
    if (initials.isNotBlank()) return initials
    return profile.nickname
        .trim()
        .take(2)
        .uppercase()
}

private fun navigateFromSidebar(
    screen: Screen,
    onNavigate: (Screen) -> Unit,
    onNavigateToTasksTab: (TasksTab) -> Unit,
) {
    val tab = screen.toTasksTabOrNull()
    if (tab != null) {
        onNavigateToTasksTab(tab)
    } else {
        onNavigate(screen)
    }
}

private fun sidebarScreenIcon(screen: Screen): androidx.compose.ui.graphics.vector.ImageVector {
    val tab = screen.toTasksTabOrNull()
    return if (tab == TasksTab.ALL) Icons.AutoMirrored.Filled.List else tab?.icon ?: screen.icon
}

private fun isChildActive(
    root: Screen,
    child: Screen,
    currentScreen: Screen?,
    currentRootScreen: Screen?,
    activeTasksTab: TasksTab,
): Boolean {
    val tab = child.toTasksTabOrNull()
    if (tab != null && root == Screen.Tasks) {
        return currentRootScreen == Screen.Tasks && activeTasksTab == tab
    }

    if (currentScreen?.route == child.route) {
        return true
    }

    return child is Screen.Sub &&
        root == Screen.Settings &&
        child.route.contains("${Screen.PARAM_TAB}=${Screen.Settings.SUB_PREFERENCES}") &&
        currentScreen == Screen.Settings
}

private fun Screen.toTasksTabOrNull(): TasksTab? =
    if (this is Screen.Sub && parent == Screen.Tasks) {
        TasksTab.fromRouteSegment(route.substringAfterLast("="))
    } else {
        null
    }

private fun sidebarChildren(screen: Screen): List<Screen> =
    screen.children.filterNot { child ->
        val isDefaultTasksChild =
            screen == Screen.Tasks && child.toTasksTabOrNull() == TasksTab.COMMAND
        val isDefaultSettingsChild =
            screen == Screen.Settings &&
                child is Screen.Sub &&
                child.route.contains("${Screen.PARAM_TAB}=${Screen.Settings.SUB_PREFERENCES}")
        isDefaultTasksChild || isDefaultSettingsChild
    }
