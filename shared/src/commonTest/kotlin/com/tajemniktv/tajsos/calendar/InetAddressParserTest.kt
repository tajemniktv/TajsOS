package com.tajemniktv.tajsos.calendar

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class InetAddressParserTest {
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
}
