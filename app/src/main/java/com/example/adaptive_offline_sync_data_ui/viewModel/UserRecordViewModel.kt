package com.example.adaptive_offline_sync_data_ui.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.adaptive_offline_sync_data_ui.data.entity.DataRecordEntity
import com.example.adaptive_offline_sync_data_ui.data.local.AppDatabase
import com.example.adaptive_offline_sync_data_ui.data.local.TokenManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

sealed interface RecordsUiState {
    data object Loading : RecordsUiState
    data class Error(val message: String) : RecordsUiState
    data class Success(
        val userEmail: String,
        val records: List<DataRecordEntity>
    ) : RecordsUiState
}

class RecordsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val recordDao = db.dataRecordDao()
    private val tokenManager = TokenManager(application)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<RecordsUiState> = tokenManager.getUserEmail
        .flatMapLatest { email ->
            val activeEmail = email.ifBlank { "dataofficer@gmail.com" }
            recordDao.getRecordsForUser(activeEmail)
                .map<List<DataRecordEntity>, RecordsUiState> { records ->
                    RecordsUiState.Success(userEmail = activeEmail, records = records)
                }
                .catch { e ->
                    emit(RecordsUiState.Error(e.localizedMessage ?: "Failed to load user records"))
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RecordsUiState.Loading
        )
}