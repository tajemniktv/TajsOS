/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos

import io.ktor.client.request.header
import com.tajemniktv.tajsos.dto.HealthResponse
import com.tajemniktv.tajsos.dto.SyncItem
import com.tajemniktv.tajsos.dto.SyncRequest
import com.tajemniktv.tajsos.dto.SyncResponse
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.TestResult
import kotlinx.serialization.json.Json

@Suppress("unused")
class ApplicationTest {
    @Test
    fun testSyncEndpoint_Unauthorized(): TestResult = testApplication {
        environment {
            config = io.ktor.server.config.MapApplicationConfig(
                "TAJSOS_SYNC_TOKEN" to "default-dev-token",
                "ktor.application.version" to "1.0.0"
            )
        }
        application {
            module()
        }
        val response =
            client.post("/sync") {
                setBody("{\"lastSyncTime\": 0, \"items\": []}")
            }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun testRoot(): TestResult = testApplication {
        environment {
            config = io.ktor.server.config.MapApplicationConfig(
                "TAJSOS_SYNC_TOKEN" to "default-dev-token",
                "ktor.application.version" to "1.0.0"
            )
        }
        application {
            module()
        }
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("Ktor: Hello, Java"))
    }

    @Test
    fun testHealthEndpoint(): TestResult = testApplication {
        environment {
            config = io.ktor.server.config.MapApplicationConfig(
                "TAJSOS_SYNC_TOKEN" to "default-dev-token",
                "ktor.application.version" to "1.2.3"
            )
        }
        application {
            module()
        }
        val clientWithNegotiation =
            createClient {
                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                        }
                    )
                }
            }

        val response = clientWithNegotiation.get("/health")
        assertEquals(HttpStatusCode.OK, response.status)

        val health = response.body<HealthResponse>()
        assertEquals("OK", health.status)
        assertEquals("1.2.3", health.version)
        assertTrue(health.uptime >= 0)
    }

    @Test
    fun testSyncEndpoint_HappyPath(): TestResult = testApplication {
        environment {
            config = io.ktor.server.config.MapApplicationConfig(
                "TAJSOS_SYNC_TOKEN" to "default-dev-token",
                "ktor.application.version" to "1.0.0"
            )
        }
        application {
            module()
        }
        val clientWithNegotiation =
            createClient {
                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                        }
                    )
                }
            }

        val requestPayload =
            SyncRequest(
                protocolVersion = 2,
                lastSyncTime = 1234567890L,
                items =
                    listOf(
                        SyncItem(
                            id = "node-1",
                            entityType = "task",
                            payload = "{\"title\":\"Buy milk\"}",
                            updatedAt = 1234567890L
                        )
                    )
            )

        val response =
            clientWithNegotiation.post("/sync") {
                header(io.ktor.http.HttpHeaders.Authorization, "Bearer default-dev-token")

                contentType(ContentType.Application.Json)
                setBody(requestPayload)
            }

        assertEquals(HttpStatusCode.OK, response.status)

        val syncResponse = response.body<SyncResponse>()
        assertEquals(2, syncResponse.protocolVersion)
        assertTrue(syncResponse.serverTime > 0)
        assertTrue(syncResponse.items.isEmpty()) // Empty for dummy implementation
        assertTrue(syncResponse.conflicts.isEmpty())
        assertEquals(listOf("node-1"), syncResponse.ackedItemIds)
    }

    @Test
    fun testSyncEndpoint_BadPayload(): TestResult = testApplication {
        environment {
            config = io.ktor.server.config.MapApplicationConfig(
                "TAJSOS_SYNC_TOKEN" to "default-dev-token",
                "ktor.application.version" to "1.0.0"
            )
        }
        application {
            module()
        }
        val response =
            client.post("/sync") {
                header(io.ktor.http.HttpHeaders.Authorization, "Bearer default-dev-token")

                contentType(ContentType.Application.Json)
                setBody("{\"malformed\": \"json\"}")
            }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val bodyText = response.bodyAsText()
        assertTrue(bodyText.contains("Invalid sync request payload"))
    }

    @Test
    fun testSyncEndpoint_returnsDeltaAndConflict(): TestResult = testApplication {
        environment {
            config = io.ktor.server.config.MapApplicationConfig(
                "TAJSOS_SYNC_TOKEN" to "default-dev-token",
                "ktor.application.version" to "1.0.0"
            )
        }
        application {
            module()
        }
        val clientWithNegotiation =
            createClient {
                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                        }
                    )
                }
            }

        val firstRequest =
            SyncRequest(
                protocolVersion = 2,
                lastSyncTime = 0L,
                items =
                    listOf(
                        SyncItem(
                            id = "node-sync-1",
                            entityType = "task",
                            payload = "{\"title\":\"v1\"}",
                            updatedAt = 2_000L
                        )
                    )
            )

        val firstResponse =
            clientWithNegotiation.post("/sync") {
                header(io.ktor.http.HttpHeaders.Authorization, "Bearer default-dev-token")

                contentType(ContentType.Application.Json)
                setBody(firstRequest)
            }
        assertEquals(HttpStatusCode.OK, firstResponse.status)

        val staleUpdate =
            SyncRequest(
                protocolVersion = 2,
                lastSyncTime = 0L,
                items =
                    listOf(
                        SyncItem(
                            id = "node-sync-1",
                            entityType = "task",
                            payload = "{\"title\":\"stale\"}",
                            updatedAt = 1_000L
                        )
                    )
            )

        val secondResponse =
            clientWithNegotiation.post("/sync") {
                header(io.ktor.http.HttpHeaders.Authorization, "Bearer default-dev-token")

                contentType(ContentType.Application.Json)
                setBody(staleUpdate)
            }
        assertEquals(HttpStatusCode.OK, secondResponse.status)
        val secondBody = secondResponse.body<SyncResponse>()
        assertTrue(secondBody.conflicts.contains("node-sync-1"))
        assertFalse(secondBody.ackedItemIds.contains("node-sync-1"))
        assertTrue(secondBody.items.any { it.id == "node-sync-1" && it.updatedAt == 2_000L })
    }

    @Test
    fun testSyncEndpoint_MissingContentType(): TestResult = testApplication {
        environment {
            config = io.ktor.server.config.MapApplicationConfig(
                "TAJSOS_SYNC_TOKEN" to "default-dev-token",
                "ktor.application.version" to "1.0.0"
            )
        }
        application {
            module()
        }
        val response =
            client.post("/sync") {
                header(io.ktor.http.HttpHeaders.Authorization, "Bearer default-dev-token")

                setBody("{\"lastSyncTime\": 0, \"items\": []}")
            }

        assertEquals(HttpStatusCode.UnsupportedMediaType, response.status)
    }
}
