/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos

import com.tajemniktv.tajsos.dto.HealthResponse
import com.tajemniktv.tajsos.dto.SyncItem
import com.tajemniktv.tajsos.dto.SyncRequest
import com.tajemniktv.tajsos.dto.SyncResponse
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.coroutines.test.TestResult
import kotlinx.serialization.json.Json
import kotlin.test.*

class ApplicationTest {
    @Test
    fun testRoot(): TestResult =
        testApplication {
            application {
                module()
            }
            val response = client.get("/")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("Ktor: ${Greeting().greet()}", response.bodyAsText())
        }

    @Test
    fun testHealthEndpoint(): TestResult =
        testApplication {
            application {
                module()
            }
            val clientWithNegotiation =
                createClient {
                    install(ContentNegotiation) {
                        json(
                            Json {
                                ignoreUnknownKeys = true
                            },
                        )
                    }
                }

            val response = clientWithNegotiation.get("/health")
            assertEquals(HttpStatusCode.OK, response.status)

            val health = response.body<HealthResponse>()
            assertEquals("OK", health.status)
            assertEquals("1.0.0", health.version)
            assertTrue(health.uptime >= 0)
        }

    @Test
    fun testSyncEndpoint_HappyPath(): TestResult =
        testApplication {
            application {
                module()
            }
            val clientWithNegotiation =
                createClient {
                    install(ContentNegotiation) {
                        json(
                            Json {
                                ignoreUnknownKeys = true
                            },
                        )
                    }
                }

            val requestPayload =
                SyncRequest(
                    lastSyncTime = 1234567890L,
                    items =
                        listOf(
                            SyncItem(
                                id = "node-1",
                                entityType = "task",
                                payload = "{\"title\":\"Buy milk\"}",
                                updatedAt = 1234567890L,
                            ),
                        ),
                )

            val response =
                clientWithNegotiation.post("/sync") {
                    contentType(ContentType.Application.Json)
                    setBody(requestPayload)
                }

            assertEquals(HttpStatusCode.OK, response.status)

            val syncResponse = response.body<SyncResponse>()
            assertTrue(syncResponse.serverTime > 0)
            assertTrue(syncResponse.items.isEmpty()) // Empty for dummy implementation
            assertTrue(syncResponse.conflicts.isEmpty())
        }

    @Test
    fun testSyncEndpoint_BadPayload(): TestResult =
        testApplication {
            application {
                module()
            }
            val response =
                client.post("/sync") {
                    contentType(ContentType.Application.Json)
                    setBody("{\"malformed\": \"json\"}")
                }

            assertEquals(HttpStatusCode.BadRequest, response.status)
            val bodyText = response.bodyAsText()
            assertTrue(bodyText.contains("Invalid sync request payload"))
        }

    @Test
    fun testSyncEndpoint_MissingContentType(): TestResult =
        testApplication {
            application {
                module()
            }
            val response =
                client.post("/sync") {
                    setBody("{\"lastSyncTime\": 0, \"items\": []}")
                }

            assertEquals(HttpStatusCode.UnsupportedMediaType, response.status)
        }
}
