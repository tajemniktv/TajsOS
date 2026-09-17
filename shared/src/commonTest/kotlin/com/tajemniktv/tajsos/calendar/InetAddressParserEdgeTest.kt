package com.tajemniktv.tajsos.calendar

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertEquals

class InetAddressParserEdgeTest {

    /** Protects network address classification at both ends of restricted IPv4 ranges. */
    @Test
    fun testIpv4LoopbackAndLinkLocalBoundaries() {
        for (host in listOf("127.0.0.0", "127.255.255.255")) {
            assertTrue(assertNotNull(parseIpAddress(host)).isLoopback(), host)
        }
        for (host in listOf("126.255.255.255", "128.0.0.0")) {
            assertFalse(assertNotNull(parseIpAddress(host)).isLoopback(), host)
        }
        for (host in listOf("169.254.0.0", "169.254.255.255")) {
            assertTrue(assertNotNull(parseIpAddress(host)).isLinkLocal(), host)
        }
        for (host in listOf("169.253.255.255", "169.255.0.0")) {
            assertFalse(assertNotNull(parseIpAddress(host)).isLinkLocal(), host)
        }
    }

    @Test
    fun testIpv6LoopbackBoundaries() {
        for (host in listOf("::1", "0:0:0:0:0:0:0:1")) {
            assertTrue(assertNotNull(parseIpAddress(host)).isLoopback(), host)
        }
        for (host in listOf("::", "::2", "1::1")) {
            assertFalse(assertNotNull(parseIpAddress(host)).isLoopback(), host)
        }
    }

    @Test
    fun testIpv4SiteLocalEdges() {
        // 10.x.x.x boundary
        assertTrue(parseIpAddress("10.0.0.0")!!.isSiteLocal())
        assertTrue(parseIpAddress("10.255.255.255")!!.isSiteLocal())
        assertFalse(assertNotNull(parseIpAddress("9.255.255.255")).isSiteLocal())
        assertFalse(assertNotNull(parseIpAddress("11.0.0.0")).isSiteLocal())

        // 172.16.x.x - 172.31.x.x boundaries
        assertTrue(parseIpAddress("172.16.0.0")!!.isSiteLocal())
        assertTrue(parseIpAddress("172.31.255.255")!!.isSiteLocal())
        assertFalse(parseIpAddress("172.15.255.255")!!.isSiteLocal())
        assertFalse(parseIpAddress("172.32.0.0")!!.isSiteLocal())

        // 192.168.x.x boundary
        assertTrue(parseIpAddress("192.168.0.0")!!.isSiteLocal())
        assertTrue(parseIpAddress("192.168.255.255")!!.isSiteLocal())
        assertFalse(parseIpAddress("192.167.255.255")!!.isSiteLocal())
        assertFalse(assertNotNull(parseIpAddress("192.169.0.0")).isSiteLocal())
    }

    @Test
    fun testIpv6SiteLocalEdges() {
        // fc00::/7 (fc00:: to fdff:ffff:ffff:ffff:ffff:ffff:ffff:ffff)
        assertTrue(parseIpAddress("fc00::")!!.isSiteLocal())
        assertTrue(parseIpAddress("fdff:ffff:ffff:ffff:ffff:ffff:ffff:ffff")!!.isSiteLocal())

        // Edge out of bounds
        assertFalse(parseIpAddress("fbff:ffff:ffff:ffff:ffff:ffff:ffff:ffff")!!.isSiteLocal())
        assertFalse(parseIpAddress("fe00::")!!.isSiteLocal())
    }

    @Test
    fun testIpv6LinkLocalEdges() {
        // fe80::/10 (fe80:: to febf:ffff:ffff:ffff:ffff:ffff:ffff:ffff)
        assertTrue(parseIpAddress("fe80::")!!.isLinkLocal())
        assertTrue(parseIpAddress("febf:ffff:ffff:ffff:ffff:ffff:ffff:ffff")!!.isLinkLocal())

        // Out of bounds
        assertFalse(parseIpAddress("fe7f:ffff:ffff:ffff:ffff:ffff:ffff:ffff")!!.isLinkLocal())
        assertFalse(parseIpAddress("fec0::")!!.isLinkLocal())
    }

    @Test
    fun testIpv6Equality() {
        val ipv6A = parseIpAddress("2001:db8::1")
        val ipv6B = parseIpAddress("2001:db8:0:0:0:0:0:1")
        assertEquals(ipv6A, ipv6B)
        assertEquals(ipv6A!!.hashCode(), ipv6B!!.hashCode())
    }
}
