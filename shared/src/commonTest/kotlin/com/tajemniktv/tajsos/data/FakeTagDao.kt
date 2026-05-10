package com.tajemniktv.tajsos.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeTagDao : TagDao {
    private val tags = mutableListOf<TagEntity>()
    private val nodeTags = mutableListOf<NodeTagEntity>()
    private val tagsFlow = MutableStateFlow<List<TagEntity>>(emptyList())
    private val nodeTagsFlow = MutableStateFlow<List<NodeTagEntity>>(emptyList())

    override fun getAllTags(): Flow<List<TagEntity>> = tagsFlow

    override suspend fun insertTag(tag: TagEntity): Long {
        val newId = (tags.size + 1).toLong()
        val newTag = tag.copy(id = newId)
        tags.add(newTag)
        tagsFlow.value = tags.toList()
        return newId
    }

    override suspend fun insertTags(tags: List<TagEntity>) {
        tags.forEach { insertTag(it) }
    }

    override fun getTagsForNode(nodeId: Long): Flow<List<TagEntity>> {
        return nodeTagsFlow.map { links ->
            val tagIdsForNode = links.filter { it.nodeId == nodeId }.map { it.tagId }
            tags.filter { it.id in tagIdsForNode }
        }
    }

    override suspend fun attachTagToNode(nodeTag: NodeTagEntity) {
        if (!nodeTags.any { it.nodeId == nodeTag.nodeId && it.tagId == nodeTag.tagId }) {
            nodeTags.add(nodeTag)
            nodeTagsFlow.value = nodeTags.toList()
        }
    }

    override suspend fun detachTagFromNode(nodeId: Long, tagId: Long) {
        nodeTags.removeAll { it.nodeId == nodeId && it.tagId == tagId }
        nodeTagsFlow.value = nodeTags.toList()
    }

    override suspend fun detachAllTagsFromNode(nodeId: Long) {
        nodeTags.removeAll { it.nodeId == nodeId }
        nodeTagsFlow.value = nodeTags.toList()
    }
}
