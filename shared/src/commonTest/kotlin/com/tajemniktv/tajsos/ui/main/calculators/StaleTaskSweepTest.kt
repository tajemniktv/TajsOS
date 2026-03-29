/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.main.calculators

import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.TaskState
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.test.Test
import kotlin.test.assertEquals

class StaleTaskSweepTest {

    @Test
    fun `calculateStaleTasks excludes non-task nodes`() {
        val now = Clock.System.now()
        val staleTime = (now - 4.days).toEpochMilliseconds()
        
        val node = NodeEntity(title = "test", type = "note", status = "active", dueAt = staleTime)
        
        val result = calculateStaleTasks(listOf(node), now, 3)
        assertEquals(0, result.size)
    }

    @Test
    fun `calculateStaleTasks excludes archived or done tasks`() {
        val now = Clock.System.now()
        val staleTime = (now - 4.days).toEpochMilliseconds()
        
        val doneTask = NodeEntity(title = "test", type = "task", status = "done", dueAt = staleTime)
        val archivedTask = NodeEntity(title = "test", type = "task", status = "archived", dueAt = staleTime)
        
        val result = calculateStaleTasks(listOf(doneTask, archivedTask), now, 3)
        assertEquals(0, result.size)
    }

    @Test
    fun `calculateStaleTasks excludes someday tasks`() {
        val now = Clock.System.now()
        val staleTime = (now - 4.days).toEpochMilliseconds()
        
        val somedayTask = NodeEntity(title = "test", type = "task", status = TaskState.SOMEDAY.storageKey, dueAt = staleTime)
        
        val result = calculateStaleTasks(listOf(somedayTask), now, 3)
        assertEquals(0, result.size)
    }

    @Test
    fun `calculateStaleTasks excludes recurring tasks`() {
        val now = Clock.System.now()
        val staleTime = (now - 4.days).toEpochMilliseconds()
        
        val recurringTask1 = NodeEntity(title = "test", type = "task", status = "active", dueAt = staleTime, isRecurring = true)
        val recurringTask2 = NodeEntity(title = "test", type = "task", status = "active", dueAt = staleTime, recurringInterval = "DAILY")
        
        val result = calculateStaleTasks(listOf(recurringTask1, recurringTask2), now, 3)
        assertEquals(0, result.size)
    }

    @Test
    fun `calculateStaleTasks excludes pinned tasks`() {
        val now = Clock.System.now()
        val staleTime = (now - 4.days).toEpochMilliseconds()
        
        val pinnedTask = NodeEntity(title = "test", type = "task", status = "active", dueAt = staleTime, isPinned = true)
        
        val result = calculateStaleTasks(listOf(pinnedTask), now, 3)
        assertEquals(0, result.size)
    }

    @Test
    fun `calculateStaleTasks excludes tasks without due date`() {
        val now = Clock.System.now()
        
        val noDueTask = NodeEntity(title = "test", type = "task", status = "active", dueAt = null)
        
        val result = calculateStaleTasks(listOf(noDueTask), now, 3)
        assertEquals(0, result.size)
    }

    @Test
    fun `calculateStaleTasks respects cutoff boundary`() {
        val now = Clock.System.now()
        val exactly3DaysAgo = (now - 3.days).toEpochMilliseconds()
        val justUnder3DaysAgo = exactly3DaysAgo + 1000 // 1 second newer
        val justOver3DaysAgo = exactly3DaysAgo - 1000  // 1 second older
        
        val taskNewer = NodeEntity(title = "test", type = "task", status = "active", dueAt = justUnder3DaysAgo)
        val taskExactly = NodeEntity(title = "test", type = "task", status = "active", dueAt = exactly3DaysAgo)
        val taskOlder = NodeEntity(title = "test", type = "task", status = "active", dueAt = justOver3DaysAgo)
        
        val result = calculateStaleTasks(listOf(taskNewer, taskExactly, taskOlder), now, 3)
        
        assertEquals(1, result.size)
        assertEquals(taskOlder, result[0])
    }

    @Test
    fun `calculateStaleTasks includes valid stale tasks`() {
        val now = Clock.System.now()
        val staleTime = (now - 5.days).toEpochMilliseconds()
        
        val validTask = NodeEntity(title = "test", type = "task", status = "active", dueAt = staleTime)
        
        val result = calculateStaleTasks(listOf(validTask), now, 3)
        assertEquals(1, result.size)
        assertEquals(validTask, result[0])
    }
}
