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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.ModeEntity
import com.tajemniktv.tajsos.data.UserProfile
import com.tajemniktv.tajsos.data.resolveDisplayName
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.screens.tasks.TasksTab
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private val ExpandedSidebarWidth = 304.dp
private val CollapsedSidebarWidth = 86.dp

/**
 * Persistent app sidebar with explicit behavior modes and inline expandable root sections.
 */
@Composable
fun AppSidebar(
    shellState: AppShellState,
    menuGroups: List<Pair<StringResource, List<Screen>>>,
    currentRootScreen: Screen?,
    activeTasksTab: TasksTab,
    currentMode: ModeEntity?,
    userProfile: UserProfile,
    onNavigate: (Screen) -> Unit,
    onNavigateToTasksTab: (TasksTab) -> Unit,
    onNewEntry: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hoverInteraction = remember { MutableInteractionSource() }
    val isHovered by hoverInteraction.collectIsHoveredAsState()

    LaunchedEffect(isHovered, shellState.sidebarMode) {
        if (shellState.sidebarMode != SidebarMode.HOVER_EXPAND) {
            shellState.hoverExpanded = false
            return@LaunchedEffect
        }

        if (isHovered) {
            delay(110)
            shellState.hoverExpanded = true
        } else {
            delay(180)
            shellState.hoverExpanded = false
        }
    }

    val showExpandedContent = shellState.isSidebarExpandedPresentation
    val sidebarWidth by animateDpAsState(
        targetValue = if (showExpandedContent) ExpandedSidebarWidth else CollapsedSidebarWidth,
        label = "sidebarWidth",
    )

    Surface(
        modifier =
            modifier
                .width(sidebarWidth)
                .fillMaxHeight()
                .hoverable(
                    interactionSource = hoverInteraction,
                    enabled = shellState.sidebarMode == SidebarMode.HOVER_EXPAND,
                ),
        color = TactileTheme.SurfaceLowest,
        shape = RoundedCornerShape(0.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SidebarLogoHeader(showExpandedContent = showExpandedContent)
            SidebarModeControl(shellState = shellState, showExpandedContent = showExpandedContent)

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
                            color = TactileTheme.Muted,
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
                        val expandable = rootScreen == Screen.Tasks
                        ExpandableNavSection(
                            screen = rootScreen,
                            isExpandedPresentation = showExpandedContent,
                            isActiveRoot = isActiveRoot,
                            isExpandedRoot = shellState.expandedRootRoute == rootScreen.route,
                            activeTasksTab = activeTasksTab,
                            expandable = expandable,
                            onRootClick = {
                                if (expandable) {
                                    shellState.expandedRootRoute =
                                        if (shellState.expandedRootRoute == rootScreen.route) {
                                            null
                                        } else {
                                            rootScreen.route
                                        }
                                }
                                onNavigate(rootScreen)
                            },
                            onTaskChildClick = { tab ->
                                shellState.expandedRootRoute = Screen.Tasks.route
                                onNavigateToTasksTab(tab)
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
        TBoxIcon(tintColor = TactileTheme.Primary)
        AnimatedVisibility(
            visible = showExpandedContent,
            enter = fadeIn() + slideInHorizontally(initialOffsetX = { -it / 2 }),
            exit = fadeOut() + slideOutHorizontally(targetOffsetX = { -it / 2 }),
        ) {
            Text(
                text = "TAJSOS",
                style = MaterialTheme.typography.titleMedium,
                color = TactileTheme.Text,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

/**
 * Sidebar mode toggle controls.
 */
@Composable
fun SidebarModeControl(
    shellState: AppShellState,
    showExpandedContent: Boolean,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
    ) {
        SidebarModeButton(
            label = "EXP",
            selected = shellState.sidebarMode == SidebarMode.EXPANDED,
            onClick = { shellState.sidebarMode = SidebarMode.EXPANDED },
            showExpandedContent = showExpandedContent,
            tooltip = "Expanded sidebar mode",
        )
        SidebarModeButton(
            label = "COL",
            selected = shellState.sidebarMode == SidebarMode.COLLAPSED,
            onClick = { shellState.sidebarMode = SidebarMode.COLLAPSED },
            showExpandedContent = showExpandedContent,
            tooltip = "Collapsed sidebar mode",
        )
        SidebarModeButton(
            label = "HOV",
            selected = shellState.sidebarMode == SidebarMode.HOVER_EXPAND,
            onClick = { shellState.sidebarMode = SidebarMode.HOVER_EXPAND },
            showExpandedContent = showExpandedContent,
            tooltip = "Hover-expand sidebar mode",
        )
    }
}

@Composable
private fun SidebarModeButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    showExpandedContent: Boolean,
    tooltip: String,
) {
    SidebarTooltip(enabled = !showExpandedContent, text = tooltip) {
        OutlinedButton(
            onClick = onClick,
            modifier =
                Modifier
                    .width(if (showExpandedContent) 80.dp else 40.dp)
                    .height(34.dp),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(
                text = if (showExpandedContent) label else label.first().toString(),
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) TactileTheme.Primary else TactileTheme.Muted,
                maxLines = 1,
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
    isExpandedPresentation: Boolean,
    isActiveRoot: Boolean,
    isExpandedRoot: Boolean,
    activeTasksTab: TasksTab,
    expandable: Boolean,
    onRootClick: () -> Unit,
    onTaskChildClick: (TasksTab) -> Unit,
) {
    val rootLabel = stringResource(screen.label)

    SidebarTooltip(enabled = !isExpandedPresentation, text = rootLabel) {
        Surface(
            onClick = onRootClick,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 2.dp),
            shape = RoundedCornerShape(10.dp),
            color =
                if (isActiveRoot) {
                    TactileTheme.Primary.copy(alpha = 0.17f)
                } else {
                    Color.Transparent
                },
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = screen.icon,
                    contentDescription = rootLabel,
                    tint = if (isActiveRoot) TactileTheme.Primary else TactileTheme.Muted,
                )
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
                            color = if (isActiveRoot) TactileTheme.Text else TactileTheme.Muted,
                            fontWeight = if (isActiveRoot) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (expandable) {
                            Icon(
                                imageVector = if (isExpandedRoot) Icons.Default.ArrowDropDown else Icons.AutoMirrored.Filled.ArrowRight,
                                contentDescription = null,
                                tint = TactileTheme.Muted,
                            )
                        }
                    }
                }
            }
        }
    }

    val shouldShowChildren = isExpandedPresentation && expandable && isExpandedRoot
    AnimatedVisibility(visible = shouldShowChildren, enter = fadeIn(), exit = fadeOut()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(start = 34.dp, end = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            listOf(
                TasksTab.INBOX,
                TasksTab.TODAY,
                TasksTab.ALL,
                TasksTab.ARCHIVE,
                TasksTab.COMMAND,
            ).forEach { tab ->
                val isActiveChild = isActiveRoot && activeTasksTab == tab
                Surface(
                    onClick = { onTaskChildClick(tab) },
                    modifier = Modifier.fillMaxWidth(),
                    color =
                        if (isActiveChild) {
                            TactileTheme.Primary.copy(alpha = 0.12f)
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
                            imageVector = if (tab == TasksTab.ALL) Icons.AutoMirrored.Filled.List else tab.icon,
                            contentDescription = null,
                            tint =
                                if (isActiveChild) {
                                    TactileTheme.Primary
                                } else {
                                    TactileTheme.Muted.copy(alpha = 0.75f)
                                },
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = stringResource(tab.label),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isActiveChild) TactileTheme.Text else TactileTheme.Muted,
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
    val label = "NEW ENTRY"
    SidebarTooltip(enabled = !expanded, text = label) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            color = TactileTheme.Primary,
            modifier = Modifier.fillMaxWidth().height(if (expanded) 46.dp else 42.dp),
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
                    tint = TactileTheme.Background,
                )
                AnimatedVisibility(visible = expanded, enter = fadeIn(), exit = fadeOut()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            color = TactileTheme.Background,
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
    val modeName = currentMode?.name ?: "No active mode"
    val initials = profileInitials(userProfile)

    SidebarTooltip(enabled = !expanded, text = displayName) {
        Surface(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = TactileTheme.SurfaceLow,
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
                            ?: TactileTheme.SurfaceHigh,
                    modifier = Modifier.size(32.dp),
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        if (initials.isBlank()) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = TactileTheme.Muted,
                                modifier = Modifier.size(18.dp),
                            )
                        } else {
                            Text(
                                text = initials,
                                color = TactileTheme.Text,
                                style = MaterialTheme.typography.labelSmall,
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
                            color = TactileTheme.Text,
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
                                tint = TactileTheme.Success,
                                modifier = Modifier.size(9.dp),
                            )
                            Text(
                                text = modeName,
                                style = MaterialTheme.typography.labelSmall,
                                color = TactileTheme.Muted,
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
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
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
