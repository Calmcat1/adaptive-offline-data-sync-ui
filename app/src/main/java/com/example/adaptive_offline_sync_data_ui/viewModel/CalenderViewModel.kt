package com.example.adaptive_offline_sync_data_ui.viewModel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adaptive_offline_sync_data_ui.data.entity.DataRecordEntity
import com.example.adaptive_offline_sync_data_ui.data.repository.DataRecordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

@RequiresApi(Build.VERSION_CODES.O)
data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val recordsByDate: Map<LocalDate, List<DataRecordEntity>> = emptyMap(),
    val isLoading: Boolean = true
) {
    val selectedDayRecords: List<DataRecordEntity>
        get() = recordsByDate[selectedDate].orEmpty()
}

@RequiresApi(Build.VERSION_CODES.O)
class CalendarViewModel(
    private val recordRepository: DataRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        observeRecordsByDate()
    }

    private fun observeRecordsByDate() {
        viewModelScope.launch {
            recordRepository.getUserRecords().collect { (_, records) ->
                val zone = ZoneId.systemDefault()
                val grouped = records
                    .filter { !it.isDeleted }
                    .groupBy { entity ->
                        Instant.ofEpochMilli(entity.recordDate)
                            .atZone(zone)
                            .toLocalDate()
                    }

                _uiState.update {
                    it.copy(
                        recordsByDate = grouped,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun previousMonth() {
        _uiState.update { it.copy(currentMonth = it.currentMonth.minusMonths(1)) }
    }

    fun nextMonth() {
        _uiState.update { it.copy(currentMonth = it.currentMonth.plusMonths(1)) }
    }
}