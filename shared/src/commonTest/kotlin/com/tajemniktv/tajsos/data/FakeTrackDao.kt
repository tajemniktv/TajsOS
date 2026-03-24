package com.tajemniktv.tajsos.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeTrackDao : TrackDao {
    private val entries = mutableListOf<TrackEntryEntity>()
    private val entriesFlow = MutableStateFlow<List<TrackEntryEntity>>(emptyList())

    override fun getAllTrackEntries(): Flow<List<TrackEntryEntity>> {
        return entriesFlow.map { list -> list.sortedWith(compareByDescending<TrackEntryEntity> { it.date }.thenByDescending { it.createdAt }) }
    }

    override suspend fun insertTrackEntry(entry: TrackEntryEntity): Long {
        val newId = (entries.size + 1).toLong()
        val newEntry = entry.copy(id = newId)
        entries.add(newEntry)
        entriesFlow.value = entries.toList()
        return newId
    }

    override suspend fun getTrackEntryByDate(date: String): TrackEntryEntity? {
        return entriesFlow.value.find { it.date == date }
    }

    override suspend fun insertTrackMedication(join: TrackMedicationJoinEntity) {}

    override fun getTrackMedications(trackEntryId: Long): Flow<List<TrackMedicationJoinEntity>> {
        return kotlinx.coroutines.flow.flowOf(emptyList())
    }
}
