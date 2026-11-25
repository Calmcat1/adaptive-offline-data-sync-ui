package com.example.adaptive_offline_sync_data_ui.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

// NetworkTelemetry
data class NetworkTelemetry(
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("signal_strength_dbm") val signalStrengthDbm: Int,
    @SerializedName("sync_mode") val syncMode: String = "ADAPTIVE",
    @SerializedName("network_type") val networkType: String, // "NONE", "2G", "3G", "4G", "WIFI"
    @SerializedName("connection_stability_drops") val connectionStabilityDrops: Int = 0,
    @SerializedName("pending_records_count") val pendingRecordsCount: Int,
    @SerializedName("pending_payload_bytes") val pendingPayloadBytes: Int,
    @SerializedName("battery_level") val batteryLevel: Int,
    @SerializedName("is_charging") val isCharging: Boolean = false,
    @SerializedName("last_sync_timestamp") val lastSyncTimestamp: String? = null
)

data class OfflineRecord(
    @SerializedName("client_record_id") val clientRecordId: String,
    @SerializedName("title") val title: String,
    @SerializedName("category") val category: String,
    @SerializedName("payload") val payload: Map<String, Any>,
    @SerializedName("client_created_at") val clientCreatedAt: String,
    @SerializedName("client_updated_at") val clientUpdatedAt: String,
    @SerializedName("is_deleted") val isDeleted: Boolean = false
)

data class RemoteRecord(
    @SerializedName("id") val id: String,
    @SerializedName("client_record_id") val clientRecordId: String,
    @SerializedName("title") val title: String,
    @SerializedName("category") val category: String,
    @SerializedName("payload") val payload: Map<String, Any>,
    @SerializedName("is_deleted") val isDeleted: Boolean = false,
    @SerializedName("client_created_at") val clientCreatedAt: String,
    @SerializedName("client_updated_at") val clientUpdatedAt: String,
    @SerializedName("server_synced_at") val serverSyncedAt: String
)

data class SyncPolicyResponse(
    @SerializedName("strategy") val strategy: String, // "FULL_SYNC", "PARTIAL_SYNC", "DELAY_SYNC"
    @SerializedName("sync_direction") val syncDirection: String = "BIDIRECTIONAL",
    @SerializedName("recommended_batch_size") val recommendedBatchSize: Int,
    @SerializedName("recommended_download_limit") val recommendedDownloadLimit: Int = 50,
    @SerializedName("next_sync_interval_seconds") val nextSyncIntervalSeconds: Int,
    @SerializedName("network_quality_score") val networkQualityScore: Double,
    @SerializedName("component_scores") val componentScores: Map<String, Double>,
    @SerializedName("rationale") val rationale: String
)

data class BatchSyncRequest(
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("sync_mode") val syncMode: String = "ADAPTIVE",
    @SerializedName("telemetry") val telemetry: NetworkTelemetry,
    @SerializedName("records") val records: List<OfflineRecord>,
    @SerializedName("last_sync_timestamp") val lastSyncTimestamp: String? = null,
    @SerializedName("download_limit") val downloadLimit: Int = 50,
    @SerializedName("client_latency_ms") val clientLatencyMs: Double,
    @SerializedName("battery_drain_pct") val batteryDrainPct: Double,
    @SerializedName("retry_count") val retryCount: Int = 0
)

data class BatchSyncResponse(
    @SerializedName("synced_count") val syncedCount: Int,
    @SerializedName("failed_count") val failedCount: Int,
    @SerializedName("downloaded_count") val downloadedCount: Int = 0,
    @SerializedName("conflicts_resolved") val conflictsResolved: Int = 0,
    @SerializedName("downloaded_records") val downloadedRecords: List<RemoteRecord> = emptyList(),
    @SerializedName("has_more_downloads") val hasMoreDownloads: Boolean = false,
    @SerializedName("bytes_received") val bytesReceived: Int,
    @SerializedName("bytes_sent") val bytesSent: Int = 0,
    @SerializedName("server_timestamp") val serverTimestamp: String,
    @SerializedName("next_policy") val nextPolicy: SyncPolicyResponse
)

interface SyncServiceApi {

    @POST("api/v1/sync/policy")
    suspend fun evaluatePolicy(
        @Query("sync_mode") syncMode: String = "ADAPTIVE",
        @Body telemetry: NetworkTelemetry
    ): Response<SyncPolicyResponse>

    @POST("api/v1/sync/batch")
    suspend fun syncBatch(
        @Body payload: BatchSyncRequest
    ): Response<BatchSyncResponse>


}