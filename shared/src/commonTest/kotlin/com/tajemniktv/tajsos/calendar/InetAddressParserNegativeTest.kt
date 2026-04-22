package com.tajemniktv.tajsos.calendar

import kotlin.test.Test
import kotlin.test.assertNull

class InetAddressParserNegativeTest {
    @Test
    fun testInvalidIpv4Formats() {
        assertNull(parseIpAddress("1.1.1.01"), "Should reject octets with leading zeros")
        assertNull(parseIpAddress("1.1.1.-1"), "Should reject negative octets")
        assertNull(parseIpAddress("999999999999999999999999.0.0.1"), "Should reject out of bounds long")
        assertNull(parseIpAddress("256.1.1.1"), "Should reject out of bounds positive byte")
        assertNull(parseIpAddress("1.1.1.x"), "Should reject non-digit character")
        assertNull(parseIpAddress("1.1.1. 1"), "Should reject space in octet")
        // assertNull(parseIpAddress("1.1.1.1 "), "Should reject trailing space after trim") // handled by trim()
        assertNull(parseIpAddress("1.1.1."), "Should reject trailing dot")
        assertNull(parseIpAddress(".1.1.1"), "Should reject leading dot")
        assertNull(parseIpAddress("1.1.1.1.1"), "Should reject 5 octets")
        assertNull(parseIpAddress("1.1.1.00"), "Should reject octet with two leading zeros")
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
        assertNull(parseIpAddress("+1::1"), "Should reject positive values with plus sign")
        assertNull(parseIpAddress("::1:2:3:4:5:6:7:8:9:10"), "Should reject too many segments on the right")
        assertNull(parseIpAddress("1:2:3:4:5:6:7:8:9::"), "Should reject too many segments on the left")
        assertNull(parseIpAddress("1::2:3:4:5:6:7:8:9"), "Should reject too many total segments in abbreviated form")
        assertNull(parseIpAddress("1:2:3:4:5:6:7:8:9:10"), "Should reject too many segments in full form")
        assertNull(parseIpAddress("1:2:3:4:5:6:7::9"), "Should reject out of bounds index during abbreviated form")
        assertNull(parseIpAddress("1::2:3:4:5:6:7:8"), "Should reject out of bounds index right side")
        assertNull(parseIpAddress("10000::1"), "Should reject too large value")
        assertNull(parseIpAddress("1:2:3:4:5:6:7:x"), "Should reject non-hex character in full form")
        assertNull(parseIpAddress("::x"), "Should reject non-hex character in abbreviated form")
        assertNull(parseIpAddress("::12345"), "Should reject segment with length > 4")
        assertNull(parseIpAddress("::1ffff"), "Should reject value > 0xFFFF")
        assertNull(parseIpAddress("1:2:3:4:5:6:7:8::"), "Should skip empty part in middle")
        assertNull(parseIpAddress("1:2:3:4:5:6:7:8:9:10:11"), "Should reject too many parts full")
        assertNull(parseIpAddress("12345::"), "Should reject segment with length > 4 on left")
        assertNull(parseIpAddress("1:2:3:4:5:6:7:8::1"), "Should reject left parsed out of bounds")
        assertNull(parseIpAddress("1::2:3:4:5:6:7:8:9"), "Should reject right parsed out of bounds")
    }
}
