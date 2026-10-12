/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** Models pin row identity and observable changes, not Room's writer transaction guarantees. */
class FakeTodayPinDao : TodayPinDao {
    /** Pin snapshots shared with fake node projections. */
    internal val rows = MutableStateFlow<List<TodayPinEntity>>(emptyList())
    private var nextId = 1L

    override suspend fun pinToToday(pin: TodayPinEntity) {
        val id = if (pin.id == 0L) nextId else pin.id
        nextId = maxOf(nextId, id + 1)
        rows.value = rows.value.filterNot { it.id == id } + pin.copy(id = id)
    }

    override suspend fun getPinsForNodeOnDate(nodeId: Long, date: String): List<TodayPinEntity> =
        rows.value.filter { it.nodeId == nodeId && it.date == date }.sortedBy { it.id }

    override suspend fun unpinFromDate(nodeId: Long, date: String) {
        rows.value = rows.value.filterNot { it.nodeId == nodeId && it.date == date }
    }

    override suspend fun unpinFromToday(nodeId: Long) {
        rows.value = rows.value.filterNot { it.nodeId == nodeId }
    }

    override fun isPinnedToToday(nodeId: Long): Flow<Boolean> = rows.map { pins -> pins.any { it.nodeId == nodeId } }
}
