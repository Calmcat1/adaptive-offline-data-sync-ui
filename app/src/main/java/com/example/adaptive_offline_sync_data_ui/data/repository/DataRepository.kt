package com.example.adaptive_offline_sync_data_ui.data.repository

import android.content.Context
import com.example.adaptive_offline_sync_data_ui.data.entity.DataRecordEntity
import com.example.adaptive_offline_sync_data_ui.data.local.dao.DataRecordDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class DataRepository(
    private val dataRecordDao: DataRecordDao
) {

    suspend fun insertRecord(record: DataRecordEntity): Long = withContext(Dispatchers.IO) {
        dataRecordDao.insert(record)
    }

    suspend fun getAllRecords(): List<DataRecordEntity> = withContext(Dispatchers.IO) {
        dataRecordDao.getAllRecords()
    }

    suspend fun getUnsyncedRecords(): List<DataRecordEntity> = withContext(Dispatchers.IO) {
        dataRecordDao.getUnsyncedRecords()
    }

    suspend fun clearAllRecords() = withContext(Dispatchers.IO) {
        dataRecordDao.deleteAll()
    }

}