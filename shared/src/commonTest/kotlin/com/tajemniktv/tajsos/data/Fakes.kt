/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved. 
 */

package com.tajemniktv.tajsos.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeNodeSnapshotDao : NodeSnapshotDao {
    private val snapshots = mutableListOf<NodeSnapshotEntity>()
    override fun getSnapshotsForNode(nodeId: Long): Flow<List<NodeSnapshotEntity>> =
        MutableStateFlow(snapshots.filter { it.nodeId == nodeId })

    override suspend fun insertSnapshot(snapshot: NodeSnapshotEntity) {
        snapshots.add(snapshot)
    }

    override suspend fun deleteSnapshot(snapshot: NodeSnapshotEntity) {
        snapshots.remove(snapshot)
    }

    override suspend fun deleteSnapshotsForNode(nodeId: Long) {
        snapshots.removeAll { it.nodeId == nodeId }
    }
}

class FakeReviewDao : ReviewDao {
    private val reviews = mutableListOf<ReviewEntity>()
    override fun getAllReviews(): Flow<List<ReviewEntity>> = MutableStateFlow(reviews.toList())
    override suspend fun insertReview(review: ReviewEntity): Long {
        reviews.add(review)
        return reviews.size.toLong()
    }

    override suspend fun insertReviews(reviews: List<ReviewEntity>) {
        reviews.forEach { insertReview(it) }
    }

    override suspend fun getLastReviewByType(type: String): ReviewEntity? =
        reviews.filter { it.type == type }.maxByOrNull { it.completedAt }
}

class FakeCalendarProviderDao : CalendarProviderDao {
    override fun getAllProviders(): Flow<List<CalendarProviderEntity>> =
        MutableStateFlow(emptyList())

    override suspend fun insertProvider(provider: CalendarProviderEntity): Long = 0

    override suspend fun insertProviders(providers: List<CalendarProviderEntity>) {
        providers.forEach { insertProvider(it) }
    }
    override suspend fun updateProvider(provider: CalendarProviderEntity) {}
    override suspend fun deleteProvider(provider: CalendarProviderEntity) {}
    override suspend fun getProviderById(id: Long): CalendarProviderEntity? = null
}

class FakeCalendarEventDao : CalendarEventDao {
    override fun getEventsInRange(from: Long, to: Long): Flow<List<CalendarEventEntity>> =
        MutableStateFlow(emptyList())

    override suspend fun insertEvents(events: List<CalendarEventEntity>) {}
    override suspend fun deleteEventsByProvider(providerId: Long) {}
    override suspend fun insertEvent(event: CalendarEventEntity): Long = 0
    override suspend fun updateEvent(event: CalendarEventEntity) {}
    override suspend fun deleteEvent(event: CalendarEventEntity) {}
}
