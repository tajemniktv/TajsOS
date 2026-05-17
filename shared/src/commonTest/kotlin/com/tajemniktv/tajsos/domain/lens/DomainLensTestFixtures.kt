package com.tajemniktv.tajsos.domain.lens

import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.TagEntity

internal fun createTestNode(
    id: Long,
    title: String,
    content: String = "",
    type: String = "task",
    status: String = "active",
    tags: List<String> = emptyList(),
    maintenanceType: String? = null,
    noteType: String? = null,
    dueAt: Long? = null,
    updatedAt: Long = 0L,
): NodeWithPin =
    NodeWithPin(
        node =
            NodeEntity(
                id = id,
                title = title,
                content = content,
                type = type,
                status = status,
                maintenanceType = maintenanceType,
                noteType = noteType,
                dueAt = dueAt,
                updatedAt = updatedAt,
            ),
        pin = null,
        tags = tags.mapIndexed { index, tag -> TagEntity(id = index.toLong(), name = tag, normalizedName = tag.lowercase()) },
    )
