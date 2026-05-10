package com.tajemniktv.tajsos.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeTrackDao : TrackDao {
    private val entries = mutableListOf<TrackEntryEntity>()
    private val entriesFlow = MutableStateFlow<List<TrackEntryEntity>>(emptyList())
    private val trackMedications = mutableListOf<TrackMedicationJoinEntity>()
    private val trackMedicationsFlow = MutableStateFlow<List<TrackMedicationJoinEntity>>(emptyList())

    override fun getAllTrackEntries(): Flow<List<TrackEntryEntity>> {
        return entriesFlow.map { list -> list.sortedWith(compareByDescending<TrackEntryEntity> { it.date }.thenByDescending { it.createdAt }) }
    }

    override suspend fun insertTrackEntries(entries: List<TrackEntryEntity>) { entries.forEach { insertTrackEntry(it) } }
    override suspend fun insertTrackEntry(entry: TrackEntryEntity): Long {
        val index = entries.indexOfFirst { it.id == entry.id }
        if (index != -1 && entry.id != 0L) {
            entries[index] = entry
            entriesFlow.value = entries.toList()
            return -1L
        } else {
            val newId = if (entry.id != 0L) entry.id else (entries.maxOfOrNull { it.id } ?: 0L) + 1L
            val newEntry = entry.copy(id = newId)
            entries.add(newEntry)
            entriesFlow.value = entries.toList()
            return newId
        }
    }

    override suspend fun getTrackEntryByDate(date: String): TrackEntryEntity? {
        return entriesFlow.value.find { it.date == date }
    }

    override suspend fun insertTrackMedication(join: TrackMedicationJoinEntity) {
        trackMedications.add(join)
        trackMedicationsFlow.value = trackMedications.toList()
    }

    override fun getTrackMedications(trackEntryId: Long): Flow<List<TrackMedicationJoinEntity>> {
        return trackMedicationsFlow.map { list -> list.filter { it.trackEntryId == trackEntryId } }
    }
}
