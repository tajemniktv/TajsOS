package com.tajemniktv.tajsos.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeProtocolDao : ProtocolDao {
    private val historyList = mutableListOf<ProtocolHistoryEntity>()
    private val historyFlow = MutableStateFlow<List<ProtocolHistoryEntity>>(emptyList())

    override fun getAllProtocolHistory(): Flow<List<ProtocolHistoryEntity>> = historyFlow.asStateFlow()

    override suspend fun insertProtocolHistory(history: ProtocolHistoryEntity): Long {
        val newId = if (history.id != 0L) history.id else (historyList.maxOfOrNull { it.id } ?: 0L) + 1L
        historyList.add(history.copy(id = newId))
        historyFlow.value = historyList.toList()
        return newId
    }
}
