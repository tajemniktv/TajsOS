package com.tajemniktv.tajsos.calendar

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class InetAddressParserTest {
    @Test
    fun testBracketsAndWhitespace() {
        val ipv4Clean = parseIpAddress("1.2.3.4")
        val ipv4Dirty = parseIpAddress("  1.2.3.4  ")
        assertNotNull(ipv4Dirty, "ipv4Dirty was null")
        kotlin.test.assertEquals(ipv4Clean, ipv4Dirty)

        val ipv6Clean = parseIpAddress("::1")
        val ipv6Brackets = parseIpAddress("[::1]")
        assertNotNull(ipv6Brackets, "ipv6Brackets was null")
        kotlin.test.assertEquals(ipv6Clean, ipv6Brackets)

        val ipv6BracketsWhitespace = parseIpAddress("  [::1]  ")
        assertNotNull(ipv6BracketsWhitespace, "ipv6BracketsWhitespace was null")
        kotlin.test.assertEquals(ipv6Clean, ipv6BracketsWhitespace)
    }

    @Test
    fun testIpv4() {
        val loopback = parseIpAddress("127.0.0.1")
        assertNotNull(loopback)
        assertTrue(loopback.isLoopback())

        val privateA = parseIpAddress("10.1.2.3")
        assertNotNull(privateA)
        assertTrue(privateA.isSiteLocal())

        val privateB = parseIpAddress("172.16.0.1")
        assertNotNull(privateB)
        assertTrue(privateB.isSiteLocal())

        val privateC = parseIpAddress("192.168.0.1")
        assertNotNull(privateC)
        assertTrue(privateC.isSiteLocal())

        val linkLocal = parseIpAddress("169.254.1.2")
        assertNotNull(linkLocal)
        assertTrue(linkLocal.isLinkLocal())

        val publicIp = parseIpAddress("8.8.8.8")
        assertNotNull(publicIp)
        assertFalse(publicIp.isPrivateOrLocal())
    }

    @Test
    fun testIpv6() {
        val loopback = parseIpAddress("::1")
        assertNotNull(loopback)
        assertTrue(loopback.isLoopback())

        val loopbackFull = parseIpAddress("0:0:0:0:0:0:0:1")
        assertNotNull(loopbackFull)
        assertTrue(loopbackFull.isLoopback())

        val siteLocal = parseIpAddress("fd12::2")
        assertNotNull(siteLocal)
        assertTrue(siteLocal.isSiteLocal())

        val publicIpv6 = parseIpAddress("2606:4700:4700::1111")
        assertNotNull(publicIpv6)
        assertFalse(publicIpv6.isPrivateOrLocal())
    }

    @Test
    fun testInvalidIpv4() {
        assertNull(parseIpAddress("256.0.0.1"))
        assertNull(parseIpAddress("1.2.3"))
        assertNull(parseIpAddress("1.2.3.4.5"))
        assertNull(parseIpAddress("not.an.ip"))
        assertNull(parseIpAddress(""))
    }

    @Test
    fun testInvalidIpv6() {
        assertNull(parseIpAddress("g::1"))
        assertNull(parseIpAddress("1ffff::1"))
        assertNull(parseIpAddress("not:an:ipv6:at:all"))
        assertNull(parseIpAddress("1:2:3:4:5:6:7:8:9:10::"))
    }

    @Test
    fun testInvalidIpv6MultipleDoubleColons() {
        assertNull(parseIpAddress("1::2::3"), "Should reject multiple double colons")
    }

    @Test
    fun testInvalidIpv6TripleColons() {
        assertNull(parseIpAddress(":::"), "Should reject triple colons or more")
        assertNull(parseIpAddress("1:::2"), "Should reject triple colons or more")
    }

    @Test
    fun testIsPrivateOrLocal() {
        val loopbackV4 = parseIpAddress("127.0.0.1")
        assertNotNull(loopbackV4)
        assertTrue(loopbackV4.isPrivateOrLocal())

        val loopbackV6 = parseIpAddress("::1")
        assertNotNull(loopbackV6)
        assertTrue(loopbackV6.isPrivateOrLocal())

        val linkLocalV4 = parseIpAddress("169.254.1.2")
        assertNotNull(linkLocalV4)
        assertTrue(linkLocalV4.isPrivateOrLocal())

        val linkLocalV6 = parseIpAddress("fe80::1")
        assertNotNull(linkLocalV6)
        assertTrue(linkLocalV6.isPrivateOrLocal())

        val siteLocalV4_10 = parseIpAddress("10.0.0.1")
        assertNotNull(siteLocalV4_10)
        assertTrue(siteLocalV4_10.isPrivateOrLocal())

        val siteLocalV4_172 = parseIpAddress("172.16.0.1")
        assertNotNull(siteLocalV4_172)
        assertTrue(siteLocalV4_172.isPrivateOrLocal())

        val siteLocalV4_192 = parseIpAddress("192.168.1.1")
        assertNotNull(siteLocalV4_192)
        assertTrue(siteLocalV4_192.isPrivateOrLocal())

        val siteLocalV6 = parseIpAddress("fd00::1")
        assertNotNull(siteLocalV6)
        assertTrue(siteLocalV6.isPrivateOrLocal())

        val publicV4 = parseIpAddress("8.8.8.8")
        assertNotNull(publicV4)
        assertFalse(publicV4.isPrivateOrLocal())

        val publicV6 = parseIpAddress("2001:4860:4860::8888")
        assertNotNull(publicV6)
        assertFalse(publicV6.isPrivateOrLocal())
    }

    @Test
    fun testInvalidIpv6EdgeCases() {
        assertNull(parseIpAddress(":1:2:3:4:5:6:7"), "Should reject starting with single colon")
        assertNull(parseIpAddress("1:2:3:4:5:6:7:"), "Should reject ending with single colon")
    }
}
