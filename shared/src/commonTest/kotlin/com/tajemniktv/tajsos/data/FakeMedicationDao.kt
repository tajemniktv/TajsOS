package com.tajemniktv.tajsos.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeMedicationDao : MedicationDao {
    override fun getAllMedications(): Flow<List<MedicationEntity>> = flowOf(emptyList())
    override suspend fun insertMedication(medication: MedicationEntity): Long = 0
    override suspend fun updateMedication(medication: MedicationEntity) {}
    override suspend fun deleteMedication(medication: MedicationEntity) {}
}
