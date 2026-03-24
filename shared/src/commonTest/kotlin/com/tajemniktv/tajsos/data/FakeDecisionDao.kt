package com.tajemniktv.tajsos.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeDecisionDao : DecisionDao {
    private val optionsFlow = MutableStateFlow<List<DecisionOptionEntity>>(emptyList())
    private var nextId = 1L

    override fun getOptionsForDecision(nodeId: Long): Flow<List<DecisionOptionEntity>> {
        return optionsFlow.map { list -> list.filter { it.decisionNodeId == nodeId } }
    }

    override suspend fun insertDecisionOption(option: DecisionOptionEntity): Long {
        val newId = nextId++
        val newOption = option.copy(id = newId)
        optionsFlow.value = optionsFlow.value + newOption
        return newId
    }

    override suspend fun updateDecisionOption(option: DecisionOptionEntity) {
        optionsFlow.value = optionsFlow.value.map {
            if (it.id == option.id) option else it
        }
    }

    override suspend fun deleteDecisionOption(option: DecisionOptionEntity) {
        optionsFlow.value = optionsFlow.value.filter { it.id != option.id }
    }
}
