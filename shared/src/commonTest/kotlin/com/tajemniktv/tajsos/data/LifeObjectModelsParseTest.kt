package com.tajemniktv.tajsos.data

import kotlin.test.Test
import kotlin.test.assertEquals

class LifeObjectModelsParseTest {

    @Test
    fun parseCapturedText_emptyOrBlank() {
        assertEquals(ParsedCaptureText("", ""), parseCapturedText(""))
        assertEquals(ParsedCaptureText("", ""), parseCapturedText("   "))
        assertEquals(ParsedCaptureText("", ""), parseCapturedText("\n\n\t"))
    }

    @Test
    fun parseCapturedText_singleLine() {
        assertEquals(ParsedCaptureText("Hello world", ""), parseCapturedText("Hello world"))
        assertEquals(ParsedCaptureText("Trim me", ""), parseCapturedText("  Trim me  "))
    }

    @Test
    fun parseCapturedText_multiLine() {
        val raw = "Title goes here\n\nBody line 1\nBody line 2\n"
        val parsed = parseCapturedText(raw)
        assertEquals("Title goes here", parsed.title)
        assertEquals("Body line 1\nBody line 2", parsed.content)
    }

    @Test
    fun parseCapturedText_withLeadingBlankLines() {
        val raw = "\n\n  \nFirst real line\nSecond line"
        val parsed = parseCapturedText(raw)
        assertEquals("First real line", parsed.title)
        assertEquals("Second line", parsed.content)
    }
}
