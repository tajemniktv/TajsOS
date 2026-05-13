package com.tajemniktv.tajsos

import kotlin.test.Test
import kotlin.test.assertTrue

class GreetingTest {
    @Test
    fun testGreeting() {
        val greeting = Greeting().greet()
        assertTrue(greeting.startsWith("Hello, "), "Greeting should start with 'Hello, '")
        assertTrue(greeting.endsWith("!"), "Greeting should end with '!'")
        assertTrue(greeting.length > 8, "Greeting should contain a platform name")
    }
}
