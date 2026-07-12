/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.profile

import com.tajemniktv.tajsos.ui.components.TactileOutlinedTextField
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.screen.ScreenScaffold
import com.tajemniktv.tajsos.ui.components.screen.ScreenScrollBehavior
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
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

/**
 * Profile feature route that owns state orchestration and delegates visual sections to block
 * renderers.
 *
 * @param viewModel The main ViewModel.
 * @param onNavigate Navigation callback.
 * @param modifier The modifier to be applied to the layout.
 * @param onPickAvatar Optional callback to request a platform avatar picker.
 * @param pickedAvatarRef Optional URI of a picked avatar.
 * @param onAvatarPickConsume Callback invoked when an avatar pick has been consumed.
 */
@Composable
fun ProfileRoute(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    onPickAvatar: (() -> Unit)? = null,
    pickedAvatarRef: String? = null,
    onAvatarPickConsume: () -> Unit = {},
) {
    val profile by viewModel.userProfile.collectAsState()
    val medications by viewModel.medications.collectAsState()

    var editor by remember(profile.updatedAt) { mutableStateOf(ProfileEditorState.from(profile)) }
    var showAddMedDialog by remember { mutableStateOf(value = false) }
    var showEmailError by remember { mutableStateOf(value = false) }
    var justSaved by remember { mutableStateOf(value = false) }

    val currentOnAvatarPickConsume by rememberUpdatedState(onAvatarPickConsume)

    LaunchedEffect(pickedAvatarRef) {
        if (!pickedAvatarRef.isNullOrBlank()) {
            editor = editor.copy(avatarRef = pickedAvatarRef)
            currentOnAvatarPickConsume()
        }
    }

    val hasChanges = editor.toProfile(profile).withoutUpdatedAt() != profile.withoutUpdatedAt()
    val emailValid = editor.email.isBlank() || EMAIL_REGEX.matches(editor.email.trim())
    val completion = editor.completionPercent()

    LaunchedEffect(hasChanges) {
        if (hasChanges) justSaved = false
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val surface =
            if (maxWidth > 920.dp) {
                ProfileDashboardSurface.DESKTOP
            } else {
                ProfileDashboardSurface.MOBILE
            }
        val plan = buildProfileDashboardPlan(surface)
        val blockSequence = plan.primary + plan.secondary
        val context =
            ProfileScreenContext(
                profile = profile,
                editor = editor,
                medications = medications,
                completion = completion,
                hasChanges = hasChanges,
                justSaved = justSaved,
                showEmailError = showEmailError,
                isEmailValid = emailValid,
                onEditorChange = { editor = it },
                onPickAvatar = onPickAvatar,
                onSaveProfile = {
                    viewModel.saveUserProfile(editor.toProfile(profile))
                    justSaved = true
                },
                onShowEmailErrorChange = { showEmailError = it },
                onOpenAddMedication = { showAddMedDialog = true },
            ) {
                viewModel.deleteMedication(it)
            }

        ProfileScreen(
            context = context,
            blockSequence = blockSequence,
            onNavigate = onNavigate,
        )
    }

    if (showAddMedDialog) {
        AddMedicationDialog(
            onDismiss = { showAddMedDialog = false },
        ) { sub, br, dos, hr, isOpt ->
            viewModel.addMedication(sub, br, dos, hr, isOpt)
            showAddMedDialog = false
        }
    }
}

/**
 * Stateless profile screen content.
 *
 * @param context Profile screen context.
 * @param blockSequence Sequence of blocks to render.
 * @param onNavigate Navigation callback.
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
fun ProfileScreen(
    context: ProfileScreenContext,
    blockSequence: List<ProfileDashboardBlock>,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenScaffold(
        screen = Screen.Profile,
        onNavigate = onNavigate,
        scrollBehavior = ScreenScrollBehavior.None,
        modifier = modifier,
        backgroundBrush =
            Brush.verticalGradient(
                colors =
                    listOf(
                        TajsOSTheme.Background,
                        TajsOSTheme.SurfaceLowest,
                        TajsOSTheme.Background,
                    ),
            ),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd),
        ) {
            items(blockSequence, key = { it.id }) {
                renderProfileBlock(it.id, context)
            }
        }
    }
}

/**
 * Dialog for adding a new medication.
 *
 * @param onDismiss Callback invoked when the dialog is dismissed.
 * @param modifier The modifier to be applied to the layout.
 * @param onSave Callback invoked when the medication is saved.
 */
@Composable
private fun AddMedicationDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onSave: (substance: String, brands: String, dosage: String?, hour: Int?, optional: Boolean) -> Unit,
) {
    var substance by remember { mutableStateOf("") }
    var brands by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var hour by remember { mutableStateOf("") }
    var isOptional by remember { mutableStateOf(value = false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = { Text(stringResource(Res.string.profile_add_med)) },
        text = {
            androidx.compose.foundation.layout.Column(
                verticalArrangement =
                    Arrangement.spacedBy(
                        TajsOSTheme.SpacingSm,
                    ),
            ) {
                TactileOutlinedTextField(
                    value = substance,
                    onValueChange = { substance = it },
                    label = { Text(stringResource(Res.string.med_substance)) },
                    shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
                )
                TactileOutlinedTextField(
                    value = brands,
                    onValueChange = { brands = it },
                    label = { Text(stringResource(Res.string.med_brand_names)) },
                    shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
                )
                TactileOutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text(stringResource(Res.string.med_dosage)) },
                    shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
                )
                TactileOutlinedTextField(
                    value = hour,
                    onValueChange = { hour = it },
                    label = { Text(stringResource(Res.string.med_take_at)) },
                    shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
                )
                androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
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
            TextButton(modifier = Modifier.pointerHoverIcon(PointerIcon.Hand), onClick = onDismiss) {
                Text(stringResource(Res.string.common_back))
            }
        },
    )
}
