package com.example.adaptive_offline_sync_data_ui.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.adaptive_offline_sync_data_ui.data.entity.DataRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DataRecordDao {

    @Query("SELECT * FROM data_records WHERE userEmail = :email AND isDeleted = 0 ORDER BY recordDate DESC")
    fun getRecordsForUser(email: String): Flow<List<DataRecordEntity>>

    @Query("SELECT * FROM data_records WHERE isSynced = 0 ORDER BY updatedAt ASC LIMIT :limit")
    suspend fun getUnsyncedRecords(limit: Int): List<DataRecordEntity>

    @Query("SELECT COUNT(*) FROM data_records WHERE isSynced = 0")
    suspend fun getUnsyncedCount(): Int
    @Query("SELECT * FROM data_records WHERE clientRecordId = :clientRecordId LIMIT 1")
    suspend fun getByClientRecordId(clientRecordId: String): DataRecordEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: DataRecordEntity): Long
    @Update
    suspend fun updateRecord(record: DataRecordEntity)
    @Query("UPDATE data_records SET isSynced = 1 WHERE clientRecordId IN (:clientRecordIds)")
    suspend fun markRecordsAsSynced(clientRecordIds: List<String>)
    @Query("DELETE FROM data_records WHERE clientRecordId IN (:clientRecordIds)")
    suspend fun deleteByClientRecordIds(clientRecordIds: List<String>)
    @Transaction
    suspend fun upsertFromServer(remoteRecords: List<DataRecordEntity>) {
        for (remote in remoteRecords) {
            val existing = getByClientRecordId(remote.clientRecordId)
            if (existing != null) {
                // Preserve local auto-generated Room primary key id while updating server state
                updateRecord(remote.copy(id = existing.id, isSynced = true))
            } else {
                insertRecord(remote.copy(id = 0, isSynced = true))
            }
        }
    }
}