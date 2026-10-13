/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */
package com.tajemniktv.tajos

import com.tajemniktv.tajos.dto.SyncResponse
import com.tajemniktv.tajos.routes.syncRoutes
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.bearer
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.ApplicationReceivePipeline
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class SyncCancellationTest {
    @Test
    fun receiveCancellationEscapesRouteWithoutAcknowledgingOrStoringTheItem() = testApplication {
        val cancellation = CancellationException("Injected sync receive cancellation")
        var receiveReached = false
        var escapedRoute: Throwable? = null
        application {
            install(ContentNegotiation) { json() }
            install(Authentication) {
                bearer("sync-auth") { authenticate { UserIdPrincipal("test-client") } }
            }
            intercept(ApplicationCallPipeline.Monitoring) {
                try {
                    proceed()
                } catch (failure: Throwable) {
                    escapedRoute = failure
                    throw failure
                }
            }
            receivePipeline.intercept(ApplicationReceivePipeline.Before) {
                if (call.request.headers["X-Test-Cancel-Receive"] == "true") {
                    receiveReached = true
                    throw cancellation
                }
            }
            routing { syncRoutes() }
        }
        val cancelledId = "receive-cancelled-item"
        val response = client.post("/sync") {
            header(HttpHeaders.Authorization, "Bearer test-token")
            header("X-Test-Cancel-Receive", "true")
            contentType(ContentType.Application.Json)
            setBody("""{"lastSyncTime":0,"items":[{"id":"$cancelledId","entityType":"task","payload":"{}","updatedAt":100}]}""")
        }
        assertTrue(receiveReached, "Authentication and content-type checks must reach receive")
        assertSame(cancellation, escapedRoute, "The production route must propagate the exact cancellation")
        // Ktor's test engine translates unhandled server failures to 500, not client-side cancellation.
        assertEquals(HttpStatusCode.InternalServerError, response.status)
        assertFalse(response.bodyAsText().contains("ackedItemIds"))
        val subsequent = client.post("/sync") {
            header(HttpHeaders.Authorization, "Bearer test-token")
            contentType(ContentType.Application.Json)
            setBody("""{"lastSyncTime":0,"items":[]}""")
        }
        assertEquals(HttpStatusCode.OK, subsequent.status)
        val snapshot = Json.decodeFromString<SyncResponse>(subsequent.bodyAsText())
        assertTrue(snapshot.ackedItemIds.isEmpty())
        assertTrue(snapshot.items.none { it.id == cancelledId })
    }
}
