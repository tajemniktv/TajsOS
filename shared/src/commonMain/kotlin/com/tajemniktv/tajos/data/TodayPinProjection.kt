/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/** Resolves the device's current calendar date before translating it to the legacy storage key. */
internal fun currentLocalDate(): LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

/**
 * Projects current-date pins over node/tag/snapshot observations without trusting a singular Room
 * relation that may select a historical row. Each subscription resolves the local date afresh.
 * Duplicate current-date rows use the lowest persisted ID, matching the pin repair command.
 */
internal fun Flow<List<NodeWithPin>>.withLocalTodayPins(pins: TodayPinDao): Flow<List<NodeWithPin>> = flow {
    val date = currentLocalDate().toString()
    emitAll(combine(this@withLocalTodayPins, pins.observePinsForDate(date)) { nodes, currentPins ->
        val byNode = currentPins.distinctBy { it.nodeId }.associateBy { it.nodeId }
        nodes.map { it.copy(pin = byNode[it.node.id]) }
    })
}
