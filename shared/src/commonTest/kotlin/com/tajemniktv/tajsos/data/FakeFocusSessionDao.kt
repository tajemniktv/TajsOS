package com.tajemniktv.tajsos.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeFocusSessionDao : FocusSessionDao {
    private val sessions = mutableListOf<FocusSessionEntity>()
    private val sessionsFlow = MutableStateFlow<List<FocusSessionEntity>>(emptyList())

    override fun getAllSessions(): Flow<List<FocusSessionEntity>> {
        return sessionsFlow.map { it.sortedByDescending { session -> session.startedAt } }
    }

    override suspend fun insertSession(session: FocusSessionEntity): Long {
        val newId = (sessions.size + 1).toLong()
        val newSession = session.copy(id = newId)
        sessions.add(newSession)
        sessionsFlow.value = sessions.toList()
        return newId
    }

    override suspend fun updateSession(session: FocusSessionEntity) {
        val index = sessions.indexOfFirst { it.id == session.id }
        if (index != -1) {
            sessions[index] = session
            sessionsFlow.value = sessions.toList()
        }
    }

    override fun getActiveSession(): Flow<FocusSessionEntity?> {
        return sessionsFlow.map { it.find { session -> session.endedAt == null } }
    }
}
