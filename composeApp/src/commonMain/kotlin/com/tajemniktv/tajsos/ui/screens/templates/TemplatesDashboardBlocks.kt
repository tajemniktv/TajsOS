/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.templates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tajemniktv.tajsos.ui.components.common.EmptyState
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.archive_delete
import tajsos.composeapp.generated.resources.templates_add_desc
import tajsos.composeapp.generated.resources.templates_empty
import tajsos.composeapp.generated.resources.type_area
import tajsos.composeapp.generated.resources.type_note
import tajsos.composeapp.generated.resources.type_project
import tajsos.composeapp.generated.resources.type_record
import tajsos.composeapp.generated.resources.type_task
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon

object TemplatesDashboardBlocks {
    private val renderers: Map<String, TemplatesDashboardBlockRenderer> =
        mapOf(
            "templates_list" to ::renderTemplatesList,
        )

    fun resolve(id: String): TemplatesDashboardBlockRenderer? = renderers[id]
}

@Composable
private fun renderTemplatesList(context: TemplatesDashboardContext) {
    val templates = context.templates
    if (templates.isEmpty()) {
        EmptyState(message = stringResource(Res.string.templates_empty)) {
            Spacer(modifier = Modifier.height(TajsOSTheme.SpacingMd))
            Button(onClick = context.onShowAddDialog, modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)) {
                Text(stringResource(Res.string.templates_add_desc))
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
        ) {
            items(templates, key = { it.id }) { template ->
                ListItem(
                    headlineContent = { Text(template.name) },
                    supportingContent = {
                        val typeLabel =
                            when (template.nodeType) {
                                "task" -> stringResource(Res.string.type_task)
                                "note" -> stringResource(Res.string.type_note)
                                "record" -> stringResource(Res.string.type_record)
                                "project" -> stringResource(Res.string.type_project)
                                "area" -> stringResource(Res.string.type_area)
                                else -> template.nodeType
                            }
                        Text(typeLabel.uppercase())
                    },
                    trailingContent = {
                        IconButton(onClick = { context.onDeleteTemplate(template) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(Res.string.archive_delete),
                                tint = TajsOSTheme.Error,
                            )
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = TajsOSTheme.CardSurface),
                )
            }
        }
    }
}
