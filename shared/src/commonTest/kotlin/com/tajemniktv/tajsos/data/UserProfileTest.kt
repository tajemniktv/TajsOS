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

    @Test
    fun testResolveDisplayName_DefaultProfile() {
        val profile = UserProfile()
        assertEquals("OPERATOR", profile.resolveDisplayName())
    }

    @Test
    fun testToUserProfile_mapsCorrectly() {
        val entity = UserEntity(
            id = 10L,
            firstName = "Alice",
            lastName = "Smith",
            nickname = "Ali",
            email = "alice@example.com",
            avatarRef = "avatar.png",
            bio = "Hello",
            phoneNumber = "123456",
            birthDate = "2000-01-01",
            city = "New York",
            country = "USA",
            timezone = "UTC",
            occupation = "Engineer",
            website = "example.com",
            preferredGreeting = "Hi",
            displayNameFormat = "FIRST_NAME",
            createdAt = 1000L,
            updatedAt = 2000L
        )
        val profile = entity.toUserProfile()

        assertEquals(10L, profile.id)
        assertEquals("Alice", profile.firstName)
        assertEquals("Smith", profile.lastName)
        assertEquals("Ali", profile.nickname)
        assertEquals("alice@example.com", profile.email)
        assertEquals("avatar.png", profile.avatarRef)
        assertEquals("Hello", profile.bio)
        assertEquals("123456", profile.phoneNumber)
        assertEquals("2000-01-01", profile.birthDate)
        assertEquals("New York", profile.city)
        assertEquals("USA", profile.country)
        assertEquals("UTC", profile.timezone)
        assertEquals("Engineer", profile.occupation)
        assertEquals("example.com", profile.website)
        assertEquals("Hi", profile.preferredGreeting)
        assertEquals(UserDisplayNameFormat.FIRST_NAME, profile.displayNameFormat)
        assertEquals(1000L, profile.createdAt)
        assertEquals(2000L, profile.updatedAt)
    }

    @Test
    fun testToUserProfile_handlesInvalidDisplayNameFormat() {
        val entity = UserEntity(
            id = 1L,
            displayNameFormat = "INVALID_FORMAT",
            createdAt = 1000L,
            updatedAt = 2000L
        )
        val profile = entity.toUserProfile()
        assertEquals(UserDisplayNameFormat.NICKNAME, profile.displayNameFormat)
    }

    @Test
    fun testToEntity_withoutExisting_mapsCorrectly() {
        val profile = UserProfile(
            id = 10L,
            firstName = "  Alice  ",
            lastName = " Smith ",
            nickname = " Ali ",
            email = " alice@example.com ",
            avatarRef = "avatar.png",
            bio = " Hello ",
            phoneNumber = " 123456 ",
            birthDate = " 2000-01-01 ",
            city = " New York ",
            country = " USA ",
            timezone = " UTC ",
            occupation = " Engineer ",
            website = " example.com ",
            preferredGreeting = " Hi ",
            displayNameFormat = UserDisplayNameFormat.FIRST_NAME,
            createdAt = 1000L,
            updatedAt = 2000L
        )
        val entity = profile.toEntity(existing = null)

        assertEquals(10L, entity.id)
        assertEquals("Alice", entity.firstName)
        assertEquals("Smith", entity.lastName)
        assertEquals("Ali", entity.nickname)
        assertEquals("alice@example.com", entity.email)
        assertEquals("avatar.png", entity.avatarRef)
        assertEquals("Hello", entity.bio)
        assertEquals("123456", entity.phoneNumber)
        assertEquals("2000-01-01", entity.birthDate)
        assertEquals("New York", entity.city)
        assertEquals("USA", entity.country)
        assertEquals("UTC", entity.timezone)
        assertEquals("Engineer", entity.occupation)
        assertEquals("example.com", entity.website)
        assertEquals("Hi", entity.preferredGreeting)
        assertEquals("FIRST_NAME", entity.displayNameFormat)
        assertEquals(1000L, entity.createdAt)
        // updatedAt should be updated
        kotlin.test.assertTrue(entity.updatedAt >= 2000L)
    }

    @Test
    fun testToEntity_withExisting_preservesMetadata() {
        val existing = UserEntity(
            id = 55L,
            createdAt = 500L,
            updatedAt = 600L
        )
        val profile = UserProfile(
            id = 10L, // should be ignored
            nickname = "New Nick",
            createdAt = 1000L, // should be ignored
            updatedAt = 2000L
        )
        val entity = profile.toEntity(existing = existing)

        assertEquals(55L, entity.id)
        assertEquals("New Nick", entity.nickname)
        assertEquals(500L, entity.createdAt)
        kotlin.test.assertTrue(entity.updatedAt >= 2000L)
    }

    @Test
    fun testToEntity_blankNicknameFallback() {
        val profile = UserProfile(
            id = 1L,
            nickname = "   ",
            createdAt = 1000L,
            updatedAt = 2000L
        )
        val entity = profile.toEntity(existing = null)
        assertEquals("OPERATOR", entity.nickname)
    }
}
