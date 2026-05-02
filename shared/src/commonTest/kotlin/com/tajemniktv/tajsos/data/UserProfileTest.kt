package com.tajemniktv.tajsos.data

import kotlin.test.Test
import kotlin.test.assertEquals

class UserProfileTest {

    @Test
    fun testResolveDisplayName_NicknameFormat() {
        val profile = UserProfile(
            firstName = "John",
            lastName = "Doe",
            nickname = "Johnny",
            displayNameFormat = UserDisplayNameFormat.NICKNAME
        )
        assertEquals("Johnny", profile.resolveDisplayName())
    }

    @Test
    fun testResolveDisplayName_NicknameFallbackToFirstName() {
        val profile = UserProfile(
            firstName = "John",
            lastName = "Doe",
            nickname = "",
            displayNameFormat = UserDisplayNameFormat.NICKNAME
        )
        assertEquals("John", profile.resolveDisplayName())
    }

    @Test
    fun testResolveDisplayName_NicknameFallbackToOperator() {
        val profile = UserProfile(
            firstName = "",
            lastName = "Doe",
            nickname = "",
            displayNameFormat = UserDisplayNameFormat.NICKNAME
        )
        assertEquals("OPERATOR", profile.resolveDisplayName())
    }

    @Test
    fun testResolveDisplayName_FirstNameFormat() {
        val profile = UserProfile(
            firstName = "John",
            lastName = "Doe",
            nickname = "Johnny",
            displayNameFormat = UserDisplayNameFormat.FIRST_NAME
        )
        assertEquals("John", profile.resolveDisplayName())
    }

    @Test
    fun testResolveDisplayName_FirstNameFallbackToNickname() {
        val profile = UserProfile(
            firstName = "",
            lastName = "Doe",
            nickname = "Johnny",
            displayNameFormat = UserDisplayNameFormat.FIRST_NAME
        )
        assertEquals("Johnny", profile.resolveDisplayName())
    }

    @Test
    fun testResolveDisplayName_FirstNameFallbackToOperator() {
        val profile = UserProfile(
            firstName = "",
            lastName = "Doe",
            nickname = "",
            displayNameFormat = UserDisplayNameFormat.FIRST_NAME
        )
        assertEquals("OPERATOR", profile.resolveDisplayName())
    }

    @Test
    fun testResolveDisplayName_FirstLastFormat() {
        val profile = UserProfile(
            firstName = "John",
            lastName = "Doe",
            nickname = "Johnny",
            displayNameFormat = UserDisplayNameFormat.FIRST_LAST
        )
        assertEquals("John Doe", profile.resolveDisplayName())
    }

    @Test
    fun testResolveDisplayName_FirstLastFormat_MissingLastName() {
        val profile = UserProfile(
            firstName = "John",
            lastName = "",
            nickname = "Johnny",
            displayNameFormat = UserDisplayNameFormat.FIRST_LAST
        )
        assertEquals("John", profile.resolveDisplayName())
    }

    @Test
    fun testResolveDisplayName_FirstLastFormat_MissingFirstName() {
        val profile = UserProfile(
            firstName = "",
            lastName = "Doe",
            nickname = "Johnny",
            displayNameFormat = UserDisplayNameFormat.FIRST_LAST
        )
        assertEquals("Doe", profile.resolveDisplayName())
    }

    @Test
    fun testResolveDisplayName_FirstLastFallbackToNickname() {
        val profile = UserProfile(
            firstName = "",
            lastName = "",
            nickname = "Johnny",
            displayNameFormat = UserDisplayNameFormat.FIRST_LAST
        )
        assertEquals("Johnny", profile.resolveDisplayName())
    }

    @Test
    fun testResolveDisplayName_FirstLastFallbackToOperator() {
        val profile = UserProfile(
            firstName = "",
            lastName = "",
            nickname = "",
            displayNameFormat = UserDisplayNameFormat.FIRST_LAST
        )
        assertEquals("OPERATOR", profile.resolveDisplayName())
    }

    @Test
    fun testResolveDisplayName_FullNameFormat() {
        val profile = UserProfile(
            firstName = "John",
            lastName = "Doe",
            nickname = "Johnny",
            displayNameFormat = UserDisplayNameFormat.FULL_NAME
        )
        assertEquals("John Doe", profile.resolveDisplayName())
    }

    @Test
    fun testResolveDisplayName_FullNameFallbackToNickname() {
        val profile = UserProfile(
            firstName = "",
            lastName = "",
            nickname = "Johnny",
            displayNameFormat = UserDisplayNameFormat.FULL_NAME
        )
        assertEquals("Johnny", profile.resolveDisplayName())
    }

    @Test
    fun testResolveDisplayName_FullNameFallbackToOperator() {
        val profile = UserProfile(
            firstName = "",
            lastName = "",
            nickname = "",
            displayNameFormat = UserDisplayNameFormat.FULL_NAME
        )
        assertEquals("OPERATOR", profile.resolveDisplayName())
    }

    @Test
    fun testResolveDisplayName_TrimsWhitespace() {
        val profile = UserProfile(
            firstName = " John ",
            lastName = " Doe ",
            nickname = " Johnny ",
            displayNameFormat = UserDisplayNameFormat.FULL_NAME
        )
        assertEquals("John Doe", profile.resolveDisplayName())
    }
}
