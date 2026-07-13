/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.profile

import com.tajemniktv.tajsos.ui.components.TactileOutlinedTextField
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.MedicationEntity
import com.tajemniktv.tajsos.data.UserDisplayNameFormat
import com.tajemniktv.tajsos.data.resolveDisplayName
import com.tajemniktv.tajsos.ui.components.common.EmptyState
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.medication_empty_message
import tajsos.composeapp.generated.resources.profile_bio
import tajsos.composeapp.generated.resources.profile_birth_date
import tajsos.composeapp.generated.resources.profile_city
import tajsos.composeapp.generated.resources.profile_completion
import tajsos.composeapp.generated.resources.profile_contact_section
import tajsos.composeapp.generated.resources.profile_country
import tajsos.composeapp.generated.resources.profile_display_first_last
import tajsos.composeapp.generated.resources.profile_display_first_name
import tajsos.composeapp.generated.resources.profile_display_format
import tajsos.composeapp.generated.resources.profile_display_full_name
import tajsos.composeapp.generated.resources.profile_display_nickname
import tajsos.composeapp.generated.resources.profile_email
import tajsos.composeapp.generated.resources.profile_email_invalid
import tajsos.composeapp.generated.resources.profile_empty_hint
import tajsos.composeapp.generated.resources.profile_first_name
import tajsos.composeapp.generated.resources.profile_greeting
import tajsos.composeapp.generated.resources.profile_identity_section
import tajsos.composeapp.generated.resources.profile_last_name
import tajsos.composeapp.generated.resources.profile_medications
import tajsos.composeapp.generated.resources.profile_nickname
import tajsos.composeapp.generated.resources.profile_occupation
import tajsos.composeapp.generated.resources.profile_phone
import tajsos.composeapp.generated.resources.profile_remove_avatar
import tajsos.composeapp.generated.resources.profile_save
import tajsos.composeapp.generated.resources.profile_saved
import tajsos.composeapp.generated.resources.profile_select_avatar
import tajsos.composeapp.generated.resources.profile_time_zone
import tajsos.composeapp.generated.resources.profile_unsaved
import tajsos.composeapp.generated.resources.profile_website
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.PointerIcon

private val profileBlockRenderers: Map<String, ProfileDashboardBlockRenderer> =
    mapOf(
        "identity_header" to ::renderIdentityHeaderBlock,
        "signature_panel" to ::renderSignaturePanelBlock,
        "identity_module" to ::renderIdentityModuleBlock,
        "contact_module" to ::renderContactModuleBlock,
        "about_module" to ::renderAboutModuleBlock,
        "medications_module" to ::renderMedicationsModuleBlock,
    )

@Composable
internal fun renderProfileBlock(
    blockId: String,
    context: ProfileScreenContext,
) {
    profileBlockRenderers[blockId]?.invoke(context)
}

@Composable
private fun renderIdentityHeaderBlock(context: ProfileScreenContext) {
    val displayName = context.editor.toProfile(context.profile).resolveDisplayName()
    ModulePanel(backgroundAlpha = 0.95f) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "SYSTEM AUTH: SECURE",
                    style = MaterialTheme.typography.labelSmall,
                    color = TajsOSTheme.AccentGreen,
                )
                Text(
                    "IDENTITY MANAGEMENT",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TajsOSTheme.Text,
                )
                Text(
                    displayName.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = TajsOSTheme.Muted,
                )
                val avatarRef = context.editor.avatarRef
                if (!avatarRef.isNullOrBlank()) {
                    Text(
                        avatarRef,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        color = TajsOSTheme.Muted,
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TinyStatCard(title = "PROFILE", value = "${context.completion}%")
                TinyStatCard(title = "SYNC", value = "LOCAL")
            }
        }
    }
}

@Composable
private fun renderSignaturePanelBlock(context: ProfileScreenContext) {
    ModulePanel {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(108.dp)
                        .clip(RoundedCornerShape(TajsOSTheme.RadiusMd))
                        .background(
                            Brush.linearGradient(
                                colors =
                                    listOf(
                                        TajsOSTheme.SurfaceLowest,
                                        TajsOSTheme.SurfaceHigh,
                                    ),
                            ),
                        ),
                contentAlignment = Alignment.Center,
            ) {
                val initials = context.editor.initials()
                if (initials.isBlank()) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = TajsOSTheme.Primary,
                        modifier = Modifier.size(44.dp),
                    )
                } else {
                    Text(
                        initials,
                        style = MaterialTheme.typography.headlineMedium,
                        color = TajsOSTheme.Primary,
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
            ) {
                Text(
                    "NEURAL SIGNATURE",
                    style = MaterialTheme.typography.titleMedium,
                    color = TajsOSTheme.Text,
                )
                Text(
                    context.editor.avatarRef ?: stringResource(Res.string.profile_empty_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = TajsOSTheme.Muted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { context.onPickAvatar?.invoke() },
                        enabled = context.onPickAvatar != null,
                        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            stringResource(Res.string.profile_select_avatar),
                            modifier = Modifier.padding(start = 6.dp),
                        )
                    }
                    TextButton(
                        onClick = {
                            context.onEditorChange(context.editor.copy(avatarRef = null))
                        },
                        enabled = context.editor.avatarRef != null,
                    ) {
                        Text(stringResource(Res.string.profile_remove_avatar))
                    }
                }
            }
        }
    }
}

@Composable
private fun renderIdentityModuleBlock(context: ProfileScreenContext) {
    ModulePanel {
        ModuleTitle(title = stringResource(Res.string.profile_identity_section))
        ProfileField(
            value = context.editor.firstName,
            onValueChange = { context.onEditorChange(context.editor.copy(firstName = it)) },
            label = stringResource(Res.string.profile_first_name),
        )
        ProfileField(
            value = context.editor.lastName,
            onValueChange = { context.onEditorChange(context.editor.copy(lastName = it)) },
            label = stringResource(Res.string.profile_last_name),
        )
        ProfileField(
            value = context.editor.nickname,
            onValueChange = { context.onEditorChange(context.editor.copy(nickname = it)) },
            label = stringResource(Res.string.profile_nickname),
        )
        ProfileField(
            value = context.editor.birthDate,
            onValueChange = { context.onEditorChange(context.editor.copy(birthDate = it)) },
            label = stringResource(Res.string.profile_birth_date),
        )
        ProfileDisplayNameFormatSelector(
            selected = context.editor.displayNameFormat,
            onSelected = { format ->
                context.onEditorChange(context.editor.copy(displayNameFormat = format))
            },
        )
    }
}

@Composable
private fun renderContactModuleBlock(context: ProfileScreenContext) {
    ModulePanel {
        ModuleTitle(title = stringResource(Res.string.profile_contact_section))
        ProfileField(
            value = context.editor.email,
            onValueChange = {
                context.onEditorChange(context.editor.copy(email = it))
                context.onShowEmailErrorChange(false)
            },
            label = stringResource(Res.string.profile_email),
            isError = context.showEmailError,
            supportingText =
                if (context.showEmailError) {
                    stringResource(Res.string.profile_email_invalid)
                } else {
                    null
                },
        )
        ProfileField(
            value = context.editor.phoneNumber,
            onValueChange = { context.onEditorChange(context.editor.copy(phoneNumber = it)) },
            label = stringResource(Res.string.profile_phone),
        )
        ProfileField(
            value = context.editor.timezone,
            onValueChange = { context.onEditorChange(context.editor.copy(timezone = it)) },
            label = stringResource(Res.string.profile_time_zone),
        )
        ProfileField(
            value = context.editor.city,
            onValueChange = { context.onEditorChange(context.editor.copy(city = it)) },
            label = stringResource(Res.string.profile_city),
        )
        ProfileField(
            value = context.editor.country,
            onValueChange = { context.onEditorChange(context.editor.copy(country = it)) },
            label = stringResource(Res.string.profile_country),
        )
        ProfileField(
            value = context.editor.occupation,
            onValueChange = { context.onEditorChange(context.editor.copy(occupation = it)) },
            label = stringResource(Res.string.profile_occupation),
        )
        ProfileField(
            value = context.editor.website,
            onValueChange = { context.onEditorChange(context.editor.copy(website = it)) },
            label = stringResource(Res.string.profile_website),
        )
    }
}

@Composable
private fun renderAboutModuleBlock(context: ProfileScreenContext) {
    ModulePanel {
        ModuleTitle(title = "ABOUT MODULE")
        ProfileField(
            value = context.editor.preferredGreeting,
            onValueChange = { context.onEditorChange(context.editor.copy(preferredGreeting = it)) },
            label = stringResource(Res.string.profile_greeting),
        )
        TactileOutlinedTextField(
            value = context.editor.bio,
            onValueChange = { context.onEditorChange(context.editor.copy(bio = it)) },
            label = { Text(stringResource(Res.string.profile_bio)) },
            modifier = Modifier.fillMaxWidth().height(124.dp),
            shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
            singleLine = false,
            colors = profileFieldColors(),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text =
                    when {
                        context.justSaved -> {
                            stringResource(Res.string.profile_saved)
                        }

                        context.hasChanges -> {
                            stringResource(Res.string.profile_unsaved)
                        }

                        else -> {
                            stringResource(
                                Res.string.profile_completion,
                                context.completion,
                            )
                        }
                    },
                style = MaterialTheme.typography.labelSmall,
                color = if (context.justSaved) TajsOSTheme.Primary else TajsOSTheme.Muted,
            )
            Button(
                onClick = {
                    if (!context.isEmailValid) {
                        context.onShowEmailErrorChange(true)
                        return@Button
                    }
                    context.onSaveProfile()
                },
                enabled = context.hasChanges,
                shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = TajsOSTheme.Primary,
                        contentColor = TajsOSTheme.Background,
                    ),
            ) {
                Text(
                    stringResource(Res.string.profile_save),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun renderMedicationsModuleBlock(context: ProfileScreenContext) {
    ModulePanel {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ModuleTitle(title = stringResource(Res.string.profile_medications))
            IconButton(onClick = context.onOpenAddMedication, modifier = Modifier.pointerHoverIcon(PointerIcon.Hand).size(48.dp)) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = TajsOSTheme.Primary,
                )
            }
        }
        if (context.medications.isEmpty()) {
            EmptyState(
                message = stringResource(Res.string.medication_empty_message),
                description = null,
                fillParent = false,
                showContainer = false,
            )
        } else {
            context.medications.forEach { med ->
                MedicationItem(
                    medication = med,
                    onDelete = { context.onDeleteMedication(med) },
                )
            }
        }
    }
}

@Composable
private fun TinyStatCard(
    title: String,
    value: String,
) {
    Surface(
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
        color = TajsOSTheme.SurfaceHighest.copy(alpha = 0.88f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = TajsOSTheme.Muted)
            Text(value, style = MaterialTheme.typography.titleSmall, color = TajsOSTheme.Primary)
        }
    }
}

@Composable
private fun ModulePanel(
    backgroundAlpha: Float = 0.82f,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        color = TajsOSTheme.SurfaceLow.copy(alpha = backgroundAlpha),
        shape = RoundedCornerShape(TajsOSTheme.RadiusLg),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(TajsOSTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
            content = content,
        )
    }
}

@Composable
private fun ModuleTitle(title: String) {
    Text(
        title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = TajsOSTheme.Primary,
    )
}

@Composable
private fun ProfileField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean = false,
    supportingText: String? = null,
) {
    TactileOutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
        isError = isError,
        supportingText = {
            if (supportingText != null) {
                Text(supportingText)
            }
        },
        colors = profileFieldColors(),
    )
}

@Composable
private fun profileFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedTextColor = TajsOSTheme.Text,
        unfocusedTextColor = TajsOSTheme.Text,
        focusedContainerColor = TajsOSTheme.SurfaceLowest.copy(alpha = 0.95f),
        unfocusedContainerColor = TajsOSTheme.SurfaceLowest.copy(alpha = 0.92f),
        focusedBorderColor = TajsOSTheme.GhostBorder,
        unfocusedBorderColor = TajsOSTheme.GhostBorder.copy(alpha = 0.25f),
        cursorColor = TajsOSTheme.Primary,
        focusedLabelColor = TajsOSTheme.Primary,
        unfocusedLabelColor = TajsOSTheme.Muted,
    )

@Composable
private fun ProfileDisplayNameFormatSelector(
    selected: UserDisplayNameFormat,
    onSelected: (UserDisplayNameFormat) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            stringResource(Res.string.profile_display_format),
            style = MaterialTheme.typography.labelSmall,
            color = TajsOSTheme.Primary,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ProfileFormatChip(
                label = stringResource(Res.string.profile_display_nickname),
                selected = selected == UserDisplayNameFormat.NICKNAME,
                onClick = { onSelected(UserDisplayNameFormat.NICKNAME) },
            )
            ProfileFormatChip(
                label = stringResource(Res.string.profile_display_first_name),
                selected = selected == UserDisplayNameFormat.FIRST_NAME,
                onClick = { onSelected(UserDisplayNameFormat.FIRST_NAME) },
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ProfileFormatChip(
                label = stringResource(Res.string.profile_display_first_last),
                selected = selected == UserDisplayNameFormat.FIRST_LAST,
                onClick = { onSelected(UserDisplayNameFormat.FIRST_LAST) },
            )
            ProfileFormatChip(
                label = stringResource(Res.string.profile_display_full_name),
                selected = selected == UserDisplayNameFormat.FULL_NAME,
                onClick = { onSelected(UserDisplayNameFormat.FULL_NAME) },
            )
        }
    }
}

@Composable
private fun ProfileFormatChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        colors =
            ButtonDefaults.textButtonColors(
                contentColor = if (selected) TajsOSTheme.Primary else TajsOSTheme.Muted,
            ),
    ) {
        Text(label)
    }
}

@Composable
private fun MedicationItem(
    medication: MedicationEntity,
    onDelete: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TajsOSTheme.CardSurface.copy(alpha = 0.7f),
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
    ) {
        Row(
            modifier = Modifier.padding(TajsOSTheme.SpacingMd),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    medication.substance,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TajsOSTheme.Text,
                )
                if (medication.brandNames.isNotEmpty()) {
                    Text(
                        medication.brandNames,
                        style = MaterialTheme.typography.labelSmall,
                        color = TajsOSTheme.Muted,
                    )
                }
                Text(
                    "${medication.dosage ?: ""} ${if (medication.takeAtHour != null) "@ ${medication.takeAtHour}:00" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TajsOSTheme.Primary,
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.pointerHoverIcon(PointerIcon.Hand).size(48.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TajsOSTheme.Error)
            }
        }
    }
}
