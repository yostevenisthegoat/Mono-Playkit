package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_tools")
data class FavoriteToolEntity(
    @PrimaryKey val toolId: String,
    val sortOrder: Int,
    val addedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "history_records")
data class HistoryRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val toolId: String,
    val category: String,
    val title: String,
    val summary: String,
    val detail: String = "",
    val metricMs: Long? = null,
    val timestamp: Long = System.currentTimeMillis(),
)

@Entity(tableName = "checklist_projects")
data class ChecklistProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val toolType: String, // "LEGO" or "MODEL_KIT"
    val name: String,
    val setNumber: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "checklist_items")
data class ChecklistItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val title: String,
    val subtitle: String = "",
    val currentCount: Int = 0,
    val targetCount: Int = 1,
    val isDone: Boolean = false,
)

@Entity(tableName = "custom_timer_presets")
data class CustomTimerPresetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val timerType: String, // "COUNTDOWN" or "INTERVAL"
    val workSeconds: Int,
    val restSeconds: Int = 0,
    val rounds: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "saved_counters")
data class SavedCounterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val value: Int = 0,
    val step: Int = 1,
    val updatedAt: Long = System.currentTimeMillis(),
)
