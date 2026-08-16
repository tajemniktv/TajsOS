package com.tajemniktv.tajsos.dto

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull

class SyncDtosTest {
    @Test
    fun testSyncRequestDefaults() {
        val request = SyncRequest(
            lastSyncTime = 12345L,
            items = emptyList()
        )
        assertEquals(1, request.protocolVersion)
        assertNull(request.clientId)
        assertEquals(12345L, request.lastSyncTime)
        assertTrue(request.items.isEmpty())
        assertTrue(request.enabledPacks.isEmpty())
    }

    @Test
    fun testSyncItemDefaults() {
        val item = SyncItem(
            id = "item-1",
            entityType = "NodeEntity",
            payload = "{}",
            updatedAt = 1000L
        )
        assertEquals("item-1", item.id)
        assertEquals("NodeEntity", item.entityType)
        assertEquals("{}", item.payload)
        assertEquals(1000L, item.updatedAt)
        assertFalse(item.isDeleted)
        assertEquals("upsert", item.operation)
        assertEquals(1, item.payloadVersion)
    }

    @Test
    fun testSyncResponseDefaults() {
        val response = SyncResponse(
            serverTime = 54321L,
            items = emptyList()
        )
        assertEquals(1, response.protocolVersion)
        assertEquals(54321L, response.serverTime)
        assertTrue(response.items.isEmpty())
        assertTrue(response.conflicts.isEmpty())
        assertTrue(response.ackedItemIds.isEmpty())
    }

    @Test
    fun testHealthResponse() {
        val health = HealthResponse(
            status = "ok",
            version = "1.0.0",
            uptime = 9999L
        )
        assertEquals("ok", health.status)
        assertEquals("1.0.0", health.version)
        assertEquals(9999L, health.uptime)
    }

    @Test
    fun testErrorResponse() {
        val error = ErrorResponse(
            error = "Not Found",
            details = "Item not found"
        )
        assertEquals("Not Found", error.error)
        assertEquals("Item not found", error.details)

        val errorNoDetails = ErrorResponse(
            error = "Bad Request"
        )
        assertEquals("Bad Request", errorNoDetails.error)
        assertNull(errorNoDetails.details)
    }
}
