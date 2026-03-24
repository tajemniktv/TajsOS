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

    private fun unfoldLines(content: String): Sequence<String> = sequence {
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

    private fun parseIcs(content: String, providerId: Long): List<CalendarEventEntity> {
        val events = mutableListOf<CalendarEventEntity>()
        var currentEvent: MutableMap<String, String>? = null

        unfoldLines(content).forEach { line ->
            val trimmed = line.trim()
            when {
                trimmed.startsWith("BEGIN:VEVENT") -> {
                    currentEvent = mutableMapOf()
                }
                trimmed.startsWith("END:VEVENT") -> {
                    currentEvent?.let {
                        val event = mapToEntity(it, providerId)
                        if (event != null) events.add(event)
                    }
                    currentEvent = null
                }
                currentEvent != null -> {
                    processEventLine(trimmed, currentEvent)
                }
            }
        }
        return events
    }

    private fun processEventLine(line: String, currentEvent: MutableMap<String, String>) {
        val parts = line.split(":", limit = 2)
        if (parts.size != 2) return

        val rawKey = parts[0]
        val key = rawKey.substringBefore(";")
        val value = parts[1]

        currentEvent[key] = unescapeIcs(value)

        if (key == "DTSTART" || key == "DTEND") {
            currentEvent[key + "_RAW_KEY"] = rawKey
        }
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
        val endStr = map["DTEND"] ?: startStr

        val startRawKey = map["DTSTART_RAW_KEY"] ?: "DTSTART"
        val endRawKey = map["DTEND_RAW_KEY"] ?: "DTEND"

        val start = parseIcsDate(startStr, startRawKey) ?: return null
        val end = parseIcsDate(endStr, endRawKey) ?: start

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
            if (cleanDate.length == 8) {
                val year = cleanDate.substring(0, 4).toInt()
                val month = cleanDate.substring(4, 6).toInt()
                val day = cleanDate.substring(6, 8).toInt()
                LocalDateTime(year, month, day, 0, 0).toInstant(TimeZone.UTC)
            } else if (cleanDate.contains("T")) {
                parseIsoIcsDate(cleanDate, rawKey)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun parseIsoIcsDate(cleanDate: String, rawKey: String): Instant {
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
            val timeZone = extractTimeZone(rawKey)
            LocalDateTime.parse(iso).toInstant(timeZone)
        }
    }

    private fun extractTimeZone(rawKey: String): TimeZone {
        val tzidMatch = Regex("TZID=([^;:]+)").find(rawKey) ?: return TimeZone.currentSystemDefault()
        return try {
            TimeZone.of(tzidMatch.groupValues[1])
        } catch (e: Exception) {
            TimeZone.currentSystemDefault()
        }
    }
}
