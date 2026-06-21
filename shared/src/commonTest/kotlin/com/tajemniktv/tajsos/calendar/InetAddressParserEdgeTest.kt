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
        assertEquals(true, requireNotNull(parseIpAddress("10.0.0.0")).isSiteLocal())
        assertEquals(true, requireNotNull(parseIpAddress("10.255.255.255")).isSiteLocal())

        // 172.16.x.x - 172.31.x.x boundaries
        assertEquals(true, requireNotNull(parseIpAddress("172.16.0.0")).isSiteLocal())
        assertEquals(true, requireNotNull(parseIpAddress("172.31.255.255")).isSiteLocal())
        assertEquals(false, requireNotNull(parseIpAddress("172.15.255.255")).isSiteLocal())
        assertEquals(false, requireNotNull(parseIpAddress("172.32.0.0")).isSiteLocal())

        // 192.168.x.x boundary
        assertEquals(true, requireNotNull(parseIpAddress("192.168.0.0")).isSiteLocal())
        assertEquals(true, requireNotNull(parseIpAddress("192.168.255.255")).isSiteLocal())
        assertEquals(false, requireNotNull(parseIpAddress("192.167.255.255")).isSiteLocal())
    }

    @Test
    fun testIpv6SiteLocalEdges() {
        // fc00::/7 (fc00:: to fdff:ffff:ffff:ffff:ffff:ffff:ffff:ffff)
        assertEquals(true, requireNotNull(parseIpAddress("fc00::")).isSiteLocal())
        assertEquals(true, requireNotNull(parseIpAddress("fdff:ffff:ffff:ffff:ffff:ffff:ffff:ffff")).isSiteLocal())

        // Edge out of bounds
        assertEquals(false, requireNotNull(parseIpAddress("fbff:ffff:ffff:ffff:ffff:ffff:ffff:ffff")).isSiteLocal())
        assertEquals(false, requireNotNull(parseIpAddress("fe00::")).isSiteLocal())
    }

    @Test
    fun testIpv6LinkLocalEdges() {
        // fe80::/10 (fe80:: to febf:ffff:ffff:ffff:ffff:ffff:ffff:ffff)
        assertEquals(true, requireNotNull(parseIpAddress("fe80::")).isLinkLocal())
        assertEquals(true, requireNotNull(parseIpAddress("febf:ffff:ffff:ffff:ffff:ffff:ffff:ffff")).isLinkLocal())

        // Out of bounds
        assertEquals(false, requireNotNull(parseIpAddress("fe7f:ffff:ffff:ffff:ffff:ffff:ffff:ffff")).isLinkLocal())
        assertEquals(false, requireNotNull(parseIpAddress("fec0::")).isLinkLocal())
    }

    @Test
    fun testIpv6Equality() {
        val ipv6A = parseIpAddress("2001:db8::1")
        val ipv6B = parseIpAddress("2001:db8:0:0:0:0:0:1")
        assertEquals(ipv6A, ipv6B)
        assertEquals(requireNotNull(ipv6A).hashCode(), requireNotNull(ipv6B).hashCode())
    }
}
