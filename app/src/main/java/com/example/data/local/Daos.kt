package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorite_tools ORDER BY sortOrder ASC, addedAt ASC")
    fun observeFavorites(): Flow<List<FavoriteToolEntity>>

    @Query("SELECT * FROM favorite_tools ORDER BY sortOrder ASC")
    suspend fun getAllSync(): List<FavoriteToolEntity>

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM favorite_tools")
    suspend fun getMaxOrder(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: FavoriteToolEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<FavoriteToolEntity>)

    @Query("DELETE FROM favorite_tools WHERE toolId = :toolId")
    suspend fun deleteByToolId(toolId: String)

    @Query("DELETE FROM favorite_tools")
    suspend fun clearAll()
}

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history_records ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<HistoryRecordEntity>>

    @Query("SELECT * FROM history_records WHERE toolId = :toolId ORDER BY timestamp DESC")
    fun observeByTool(toolId: String): Flow<List<HistoryRecordEntity>>

    @Query("SELECT * FROM history_records ORDER BY timestamp DESC")
    suspend fun getAllSync(): List<HistoryRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: HistoryRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<HistoryRecordEntity>)

    @Query("DELETE FROM history_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM history_records WHERE toolId = :toolId")
    suspend fun deleteByToolId(toolId: String)

    @Query("DELETE FROM history_records")
    suspend fun clearAll()
}

@Dao
interface ChecklistDao {
    @Query("SELECT * FROM checklist_projects WHERE toolType = :toolType ORDER BY createdAt DESC")
    fun observeProjects(toolType: String): Flow<List<ChecklistProjectEntity>>

    @Query("SELECT * FROM checklist_projects ORDER BY createdAt DESC")
    suspend fun getAllProjectsSync(): List<ChecklistProjectEntity>

    @Query("SELECT * FROM checklist_items WHERE projectId = :projectId ORDER BY id ASC")
    fun observeItems(projectId: Long): Flow<List<ChecklistItemEntity>>

    @Query("SELECT * FROM checklist_items ORDER BY id ASC")
    suspend fun getAllItemsSync(): List<ChecklistItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ChecklistProjectEntity): Long

    @Update
    suspend fun updateProject(project: ChecklistProjectEntity)

    @Query("DELETE FROM checklist_projects WHERE id = :projectId")
    suspend fun deleteProject(projectId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ChecklistItemEntity): Long

    @Update
    suspend fun updateItem(item: ChecklistItemEntity)

    @Query("DELETE FROM checklist_items WHERE id = :itemId")
    suspend fun deleteItem(itemId: Long)

    @Query("DELETE FROM checklist_items WHERE projectId = :projectId")
    suspend fun deleteItemsByProject(projectId: Long)

    @Query("DELETE FROM checklist_items")
    suspend fun clearItems()

    @Query("DELETE FROM checklist_projects")
    suspend fun clearProjects()
}

@Dao
interface TimerPresetDao {
    @Query("SELECT * FROM custom_timer_presets ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<CustomTimerPresetEntity>>

    @Query("SELECT * FROM custom_timer_presets ORDER BY createdAt DESC")
    suspend fun getAllSync(): List<CustomTimerPresetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(preset: CustomTimerPresetEntity): Long

    @Query("DELETE FROM custom_timer_presets WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM custom_timer_presets")
    suspend fun clearAll()
}

@Dao
interface SavedCounterDao {
    @Query("SELECT * FROM saved_counters ORDER BY id ASC")
    fun observeAll(): Flow<List<SavedCounterEntity>>

    @Query("SELECT * FROM saved_counters ORDER BY id ASC")
    suspend fun getAllSync(): List<SavedCounterEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(counter: SavedCounterEntity): Long

    @Update
    suspend fun update(counter: SavedCounterEntity)

    @Query("DELETE FROM saved_counters WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM saved_counters")
    suspend fun clearAll()
}
