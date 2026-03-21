package com.tajemniktv.tajsos.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeTrackDao : TrackDao {
    private val entries = mutableListOf<TrackEntryEntity>()
    private val entriesFlow = MutableStateFlow<List<TrackEntryEntity>>(emptyList())

    override fun getAllTrackEntries(): Flow<List<TrackEntryEntity>> {
        return entriesFlow.map { it.sortedWith(compareByDescending<TrackEntryEntity> { entry -> entry.date }.thenByDescending { entry -> entry.createdAt }) }
    }

    override suspend fun insertTrackEntry(entry: TrackEntryEntity) {
        val newId = (entries.size + 1).toLong()
        val newEntry = entry.copy(id = newId)
        entries.add(newEntry)
        entriesFlow.value = entries.toList()
    }
}
