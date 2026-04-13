/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.routes

import com.tajemniktv.tajsos.dto.ErrorResponse
import com.tajemniktv.tajsos.dto.SyncItem
import com.tajemniktv.tajsos.dto.SyncRequest
import com.tajemniktv.tajsos.dto.SyncResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CancellationException

/**
 * In-memory development stub for storing synchronized items.
 *
 * NOTE: This is strictly for development and testing purposes.
 * It does not provide long-term persistence, clustering, or multi-user tenant separation.
 */
private val syncStore = LinkedHashMap<String, SyncItem>()

/**
 * Concurrency lock for safe access to the development in-memory [syncStore].
 */
private val syncStoreLock = Any()

/**
 * Configures the /sync endpoint for processing TajsOS synchronization payloads.
 *
 * NOTE: This endpoint currently relies on an internal in-memory store as a development stub.
 * It is not intended for long-term multi-user persistence in its current state.
 */
fun Route.syncRoutes() {
    route("/sync") {
        post {
            try {
                // Manually check content type but use match to handle charsets correctly
                val contentType = call.request.contentType()
                if (!contentType.match(ContentType.Application.Json)) {
                    call.respond(
                        HttpStatusCode.UnsupportedMediaType,
                        ErrorResponse(
                            error = "Unsupported Media Type",
                            details = "Content-Type must be application/json",
                        ),
                    )
                    return@post
                }

                val request = call.receive<SyncRequest>()

                val conflicts = mutableListOf<String>()
                val acknowledged = mutableListOf<String>()
                val outgoingItems: List<SyncItem>

                synchronized(syncStoreLock) {
                    request.items.forEach { incoming ->
                        val existing = syncStore[incoming.id]
                        if (existing == null || incoming.updatedAt >= existing.updatedAt) {
                            syncStore[incoming.id] = incoming
                            acknowledged += incoming.id
                        } else {
                            conflicts += incoming.id
                        }
                    }

                    outgoingItems =
                        syncStore.values
                            .filter { it.updatedAt > request.lastSyncTime }
                            .filterNot { serverItem ->
                                request.items.any { local ->
                                    local.id == serverItem.id && local.updatedAt == serverItem.updatedAt
                                }
                            }.sortedByDescending { it.updatedAt }
                }

                val response =
                    SyncResponse(
                        protocolVersion = request.protocolVersion,
                        serverTime = System.currentTimeMillis(),
                        items = outgoingItems,
                        conflicts = conflicts.distinct(),
                        ackedItemIds = acknowledged.distinct(),
                    )

                call.respond(HttpStatusCode.OK, response)
            } catch (e: CancellationException) {
                // Rethrow cancellation to let coroutines cancel gracefully
                throw e
            } catch (e: Exception) {
                // If it fails to deserialize or handle the request, return a 400 Bad Request
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(
                        error = "Invalid sync request payload",
                        details = "The request could not be processed due to a malformed payload",
                    ),
                )
            }
        }
    }
}
