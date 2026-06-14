package com.tajemniktv.tajsos.ui

import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.TagEntity

internal fun buildTestNode(
    id: Long,
    title: String,
    content: String = "",
    type: String = "task",
    status: String = "active",
    friction: String? = null,
    tags: List<String> = emptyList(),
    updatedAt: Long = 0L,
    dueAt: Long? = null,
): NodeWithPin =
    NodeWithPin(
        node =
            NodeEntity(
                id = id,
                title = title,
                content = content,
                type = type,
                status = status,
                friction = friction,
                updatedAt = updatedAt,
                dueAt = dueAt,
            ),
        pin = null,
        tags =
            tags.mapIndexed { index, tag ->
                TagEntity(id = index.toLong(), name = tag, normalizedName = tag.lowercase())
            },
    )
