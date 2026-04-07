package com.tajemniktv.tajsos.calendar

import kotlin.test.Test
import kotlin.test.assertNull

class InetAddressParserCrashTest {
    @Test
    fun testIpv6OutOfBounds() {
        assertNull(parseIpAddress("1:2:3:4:5:6:7:8:9:10::"))
    }
}
