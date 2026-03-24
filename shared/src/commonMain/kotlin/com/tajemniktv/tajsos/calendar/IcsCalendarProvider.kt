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
        var currentBuilder: IcsEventBuilder? = null

        unfoldLines(content).forEach { line ->
            val trimmed = line.trim()
            when {
                trimmed.startsWith("BEGIN:VEVENT") -> {
                    currentBuilder = IcsEventBuilder()
                }
                trimmed.startsWith("END:VEVENT") -> {
                    currentBuilder?.build(providerId)?.let { events.add(it) }
                    currentBuilder = null
                }
                currentBuilder != null -> {
                    currentBuilder?.processLine(trimmed)
                }
            }
        }
        return events
    }
}

private data class IcsDateProperty(val value: String, val rawKey: String) {
    val isAllDay: Boolean get() = rawKey.contains("VALUE=DATE") || value.length == 8
}

private class IcsEventBuilder {
    private var uid: String? = null
    private var summary: String = "No Title"
    private var description: String? = null
    private var location: String? = null
    private var startProp: IcsDateProperty? = null
    private var endProp: IcsDateProperty? = null

    fun processLine(line: String) {
        val parts = line.split(":", limit = 2)
        if (parts.size != 2) return

        val rawKey = parts[0]
        val key = rawKey.substringBefore(";")
        val value = unescapeIcs(parts[1])

        when (key) {
            "UID" -> uid = value
            "SUMMARY" -> summary = value
            "DESCRIPTION" -> description = value
            "LOCATION" -> location = value
            "DTSTART" -> startProp = IcsDateProperty(value, rawKey)
            "DTEND" -> endProp = IcsDateProperty(value, rawKey)
        }
    }

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
            updatedAt = now
        )
    }

    private fun unescapeIcs(value: String): String {
        return value.replace("\\\\", "\\")
            .replace("\\;", ";")
            .replace("\\,", ",")
            .replace("\\n", "\n")
            .replace("\\N", "\n")
    }

    private fun parseDate(property: IcsDateProperty): Instant? {
        return try {
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
    }

    private fun parseAllDayDate(cleanDate: String): Instant {
        val year = cleanDate.substring(0, 4).toInt()
        val month = cleanDate.substring(4, 6).toInt()
        val day = cleanDate.substring(6, 8).toInt()
        return LocalDateTime(year, month, day, 0, 0).toInstant(TimeZone.UTC)
    }

    private fun parseIsoDate(property: IcsDateProperty): Instant {
        val cleanDate = property.value.trim()
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

    private fun extractTimeZone(rawKey: String): TimeZone {
        val tzidMatch = Regex("TZID=([^;:]+)").find(rawKey) ?: return TimeZone.currentSystemDefault()
        return try {
            TimeZone.of(tzidMatch.groupValues[1])
        } catch (e: Exception) {
            TimeZone.currentSystemDefault()
        }
    }
}
