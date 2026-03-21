package com.tajemniktv.tajsos.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeRelationDao : RelationDao {
    private val relations = mutableListOf<RelationEntity>()
    private val relationsFlow = MutableStateFlow<List<RelationEntity>>(emptyList())

    override fun getRelationsForNode(nodeId: Long): Flow<List<RelationEntity>> {
        return relationsFlow.map { it.filter { relation -> relation.fromNodeId == nodeId || relation.toNodeId == nodeId } }
    }

    override suspend fun insertRelation(relation: RelationEntity) {
        val newId = (relations.size + 1).toLong()
        val newRelation = relation.copy(id = newId)
        relations.add(newRelation)
        relationsFlow.value = relations.toList()
    }

    override suspend fun deleteRelation(relation: RelationEntity) {
        relations.removeIf { it.id == relation.id }
        relationsFlow.value = relations.toList()
    }
}
