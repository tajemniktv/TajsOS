package com.tajemniktv.tajsos.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeDecisionDao : DecisionDao {
    override fun getOptionsForDecision(nodeId: Long): Flow<List<DecisionOptionEntity>> = flowOf(emptyList())
    override suspend fun insertDecisionOption(option: DecisionOptionEntity): Long = 0
    override suspend fun updateDecisionOption(option: DecisionOptionEntity) {}
    override suspend fun deleteDecisionOption(option: DecisionOptionEntity) {}
}
