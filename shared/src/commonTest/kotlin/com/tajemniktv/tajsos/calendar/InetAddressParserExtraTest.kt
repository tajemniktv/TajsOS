package com.tajemniktv.tajsos.calendar

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class InetAddressParserExtraTest {

    @Test
    fun testIpv4BoundaryValues() {
        assertNotNull(parseIpAddress("0.0.0.0"), "Should parse 0.0.0.0")
        assertNotNull(parseIpAddress("255.255.255.255"), "Should parse 255.255.255.255")
    }

    @Test
    fun testIpv4LeadingZerosRejected() {
        assertNull(parseIpAddress("192.168.01.1"), "Should reject leading zero in any segment unless it is exactly 0")
        assertNull(parseIpAddress("01.0.0.0"), "Should reject leading zero")
        assertNull(parseIpAddress("192.168.1.00"), "Should reject multiple zeros")
    }

    @Test
    fun testIpv6BoundaryValues() {
        assertNotNull(parseIpAddress("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff"), "Should parse max IPv6 values")
        assertNotNull(parseIpAddress("0:0:0:0:0:0:0:0"), "Should parse all zeros IPv6")
    }

    @Test
    fun testIpv6DoubleColonAtEdges() {
        assertNotNull(parseIpAddress("::1"), "Should handle double colon at the start")
        assertNotNull(parseIpAddress("1::"), "Should handle double colon at the end")
        assertNotNull(parseIpAddress("::"), "Should handle just double colon")
    }

    @Test
    fun testIpv6InvalidSegments() {
        assertNull(parseIpAddress("12345::1"), "Should reject segments with more than 4 hex digits")
        assertNull(parseIpAddress("1::g"), "Should reject invalid hex characters")
        assertNull(parseIpAddress("-1::1"), "Should reject negative signs")
        assertNull(parseIpAddress("+1::1"), "Should reject positive signs")
    }
}
