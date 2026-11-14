package com.example.adaptive_offline_sync_data_ui.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.adaptive_offline_sync_data_ui.data.entity.DataRecordEntity
import com.example.adaptive_offline_sync_data_ui.data.entity.TelemetryEntity
import com.example.adaptive_offline_sync_data_ui.data.local.dao.DataRecordDao
import com.example.adaptive_offline_sync_data_ui.data.local.dao.TelemetryDao

@Database(entities = [TelemetryEntity::class, DataRecordEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun telemetryDao(): TelemetryDao
    abstract fun DataRecordDao(): DataRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_telemetry.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}