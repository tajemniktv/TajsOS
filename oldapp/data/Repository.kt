/*
 * Copyright (c) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * AppRepository is the single source of truth for TajsOS's Room database.
 */
class AppRepository(
    private val itemDao: ItemDao,
    private val projectDao: ProjectDao,
    private val areaDao: AreaDao,
    private val focusSessionDao: FocusSessionDao,
    private val trackDao: TrackDao
) {
    /**
     * getAllItems provides a real-time Flow of all active entries in the database.
     */
    fun getAllItems(): Flow<List<ItemWithPin>> = itemDao.getAllItemsWithPins()

    /**
     * getTodayItems filters items pinned for the current system date (today).
     */
    fun getTodayItems(): Flow<List<ItemEntity>> {
        val today = LocalDate.now().toString()
        return itemDao.getTodayItems(today)
    }

    suspend fun insertItem(item: ItemEntity): Long = itemDao.insertItem(item)

    suspend fun updateItem(item: ItemEntity) = itemDao.updateItem(item)

    suspend fun deleteItem(item: ItemEntity) = itemDao.deleteItem(item)

    /**
     * Pins an item to the "Today" list for the current system date.
     */
    suspend fun pinToToday(itemId: Long) {
        val today = LocalDate.now().toString()
        itemDao.pinToToday(TodayPinEntity(itemId, today, 0))
    }

    /**
     * Removes an item from the "Today" shortlist.
     */
    suspend fun unpinFromToday(itemId: Long) = itemDao.unpinFromToday(itemId)

    /**
     * Checks if a specific item is currently pinned for the day.
     */
    fun isPinnedToToday(itemId: Long): Flow<Boolean> = itemDao.isPinnedToToday(itemId)

    // Projects: Grouping tasks and notes.
    fun getAllProjects(): Flow<List<ProjectEntity>> = projectDao.getAllProjects()
    suspend fun insertProject(project: ProjectEntity) = projectDao.insertProject(project)

    // Areas: Life domains.
    fun getAllAreas(): Flow<List<AreaEntity>> = areaDao.getAllAreas()
    suspend fun insertArea(area: AreaEntity) = areaDao.insertArea(area)

    // Focus sessions: Logging the user's progress for future insights.
    fun getAllSessions(): Flow<List<FocusSessionEntity>> = focusSessionDao.getAllSessions()
    fun getActiveSession(): Flow<FocusSessionEntity?> = focusSessionDao.getActiveSession()
    suspend fun insertSession(session: FocusSessionEntity): Long = focusSessionDao.insertSession(session)
    suspend fun updateSession(session: FocusSessionEntity) = focusSessionDao.updateSession(session)

    // Daily tracking: For mood and micro-habits.
    fun getAllTrackEntries(): Flow<List<TrackEntryEntity>> = trackDao.getAllTrackEntries()
    suspend fun insertTrackEntry(entry: TrackEntryEntity) = trackDao.insertTrackEntry(entry)
}
