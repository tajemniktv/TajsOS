/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeMedicationDao : MedicationDao {
    override fun getAllMedications(): Flow<List<MedicationEntity>> = flowOf(emptyList())

    override suspend fun insertMedication(medication: MedicationEntity): Long = 0

    override suspend fun updateMedication(medication: MedicationEntity): Unit = Unit

    override suspend fun deleteMedication(medication: MedicationEntity): Unit = Unit
}
