package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        FavoriteToolEntity::class,
        HistoryRecordEntity::class,
        ChecklistProjectEntity::class,
        ChecklistItemEntity::class,
        CustomTimerPresetEntity::class,
        SavedCounterEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class MonoDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun historyDao(): HistoryDao
    abstract fun checklistDao(): ChecklistDao
    abstract fun timerPresetDao(): TimerPresetDao
    abstract fun savedCounterDao(): SavedCounterDao

    companion object {
        @Volatile
        private var instance: MonoDatabase? = null

        fun getInstance(context: Context): MonoDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    MonoDatabase::class.java,
                    "mono_playkit.db",
                ).build().also { instance = it }
            }
        }
    }
}
