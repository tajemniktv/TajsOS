/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.profile

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.data.MedicationEntity
import com.tajemniktv.tajsos.data.UserDisplayNameFormat
import com.tajemniktv.tajsos.data.UserProfile

/**
 * Surface variants for profile layouts.
 */
enum class ProfileDashboardSurface {
    MOBILE,
    DESKTOP,
}

/**
 * Block instance in a profile layout.
 */
data class ProfileDashboardBlock(
    val id: String,
)

/**
 * Render plan for profile layout.
 */
data class ProfileDashboardPlan(
    val primary: List<ProfileDashboardBlock>,
    val secondary: List<ProfileDashboardBlock> = emptyList(),
)

/**
 * Shared context passed to profile block renderers.
 */
data class ProfileScreenContext(
    val profile: UserProfile,
    val editor: ProfileEditorState,
    val medications: List<MedicationEntity>,
    val completion: Int,
    val hasChanges: Boolean,
    val justSaved: Boolean,
    val showEmailError: Boolean,
    val isEmailValid: Boolean,
    val onEditorChange: (ProfileEditorState) -> Unit,
    val onPickAvatar: (() -> Unit)?,
    val onSaveProfile: () -> Unit,
    val onShowEmailErrorChange: (Boolean) -> Unit,
    val onOpenAddMedication: () -> Unit,
    val onDeleteMedication: (MedicationEntity) -> Unit,
)

/**
 * Function signature for one profile block renderer.
 */
typealias ProfileDashboardBlockRenderer = @Composable (ProfileScreenContext) -> Unit

/**
 * Editable profile snapshot held by screen UI state.
 */
data class ProfileEditorState(
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
        return (first + second).ifBlank { nickname.trim().take(2).uppercase() }
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

internal fun UserProfile.withoutUpdatedAt(): UserProfile = copy(updatedAt = 0L)

internal val EMAIL_REGEX: Regex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")

