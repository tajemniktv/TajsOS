/*
 * Copyright (c) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * ItemDao provides methods for accessing the core "Inbox" and "Today" items.
 * It uses Kotlin Coroutines (suspend) for single operations and 
 * StateFlow (Flow) for continuous data updates.
 */
@Dao
interface ItemDao {

    /**
     * getAllItemsWithPins returns the list of all active items with their 
     * join status (ItemWithPin). This allows the UI to stay updated 
     * in real-time as items are added, updated, or pinned.
     */
    @Transaction
    @Query("SELECT * FROM items WHERE status != 'archived' ORDER BY createdAt DESC")
    fun getAllItemsWithPins(): Flow<List<ItemWithPin>>

    /**
     * getTodayItems performs an INNER JOIN to retrieve only items 
     * that are currently "pinned" for the specific date provided.
     */
    @Query(
        """
        SELECT items.* FROM items 
        INNER JOIN today_pins ON items.id = today_pins.itemId 
        WHERE items.status = 'active' AND today_pins.date = :date
        ORDER BY today_pins.position ASC
    """
    )
    fun getTodayItems(date: String): Flow<List<ItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemEntity): Long

    @Update
    suspend fun updateItem(item: ItemEntity)

    @Delete
    suspend fun deleteItem(item: ItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun pinToToday(pin: TodayPinEntity)

    @Query("DELETE FROM today_pins WHERE itemId = :itemId")
    suspend fun unpinFromToday(itemId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM today_pins WHERE itemId = :itemId)")
    fun isPinnedToToday(itemId: Long): Flow<Boolean>
}

/**
 * ProjectDao manages long-lived containers for items.
 */
@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects WHERE status != 'archived'")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)

    @Update
    suspend fun updateProject(project: ProjectEntity)
}

/**
 * AreaDao manages stable life domains.
 */
@Dao
interface AreaDao {
    @Query("SELECT * FROM areas")
    fun getAllAreas(): Flow<List<AreaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArea(area: AreaEntity)
}

/**
 * FocusSessionDao manages the history of "Focus" mode activities.
 */
@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM focus_sessions ORDER BY startAt DESC")
    fun getAllSessions(): Flow<List<FocusSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSessionEntity): Long

    @Update
    suspend fun updateSession(session: FocusSessionEntity)

    @Query("SELECT * FROM focus_sessions WHERE endAt IS NULL LIMIT 1")
    fun getActiveSession(): Flow<FocusSessionEntity?>
}

/**
 * TrackDao handles daily mood and energy check-ins.
 */
@Dao
interface TrackDao {
    /**
     * Retrieves all history entries, sorted by most recent date.
     */
    @Query("SELECT * FROM track_entries ORDER BY date DESC")
    fun getAllTrackEntries(): Flow<List<TrackEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrackEntry(entry: TrackEntryEntity)
}
