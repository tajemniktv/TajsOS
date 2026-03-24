package com.tajemniktv.tajsos.calendar

import com.tajemniktv.tajsos.data.CalendarProviderEntity
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class IcsCalendarProviderTest {

    private fun createProviderWithIcs(icsContent: String): IcsCalendarProvider {
        val mockEngine = MockEngine { request ->
            respond(
                content = icsContent,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "text/calendar")
            )
        }
        val client = HttpClient(mockEngine)
        return IcsCalendarProvider(client)
    }

    private val testProviderEntity = CalendarProviderEntity(
        id = 1,
        name = "Test Provider",
        type = "ICS",
        url = "https://example.com/calendar.ics"
    )

    // Some arbitrary large range for tests where we just want all events
    private val defaultFrom = Instant.fromEpochMilliseconds(0)
    private val defaultTo = Instant.fromEpochMilliseconds(4102444800000) // year 2100

    @Test
    fun `test basic event parsing`() = runTest {
        val ics = """
            BEGIN:VCALENDAR
            BEGIN:VEVENT
            UID:12345
            SUMMARY:Test Event
            DESCRIPTION:This is a test\nwith newlines
            LOCATION:Home
            DTSTART:20231024T100000Z
            DTEND:20231024T110000Z
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val provider = createProviderWithIcs(ics)
        val events = provider.fetchEvents(testProviderEntity, defaultFrom, defaultTo)

        assertEquals(1, events.size)
        val event = events[0]
        assertEquals("12345", event.externalId)
        assertEquals("Test Event", event.title)
        assertEquals("This is a test\nwith newlines", event.description)
        assertEquals("Home", event.location)
        assertEquals(Instant.parse("2023-10-24T10:00:00Z").toEpochMilliseconds(), event.startAt)
        assertEquals(Instant.parse("2023-10-24T11:00:00Z").toEpochMilliseconds(), event.endAt)
        assertEquals(false, event.isAllDay)
    }

    @Test
    fun `test all day event parsing`() = runTest {
        val ics = """
            BEGIN:VCALENDAR
            BEGIN:VEVENT
            UID:allday
            SUMMARY:All Day Event
            DTSTART;VALUE=DATE:20231025
            DTEND;VALUE=DATE:20231026
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val provider = createProviderWithIcs(ics)
        val events = provider.fetchEvents(testProviderEntity, defaultFrom, defaultTo)

        assertEquals(1, events.size)
        val event = events[0]
        assertEquals("All Day Event", event.title)
        assertTrue(event.isAllDay)
        assertEquals(Instant.parse("2023-10-25T00:00:00Z").toEpochMilliseconds(), event.startAt)
        assertEquals(Instant.parse("2023-10-26T00:00:00Z").toEpochMilliseconds(), event.endAt)
    }

    @Test
    fun `test event with folded lines and escaped chars`() = runTest {
        val ics = """
            BEGIN:VCALENDAR
            BEGIN:VEVENT
            UID:folded
            SUMMARY:Folded Event\, with comma
            DESCRIPTION:Line 1
              Line 2
              Line 3\\
            DTSTART:20231024T100000Z
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val provider = createProviderWithIcs(ics)
        val events = provider.fetchEvents(testProviderEntity, defaultFrom, defaultTo)

        assertEquals(1, events.size)
        val event = events[0]
        assertEquals("Folded Event, with comma", event.title)
        assertEquals("Line 1 Line 2 Line 3\\", event.description)
    }

    @Test
    fun `test filtering by date range`() = runTest {
        val ics = """
            BEGIN:VCALENDAR
            BEGIN:VEVENT
            UID:1
            SUMMARY:Early Event
            DTSTART:20230101T100000Z
            DTEND:20230101T110000Z
            END:VEVENT
            BEGIN:VEVENT
            UID:2
            SUMMARY:Late Event
            DTSTART:20240101T100000Z
            DTEND:20240101T110000Z
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val provider = createProviderWithIcs(ics)

        // Filter out Late Event
        val from = Instant.parse("2022-12-01T00:00:00Z")
        val to = Instant.parse("2023-12-31T00:00:00Z")

        val events = provider.fetchEvents(testProviderEntity, from, to)

        assertEquals(1, events.size)
        assertEquals("Early Event", events[0].title)
    }

    @Test
    fun `test fallback to system default when TZID is invalid`() = runTest {
        val ics = """
            BEGIN:VCALENDAR
            BEGIN:VEVENT
            UID:tz
            SUMMARY:TZ Event
            DTSTART;TZID=Invalid/Timezone:20231024T100000
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val provider = createProviderWithIcs(ics)
        val events = provider.fetchEvents(testProviderEntity, defaultFrom, defaultTo)

        assertEquals(1, events.size)
        val event = events[0]
        assertEquals("TZ Event", event.title)
        // Ensure it doesn't crash and actually parses something
        assertNotNull(event.startAt)
    }
}
