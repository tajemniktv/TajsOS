/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.ui.components.common.GlassMaterial
import com.tajemniktv.tajsos.ui.components.common.glassContainerColor
import com.tajemniktv.tajsos.ui.components.common.glassChrome
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme

@Composable
fun InfoCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    color: Color = TajsOSTheme.Primary,
    onClick: () -> Unit = {},
) {
    Surface(
        onClick = onClick,
        modifier = modifier.glassChrome(shape = RoundedCornerShape(TajsOSTheme.RadiusMd), material = GlassMaterial.REGULAR),
        color = glassContainerColor(TajsOSTheme.Surface),
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
        border = BorderStroke(1.dp, TajsOSTheme.Border),
    ) {
        Column(modifier = Modifier.padding(TajsOSTheme.SpacingMd)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = TajsOSTheme.Muted,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = TajsOSTheme.Muted,
                    fontWeight = FontWeight.Bold,
                    fontSize = 8.sp,
                )
            }
            Spacer(Modifier.width(TajsOSTheme.SpacingSm))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
fun StatusCard(
    status: String,
    modifier: Modifier = Modifier,
    color: Color = TajsOSTheme.Success,
    onClick: () -> Unit = {},
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().glassChrome(shape = RoundedCornerShape(TajsOSTheme.RadiusMd), material = GlassMaterial.REGULAR),
        color = glassContainerColor(TajsOSTheme.Surface),
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
        border = BorderStroke(1.dp, TajsOSTheme.Border),
    ) {
        Column(modifier = Modifier.padding(TajsOSTheme.SpacingMd)) {
            Text(
                text = "STATUS",
                style = MaterialTheme.typography.labelSmall,
                color = TajsOSTheme.Muted,
                fontWeight = FontWeight.Bold,
                fontSize = 8.sp,
            )
            Spacer(Modifier.width(TajsOSTheme.SpacingSm))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = status.uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = TajsOSTheme.Text,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
    }
}

@Composable
fun LinkedNodeItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().glassChrome(shape = RoundedCornerShape(TajsOSTheme.RadiusMd), material = GlassMaterial.REGULAR),
        color = glassContainerColor(TajsOSTheme.Surface),
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
        border = BorderStroke(1.dp, TajsOSTheme.Border),
    ) {
        Row(
            modifier = Modifier.padding(TajsOSTheme.SpacingMd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .background(TajsOSTheme.Background, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = TajsOSTheme.Muted,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(TajsOSTheme.SpacingMd))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = TajsOSTheme.Text,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = TajsOSTheme.Muted,
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TajsOSTheme.Muted,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
fun ConnectionCard(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().glassChrome(shape = RoundedCornerShape(TajsOSTheme.RadiusMd), material = GlassMaterial.THIN),
        color = glassContainerColor(Color.Transparent),
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
        border = BorderStroke(1.dp, TajsOSTheme.Border.copy(alpha = 0.5f)),
    ) {
        Row(
            modifier = Modifier.padding(TajsOSTheme.SpacingMd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                tint = TajsOSTheme.Muted,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = TajsOSTheme.Muted,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
            )
        }
    }
}
