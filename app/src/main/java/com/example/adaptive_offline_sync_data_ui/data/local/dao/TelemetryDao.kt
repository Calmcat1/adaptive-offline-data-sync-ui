package com.example.adaptive_offline_sync_data_ui.data.local.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.adaptive_offline_sync_data_ui.data.entity.TelemetryEntity

@Dao
interface TelemetryDao {
    @Insert
    suspend fun insert(telemetry: TelemetryEntity)
    @Query("SELECT * FROM network_telemetry ORDER BY timestamp DESC")
    suspend fun getAllTelemetry(): List<TelemetryEntity>
    @Query("DELETE FROM network_telemetry")
    suspend fun deleteAll()
}