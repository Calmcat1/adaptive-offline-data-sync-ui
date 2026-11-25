package com.example.adaptive_offline_sync_data_ui.uiStateModel


import com.example.adaptive_offline_sync_data_ui.data.remote.MetricsSummary
import com.example.adaptive_offline_sync_data_ui.data.remote.OfflineRecord
import com.example.adaptive_offline_sync_data_ui.data.remote.SyncPolicyResponse

data class SyncUiState(
    val selectedSyncMode: String = "ADAPTIVE", // "ADAPTIVE" or "BASELINE"
    val pendingRecords: List<OfflineRecord> = emptyList(),
    val activePolicy: SyncPolicyResponse? = null,
    val adaptiveMetrics: MetricsSummary? = null,
    val baselineMetrics: MetricsSummary? = null,
    val isSyncing: Boolean = false,
    val lastUploadedCount: Int = 0,
    val lastDownloadedCount: Int = 0,
    val lastConflictsResolved: Int = 0,
    val isLoadingMetrics: Boolean = false,
    val isAutoSyncEnabled: Boolean = true,
    val statusMessage: String? = null,
    val errorMessage: String? = null
)