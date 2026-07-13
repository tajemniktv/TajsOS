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
import io.ktor.server.auth.*

import kotlinx.coroutines.CancellationException

private val syncStore = LinkedHashMap<String, SyncItem>()
private val syncStoreLock = Any()

private suspend fun io.ktor.server.application.ApplicationCall.handleSyncError(e: Throwable) {
    application.environment.log.error("Failed to process sync request", e)
    respond(
        HttpStatusCode.BadRequest,
        ErrorResponse(
            error = "Invalid sync request payload",
            details = "The request could not be processed due to a malformed payload",
        ),
    )
}

fun Route.syncRoutes() {
    authenticate("sync-auth") {
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
                } catch (e: io.ktor.server.plugins.BadRequestException) {
                    call.handleSyncError(e)
                } catch (e: kotlinx.serialization.SerializationException) {
                    call.handleSyncError(e)
                }
            }
        }
    }
}
