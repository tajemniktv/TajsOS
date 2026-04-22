package com.tajemniktv.tajsos.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeProtocolDao : ProtocolDao {
    private val historyList = mutableListOf<ProtocolHistoryEntity>()
    private val historyFlow = MutableStateFlow<List<ProtocolHistoryEntity>>(emptyList())
    private var nextId = 1L

    override fun getAllProtocolHistory(): Flow<List<ProtocolHistoryEntity>> = historyFlow

    override suspend fun insertProtocolHistory(history: ProtocolHistoryEntity): Long {
        val newHistory = history.copy(id = nextId++)
        historyList.add(newHistory)
        historyFlow.value = historyList.toList()
        return newHistory.id
    }
}
