package com.example.adaptive_offline_sync_data_ui.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query



data class MetricsSummary(
    @SerializedName("sync_mode") val syncMode: String,
    @SerializedName("total_runs") val totalRuns: Int,
    @SerializedName("avg_data_usage_kb") val avgDataUsageKb: Double,
    @SerializedName("avg_latency_ms") val avgLatencyMs: Double,
    @SerializedName("avg_battery_drain_pct") val avgBatteryDrainPct: Double,
    @SerializedName("overall_success_rate_pct") val overallSuccessRatePct: Double
)

interface AnalyticsAPIService {
    @GET("api/v1/analytics/metrics/summary")
    suspend fun getMetricsSummary(
        @Query("device_id") deviceId: String? = null
    ): Response<List<MetricsSummary>>
}