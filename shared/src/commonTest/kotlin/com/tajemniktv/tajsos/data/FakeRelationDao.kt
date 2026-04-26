package com.tajemniktv.tajsos.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeRelationDao : RelationDao {
    private val relations = mutableListOf<RelationEntity>()
    private val relationsFlow = MutableStateFlow<List<RelationEntity>>(emptyList())

    override fun getRelationsForNode(nodeId: Long): Flow<List<RelationEntity>> {
        return relationsFlow.map { list -> list.filter { it.fromNodeId == nodeId || it.toNodeId == nodeId } }
    }

    override suspend fun insertRelation(relation: RelationEntity) {
        val index = relations.indexOfFirst { it.id == relation.id }
        if (index != -1 && relation.id != 0L) {
            relations[index] = relation
            relationsFlow.value = relations.toList()
            return
        }
        val newId = if (relation.id != 0L) relation.id else (relations.maxOfOrNull { it.id } ?: 0L) + 1L
        val newRelation = relation.copy(id = newId)
        relations.add(newRelation)
        relationsFlow.value = relations.toList()
    }

    override suspend fun insertRelations(relations: List<RelationEntity>) {
        relations.forEach { insertRelation(it) }
    }

    override suspend fun deleteRelation(relation: RelationEntity) {
        relations.removeAll { it.id == relation.id }
        relationsFlow.value = relations.toList()
    }

    override suspend fun deleteRelationsForNode(nodeId: Long) {
        relations.removeAll { it.fromNodeId == nodeId || it.toNodeId == nodeId }
        relationsFlow.value = relations.toList()
    }

    override suspend fun deleteBelongsToRelations(nodeId: Long) {
        relations.removeAll { it.fromNodeId == nodeId && it.relationType == "BELONGS_TO" }
        relationsFlow.value = relations.toList()
    }

    override suspend fun deleteBelongsToRelations(nodeIds: List<Long>) {
        relations.removeAll { it.fromNodeId in nodeIds && it.relationType == "BELONGS_TO" }
        relationsFlow.value = relations.toList()
    }

    override suspend fun getBelongsToRelations(nodeId: Long): List<RelationEntity> {
        return relations.filter { it.fromNodeId == nodeId && it.relationType == "BELONGS_TO" }
    }

    override suspend fun anyRelationExists(
        from: Long,
        to: Long,
        relationType: String,
    ): Boolean {
        return relations.any {
            it.fromNodeId == from &&
                it.toNodeId == to &&
                it.relationType == relationType
        }
    }

    override fun getAllRelations(): Flow<List<RelationEntity>> {
        return relationsFlow
    }
}
