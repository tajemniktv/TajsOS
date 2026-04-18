package com.tajemniktv.tajsos.calendar

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

class InetAddressParserAdditionalTest {

    @Test
    fun testBracketsAndWhitespace() {
        val ipv4 = parseIpAddress("  1.2.3.4  ")
        assertNotNull(ipv4, "ipv4 was null")
        assertTrue(ipv4 is IpAddress.Ipv4)

        val ipv6Brackets = parseIpAddress("[::1]")
        assertNotNull(ipv6Brackets, "ipv6Brackets was null")
        assertTrue(ipv6Brackets is IpAddress.Ipv6)

        val ipv6BracketsWhitespace = parseIpAddress("  [::1]  ")
        assertNotNull(ipv6BracketsWhitespace, "ipv6BracketsWhitespace was null")
        assertTrue(ipv6BracketsWhitespace is IpAddress.Ipv6)
    }
}
