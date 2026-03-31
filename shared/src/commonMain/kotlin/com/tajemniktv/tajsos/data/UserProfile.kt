/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import kotlinx.serialization.Serializable
import kotlin.time.Clock

/**
 * Typed display-name formatting options for rendering personal identity across the app shell.
 */
@Serializable
enum class UserDisplayNameFormat {
    NICKNAME,
    FIRST_NAME,
    FIRST_LAST,
    FULL_NAME,
}

/**
 * Local-first personal identity model for the operator profile.
 *
 * This model is intentionally scoped to local profile data inside TajsOS and does not represent an
 * authentication account.
 */
@Serializable
data class UserProfile(
    val id: Long = 1L,
    val firstName: String = "",
    val lastName: String = "",
    val nickname: String = "OPERATOR",
    val email: String = "",
    val avatarRef: String? = null,
    val bio: String = "",
    val phoneNumber: String = "",
    /**
     * ISO date string `YYYY-MM-DD`.
     */
    val birthDate: String = "",
    val city: String = "",
    val country: String = "",
    val timezone: String = "",
    val occupation: String = "",
    val website: String = "",
    val preferredGreeting: String = "",
    val displayNameFormat: UserDisplayNameFormat = UserDisplayNameFormat.NICKNAME,
    val createdAt: Long =
        Clock.System
            .now()
            .toEpochMilliseconds(),
    val updatedAt: Long =
        Clock.System
            .now()
            .toEpochMilliseconds(),
)

/**
 * Resolves the operator's display name using their preferred formatting strategy.
 */
fun UserProfile.resolveDisplayName(): String {
    val first = firstName.trim()
    val last = lastName.trim()
    val nick = nickname.trim()
    val full = listOf(first, last).filter { it.isNotBlank() }.joinToString(" ").trim()

    return when (displayNameFormat)
    {
        UserDisplayNameFormat.NICKNAME -> nick.ifBlank { first.ifBlank { "OPERATOR" } }
        UserDisplayNameFormat.FIRST_NAME -> first.ifBlank { nick.ifBlank { "OPERATOR" } }
        UserDisplayNameFormat.FIRST_LAST -> full.ifBlank { nick.ifBlank { "OPERATOR" } }
        UserDisplayNameFormat.FULL_NAME -> full.ifBlank { nick.ifBlank { "OPERATOR" } }
    }
}

/**
 * Maps [UserEntity] storage into the typed [UserProfile] model.
 */
fun UserEntity.toUserProfile(): UserProfile =
    UserProfile(
        id = id,
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
        displayNameFormat =
            UserDisplayNameFormat.entries.firstOrNull { it.name == displayNameFormat }
                ?: UserDisplayNameFormat.NICKNAME,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

/**
 * Maps typed [UserProfile] data to persisted [UserEntity] state.
 *
 * @param existing Existing entity from storage used to preserve creation metadata when available.
 */
fun UserProfile.toEntity(existing: UserEntity? = null): UserEntity =
    UserEntity(
        id = existing?.id ?: id,
        firstName = firstName.trim(),
        lastName = lastName.trim(),
        nickname = nickname.trim().ifBlank { "OPERATOR" },
        email = email.trim(),
        avatarRef = avatarRef,
        bio = bio.trim(),
        phoneNumber = phoneNumber.trim(),
        birthDate = birthDate.trim(),
        city = city.trim(),
        country = country.trim(),
        timezone = timezone.trim(),
        occupation = occupation.trim(),
        website = website.trim(),
        preferredGreeting = preferredGreeting.trim(),
        displayNameFormat = displayNameFormat.name,
        createdAt = existing?.createdAt ?: createdAt,
        updatedAt = Clock.System.now().toEpochMilliseconds(),
    )
