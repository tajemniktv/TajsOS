/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeModeDao : ModeDao {
    override fun getAllModes(): Flow<List<ModeEntity>> = flowOf(emptyList())

    override suspend fun insertMode(mode: ModeEntity): Long = 0

    override suspend fun updateMode(mode: ModeEntity): Unit = Unit

    override suspend fun deleteMode(mode: ModeEntity): Unit = Unit

    override fun getPreferencesForMode(modeId: Long): Flow<ModePreferenceEntity?> = kotlinx.coroutines.flow.flowOf(null)

    override suspend fun insertPreference(preference: ModePreferenceEntity): Unit = Unit

    override fun getAreaFiltersForMode(modeId: Long): Flow<List<ModeAreaFilterEntity>> = kotlinx.coroutines.flow.flowOf(emptyList())

    override suspend fun insertAreaFilter(filter: ModeAreaFilterEntity): Unit = Unit

    override fun getTypeFiltersForMode(modeId: Long): Flow<List<ModeTypeFilterEntity>> = kotlinx.coroutines.flow.flowOf(emptyList())

    override suspend fun insertTypeFilter(filter: ModeTypeFilterEntity): Unit = Unit

    override fun getAllUsageLogs(): Flow<List<ModeUsageLogEntity>> = kotlinx.coroutines.flow.flowOf(emptyList())

    override suspend fun insertUsageLog(log: ModeUsageLogEntity): Long = 0

    override suspend fun deactivateLog(
        id: Long,
        timestamp: Long,
    ): Unit = Unit
}
