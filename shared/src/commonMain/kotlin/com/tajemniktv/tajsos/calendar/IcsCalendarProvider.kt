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
                parseIcs(icsContent, provider.id)
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

        content.lineSequence().forEach { line ->
            val trimmed = line.trim()
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
                        val key = parts[0].split(";")[0]
                        currentEvent[key] = parts[1]
                    }
                }
            }
        }
        return events
    }

    private fun mapToEntity(map: Map<String, String>, providerId: Long): CalendarEventEntity? {
        val title = map["SUMMARY"] ?: "No Title"
        val startStr = map["DTSTART"] ?: return null
        val endStr = map["DTEND"] ?: startStr

        val start = parseIcsDate(startStr) ?: return null
        val end = parseIcsDate(endStr) ?: start

        return CalendarEventEntity(
            providerId = providerId,
            externalId = map["UID"],
            title = title,
            description = map["DESCRIPTION"],
            location = map["LOCATION"],
            startAt = start.toEpochMilliseconds(),
            endAt = end.toEpochMilliseconds(),
            isAllDay = startStr.length == 8,
            createdAt = Clock.System.now().toEpochMilliseconds(),
            updatedAt = Clock.System.now().toEpochMilliseconds()
        )
    }

    private fun parseIcsDate(dateStr: String): Instant? {
        return try {
            if (dateStr.length == 8) { // 20230101
                val year = dateStr.substring(0, 4).toInt()
                val month = dateStr.substring(4, 6).toInt()
                val day = dateStr.substring(6, 8).toInt()
                LocalDateTime(year, month, day, 0, 0).toInstant(TimeZone.UTC)
            } else if (dateStr.contains("T")) {
                // Simplified regex replacement for basic ICS date formats
                val year = dateStr.substring(0, 4)
                val month = dateStr.substring(4, 6)
                val day = dateStr.substring(6, 8)
                val hour = dateStr.substring(9, 11)
                val min = dateStr.substring(11, 13)
                val sec = dateStr.substring(13, 15)
                val iso = "$year-$month-${day}T$hour:$min:$sec"
                if (dateStr.endsWith("Z")) {
                    Instant.parse(iso + "Z")
                } else {
                    LocalDateTime.parse(iso).toInstant(TimeZone.currentSystemDefault())
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
