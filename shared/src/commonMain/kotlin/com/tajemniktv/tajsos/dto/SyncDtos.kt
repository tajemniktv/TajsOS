/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.dto

import kotlinx.serialization.Serializable

@Serializable
data class SyncRequest(
    val protocolVersion: Int = 1,
    val clientId: String? = null,
    val lastSyncTime: Long,
    val items: List<SyncItem>,
    val enabledPacks: Set<String> = emptySet(),
)

@Serializable
data class SyncItem(
    val id: String,
    val entityType: String,
    val payload: String,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val operation: String = "upsert",
    val payloadVersion: Int = 1,
)

@Serializable
data class SyncResponse(
    val protocolVersion: Int = 1,
    val serverTime: Long,
    val items: List<SyncItem>,
    val conflicts: List<String> = emptyList(),
    val ackedItemIds: List<String> = emptyList(),
)

@Serializable
data class HealthResponse(
    val status: String,
    val version: String,
    val uptime: Long,
)

@Serializable
data class ErrorResponse(
    val error: String,
    val details: String? = null,
)
