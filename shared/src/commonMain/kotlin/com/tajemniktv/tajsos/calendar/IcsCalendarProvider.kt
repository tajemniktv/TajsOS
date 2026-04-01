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
        val parsedUrl = try {
            io.ktor.http.Url(url)
        val parsedUrl = try {
            io.ktor.http.Url(url)
        } catch (e: Exception) {
            return emptyList()
        }
        if (parsedUrl.protocol != io.ktor.http.URLProtocol.HTTP &&
            parsedUrl.protocol != io.ktor.http.URLProtocol.HTTPS) {
            return emptyList()
        }
        if (!isPublicRoutableHost(parsedUrl.host)) {
            println("ICS calendar URL rejected: non-public host blocked")
            return emptyList()
        }
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
            val sanitizedMessage = sanitizeErrorMessage(e.message ?: "Unknown error")
            println("ICS calendar fetch failed: $sanitizedMessage")
            emptyList()
        }
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

    /**
     * Sanitizes error messages by redacting potentially sensitive URL or user-specific data.
     *
     * @param message The original error message.
     * @return A sanitized version safe for logging.
     */
    private fun sanitizeErrorMessage(message: String): String {
        // Redact common URL patterns and user-specific data
        return message
            .replace(Regex("https?://[^\\s]+"), "[REDACTED_URL]")
            .replace(Regex("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b"), "[REDACTED_EMAIL]")
            .replace(Regex("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b"), "[REDACTED_IP]")
            .replace(Regex("\\b[0-9a-fA-F:]+:[0-9a-fA-F:]+\\b"), "[REDACTED_IPV6]")
    }

    /**
     * Checks if a host is a public, routable address.
     * Rejects loopback, link-local, private (RFC1918), and IPv6 unique-local addresses.
     *
     * @param host The hostname or IP address to validate.
     * @return true if the host is public and routable, false otherwise.
     */
    private fun isPublicRoutableHost(host: String): Boolean {
        // Check if it's an IPv4 address
        val ipv4Pattern = Regex("^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$")
        val ipv4Match = ipv4Pattern.matchEntire(host)
        if (ipv4Match != null) {
            val octets = ipv4Match.groupValues.drop(1).map { it.toIntOrNull() ?: 256 }
            if (octets.any { it > 255 }) return false

            val first = octets[0]
            val second = octets[1]

            // Loopback: 127.0.0.0/8
            if (first == 127) return false

            // Link-local: 169.254.0.0/16
            if (first == 169 && second == 254) return false

            // RFC1918 private ranges:
            // 10.0.0.0/8
            if (first == 10) return false
            // 172.16.0.0/12
            if (first == 172 && second in 16..31) return false
            // 192.168.0.0/16
            if (first == 192 && second == 168) return false

            return true
        }

        // Check if it's an IPv6 address (simplified check)
        if (host.contains(':')) {
            val normalized = normalizeIPv6(host)

            // ::1 is loopback
            if (normalized == "0000:0000:0000:0000:0000:0000:0000:0001") return false

            // fe80::/10 is link-local (fe80-febf)
            if (normalized.startsWith("fe8") || normalized.startsWith("fe9") ||
                normalized.startsWith("fea") || normalized.startsWith("feb")) {
                return false
            }

            // fc00::/7 is unique-local (fc00-fdff)
            if (normalized.startsWith("fc") || normalized.startsWith("fd")) {
                return false
            }

            return true
        }

        // For hostnames, allow them (DNS will resolve them)
        // Additional checks could be added for known metadata service hostnames
        return true
    }

    /**
     * Normalizes an IPv6 address to a consistent format for comparison.
     * This is a simplified implementation that handles basic IPv6 formats.
     *
     * @param ipv6 The IPv6 address string.
     * @return A normalized IPv6 address string in lowercase.
     */
    private fun normalizeIPv6(ipv6: String): String {
        val cleaned = ipv6.lowercase().trim()

        // Handle :: expansion
        if (cleaned.contains("::")) {
            val parts = cleaned.split("::")
            val leftParts = if (parts[0].isEmpty()) emptyList() else parts[0].split(":")
            val rightParts = if (parts.size < 2 || parts[1].isEmpty()) emptyList() else parts[1].split(":")
            val missingParts = 8 - leftParts.size - rightParts.size
            val expanded = leftParts + List(missingParts) { "0" } + rightParts
            return expanded.joinToString(":") { it.padStart(4, '0') }
        }

        // Already expanded, just pad each segment
        return cleaned.split(":").joinToString(":") { it.padStart(4, '0') }
    }
}

/**
 * Represents an extracted date property from an ICS file, encapsulating the raw value and metadata key.
 *
 * @property value The raw date/time string value (e.g., "20231225" or "20231225T100000Z").
 * @property rawKey The raw property key (e.g., "DTSTART" or "DTSTART;VALUE=DATE").
 */
private data class IcsDateProperty(
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
private class IcsEventBuilder {
    private var uid: String? = null
    private var summary: String = "No Title"
    private var description: String? = null
    private var location: String? = null
    private var startProp: IcsDateProperty? = null
    private var endProp: IcsDateProperty? = null

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
    private fun parseAllDayDate(cleanDate: String): Instant {
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
    private fun parseIsoDate(property: IcsDateProperty): Instant {
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
    private fun extractTimeZone(rawKey: String): TimeZone {
        val tzidMatch =
            Regex("TZID=([^;:]+)").find(rawKey) ?: return TimeZone.currentSystemDefault()
        return try {
            TimeZone.of(tzidMatch.groupValues[1])
        } catch (e: Exception) {
            TimeZone.currentSystemDefault()
        }
    }
}