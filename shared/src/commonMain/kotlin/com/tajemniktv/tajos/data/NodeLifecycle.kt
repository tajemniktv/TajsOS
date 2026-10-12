/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.data

/** Repeated callbacks cannot create another occurrence; archived nodes must explicitly reopen first. */
internal fun NodeEntity.canTransitionTo(next: TaskState): Boolean = when {
    status == next.storageKey -> false
    status == TaskState.ARCHIVED.storageKey -> next != TaskState.DONE
    else -> true
}

/** Changes only lifecycle metadata; archiving preserves completion history and open states clear it. */
internal fun NodeEntity.withLifecycleState(next: TaskState, timestamp: Long): NodeEntity = copy(
    status = next.storageKey,
    updatedAt = timestamp,
    completedAt = when (next) {
        TaskState.DONE -> timestamp
        TaskState.ARCHIVED -> completedAt
        else -> null
    },
    archivedAt = if (next == TaskState.ARCHIVED) timestamp else null,
)

/** Builds a successor only for a new completion with the existing fixed-duration recurrence policy. */
internal fun NodeEntity.recurringSuccessor(next: TaskState, timestamp: Long): NodeEntity? {
    if (next != TaskState.DONE) return null
    if (!isRecurring) return null
    val interval = recurringInterval ?: return null
    return copy(
        id = 0,
        status = TaskState.ACTIVE.storageKey,
        createdAt = timestamp,
        updatedAt = timestamp,
        completedAt = null,
        archivedAt = null,
        dueAt = nextRecurringDueAt(dueAt ?: timestamp, interval),
        inboxState = false,
    )
}
