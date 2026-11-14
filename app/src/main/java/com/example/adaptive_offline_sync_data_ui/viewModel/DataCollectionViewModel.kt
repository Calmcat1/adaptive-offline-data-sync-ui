package com.example.adaptive_offline_sync_data_ui.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface DataCollectionUiState {
    object Idle : DataCollectionUiState
    object Loading : DataCollectionUiState
    object Success : DataCollectionUiState
    data class Error(val message: String) : DataCollectionUiState
}

class DataCollectionViewModel() : ViewModel() {

    var uiState by mutableStateOf<DataCollectionUiState>(DataCollectionUiState.Idle)
        private set

    fun saveRecord(
        location: String,
        dateTime: String,
        recordTime: String,
        messageData: String
    ) {
        if (location.isBlank() || dateTime.isBlank() || recordTime.isBlank() || messageData.isBlank()) {
            uiState = DataCollectionUiState.Error("Please fill in all fields before saving.")
            return
        }

        viewModelScope.launch {
            uiState = DataCollectionUiState.Loading
            try {
                // Perform local Room DB insertion or network dispatch here
                // e.g., recordDao.insertRecord(...) or NetworkClient.api.postRecord(...)

                uiState = DataCollectionUiState.Success
            } catch (e: Exception) {
                uiState = DataCollectionUiState.Error(
                    e.localizedMessage ?: "Failed to save record locally."
                )
            }
        }
    }

    fun getCurrentFormattedDate(): String {
        val formatter = SimpleDateFormat("dd-MM-yy", Locale.getDefault())
        return formatter.format(Date())
    }

    fun getCurrentFormattedTime(): String {
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        return formatter.format(Date())
    }

    fun resetState() {
        uiState = DataCollectionUiState.Idle
    }
}