/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.sidebar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.theme.TactileTheme

/**
 * Shared renderer for contextual sidebars used by screen-specific sidebar variants.
 */
@Composable
internal fun ContextSidebarScaffold(
    contextHeader: String,
    panelLabel: String,
    sections: List<SidebarSection>,
    onBackToMainSidebar: () -> Unit,
    onNavigate: (Screen) -> Unit = {},
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = TactileTheme.SpacingMd,
                    vertical = TactileTheme.SpacingSm,
                ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
    ) {
        OutlinedButton(
            onClick = onBackToMainSidebar,
            shape = RoundedCornerShape(TactileTheme.RadiusSm),
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
        }
        Text(
            contextHeader.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = TactileTheme.Muted.copy(alpha = 0.85f),
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
        )
    }

    Text(
        panelLabel.uppercase(),
        modifier =
            Modifier.padding(
                horizontal = TactileTheme.SpacingMd,
                vertical = TactileTheme.SpacingSm,
            ),
        style = MaterialTheme.typography.labelSmall,
        color = TactileTheme.Muted.copy(alpha = 0.6f),
        fontSize = 10.sp,
        fontWeight = FontWeight.ExtraBold,
    )

    sections.forEach { section ->
        Text(
            section.title,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = TactileTheme.SpacingMd,
                        vertical = TactileTheme.SpacingSm,
                    ),
            style = MaterialTheme.typography.labelSmall,
            color = TactileTheme.Muted.copy(alpha = 0.75f),
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
        )

        section.items.forEach { item ->
            Surface(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                        .then(
                            if (item.screen != null) {
                                Modifier.clickable { onNavigate(item.screen) }
                            } else {
                                Modifier
                            },
                        ),
                color = Color.Transparent,
                shape = RoundedCornerShape(TactileTheme.RadiusSm),
                border = BorderStroke(1.dp, TactileTheme.Border.copy(alpha = 0.25f)),
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = TactileTheme.Muted,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        item.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = TactileTheme.Muted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
        Spacer(Modifier.size(TactileTheme.SpacingSm))
    }
}

/**
 * Sidebar content section model used by contextual sidebar implementations.
 */
internal data class SidebarSection(
    val title: String,
    val items: List<SidebarItem>,
)

/**
 * Sidebar item model with an optional navigation target.
 */
internal data class SidebarItem(
    val label: String,
    val screen: Screen? = null,
)
