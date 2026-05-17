package com.tajemniktv.tajsos.calendar

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ParseIpAddressTest {

    @Test
    fun testParseIpAddressValid() {
        // Basic IPv4
        assertNotNull(parseIpAddress("192.168.1.1"))

        // Basic IPv6
        assertNotNull(parseIpAddress("2001:db8::1"))

        // Brackets around IPv6
        assertNotNull(parseIpAddress("[2001:db8::1]"))

        // Brackets around IPv4 (the implementation parses it as long as the contents are valid IPv4)
        assertNotNull(parseIpAddress("[192.168.1.1]"))
    }

    @Test
    fun testParseIpAddressWhitespaceAndBrackets() {
        // Whitespace only
        assertNotNull(parseIpAddress("  192.168.1.1  "))
        assertNotNull(parseIpAddress("  2001:db8::1  "))

        // Whitespace around brackets
        assertNotNull(parseIpAddress("  [2001:db8::1]  "))
        assertNotNull(parseIpAddress("\t[2001:db8::1]\n"))

        // Unbalanced brackets
        // Because of removePrefix("[") and removeSuffix("]"), unmatched brackets will just
        // be removed if they are at the ends.
        assertNotNull(parseIpAddress("[2001:db8::1"))
        assertNotNull(parseIpAddress("2001:db8::1]"))

        // If there are brackets INSIDE the string after trimming, parsing fails
        assertNull(parseIpAddress("2001:db8:[:1]"))
    }

    @Test
    fun testParseIpAddressInvalid() {
        // Empty and completely invalid
        assertNull(parseIpAddress(""))
        assertNull(parseIpAddress("   "))
        assertNull(parseIpAddress("not an ip"))

        // Multiple brackets (removePrefix/removeSuffix only removes one)
        assertNull(parseIpAddress("[[2001:db8::1]]"))

        // Brackets with spaces inside them
        // " [ 2001:db8::1 ] " -> trimmed to "[ 2001:db8::1 ]" -> "[ 2001:db8::1 ]"
        // Wait, removePrefix/Suffix removes them, but the inner spaces remain.
        // The implementation does a second `.trim()`:
        // host.trim().removePrefix("[").removeSuffix("]").trim()
        // So "[ 2001:db8::1 ]" -> " 2001:db8::1 " -> "2001:db8::1", which SHOULD parse successfully!
        assertNotNull(parseIpAddress(" [ 2001:db8::1 ] "))
    }
}
