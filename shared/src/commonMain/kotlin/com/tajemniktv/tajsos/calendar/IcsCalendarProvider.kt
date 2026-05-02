/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.calendar

import com.tajemniktv.tajsos.data.CalendarEventEntity
import com.tajemniktv.tajsos.data.CalendarProviderEntity
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * A calendar provider that fetches and parses standard ICS (iCalendar) files from HTTP(S) sources.
 *
 * @param client The Ktor HttpClient used to fetch the ICS file content.
 */
class IcsCalendarProvider(
    private val client: HttpClient,
) : CalendarProvider {
    override val type: String = "ICS"

    /**
     * Fetches and parses ICS events for a given provider within the specified time range.
     * Only standard HTTP/HTTPS schemes are supported.
     *
     * @param provider The provider configuration containing the ICS URL.
     * @param from The start of the time range (inclusive).
     * @param to The end of the time range (inclusive).
     * @return A list of valid, parsed [CalendarEventEntity] instances falling within the range.
     */
    override suspend fun fetchEvents(
        provider: CalendarProviderEntity,
        from: Instant,
        to: Instant,
    ): List<CalendarEventEntity> {
        val url = provider.url ?: return emptyList()
        if (!isValidHttpUrl(url)) return emptyList()
        return try {
            val response = client.get(url)
            if (response.status.value in 200..299) {
                val icsContent = response.bodyAsText()
                val events = parseIcs(icsContent, provider.id)
                events.filter { it.startAt <= to.toEpochMilliseconds() && it.endAt >= from.toEpochMilliseconds() }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Validates that a string is an HTTP/HTTPS URL and blocks internal or private network hosts
     * to prevent Server-Side Request Forgery (SSRF).
     *
     * It parses the URL and explicitly rejects loopback addresses (e.g., 127.0.0.1, ::1),
     * link-local addresses (169.254.x.x), private RFC1918 ranges (10.x.x.x, 172.16.x.x, 192.168.x.x),
     * unique-local IPv6 addresses, and known metadata hosts (e.g., "localhost", "metadata.google.internal").
     *
     * @param url The raw URL string to validate.
     * @return `true` if the URL has a valid scheme and an external routable host, `false` otherwise
     *         (or if parsing throws an exception).
     */
    private fun isValidHttpUrl(url: String): Boolean {
        return try {
            val parsedUrl = io.ktor.http.Url(url)
            if (!isValidScheme(parsedUrl.protocol.name.lowercase())) return false
            isPublicRoutableHost(parsedUrl.host.lowercase())
        } catch (e: Exception) {
            false
        }
    }

    private fun isValidScheme(scheme: String): Boolean =
        scheme == "http" || scheme == "https"

    private fun isPublicRoutableHost(host: String): Boolean {
        val ipLiteral = parseIpAddress(host)
        if (ipLiteral != null) {
            // It's a parsed IPv4 or IPv6 literal. Verify it's not a private or local range.
            return !ipLiteral.isPrivateOrLocal()
        }

        // For non-literal hostnames, we cannot perform synchronous blocking DNS resolution
        // in commonMain without breaking KMP asynchronous patterns or introducing heavy dependencies.
        // Therefore, we fall back to blocking known critical local infrastructure names.
        // Deep DNS rebinding and IP-resolution validation should ideally be configured at the
        // Ktor Engine or system network proxy level.
        return host !in BLOCKED_METADATA_HOSTS
    }

    companion object {
        private val BLOCKED_METADATA_HOSTS = setOf(
            "localhost",
            "metadata.google.internal",
            "169.254.169.254"
        )
    }

    /**
     * Unfolds multiline entries in an ICS file into a single logical line per entry.
     * In the ICS spec, long lines are wrapped and begin with whitespace on subsequent lines.
     *
     * @param content The raw, multi-line string content of the ICS file.
     * @return A sequence of unfolded, logical lines.
     */
    private fun unfoldLines(content: String): Sequence<String> =
        sequence {
            val lines = content.lineSequence().iterator()
            if (!lines.hasNext()) return@sequence

            var currentFullLine = lines.next().trimEnd()

            while (lines.hasNext()) {
                val nextLine = lines.next().trimEnd()
                if (nextLine.startsWith(" ") || nextLine.startsWith("\t")) {
                    currentFullLine += nextLine.substring(1)
                } else {
                    yield(currentFullLine)
                    currentFullLine = nextLine
                }
            }
            yield(currentFullLine)
        }

    /**
     * Parses the unfolded lines of an ICS file and builds [CalendarEventEntity] instances for each `VEVENT`.
     *
     * @param content The raw string content of the ICS file.
     * @param providerId The internal database ID of the calendar provider owning these events.
     * @return A list of fully constructed [CalendarEventEntity] instances.
     */
    private fun parseIcs(
        content: String,
        providerId: Long,
    ): List<CalendarEventEntity> {
        val events = mutableListOf<CalendarEventEntity>()
        var currentBuilder: IcsEventBuilder? = null

        unfoldLines(content).forEach { line ->
            val trimmed = line.trim()
            when
                {
                    trimmed.startsWith("BEGIN:VEVENT") -> {
                        currentBuilder = IcsEventBuilder()
                    }

                    trimmed.startsWith("END:VEVENT") -> {
                        currentBuilder?.build(providerId)?.let { events.add(it) }
                        currentBuilder = null
                    }

                    currentBuilder != null -> {
                        currentBuilder.processLine(trimmed)
                    }
                }
        }
        return events
    }
}

/**
 * Represents an extracted date property from an ICS file, encapsulating the raw value and metadata key.
 *
 * @property value The raw date/time string value (e.g., "20231225" or "20231225T100000Z").
 * @property rawKey The raw property key (e.g., "DTSTART" or "DTSTART;VALUE=DATE").
 */
internal data class IcsDateProperty(
    val value: String,
    val rawKey: String,
) {
    /**
     * Determines if the parsed property represents a full-day event without specific start/end times.
     */
    val isAllDay: Boolean get() = rawKey.contains("VALUE=DATE") || value.length == 8
}

/**
 * A stateful builder class designed to construct a single [CalendarEventEntity] iteratively
 * by processing lines within a `BEGIN:VEVENT` and `END:VEVENT` block.
 */
internal class IcsEventBuilder {
    internal var uid: String? = null
    internal var summary: String = "No Title"
    internal var description: String? = null
    internal var location: String? = null
    internal var startProp: IcsDateProperty? = null
    internal var endProp: IcsDateProperty? = null

    /**
     * Updates the builder's fields from a single unfolded VEVENT line.
     *
     * Recognizes the keys `UID`, `SUMMARY`, `DESCRIPTION`, `LOCATION`, `DTSTART`, and `DTEND`; the value is unescaped and date properties retain their raw key text.
     *
     * @param line An unfolded logical line from within a `VEVENT` block, expected in `key:value` form.
     */
    fun processLine(line: String) {
        val parts = line.split(":", limit = 2)
        if (parts.size != 2) return

        val rawKey = parts[0]
        val key = rawKey.substringBefore(";")
        val value = unescapeIcs(parts[1])

        when (key)
        {
            "UID" -> uid = value
            "SUMMARY" -> summary = value
            "DESCRIPTION" -> description = value
            "LOCATION" -> location = value
            "DTSTART" -> startProp = IcsDateProperty(value, rawKey)
            "DTEND" -> endProp = IcsDateProperty(value, rawKey)
        }
    }

    /**
     * Completes the builder process and attempts to construct a valid [CalendarEventEntity].
     * Returns null if minimum required properties (such as a valid start time) are missing.
     *
     * @param providerId The database ID of the calendar provider creating this event.
     * @return A fully populated [CalendarEventEntity], or null if essential data is missing/invalid.
     */
    fun build(providerId: Long): CalendarEventEntity? {
        val sProp = startProp ?: return null
        val eProp = endProp ?: sProp

        val start = parseDate(sProp) ?: return null
        val end = parseDate(eProp) ?: start

        val now = Clock.System.now().toEpochMilliseconds()
        return CalendarEventEntity(
            providerId = providerId,
            externalId = uid,
            title = summary,
            description = description,
            location = location,
            startAt = start.toEpochMilliseconds(),
            endAt = end.toEpochMilliseconds(),
            isAllDay = sProp.isAllDay,
            createdAt = now,
            updatedAt = now,
        )
    }

    /**
     * Decode ICS escape sequences in a property value.
     *
     * Replaces ICS escapes (`\\`, `\;`, `\,`, `\n`, `\N`) with their corresponding characters.
     *
     * @param value Raw ICS-escaped string.
     * @return The string with ICS escape sequences unescaped.
     */
    private fun unescapeIcs(value: String): String =
        value
            .replace("\\\\", "\\")
            .replace("\\;", ";")
            .replace("\\,", ",")
            .replace("\\n", "\n")
            .replace("\\N", "\n")

    /**
     * Parses an [IcsDateProperty] into a unified [Instant].
     *
     * @param property The extracted date property object to parse.
     * @return The resulting [Instant], or null if parsing fails.
     */
    private fun parseDate(property: IcsDateProperty): Instant? =
        try {
            val cleanDate = property.value.trim()
            if (cleanDate.length == 8) {
                parseAllDayDate(cleanDate)
            } else if (cleanDate.contains("T")) {
                parseIsoDate(property)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }

    /**
     * Convert an 8-character YYYYMMDD date string to an Instant at midnight UTC.
     *
     * @param cleanDate The 8-character date string in `YYYYMMDD` format.
     * @return An Instant representing the parsed date at 00:00 UTC.
     * @throws IllegalArgumentException if `cleanDate` has fewer than 8 characters.
     */
    internal fun parseAllDayDate(cleanDate: String): Instant {
        if (cleanDate.length < 8) throw IllegalArgumentException("Invalid date length")
        val year = cleanDate.substring(0, 4).toInt()
        val month = cleanDate.substring(4, 6).toInt()
        val day = cleanDate.substring(6, 8).toInt()
        return LocalDateTime(year, month, day, 0, 0).toInstant(TimeZone.UTC)
    }

    /**
     * Parse an ISO-like datetime value from an ICS date property into an Instant.
     *
     * Handles values that end with `Z` as UTC; otherwise resolves a timezone from the property's raw key (e.g., `TZID=...`) and converts the local datetime to an Instant.
     *
     * @param property The [IcsDateProperty] containing the raw datetime string and the raw key (which may include `TZID`).
     * @return The parsed Instant representing that point in time.
     * @throws IllegalArgumentException If the datetime string is shorter than the expected minimum length.
     */
    internal fun parseIsoDate(property: IcsDateProperty): Instant {
        val cleanDate = property.value.trim()
        if (cleanDate.length < 15) throw IllegalArgumentException("Invalid ISO date length")
        val year = cleanDate.substring(0, 4)
        val month = cleanDate.substring(4, 6)
        val day = cleanDate.substring(6, 8)
        val hour = cleanDate.substring(9, 11)
        val min = cleanDate.substring(11, 13)
        val sec = cleanDate.substring(13, 15)
        val iso = "$year-$month-${day}T$hour:$min:$sec"

        return if (cleanDate.endsWith("Z")) {
            Instant.parse(iso + "Z")
        } else {
            val timeZone = extractTimeZone(property.rawKey)
            LocalDateTime.parse(iso).toInstant(timeZone)
        }
    }

    /**
     * Extracts a [TimeZone] from the raw ICS property key via the `TZID` parameter,
     * defaulting to the system default if missing or invalid.
     *
     * @param rawKey The raw property key containing potential `TZID` parameters.
     * @return The parsed [TimeZone] or the system default fallback.
     */
    internal fun extractTimeZone(rawKey: String): TimeZone {
        val tzidMatch =
            Regex("TZID=([^;:]+)").find(rawKey) ?: return TimeZone.currentSystemDefault()
        return try {
            TimeZone.of(tzidMatch.groupValues[1])
        } catch (e: Exception) {
            TimeZone.currentSystemDefault()
        }
    }
}
