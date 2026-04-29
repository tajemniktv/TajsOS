/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.DecisionOptionEntity
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.cd_delete_option

@Composable
fun OptionCard(
    option: DecisionOptionEntity,
    onUpdate: (DecisionOptionEntity) -> Unit,
    onDelete: (DecisionOptionEntity) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TajsOSTheme.Background,
        shape = RoundedCornerShape(2.dp),
    ) {
        Column(modifier = Modifier.padding(TajsOSTheme.SpacingSm)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    option.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(onClick = { onDelete(option) }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(Res.string.cd_delete_option),
                        tint = TajsOSTheme.Muted,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
            option.description?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = TajsOSTheme.Muted,
                )
            }
        }
    }
}

