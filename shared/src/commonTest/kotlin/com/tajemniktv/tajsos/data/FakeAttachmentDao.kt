package com.tajemniktv.tajsos.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeAttachmentDao : AttachmentDao {
    private val attachments = mutableListOf<AttachmentEntity>()
    private val attachmentsFlow = MutableStateFlow<List<AttachmentEntity>>(emptyList())

    override fun getAttachmentsForNode(nodeId: Long): Flow<List<AttachmentEntity>> {
        return attachmentsFlow.map { it.filter { attachment -> attachment.nodeId == nodeId } }
    }

    override suspend fun insertAttachment(attachment: AttachmentEntity) {
        val newId = (attachments.size + 1).toLong()
        val newAttachment = attachment.copy(id = newId)
        attachments.add(newAttachment)
        attachmentsFlow.value = attachments.toList()
    }

    override suspend fun deleteAttachment(attachment: AttachmentEntity) {
        attachments.removeIf { it.id == attachment.id }
        attachmentsFlow.value = attachments.toList()
    }
}
