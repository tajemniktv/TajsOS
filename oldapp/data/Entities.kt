/*
 * Copyright (c) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Embedded
import androidx.room.Relation

/**
 * ItemEntity represents the core "unit of capture" in TajsOS.
 * It can be a task, a note, or a stray idea.
 */
@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // task, note, idea
    val title: String,
    val body: String = "",
    val status: String = "active", // active, done, archived
    val isPinned: Boolean = false,
    val projectId: Long? = null,
    val areaId: Long? = null,
    val linkedItemId: Long? = null,
    val reminderAt: Long? = null, // Epoch ms
    val isRecurring: Boolean = false,
    val recurringInterval: String? = null, // daily, weekly, monthly
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * ProjectEntity represents a multi-step effort with its own context.
 */
@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val areaId: Long? = null,
    val status: String = "active", // active, completed, archived
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * AreaEntity represents a stable life domain (e.g., Health, Work, Personal).
 */
@Entity(tableName = "areas")
data class AreaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val color: Int? = null,
    val icon: String? = null
)

/**
 * TodayPinEntity represents an item that has been "shortlisted" for today.
 */
@Entity(tableName = "today_pins")
data class TodayPinEntity(
    @PrimaryKey val itemId: Long,
    val date: String, // YYYY-MM-DD
    val position: Int
)

/**
 * FocusSessionEntity logs the time spent on a specific task.
 */
@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemId: Long,
    val startAt: Long,
    val endAt: Long? = null,
    val durationSec: Int = 0,
    val mode: String = "pomodoro" // pomodoro, custom
)

/**
 * TrackEntryEntity handles daily micro check-ins.
 */
@Entity(tableName = "track_entries")
data class TrackEntryEntity(
    @PrimaryKey val date: String, // YYYY-MM-DD
    val mood: Int? = null,
    val energy: Int? = null,
    val focus: Int? = null,
    val sleep: Float? = null,
    val tookMeds: Boolean = false,
    val note: String = ""
)

/**
 * ItemWithPin is a "POJO" used by Room to perform a JOIN.
 */
data class ItemWithPin(
    @Embedded val item: ItemEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "itemId"
    )
    val pin: TodayPinEntity?
) {
    val isPinnedToToday: Boolean get() = pin != null
}
