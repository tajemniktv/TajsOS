package com.tajemniktv.tajsos.domain

import org.junit.Test
import kotlin.test.assertEquals

class DomainKindTest {
    @Test
    fun testDisplayName() {
        assertEquals("Finances", DomainKind.FINANCES.displayName)
        assertEquals("Health", DomainKind.HEALTH.displayName)
        assertEquals("Education", DomainKind.EDUCATION.displayName)
        assertEquals("Relationships", DomainKind.RELATIONSHIPS.displayName)
    }
}
