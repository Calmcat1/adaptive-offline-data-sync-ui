package com.example.adaptive_offline_sync_data_ui.viewModel

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adaptive_offline_sync_data_ui.data.dao.DataRecordDao
import com.example.adaptive_offline_sync_data_ui.data.entity.DataRecordEntity
import com.example.adaptive_offline_sync_data_ui.data.remote.AnalyticsAPIService
import com.example.adaptive_offline_sync_data_ui.data.remote.NetworkTelemetry
import com.example.adaptive_offline_sync_data_ui.data.remote.OfflineRecord
import com.example.adaptive_offline_sync_data_ui.data.remote.RecordsAPI
import com.example.adaptive_offline_sync_data_ui.data.remote.SyncServiceApi
import com.example.adaptive_offline_sync_data_ui.data.repository.DataRecordRepository
import com.example.adaptive_offline_sync_data_ui.data.repository.SyncRepository
import com.example.adaptive_offline_sync_data_ui.uiStateModel.SyncUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@RequiresApi(Build.VERSION_CODES.O)
class SyncViewModel(
    private val appContext: Context,
    private val syncServiceApi: SyncServiceApi,
    private val recordsAPI: RecordsAPI,
    private val syncRepository: SyncRepository,
    private val analyticsAPIService: AnalyticsAPIService,
    private val dataRecordDao: DataRecordDao,
    private val recordRepository: DataRecordRepository,
    private val deviceId: String,
    private val readTelemetry: (pendingCount: Int, pendingBytes: Int) -> NetworkTelemetry,
    private val readBatteryPct: () -> Double
) : ViewModel() {

    private val _uiState = MutableStateFlow(SyncUiState())
    val uiState: StateFlow<SyncUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null

    private val connectivityManager =
        appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            if (_uiState.value.isAutoSyncEnabled) {
                _uiState.update {
                    it.copy(
                        activePolicy = null,
                        errorMessage = null,
                        statusMessage = "Network restored — querying adaptive decision engine..."
                    )
                }
                restartAutoSyncPollingLoop()
            }
        }

        override fun onLost(network: Network) {
            if (_uiState.value.isAutoSyncEnabled) {
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        statusMessage = "Offline — waiting for network to resume sync"
                    )
                }
            }
        }
    }

    init {
        registerNetworkObserver()
        observeRoomPendingRecords()
        restartAutoSyncPollingLoop()
        refreshEvaluationMetrics()
    }

    private fun registerNetworkObserver() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        runCatching {
            connectivityManager.registerNetworkCallback(request, networkCallback)
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /** Continuously maps unsynced Room rows without bypassing the adaptive engine's timer */
    private fun observeRoomPendingRecords() {
        viewModelScope.launch {
            recordRepository.getUserRecords().collect { (_, records) ->
                val unsyncedItems = records
                    .filter { !it.isSynced }
                    .map { it.toOfflineRecord() }

                _uiState.update { state ->
                    state.copy(pendingRecords = unsyncedItems)
                }
            }
        }
    }

    /** Queries adaptive_decision_engine for the interval, waits out the full countdown, then syncs */
    private fun restartAutoSyncPollingLoop() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive && _uiState.value.isAutoSyncEnabled) {
                if (!isNetworkAvailable()) {
                    _uiState.update {
                        it.copy(
                            statusMessage = "Offline — queued ${it.pendingRecords.size} records; waiting for network"
                        )
                    }
                    delay(5000L)
                    continue
                }

                // 1. Fetch policy & variable interval from adaptive_decision_engine if not yet evaluated
                if (_uiState.value.activePolicy == null) {
                    val fetched = fetchIntervalFromAdaptiveEngine()
                    if (!fetched) {
                        delay(10_000L)
                        continue
                    }
                }

                // 2. Read the exact interval returned by adaptive_decision_engine
                val engineIntervalSec = _uiState.value.activePolicy!!.nextSyncIntervalSeconds
                    .coerceAtLeast(1)

                _uiState.update {
                    it.copy(
                        statusMessage = "Scheduled next ${it.selectedSyncMode} sync in ${engineIntervalSec}s"
                    )
                }

                // 3. Wait the full engine-computed interval before transferring records
                delay(engineIntervalSec * 1000L)

                // 4. Execute bidirectional sync once the interval expires
                if (_uiState.value.isAutoSyncEnabled && !_uiState.value.isSyncing && isNetworkAvailable()) {
                    executeSyncCycle()
                }
            }
        }
    }

    /** Calls POST /sync/policy backed by adaptive_decision_engine to get nextSyncIntervalSeconds */
    private suspend fun fetchIntervalFromAdaptiveEngine(): Boolean {
        return try {
            val pending = _uiState.value.pendingRecords
            val estimatedBytes = pending.sumOf { it.toString().toByteArray(Charsets.UTF_8).size }
            val telemetry = readTelemetry(pending.size, estimatedBytes)
            val batteryLevel = readBatteryPct().toInt().coerceIn(0, 100)
            val effectiveNetworkType = if (telemetry.networkType == "NONE") "WIFI" else telemetry.networkType

            val requestTelemetry = telemetry.copy(
                deviceId = deviceId,
                syncMode = _uiState.value.selectedSyncMode,
                networkType = effectiveNetworkType,
                batteryLevel = batteryLevel,
                pendingRecordsCount = pending.size,
                pendingPayloadBytes = estimatedBytes
            )

            val response = syncServiceApi.evaluatePolicy(telemetry = requestTelemetry)
            if (response.isSuccessful && response.body() != null) {
                val policy = response.body()!!
                _uiState.update {
                    it.copy(
                        activePolicy = policy,
                        errorMessage = null
                    )
                }
                true
            } else {
                val errDetail = response.errorBody()?.string().orEmpty()
                _uiState.update {
                    it.copy(
                        errorMessage = "Policy failed (${response.code()}): $errDetail"
                    )
                }
                false
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    statusMessage = "Waiting for adaptive decision engine: ${e.message}"
                )
            }
            false
        }
    }

    fun selectSyncStrategy(context: Context, mode: String) {
        if (_uiState.value.selectedSyncMode == mode) return

        _uiState.update {
            it.copy(
                selectedSyncMode = mode,
                activePolicy = null,
                errorMessage = null
            )
        }

        if (_uiState.value.isAutoSyncEnabled) {
            restartAutoSyncPollingLoop()
        }
    }

    fun toggleAutoSync(context: Context, enabled: Boolean) {
        _uiState.update {
            it.copy(
                isAutoSyncEnabled = enabled,
                activePolicy = if (enabled) null else it.activePolicy,
                errorMessage = null
            )
        }

        if (enabled) {
            restartAutoSyncPollingLoop()
        } else {
            pollingJob?.cancel()
            _uiState.update {
                it.copy(statusMessage = "Automatic synchronization paused")
            }
        }
    }

    fun triggerSyncNow() {
        viewModelScope.launch {
            executeSyncCycle()
        }
    }

    /** Executes bidirectional sync (Upload + Download + LWW) and updates activePolicy for the next cycle */
    private suspend fun executeSyncCycle() {
        val current = _uiState.value
        if (current.isSyncing) return

        if (!isNetworkAvailable()) {
            _uiState.update {
                it.copy(
                    isSyncing = false,
                    errorMessage = null,
                    statusMessage = "Offline — queued ${it.pendingRecords.size} records; will sync when online"
                )
            }
            return
        }

        _uiState.update { it.copy(isSyncing = true, errorMessage = null) }

        val pending = _uiState.value.pendingRecords
        val estimatedBytes = pending.sumOf { record ->
            record.toString().toByteArray(Charsets.UTF_8).size
        }
        val telemetry = readTelemetry(pending.size, estimatedBytes)
        val batteryLevel = readBatteryPct().toInt().coerceIn(0, 100)
        val effectiveNetworkType = if (telemetry.networkType == "NONE") "WIFI" else telemetry.networkType

        val result = syncRepository.runSync(
            deviceId = deviceId,
            syncMode = current.selectedSyncMode,
            signalDbm = telemetry.signalStrengthDbm,
            networkType = effectiveNetworkType,
            batteryLevel = batteryLevel,
            isCharging = telemetry.isCharging
        )

        result.fold(
            onSuccess = { body ->
                val statusSummary = buildString {
                    append("Uploaded ${body.syncedCount}, Downloaded ${body.downloadedCount}")
                    if (body.conflictsResolved > 0) {
                        append(", Resolved ${body.conflictsResolved} conflicts")
                    }
                    append(" (${body.nextPolicy.strategy})")
                }

                _uiState.update { state ->
                    state.copy(
                        activePolicy = body.nextPolicy,
                        isSyncing = false,
                        errorMessage = null,
                        lastUploadedCount = body.syncedCount,
                        lastDownloadedCount = body.downloadedCount,
                        lastConflictsResolved = body.conflictsResolved,
                        statusMessage = statusSummary
                    )
                }

                refreshEvaluationMetrics()
            },
            onFailure = { error ->
                val isOfflineOrDeferred = error is IOException ||
                        (error.message?.startsWith("Sync deferred by policy") == true)

                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        errorMessage = if (isOfflineOrDeferred) null else (error.message ?: "Sync error"),
                        statusMessage = if (isOfflineOrDeferred) {
                            error.message ?: "Waiting for network to retry..."
                        } else {
                            it.statusMessage
                        }
                    )
                }
            }
        )
    }

    fun refreshEvaluationMetrics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMetrics = true) }
            try {
                val res = analyticsAPIService.getMetricsSummary()
                if (res.isSuccessful) {
                    val list = res.body().orEmpty()
                    _uiState.update {
                        it.copy(
                            adaptiveMetrics = list.find { m -> m.syncMode == "ADAPTIVE" },
                            baselineMetrics = list.find { m -> m.syncMode == "BASELINE" },
                            isLoadingMetrics = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoadingMetrics = false)
                    }
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(isLoadingMetrics = false)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
        runCatching {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }
}

private fun DataRecordEntity.toOfflineRecord(): OfflineRecord {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    return OfflineRecord(
        clientRecordId = clientRecordId,
        title = location.ifBlank { "Data Record #$id" },
        category = "DATA_RECORD",
        payload = mapOf(
            "location" to location,
            "recordDate" to recordDate,
            "recordTime" to recordTime,
            "notes" to notes,
            "userEmail" to userEmail
        ),
        clientCreatedAt = formatter.format(Date(recordDate)),
        clientUpdatedAt = formatter.format(Date(updatedAt)),
        isDeleted = isDeleted
    )
}