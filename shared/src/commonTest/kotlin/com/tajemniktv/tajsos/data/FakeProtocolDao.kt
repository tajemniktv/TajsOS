package com.tajemniktv.tajsos.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeProtocolDao : ProtocolDao {
    override fun getAllProtocolHistory(): Flow<List<ProtocolHistoryEntity>> = flowOf(emptyList())
    override suspend fun insertProtocolHistory(history: ProtocolHistoryEntity): Long = 0
}
