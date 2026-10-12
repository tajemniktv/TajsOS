/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/** Owns pin row persistence; logical node/date deduplication belongs to repository transactions. */
@Dao
interface TodayPinDao {
    /** Upserts by row identity without hiding historical logical duplicates from repair commands. */
    @Upsert
    suspend fun pinToToday(pin: TodayPinEntity)

    /** Loads every matching row in stable ID order for indexed node/date reconciliation. */
    @Query("SELECT * FROM today_pins WHERE nodeId = :nodeId AND date = :date ORDER BY id ASC")
    suspend fun getPinsForNodeOnDate(nodeId: Long, date: String): List<TodayPinEntity>

    /** Removes one date's assignments without dropping other dates. */
    @Query("DELETE FROM today_pins WHERE nodeId = :nodeId AND date = :date")
    suspend fun unpinFromDate(nodeId: Long, date: String)

    /** Removes all assignments owned by a deleted or explicitly unpinned node. */
    @Query("DELETE FROM today_pins WHERE nodeId = :nodeId")
    suspend fun unpinFromToday(nodeId: Long)

    /** Observes whether the node is assigned to the explicitly resolved local date. */
    @Query("SELECT EXISTS(SELECT 1 FROM today_pins WHERE nodeId = :nodeId AND date = :date)")
    fun isPinnedOnDate(nodeId: Long, date: String): Flow<Boolean>

    /** Supplies every pin for one date so historical rows cannot mask current UI assignments. */
    @Query("SELECT * FROM today_pins WHERE date = :date ORDER BY id ASC")
    fun observePinsForDate(date: String): Flow<List<TodayPinEntity>>
}
