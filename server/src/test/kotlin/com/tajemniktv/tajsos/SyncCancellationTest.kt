package com.tajemniktv.tajsos

import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.request.ApplicationReceivePipeline
import io.ktor.server.application.call
import io.ktor.server.request.uri
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlinx.coroutines.CancellationException
import io.ktor.server.application.install
import io.ktor.server.routing.routing
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.bearer
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import com.tajemniktv.tajsos.routes.syncRoutes
import io.ktor.client.statement.bodyAsText

class SyncCancellationTest {
    @Test
    fun testSyncEndpoint_CancellationExceptionRethrown() = testApplication {
        application {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            install(Authentication) {
                bearer("sync-auth") {
                    authenticate { UserIdPrincipal("sync-client") }
                }
            }
            routing {
                syncRoutes()
            }

            receivePipeline.intercept(ApplicationReceivePipeline.Transform) {
                if (call.request.uri == "/sync") {
                    throw CancellationException("Simulated error in injected service/call pipeline")
                }
            }
        }

        try {
            val response = client.post("/sync") {
                header(io.ktor.http.HttpHeaders.Authorization, "Bearer token")
                contentType(ContentType.Application.Json)
                setBody("{\"lastSyncTime\": 0, \"items\": []}")
            }

            if (response.status == HttpStatusCode.BadRequest) {
                fail("Received 400 Bad Request, CancellationException was swallowed by catch (e: Exception) block! Body: ${response.bodyAsText()}")
            }
            fail("Expected CancellationException to be thrown, but received response: ${response.status}")
        } catch (e: CancellationException) {
            assertTrue(true, "Successfully caught CancellationException")
        } catch (e: Exception) {
            assertTrue(e.cause is CancellationException || e is CancellationException, "Successfully threw cancellation exception")
        }
    }
}
