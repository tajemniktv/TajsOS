package com.tajemniktv.tajsos.calendar

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertEquals

class InetAddressParserEdgeTest {

    @Test
    fun testIpv4SiteLocalEdges() {
        // 10.x.x.x boundary
        assertTrue(requireNotNull(parseIpAddress("10.0.0.0")).isSiteLocal())
        assertTrue(requireNotNull(parseIpAddress("10.255.255.255")).isSiteLocal())

        // 172.16.x.x - 172.31.x.x boundaries
        assertTrue(requireNotNull(parseIpAddress("172.16.0.0")).isSiteLocal())
        assertTrue(requireNotNull(parseIpAddress("172.31.255.255")).isSiteLocal())
        assertFalse(requireNotNull(parseIpAddress("172.15.255.255")).isSiteLocal())
        assertFalse(requireNotNull(parseIpAddress("172.32.0.0")).isSiteLocal())

        // 192.168.x.x boundary
        assertTrue(requireNotNull(parseIpAddress("192.168.0.0")).isSiteLocal())
        assertTrue(requireNotNull(parseIpAddress("192.168.255.255")).isSiteLocal())
        assertFalse(requireNotNull(parseIpAddress("192.167.255.255")).isSiteLocal())
    }

    @Test
    fun testIpv6SiteLocalEdges() {
        // fc00::/7 (fc00:: to fdff:ffff:ffff:ffff:ffff:ffff:ffff:ffff)
        assertTrue(requireNotNull(parseIpAddress("fc00::")).isSiteLocal())
        assertTrue(requireNotNull(parseIpAddress("fdff:ffff:ffff:ffff:ffff:ffff:ffff:ffff")).isSiteLocal())

        // Edge out of bounds
        assertFalse(requireNotNull(parseIpAddress("fbff:ffff:ffff:ffff:ffff:ffff:ffff:ffff")).isSiteLocal())
        assertFalse(requireNotNull(parseIpAddress("fe00::")).isSiteLocal())
    }

    @Test
    fun testIpv6LinkLocalEdges() {
        // fe80::/10 (fe80:: to febf:ffff:ffff:ffff:ffff:ffff:ffff:ffff)
        assertTrue(requireNotNull(parseIpAddress("fe80::")).isLinkLocal())
        assertTrue(requireNotNull(parseIpAddress("febf:ffff:ffff:ffff:ffff:ffff:ffff:ffff")).isLinkLocal())

        // Out of bounds
        assertFalse(requireNotNull(parseIpAddress("fe7f:ffff:ffff:ffff:ffff:ffff:ffff:ffff")).isLinkLocal())
        assertFalse(requireNotNull(parseIpAddress("fec0::")).isLinkLocal())
    }

    @Test
    fun testIpv6Equality() {
        val ipv6A = parseIpAddress("2001:db8::1")
        val ipv6B = parseIpAddress("2001:db8:0:0:0:0:0:1")
        assertEquals(ipv6A, ipv6B)
        assertEquals(requireNotNull(ipv6A).hashCode(), requireNotNull(ipv6B).hashCode())
    }
}
