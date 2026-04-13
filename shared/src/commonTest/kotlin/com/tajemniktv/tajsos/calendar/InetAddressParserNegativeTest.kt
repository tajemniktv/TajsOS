package com.tajemniktv.tajsos.calendar

import kotlin.test.Test
import kotlin.test.assertNull

class InetAddressParserNegativeTest {
    @Test
    fun testInvalidIpv4Formats() {
        assertNull(parseIpAddress("1.1.1.01"), "Should reject octets with leading zeros")
        assertNull(parseIpAddress("1.1.1.-1"), "Should reject negative octets")
        assertNull(parseIpAddress("1.1.1.1.1"), "Should reject 5 octets")
        assertNull(parseIpAddress("1.1.1"), "Should reject 3 octets")
        assertNull(parseIpAddress("..."), "Should reject empty octets")
        assertNull(parseIpAddress("1..1.1"), "Should reject missing internal octets")
        assertNull(parseIpAddress("1.1.1.1a"), "Should reject trailing characters")
        assertNull(parseIpAddress("a1.1.1.1"), "Should reject leading characters")
    }

    @Test
    fun testInvalidIpv6Formats() {
        assertNull(parseIpAddress("::G"), "Should reject non-hex characters")
        assertNull(parseIpAddress("1::2::3"), "Should reject multiple double colons")
        assertNull(parseIpAddress(":::"), "Should reject triple colons")
        assertNull(parseIpAddress("1:::2"), "Should reject triple colons internally")
        assertNull(parseIpAddress("::1::"), "Should reject leading and trailing double colons")
        assertNull(parseIpAddress("1:2:3:4:5:6:7:8:9"), "Should reject too many segments")
        assertNull(parseIpAddress("1:2:3:4:5:6:7:"), "Should reject trailing single colon")
        assertNull(parseIpAddress(":1:2:3:4:5:6:7"), "Should reject leading single colon")
        assertNull(parseIpAddress("1:2:3:4:5:6:7:8::"), "Should reject double colon with full segments")
        assertNull(parseIpAddress("1:2:3:4:5:6:7:8:9:10::"), "Should reject double colon with too many segments")
        assertNull(parseIpAddress("12345::1"), "Should reject segments with more than 4 hex characters")
        assertNull(parseIpAddress("-1::1"), "Should reject negative values")
        assertNull(parseIpAddress("+1::1"), "Should reject explicit positive signs")
    }
}
