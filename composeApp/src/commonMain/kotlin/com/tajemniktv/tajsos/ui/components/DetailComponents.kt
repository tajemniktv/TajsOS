/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.ui.theme.TactileTheme

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun DetailHeader(
    category: String,
    title: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = category.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = TactileTheme.Muted,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.align(Alignment.End)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            color = TactileTheme.Text,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.align(Alignment.End),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun ModuleHeader(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(bottom = TactileTheme.SpacingMd)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(TactileTheme.SpacingSm))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = color,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
        }
        HorizontalDivider(
            color = color.copy(alpha = 0.3f),
            thickness = 2.dp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun DashCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusLg),
        border = BorderStroke(1.dp, TactileTheme.Border)
    ) { content() }
}

@Composable
fun <T> SelectorDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    title: String,
    prefix: String = "SYSTEM_SELECTOR // MODULE_INTAKE",
    options: List<T>,
    selectedOption: T?,
    onSelect: (T) -> Unit,
    optionName: (T) -> String,
    optionIcon: (T) -> ImageVector,
    optionSubtext: (T) -> String = { "" }
) {
    if (!show) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(TactileTheme.Background.copy(alpha = 0.95f)),
            color = TactileTheme.Background.copy(alpha = 0.95f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(TactileTheme.SpacingMd)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            prefix.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = TactileTheme.Muted,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            title.uppercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = TactileTheme.Text,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Surface(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, TactileTheme.Border)
                    ) {
                        Text(
                            "STATUS: READY",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = TactileTheme.Muted,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(TactileTheme.SpacingLg))

                // Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
                    verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
                    modifier = Modifier.weight(1f)
                ) {
                    items(options) { option ->
                        val isSelected = option == selectedOption
                        Surface(
                            onClick = { onSelect(option) },
                            color = if (isSelected) TactileTheme.Primary else TactileTheme.Surface,
                            shape = RoundedCornerShape(TactileTheme.RadiusMd),
                            modifier = Modifier.height(140.dp),
                            border = if (isSelected) null else BorderStroke(
                                1.dp,
                                TactileTheme.Border
                            )
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(TactileTheme.SpacingMd),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    optionIcon(option),
                                    contentDescription = null,
                                    tint = if (isSelected) TactileTheme.Background else TactileTheme.Primary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(Modifier.height(TactileTheme.SpacingMd))
                                Text(
                                    optionName(option).uppercase(),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) TactileTheme.Background else TactileTheme.Text
                                )
                                val sub = optionSubtext(option)
                                if (sub.isNotEmpty()) {
                                    Text(
                                        sub.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) TactileTheme.Background.copy(alpha = 0.7f) else TactileTheme.Muted,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Footer
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = TactileTheme.SpacingMd),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "TAJS_OS_v1.2.0  •  NEURAL_INTERFACE",
                            style = MaterialTheme.typography.labelSmall,
                            color = TactileTheme.Muted,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = TactileTheme.Muted,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TactileTheme.Surface,
                            contentColor = TactileTheme.Text
                        ),
                        shape = RoundedCornerShape(TactileTheme.RadiusMd),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text(
                            "CANCEL SESSION",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = TactileTheme.Surface,
    contentColor: Color = TactileTheme.Text,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun DetailSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    color: Color = TactileTheme.Primary
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun InfoCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    color: Color = TactileTheme.Primary,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, TactileTheme.Border)
    ) {
        Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = TactileTheme.Muted,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Muted,
                    fontWeight = FontWeight.Bold,
                    fontSize = 8.sp
                )
            }
            Spacer(Modifier.height(TactileTheme.SpacingSm))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StatusCard(
    status: String,
    modifier: Modifier = Modifier,
    color: Color = TactileTheme.Success,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, TactileTheme.Border)
    ) {
        Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
            Text(
                text = "STATUS",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Muted,
                fontWeight = FontWeight.Bold,
                fontSize = 8.sp
            )
            Spacer(Modifier.height(TactileTheme.SpacingSm))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = status.uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = TactileTheme.Text,
                    fontWeight = FontWeight.ExtraBold
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
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, TactileTheme.Border)
    ) {
        Row(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(TactileTheme.Background, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = TactileTheme.Muted,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(TactileTheme.SpacingMd))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = TactileTheme.Text,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Muted
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TactileTheme.Muted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ConnectionCard(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        color = Color.Transparent,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, TactileTheme.Border.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                tint = TactileTheme.Muted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Muted,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}
