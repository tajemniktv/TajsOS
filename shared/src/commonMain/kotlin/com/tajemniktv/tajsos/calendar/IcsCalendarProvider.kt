/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved. 
 */

package com.tajemniktv.tajsos.calendar

import com.tajemniktv.tajsos.data.CalendarEventEntity
import com.tajemniktv.tajsos.data.CalendarProviderEntity
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.datetime.*
import kotlin.time.Clock
import kotlin.time.Instant

class IcsCalendarProvider(private val client: HttpClient) : CalendarProvider {
    override val type: String = "ICS"

    override suspend fun fetchEvents(
        provider: CalendarProviderEntity,
        from: Instant,
        to: Instant
    ): List<CalendarEventEntity> {
        val url = provider.url ?: return emptyList()
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

    private fun parseIcs(content: String, providerId: Long): List<CalendarEventEntity> {
        val events = mutableListOf<CalendarEventEntity>()
        var currentEvent: MutableMap<String, String>? = null

        val lines = content.lineSequence().iterator()
        var currentLine = if (lines.hasNext()) lines.next().trimEnd() else null

        while (currentLine != null) {
            var fullLine = currentLine
            currentLine = if (lines.hasNext()) lines.next().trimEnd() else null

            // Handle folding: lines starting with space or tab continue the previous line
            while (currentLine != null && (currentLine.startsWith(" ") || currentLine.startsWith("\t"))) {
                fullLine += currentLine.substring(1)
                currentLine = if (lines.hasNext()) lines.next().trimEnd() else null
            }

            val trimmed = fullLine.trim()
            when {
                trimmed.startsWith("BEGIN:VEVENT") -> currentEvent = mutableMapOf()
                trimmed.startsWith("END:VEVENT") -> {
                    currentEvent?.let {
                        val event = mapToEntity(it, providerId)
                        if (event != null) events.add(event)
                    }
                    currentEvent = null
                }
                currentEvent != null -> {
                    val parts = trimmed.split(":", limit = 2)
                    if (parts.size == 2) {
                        val keyParts = parts[0].split(";")
                        val key = keyParts[0]
                        val value = parts[1]

                        // We store the full key including parameters for DTSTART/DTEND to check TZID if needed
                        // But for simplicity we'll just store the value against the base key for standard fields
                        // and store the full unparsed value against the base key
                        currentEvent?.put(key, unescapeIcs(value))

                        if (key == "DTSTART" || key == "DTEND") {
                            // Also store the raw parts to extract timezone if needed
                            currentEvent?.put(key + "_RAW_KEY", parts[0])
                        }
                    }
                }
            }
        }
        return events
    }

    private fun unescapeIcs(value: String): String {
        return value.replace("\\\\", "\\")
            .replace("\\;", ";")
            .replace("\\,", ",")
            .replace("\\n", "\n")
            .replace("\\N", "\n")
    }

    private fun mapToEntity(map: Map<String, String>, providerId: Long): CalendarEventEntity? {
        val title = map["SUMMARY"] ?: "No Title"
        val startStr = map["DTSTART"] ?: return null
        // End is technically optional in ICS, defaults to start
        val endStr = map["DTEND"] ?: startStr

        val startRawKey = map["DTSTART_RAW_KEY"] ?: "DTSTART"
        val endRawKey = map["DTEND_RAW_KEY"] ?: "DTEND"

        val start = parseIcsDate(startStr, startRawKey) ?: return null
        val end = parseIcsDate(endStr, endRawKey) ?: start

        // If DTSTART contains VALUE=DATE or is exactly 8 chars long without time, it's all day
        val isAllDay = startRawKey.contains("VALUE=DATE") || startStr.length == 8

        return CalendarEventEntity(
            providerId = providerId,
            externalId = map["UID"],
            title = title,
            description = map["DESCRIPTION"],
            location = map["LOCATION"],
            startAt = start.toEpochMilliseconds(),
            endAt = end.toEpochMilliseconds(),
            isAllDay = isAllDay,
            createdAt = Clock.System.now().toEpochMilliseconds(),
            updatedAt = Clock.System.now().toEpochMilliseconds()
        )
    }

    private fun parseIcsDate(dateStr: String, rawKey: String = ""): Instant? {
        return try {
            val cleanDate = dateStr.trim()
            if (cleanDate.length == 8) { // 20230101
                val year = cleanDate.substring(0, 4).toInt()
                val month = cleanDate.substring(4, 6).toInt()
                val day = cleanDate.substring(6, 8).toInt()
                LocalDateTime(year, month, day, 0, 0).toInstant(TimeZone.UTC)
            } else if (cleanDate.contains("T")) {
                val year = cleanDate.substring(0, 4)
                val month = cleanDate.substring(4, 6)
                val day = cleanDate.substring(6, 8)
                val hour = cleanDate.substring(9, 11)
                val min = cleanDate.substring(11, 13)
                val sec = cleanDate.substring(13, 15)
                val iso = "$year-$month-${day}T$hour:$min:$sec"

                if (cleanDate.endsWith("Z")) {
                    Instant.parse(iso + "Z")
                } else {
                    // Look for TZID in rawKey, e.g. DTSTART;TZID=America/New_York
                    val tzidMatch = Regex("TZID=([^;:]+)").find(rawKey)
                    val timeZone = if (tzidMatch != null) {
                        try {
                            TimeZone.of(tzidMatch.groupValues[1])
                        } catch (e: Exception) {
                            TimeZone.currentSystemDefault()
                        }
                    } else {
                        TimeZone.currentSystemDefault()
                    }
                    LocalDateTime.parse(iso).toInstant(timeZone)
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
