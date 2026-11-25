package com.example.adaptive_offline_sync_data_ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adaptive_offline_sync_data_ui.data.remote.MetricsSummary
import com.example.adaptive_offline_sync_data_ui.data.remote.NetworkClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AnalyticsUiState(
    val isLoading: Boolean = true,
    val adaptive: MetricsSummary? = null,
    val baseline: MetricsSummary? = null,
    val errorMessage: String? = null
)

class AnalyticsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadSummary()
    }

    fun loadSummary() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val response = NetworkClient.analyticsApiService.getMetricsSummary()
                if (response.isSuccessful) {
                    val items = response.body().orEmpty()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            adaptive = items.find { m -> m.syncMode == "ADAPTIVE" },
                            baseline = items.find { m -> m.syncMode == "BASELINE" }
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "HTTP ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Failed to load metrics")
                }
            }
        }
    }
}