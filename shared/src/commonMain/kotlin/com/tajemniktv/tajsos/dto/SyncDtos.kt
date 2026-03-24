package com.tajemniktv.tajsos.dto

import kotlinx.serialization.Serializable

@Serializable
data class SyncRequest(
    val lastSyncTime: Long,
    val items: List<SyncItem>
)

@Serializable
data class SyncItem(
    val id: String,
    val entityType: String,
    val payload: String,
    val updatedAt: Long,
    val isDeleted: Boolean = false
)

@Serializable
data class SyncResponse(
    val serverTime: Long,
    val items: List<SyncItem>,
    val conflicts: List<String> = emptyList()
)

@Serializable
data class HealthResponse(
    val status: String,
    val version: String,
    val uptime: Long
)

@Serializable
data class ErrorResponse(
    val error: String,
    val details: String? = null
)
