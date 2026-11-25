package com.example.adaptive_offline_sync_data_ui.data.repository

import com.example.adaptive_offline_sync_data_ui.data.entity.DataRecordEntity
import com.example.adaptive_offline_sync_data_ui.data.dao.DataRecordDao
import com.example.adaptive_offline_sync_data_ui.data.local.TokenManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class DataRecordRepository(
    private val dataRecordDao: DataRecordDao,
    private val tokenManager: TokenManager
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getUserRecords(): Flow<Pair<String, List<DataRecordEntity>>> {
        return tokenManager.getUserEmail.flatMapLatest { email ->
            val activeEmail = email.ifBlank { "dataofficer@gmail.com" }
            dataRecordDao.getRecordsForUser(activeEmail).map { records ->
                activeEmail to records
            }
        }
    }

    suspend fun saveRecordForCurrentUser(record: DataRecordEntity) {
        val activeEmail = tokenManager.getUserEmail.first().ifBlank { "dataofficer@gmail.com" }
        val userScopedRecord = record.copy(userEmail = activeEmail)
        dataRecordDao.insertRecord(userScopedRecord)
    }

    suspend fun deleteRecord(recordId: String) {
        dataRecordDao.deleteByClientRecordIds(listOf(recordId))
    }
}