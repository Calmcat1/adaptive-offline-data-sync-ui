package com.example.adaptive_offline_sync_data_ui.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "data_records")
data class DataRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val location: String,
    val recordDate: Long = System.currentTimeMillis(),
    val recordTime: String,
    val notes: String = "",
    val isSynced: Boolean = false
)


