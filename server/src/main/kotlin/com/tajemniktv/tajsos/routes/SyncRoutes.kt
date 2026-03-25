package com.tajemniktv.tajsos.routes

import com.tajemniktv.tajsos.dto.ErrorResponse
import com.tajemniktv.tajsos.dto.SyncRequest
import com.tajemniktv.tajsos.dto.SyncResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CancellationException

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
                            details = "Content-Type must be application/json"
                        )
                    )
                    return@post
                }

                val request = call.receive<SyncRequest>()

                // For now, this is just a dummy implementation to establish the contract.
                // In a real app, this would process the items, find conflicts, and return server items.

                val response = SyncResponse(
                    serverTime = System.currentTimeMillis(),
                    items = emptyList(), // Return empty list for now
                    conflicts = emptyList()
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
                        details = "The request could not be processed due to a malformed payload"
                    )
                )
            }
        }
    }
}
