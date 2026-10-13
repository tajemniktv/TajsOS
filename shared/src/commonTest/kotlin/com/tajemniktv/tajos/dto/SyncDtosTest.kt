/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */
package com.tajemniktv.tajos.dto

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SyncDtosTest {
    @Test
    fun legacyMinimalPayloadsDecodeWithProtocolAndTombstoneDefaults() {
        val request = Json.decodeFromString<SyncRequest>("""{"lastSyncTime":7,"items":[{"id":"n1","entityType":"node","payload":"{}","updatedAt":8}]}""")
        assertEquals(SyncRequest(lastSyncTime = 7, items = listOf(SyncItem("n1", "node", "{}", 8))), request)
        val response = Json.decodeFromString<SyncResponse>("""{"serverTime":9,"items":[]}""")
        assertEquals(SyncResponse(serverTime = 9, items = emptyList()), response)
    }

    @Test
    fun versionedTombstonesAndAcknowledgementsRoundTripIncludingEscapedPayload() {
        val item = SyncItem("n1", "node", """{"title":"quoted \"text\""}""", 8, isDeleted = true, operation = "delete", payloadVersion = 2)
        val request = SyncRequest(2, "client", 7, listOf(item), setOf("core", "student"))
        assertEquals(request, Json.decodeFromString<SyncRequest>(Json.encodeToString(request)))
        val response = SyncResponse(2, 9, listOf(item), listOf("conflict"), listOf("n1"))
        assertEquals(response, Json.decodeFromString<SyncResponse>(Json.encodeToString(response)))
    }

    @Test
    fun missingRequiredFieldsAreNotSilentlyDefaulted() {
        assertFailsWith<SerializationException> { Json.decodeFromString<SyncRequest>("""{"items":[]}""") }
        assertFailsWith<SerializationException> { Json.decodeFromString<SyncItem>("""{"id":"n1","entityType":"node","updatedAt":8}""") }
    }
}
