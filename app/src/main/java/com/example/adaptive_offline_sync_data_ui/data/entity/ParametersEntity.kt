package com.example.adaptive_offline_sync_data_ui.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "network_telemetry")
data class TelemetryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val networkType: String,
    val signalStrength: Int,    // e.g., in dBm or signal level (0-4)
    val bandwidthKbps: Int,     // Downstream bandwidth estimate
    val latencyMs: Long,
    val batteryPercent: Int,
    val timestamp: Long = System.currentTimeMillis()
)


