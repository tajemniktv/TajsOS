/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.dto

import kotlinx.serialization.Serializable

/**
 * Represents a request from a client to synchronize local data with the remote server.
 * Provides both the client's current state (via [lastSyncTime]) and any pending local mutations.
 *
 * @property protocolVersion The current synchronization protocol version (default is 1).
 * @property clientId An optional identifier for the syncing client.
 * @property lastSyncTime The epoch timestamp of the client's last successful sync. Used by the server to determine what items need to be sent downstream.
 * @property items The list of local mutations (inserts/updates/deletes) that the client wants to push to the server.
 * @property enabledPacks A set of pack identifiers that are active on the client. Used to determine feature-specific data access.
 */
@Serializable
data class SyncRequest(
    val protocolVersion: Int = 1,
    val clientId: String? = null,
    val lastSyncTime: Long,
    val items: List<SyncItem>,
    val enabledPacks: Set<String> = emptySet(),
)

/**
 * Represents an individual synchronization payload for a specific entity.
 * This wrapper abstracts away the specific table or domain model from the transport layer.
 *
 * @property id The unique identifier of the entity being synced.
 * @property entityType The string representation of the underlying model type (e.g., "NodeEntity", "RelationEntity").
 * @property payload The serialized representation of the entity.
 * @property updatedAt The epoch timestamp indicating when this mutation occurred.
 * @property isDeleted A flag indicating whether this item represents a deleted state (tombstone).
 * @property operation The type of operation being performed (e.g., "upsert", "delete").
 * @property payloadVersion The version of the payload schema. Used for future migration compatibility.
 */
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

/**
 * Represents the server's response to a [SyncRequest].
 * Informs the client of new or updated items from the server, any conflicts that were rejected, and confirms which client-pushed items were accepted.
 *
 * @property protocolVersion The current synchronization protocol version (default is 1).
 * @property serverTime The current epoch timestamp on the server. The client should store this and use it as the `lastSyncTime` in the next request.
 * @property items The list of items from the server that are newer than the client's `lastSyncTime`.
 * @property conflicts A list of item IDs that the server rejected due to conflict.
 * @property ackedItemIds A list of item IDs from the client's request that were successfully processed and applied by the server.
 */
@Serializable
data class SyncResponse(
    val protocolVersion: Int = 1,
    val serverTime: Long,
    val items: List<SyncItem>,
    val conflicts: List<String> = emptyList(),
    val ackedItemIds: List<String> = emptyList(),
)

/**
 * Represents the health status of the remote synchronization server.
 *
 * @property status A descriptive health status (e.g., "ok").
 * @property version The server's deployed application version.
 * @property uptime The number of milliseconds the server has been running.
 */
@Serializable
data class HealthResponse(
    val status: String,
    val version: String,
    val uptime: Long,
)

/**
 * Represents an error state encountered during a remote operation.
 *
 * @property error A summary or standardized code of the error.
 * @property details Optional technical details providing more context about the error.
 */
@Serializable
data class ErrorResponse(
    val error: String,
    val details: String? = null,
)
