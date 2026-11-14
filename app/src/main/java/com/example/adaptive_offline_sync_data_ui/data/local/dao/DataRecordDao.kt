package com.example.adaptive_offline_sync_data_ui.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.adaptive_offline_sync_data_ui.data.entity.DataRecordEntity

@Dao
interface DataRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: DataRecordEntity): Long

    @Query("SELECT * FROM data_records ORDER BY recordDate DESC")
    suspend fun getAllRecords(): List<DataRecordEntity>

    @Query("SELECT * FROM data_records WHERE isSynced = 0")
    suspend fun getUnsyncedRecords(): List<DataRecordEntity>

    @Query("DELETE FROM data_records")
    suspend fun deleteAll()
}