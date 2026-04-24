package com.tajemniktv.tajsos.calendar

import com.tajemniktv.tajsos.data.*
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class CalendarManagerTest {
    private val fakeNodeDao = FakeNodeDao()
    private val fakeFocusSessionDao = FakeFocusSessionDao()
    private val fakeTrackDao = FakeTrackDao()
    private val fakeRelationDao = FakeRelationDao()
    private val fakeTagDao = FakeTagDao()
    private val fakeEventLogDao = FakeEventLogDao()
    private val fakeAttachmentDao = FakeAttachmentDao()
    private val fakeTemplateDao = FakeTemplateDao()
    private val fakeNodeSnapshotDao = FakeNodeSnapshotDao()
    private val fakeReviewDao = FakeReviewDao()
    private val fakeCalendarProviderDao = object : CalendarProviderDao {
        val providers = mutableListOf<CalendarProviderEntity>()
        override fun getAllProviders() = kotlinx.coroutines.flow.MutableStateFlow(providers)
        override suspend fun insertProvider(provider: CalendarProviderEntity): Long {
            val id = (providers.maxOfOrNull { it.id } ?: 0L) + 1L
            providers.add(provider.copy(id = id))
            return id
        }
        override suspend fun updateProvider(provider: CalendarProviderEntity) {
            val idx = providers.indexOfFirst { it.id == provider.id }
            if (idx != -1) providers[idx] = provider
        }
        override suspend fun deleteProvider(provider: CalendarProviderEntity) {
            providers.removeAll { it.id == provider.id }
        }
        override suspend fun getProviderById(id: Long) = providers.find { it.id == id }
    }
    private val fakeCalendarEventDao = object : CalendarEventDao {
        val events = mutableListOf<CalendarEventEntity>()
        override fun getEventsInRange(from: Long, to: Long) = kotlinx.coroutines.flow.MutableStateFlow(
            events.filter { it.startAt >= from && it.startAt <= to }
        )
        override suspend fun insertEvents(events: List<CalendarEventEntity>) {
            this.events.addAll(events)
        }
        override suspend fun deleteEventsByProvider(providerId: Long) {
            events.removeAll { it.providerId == providerId }
        }
        override suspend fun insertEvent(event: CalendarEventEntity): Long {
            val id = (events.maxOfOrNull { it.id } ?: 0L) + 1L
            events.add(event.copy(id = id))
            return id
        }
        override suspend fun updateEvent(event: CalendarEventEntity) {
            val idx = events.indexOfFirst { it.id == event.id }
            if (idx != -1) events[idx] = event
        }
        override suspend fun deleteEvent(event: CalendarEventEntity) {
            events.removeAll { it.id == event.id }
        }
    }
    private val fakeModeDao = FakeModeDao()
    private val fakeProtocolDao = FakeProtocolDao()
    private val fakeDecisionDao = FakeDecisionDao()
    private val fakeUserDao = FakeUserDao()
    private val fakeMedicationDao = FakeMedicationDao()

    private val repository = AppRepository(
        nodeDao = fakeNodeDao,
        focusSessionDao = fakeFocusSessionDao,
        trackDao = fakeTrackDao,
        relationDao = fakeRelationDao,
        tagDao = fakeTagDao,
        eventLogDao = fakeEventLogDao,
        attachmentDao = fakeAttachmentDao,
        templateDao = fakeTemplateDao,
        nodeSnapshotDao = fakeNodeSnapshotDao,
        reviewDao = fakeReviewDao,
        calendarProviderDao = fakeCalendarProviderDao,
        calendarEventDao = fakeCalendarEventDao,
        modeDao = fakeModeDao,
        protocolDao = fakeProtocolDao,
        decisionDao = fakeDecisionDao,
        userDao = fakeUserDao,
        medicationDao = fakeMedicationDao
    )

    @Test
    fun syncAll_withEnabledIcsProvider_fetchesAndSavesEvents() = runTest {
        fakeCalendarProviderDao.providers.add(
            CalendarProviderEntity(
                id = 1,
                name = "Test ICS",
                type = "ICS",
                url = "https://example.com/calendar.ics",
                isEnabled = true,
                color = 0xFF0000
            )
        )

        // Output an event for every month in 2020 to 2030 to guarantee it falls in the +/- window
        var icsContent = "BEGIN:VCALENDAR\nVERSION:2.0\n"
        for (year in 2020..2030) {
            for (month in 1..12) {
                val m = month.toString().padStart(2, '0')
                icsContent += "BEGIN:VEVENT\n"
                icsContent += "UID:event_${year}_${month}@example.com\n"
                icsContent += "SUMMARY:Test Event ${year}_${month}\n"
                icsContent += "DTSTART:${year}${m}10T100000Z\n"
                icsContent += "DTEND:${year}${m}10T110000Z\n"
                icsContent += "END:VEVENT\n"
            }
        }
        icsContent += "END:VCALENDAR"

        val mockEngine = MockEngine { request ->
            respond(
                content = icsContent,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "text/calendar")
            )
        }
        val httpClient = HttpClient(mockEngine)

        val manager = CalendarManager(repository, httpClient)
        manager.syncAll()

        val events = fakeCalendarEventDao.events
        assertTrue(events.isNotEmpty(), "Should have saved at least one event that fell into the time window")

        val provider = fakeCalendarProviderDao.providers.first()
        assertNotNull(provider.lastSyncedAt)
    }

    @Test
    fun syncAll_deduplicatesEventsByExternalId() = runTest {
        fakeCalendarProviderDao.providers.add(
            CalendarProviderEntity(
                id = 1,
                name = "Test ICS Duplicate",
                type = "ICS",
                url = "https://example.com/calendar.ics",
                isEnabled = true,
                color = 0xFF0000
            )
        )

        var icsContent = "BEGIN:VCALENDAR\nVERSION:2.0\n"
        for (year in 2020..2030) {
            for (month in 1..12) {
                val m = month.toString().padStart(2, '0')
                // Two events with the SAME UID
                icsContent += "BEGIN:VEVENT\n"
                icsContent += "UID:duplicate_${year}_${month}@example.com\n"
                icsContent += "SUMMARY:First Duplicate\n"
                icsContent += "DTSTART:${year}${m}10T100000Z\n"
                icsContent += "DTEND:${year}${m}10T110000Z\n"
                icsContent += "END:VEVENT\n"

                icsContent += "BEGIN:VEVENT\n"
                icsContent += "UID:duplicate_${year}_${month}@example.com\n"
                icsContent += "SUMMARY:Second Duplicate\n"
                icsContent += "DTSTART:${year}${m}10T100000Z\n"
                icsContent += "DTEND:${year}${m}10T110000Z\n"
                icsContent += "END:VEVENT\n"
            }
        }
        icsContent += "END:VCALENDAR"

        val mockEngine = MockEngine { request ->
            respond(
                content = icsContent,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "text/calendar")
            )
        }
        val httpClient = HttpClient(mockEngine)

        val manager = CalendarManager(repository, httpClient)
        manager.syncAll()

        val events = fakeCalendarEventDao.events
        assertTrue(events.isNotEmpty(), "Should have saved at least one event")

        // Group by external ID, every group should have size 1
        val duplicates = events.groupBy { it.externalId }.filter { it.value.size > 1 }
        assertTrue(duplicates.isEmpty(), "Should deduplicate events to a single event per externalId")
    }

    @Test
    fun syncAll_skipsDisabledProviders() = runTest {
        fakeCalendarProviderDao.providers.add(
            CalendarProviderEntity(
                id = 1,
                name = "Disabled ICS",
                type = "ICS",
                url = "https://example.com/calendar.ics",
                isEnabled = false,
                color = 0xFF0000
            )
        )

        var engineCalled = false
        val mockEngine = MockEngine { request ->
            engineCalled = true
            respond(
                content = "",
                status = HttpStatusCode.OK
            )
        }
        val httpClient = HttpClient(mockEngine)

        val manager = CalendarManager(repository, httpClient)
        manager.syncAll()

        assertFalse(engineCalled, "Should not make HTTP requests for disabled providers")
        assertEquals(0, fakeCalendarEventDao.events.size)
    }
}
