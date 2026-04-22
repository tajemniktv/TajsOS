package com.tajemniktv.tajsos.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeProtocolDao : ProtocolDao {
    private val historyList = mutableListOf<ProtocolHistoryEntity>()
    private var nextId = 1L
    private val historyFlow = kotlinx.coroutines.flow.MutableStateFlow(historyList.toList())

    override fun getAllProtocolHistory(): Flow<List<ProtocolHistoryEntity>> = historyFlow

    override suspend fun insertProtocolHistory(history: ProtocolHistoryEntity): Long {
        val id = nextId++
        historyList.add(history.copy(id = id))
        historyFlow.value = historyList.toList()
        return id
    }
}
