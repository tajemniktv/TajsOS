/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.MedicationEntity
import com.tajemniktv.tajsos.data.UserDisplayNameFormat
import com.tajemniktv.tajsos.data.UserProfile
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.common_back
import tajsos.composeapp.generated.resources.med_brand_names
import tajsos.composeapp.generated.resources.med_dosage
import tajsos.composeapp.generated.resources.med_is_optional
import tajsos.composeapp.generated.resources.med_save
import tajsos.composeapp.generated.resources.med_substance
import tajsos.composeapp.generated.resources.med_take_at
import tajsos.composeapp.generated.resources.profile_add_med
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

/**
 * Dedicated local-profile editor for the TajsOS operator identity.
 *
 * This screen intentionally manages personal profile information only (not account auth) and keeps
 * medication controls available to preserve existing behavior.
 */
@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onPickAvatar: (() -> Unit)? = null,
    pickedAvatarRef: String? = null,
    onAvatarPickedConsumed: () -> Unit = {},
) {
    val profile by viewModel.userProfile.collectAsState()
    val medications by viewModel.medications.collectAsState()

    var editor by remember(profile.updatedAt) { mutableStateOf(ProfileEditorState.from(profile)) }
    var showAddMedDialog by remember { mutableStateOf(false) }
    var showEmailError by remember { mutableStateOf(false) }
    var justSaved by remember { mutableStateOf(false) }

    LaunchedEffect(pickedAvatarRef) {
        if (!pickedAvatarRef.isNullOrBlank()) {
            editor = editor.copy(avatarRef = pickedAvatarRef)
            onAvatarPickedConsumed()
        }
    }

    val hasChanges = editor.toProfile(profile).withoutUpdatedAt() != profile.withoutUpdatedAt()
    val emailValid = editor.email.isBlank() || EMAIL_REGEX.matches(editor.email.trim())
    val completion = editor.completionPercent()

    LaunchedEffect(hasChanges) {
        if (hasChanges) {
            justSaved = false
        }
    }

    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .background(TactileTheme.Background)
                .padding(TactileTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
    ) {
        item {
            Surface(
                color = TactileTheme.Surface.copy(alpha = 0.6f),
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(TactileTheme.SpacingMd),
                    verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            stringResource(Res.string.profile_identity_section),
                            style = MaterialTheme.typography.labelSmall,
                            color = TactileTheme.Primary,
                        )
                        Text(
                            stringResource(Res.string.profile_completion, completion),
                            style = MaterialTheme.typography.labelSmall,
                            color = TactileTheme.Muted,
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(TactileTheme.Primary.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            val initials = editor.initials()
                            if (initials.isBlank()) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = TactileTheme.Primary,
                                )
                            } else {
                                Text(
                                    initials,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TactileTheme.Primary,
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = editor.avatarRef.orEmpty().ifBlank { stringResource(Res.string.profile_empty_hint) },
                                style = MaterialTheme.typography.labelSmall,
                                color = TactileTheme.Muted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { onPickAvatar?.invoke() },
                                    enabled = onPickAvatar != null,
                                    shape = RoundedCornerShape(TactileTheme.RadiusSm),
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text(
                                        stringResource(Res.string.profile_select_avatar),
                                        modifier = Modifier.padding(start = 6.dp),
                                    )
                                }
                                TextButton(
                                    onClick = { editor = editor.copy(avatarRef = null) },
                                    enabled = editor.avatarRef != null,
                                ) {
                                    Text(stringResource(Res.string.profile_remove_avatar))
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = editor.firstName,
                        onValueChange = { editor = editor.copy(firstName = it) },
                        label = { Text(stringResource(Res.string.profile_first_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(TactileTheme.RadiusSm),
                    )
                    OutlinedTextField(
                        value = editor.lastName,
                        onValueChange = { editor = editor.copy(lastName = it) },
                        label = { Text(stringResource(Res.string.profile_last_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(TactileTheme.RadiusSm),
                    )
                    OutlinedTextField(
                        value = editor.nickname,
                        onValueChange = { editor = editor.copy(nickname = it) },
                        label = { Text(stringResource(Res.string.profile_nickname)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(TactileTheme.RadiusSm),
                    )
                }
            }
        }

        item {
            Surface(
                color = TactileTheme.Surface.copy(alpha = 0.6f),
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(TactileTheme.SpacingMd),
                    verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                ) {
                    Text(
                        stringResource(Res.string.profile_contact_section),
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Primary,
                    )
                    OutlinedTextField(
                        value = editor.email,
                        onValueChange = {
                            editor = editor.copy(email = it)
                            showEmailError = false
                        },
                        label = { Text(stringResource(Res.string.profile_email)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(TactileTheme.RadiusSm),
                        isError = showEmailError,
                        supportingText = {
                            if (showEmailError) {
                                Text(stringResource(Res.string.profile_email_invalid))
                            }
                        },
                    )
                    OutlinedTextField(
                        value = editor.phoneNumber,
                        onValueChange = { editor = editor.copy(phoneNumber = it) },
                        label = { Text(stringResource(Res.string.profile_phone)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(TactileTheme.RadiusSm),
                    )
                    OutlinedTextField(
                        value = editor.birthDate,
                        onValueChange = { editor = editor.copy(birthDate = it) },
                        label = { Text(stringResource(Res.string.profile_birth_date)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(TactileTheme.RadiusSm),
                    )
                    OutlinedTextField(
                        value = editor.city,
                        onValueChange = { editor = editor.copy(city = it) },
                        label = { Text(stringResource(Res.string.profile_city)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(TactileTheme.RadiusSm),
                    )
                    OutlinedTextField(
                        value = editor.country,
                        onValueChange = { editor = editor.copy(country = it) },
                        label = { Text(stringResource(Res.string.profile_country)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(TactileTheme.RadiusSm),
                    )
                    OutlinedTextField(
                        value = editor.timezone,
                        onValueChange = { editor = editor.copy(timezone = it) },
                        label = { Text(stringResource(Res.string.profile_time_zone)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(TactileTheme.RadiusSm),
                    )
                    OutlinedTextField(
                        value = editor.occupation,
                        onValueChange = { editor = editor.copy(occupation = it) },
                        label = { Text(stringResource(Res.string.profile_occupation)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(TactileTheme.RadiusSm),
                    )
                    OutlinedTextField(
                        value = editor.website,
                        onValueChange = { editor = editor.copy(website = it) },
                        label = { Text(stringResource(Res.string.profile_website)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(TactileTheme.RadiusSm),
                    )
                }
            }
        }

        item {
            Surface(
                color = TactileTheme.Surface.copy(alpha = 0.6f),
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(TactileTheme.SpacingMd),
                    verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                ) {
                    OutlinedTextField(
                        value = editor.preferredGreeting,
                        onValueChange = { editor = editor.copy(preferredGreeting = it) },
                        label = { Text(stringResource(Res.string.profile_greeting)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(TactileTheme.RadiusSm),
                    )
                    ProfileDisplayNameFormatSelector(
                        selected = editor.displayNameFormat,
                        onSelected = { editor = editor.copy(displayNameFormat = it) },
                    )
                    OutlinedTextField(
                        value = editor.bio,
                        onValueChange = { editor = editor.copy(bio = it) },
                        label = { Text(stringResource(Res.string.profile_bio)) },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        shape = RoundedCornerShape(TactileTheme.RadiusSm),
                        singleLine = false,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text =
                                when {
                                    justSaved -> stringResource(Res.string.profile_saved)
                                    hasChanges -> stringResource(Res.string.profile_unsaved)
                                    else -> ""
                                },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (justSaved) TactileTheme.Primary else TactileTheme.Muted,
                        )
                        Button(
                            onClick = {
                                if (!emailValid) {
                                    showEmailError = true
                                    return@Button
                                }
                                viewModel.saveUserProfile(editor.toProfile(profile))
                                justSaved = true
                            },
                            enabled = hasChanges,
                            shape = RoundedCornerShape(TactileTheme.RadiusSm),
                            colors = ButtonDefaults.buttonColors(containerColor = TactileTheme.Primary),
                        ) {
                            Text(stringResource(Res.string.profile_save))
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(Res.string.profile_medications),
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Primary,
                )
                IconButton(onClick = { showAddMedDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = TactileTheme.Primary)
                }
            }
        }

        items(medications) { med ->
            MedicationItem(medication = med, onDelete = { viewModel.deleteMedication(med) })
        }
    }

    if (showAddMedDialog) {
        AddMedicationDialog(
            onDismiss = { showAddMedDialog = false },
            onSave = { substance, brands, dosage, hour, optional ->
                viewModel.addMedication(substance, brands, dosage, hour, optional)
                showAddMedDialog = false
            },
        )
    }
}

@Composable
private fun ProfileDisplayNameFormatSelector(
    selected: UserDisplayNameFormat,
    onSelected: (UserDisplayNameFormat) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            stringResource(Res.string.profile_display_format),
            style = MaterialTheme.typography.labelSmall,
            color = TactileTheme.Primary,
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
                contentColor = if (selected) TactileTheme.Primary else TactileTheme.Muted,
            ),
    ) {
        Text(label)
    }
}

/**
 * Displays a stylized medication row showing substance, optional brand names, dosage and scheduled
 * hour, with a delete action.
 */
@Composable
private fun MedicationItem(
    medication: MedicationEntity,
    onDelete: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface.copy(alpha = 0.5f),
        shape = RoundedCornerShape(TactileTheme.RadiusSm),
    ) {
        Row(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    medication.substance,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TactileTheme.Text,
                )
                if (medication.brandNames.isNotEmpty()) {
                    Text(
                        medication.brandNames,
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Muted,
                    )
                }
                Text(
                    "${medication.dosage ?: ""} ${if (medication.takeAtHour != null) "@ ${medication.takeAtHour}:00" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Primary,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TactileTheme.Error)
            }
        }
    }
}

/**
 * Displays a modal dialog that collects medication details and delivers them via callbacks.
 */
@Composable
private fun AddMedicationDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String?, Int?, Boolean) -> Unit,
) {
    var substance by remember { mutableStateOf("") }
    var brands by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var hour by remember { mutableStateOf("") }
    var isOptional by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.profile_add_med)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                OutlinedTextField(
                    value = substance,
                    onValueChange = { substance = it },
                    label = { Text(stringResource(Res.string.med_substance)) },
                )
                OutlinedTextField(
                    value = brands,
                    onValueChange = { brands = it },
                    label = { Text(stringResource(Res.string.med_brand_names)) },
                )
                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text(stringResource(Res.string.med_dosage)) },
                )
                OutlinedTextField(
                    value = hour,
                    onValueChange = { hour = it },
                    label = { Text(stringResource(Res.string.med_take_at)) },
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isOptional, onCheckedChange = { isOptional = it })
                    Text(stringResource(Res.string.med_is_optional))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        substance,
                        brands,
                        dosage.takeIf { it.isNotEmpty() },
                        hour.toIntOrNull(),
                        isOptional,
                    )
                },
            ) {
                Text(stringResource(Res.string.med_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.common_back))
            }
        },
    )
}

private data class ProfileEditorState(
    val firstName: String,
    val lastName: String,
    val nickname: String,
    val email: String,
    val avatarRef: String?,
    val bio: String,
    val phoneNumber: String,
    val birthDate: String,
    val city: String,
    val country: String,
    val timezone: String,
    val occupation: String,
    val website: String,
    val preferredGreeting: String,
    val displayNameFormat: UserDisplayNameFormat,
) {
    fun toProfile(source: UserProfile): UserProfile =
        source.copy(
            firstName = firstName,
            lastName = lastName,
            nickname = nickname,
            email = email,
            avatarRef = avatarRef,
            bio = bio,
            phoneNumber = phoneNumber,
            birthDate = birthDate,
            city = city,
            country = country,
            timezone = timezone,
            occupation = occupation,
            website = website,
            preferredGreeting = preferredGreeting,
            displayNameFormat = displayNameFormat,
        )

    fun initials(): String {
        val first = firstName.trim().firstOrNull()?.uppercaseChar()?.toString().orEmpty()
        val second = lastName.trim().firstOrNull()?.uppercaseChar()?.toString().orEmpty()
        return (first + second).ifBlank {
            nickname.trim().take(2).uppercase()
        }
    }

    fun completionPercent(): Int {
        val fields =
            listOf(
                firstName,
                lastName,
                nickname,
                email,
                bio,
                city,
                country,
                timezone,
                occupation,
            )
        val complete = fields.count { it.trim().isNotEmpty() }
        return ((complete / fields.size.toFloat()) * 100f).toInt()
    }

    companion object {
        fun from(profile: UserProfile): ProfileEditorState =
            ProfileEditorState(
                firstName = profile.firstName,
                lastName = profile.lastName,
                nickname = profile.nickname,
                email = profile.email,
                avatarRef = profile.avatarRef,
                bio = profile.bio,
                phoneNumber = profile.phoneNumber,
                birthDate = profile.birthDate,
                city = profile.city,
                country = profile.country,
                timezone = profile.timezone,
                occupation = profile.occupation,
                website = profile.website,
                preferredGreeting = profile.preferredGreeting,
                displayNameFormat = profile.displayNameFormat,
            )
    }
}

private fun UserProfile.withoutUpdatedAt(): UserProfile =
    copy(updatedAt = 0L)

private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
