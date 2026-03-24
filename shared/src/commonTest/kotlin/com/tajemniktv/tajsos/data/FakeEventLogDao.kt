package com.tajemniktv.tajsos.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeEventLogDao : EventLogDao {
    private val logs = mutableListOf<EventLogEntity>()
    private val logsFlow = MutableStateFlow<List<EventLogEntity>>(emptyList())

    override fun getRecentLogs(limit: Int): Flow<List<EventLogEntity>> {
        return logsFlow.map { it.take(limit) }
    }

    override suspend fun insertLog(log: EventLogEntity) {
        val newLog = log.copy(id = (logs.size + 1).toLong())
        logs.add(newLog)
        logsFlow.value = logs.toList()
    }

    fun getLogs(): List<EventLogEntity> = logs.toList()

    override fun getLogsForNode(nodeId: Long): Flow<List<EventLogEntity>> {
        return logsFlow.map { list -> list.filter { it.nodeId == nodeId || it.relatedNodeId == nodeId } }
    }
}
