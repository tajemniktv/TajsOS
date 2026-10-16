/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/** Resolves the device's current calendar date before translating it to the legacy storage key. */
internal fun currentLocalDate(): LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

/** One process-wide clock, active only while observed. Rechecks timezone and resume within a second. */
internal val systemLocalDates: Flow<LocalDate> by lazy {
    flow {
        while (true) {
            emit(currentLocalDate())
            delay(1_000)
        }
    }.distinctUntilChanged().shareIn(
        CoroutineScope(SupervisorJob() + Dispatchers.Default),
        SharingStarted.WhileSubscribed(stopTimeoutMillis = 0, replayExpirationMillis = 0),
        replay = 1,
    )
}

/**
 * Projects current-date pins over node/tag/snapshot observations without trusting a singular Room
 * relation that may select a historical row. Existing subscribers switch queries when the date changes.
 * Duplicate current-date rows use the lowest persisted ID, matching the pin repair command.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal fun Flow<List<NodeWithPin>>.withLocalTodayPins(
    pins: TodayPinDao,
    dates: Flow<LocalDate> = systemLocalDates,
): Flow<List<NodeWithPin>> = dates.distinctUntilChanged().flatMapLatest { date ->
    combine(this@withLocalTodayPins, pins.observePinsForDate(date.toString())) { nodes, currentPins ->
        val byNode = currentPins.distinctBy { it.nodeId }.associateBy { it.nodeId }
        nodes.map { it.copy(pin = byNode[it.node.id]) }
    }
}
