package com.example.adaptive_offline_sync_data_ui.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "data_records")
data class DataRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clientRecordId: String = UUID.randomUUID().toString(),
    val userEmail: String = "",
    val location: String,
    val recordDate: Long = System.currentTimeMillis(),
    val recordTime: String,
    val updatedAt: Long = System.currentTimeMillis(),
    val notes: String = "",
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false
)


