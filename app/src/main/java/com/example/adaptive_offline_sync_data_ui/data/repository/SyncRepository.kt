package com.example.adaptive_offline_sync_data_ui.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.adaptive_offline_sync_data_ui.data.dao.DataRecordDao
import com.example.adaptive_offline_sync_data_ui.data.entity.DataRecordEntity
import com.example.adaptive_offline_sync_data_ui.data.local.TokenManager
import com.example.adaptive_offline_sync_data_ui.data.remote.BatchSyncRequest
import com.example.adaptive_offline_sync_data_ui.data.remote.BatchSyncResponse
import com.example.adaptive_offline_sync_data_ui.data.remote.NetworkTelemetry
import com.example.adaptive_offline_sync_data_ui.data.remote.OfflineRecord
import com.example.adaptive_offline_sync_data_ui.data.remote.SyncServiceApi
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.time.Instant

class SyncRepository(
    private val syncApi: SyncServiceApi,
    private val recordDao: DataRecordDao,
    private val tokenManager: TokenManager,
    private val gson: Gson = Gson()
) {
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun runSync(
        deviceId: String,
        syncMode: String, // "ADAPTIVE" or "BASELINE"
        signalDbm: Int,
        networkType: String,
        batteryLevel: Int,
        isCharging: Boolean
    ): Result<BatchSyncResponse> = withContext(Dispatchers.IO) {
        try {
            val lastSyncTs = tokenManager.getLastSyncTimestamp()
            val currentUserEmail = tokenManager.getUserEmail.firstOrNull().orEmpty()
            val allPending = recordDao.getUnsyncedRecords(limit = 500)
            val pendingBytes = gson.toJson(allPending).toByteArray().size

            val telemetry = NetworkTelemetry(
                deviceId = deviceId,
                signalStrengthDbm = signalDbm,
                networkType = networkType,
                pendingRecordsCount = allPending.size,
                pendingPayloadBytes = pendingBytes,
                batteryLevel = batteryLevel,
                isCharging = isCharging,
                lastSyncTimestamp = lastSyncTs
            )

            // 1. Evaluate policy first (Adaptive vs. Baseline)
            val policyResp = syncApi.evaluatePolicy(syncMode, telemetry)
            if (!policyResp.isSuccessful || policyResp.body() == null) {
                return@withContext Result.failure(Exception("Policy failed: ${policyResp.code()}"))
            }
            val policy = policyResp.body()!!

            // Respect DELAY_SYNC / NONE direction in Adaptive mode
            if (policy.strategy == "DELAY_SYNC" || policy.syncDirection == "NONE") {
                return@withContext Result.failure(Exception("Sync deferred by policy: ${policy.rationale}"))
            }

            // 2. Map DataRecordEntity -> OfflineRecord according to recommended_batch_size
            val recordsToUpload = allPending
                .take(policy.recommendedBatchSize.coerceAtLeast(0))
                .map { entity ->
                    OfflineRecord(
                        clientRecordId = entity.clientRecordId,
                        title = entity.location,
                        category = "DATA_RECORD",
                        payload = mapOf(
                            "userEmail" to entity.userEmail,
                            "location" to entity.location,
                            "recordDate" to entity.recordDate,
                            "recordTime" to entity.recordTime,
                            "notes" to entity.notes
                        ),
                        clientCreatedAt = Instant.ofEpochMilli(entity.recordDate).toString(),
                        clientUpdatedAt = Instant.ofEpochMilli(entity.updatedAt).toString(),
                        isDeleted = entity.isDeleted
                    )
                }

            val startNs = System.nanoTime()
            val batchRequest = BatchSyncRequest(
                deviceId = deviceId,
                syncMode = syncMode,
                telemetry = telemetry,
                records = recordsToUpload,
                lastSyncTimestamp = lastSyncTs,
                downloadLimit = policy.recommendedDownloadLimit,
                clientLatencyMs = 0.0,
                batteryDrainPct = 0.05
            )

            // 3. Execute bidirectional /sync/batch
            val batchResp = syncApi.syncBatch(batchRequest)
            val elapsedMs = (System.nanoTime() - startNs) / 1_000_000.0

            if (!batchResp.isSuccessful || batchResp.body() == null) {
                return@withContext Result.failure(Exception("Batch sync failed: ${batchResp.code()}"))
            }
            val result = batchResp.body()!!

            // 4. Mark uploaded records as synced in Room
            if (recordsToUpload.isNotEmpty()) {
                recordDao.markRecordsAsSynced(recordsToUpload.map { it.clientRecordId })
            }

            // 5. Reconcile downloaded remote records into Room (upsert active, delete soft-deleted)
            val (deletedItems, activeItems) = result.downloadedRecords.partition { it.isDeleted }

            if (deletedItems.isNotEmpty()) {
                recordDao.deleteByClientRecordIds(deletedItems.map { it.clientRecordId })
            }

            if (activeItems.isNotEmpty()) {
                val entitiesToUpsert = activeItems.map { remote ->
                    val parsedRecordDate = (remote.payload["recordDate"] as? Number)?.toLong()
                        ?: runCatching { Instant.parse(remote.clientCreatedAt).toEpochMilli() }
                            .getOrElse { System.currentTimeMillis() }
                    val parsedUpdatedAt = runCatching { Instant.parse(remote.clientUpdatedAt).toEpochMilli() }
                        .getOrElse { System.currentTimeMillis() }

                    DataRecordEntity(
                        id = 0,
                        clientRecordId = remote.clientRecordId,
                        userEmail = (remote.payload["userEmail"] as? String)
                            .orEmpty()
                            .ifBlank { currentUserEmail },
                        location = (remote.payload["location"] as? String)
                            .orEmpty()
                            .ifBlank { remote.title },
                        recordDate = parsedRecordDate,
                        recordTime = (remote.payload["recordTime"] as? String).orEmpty(),
                        notes = (remote.payload["notes"] as? String).orEmpty(),
                        updatedAt = parsedUpdatedAt,
                        isSynced = true,
                        isDeleted = false
                    )
                }
                recordDao.upsertFromServer(entitiesToUpsert)
            }

            // 6. Advance the delta cursor to server_timestamp
            tokenManager.saveLastSyncTimestamp(result.serverTimestamp)

            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}