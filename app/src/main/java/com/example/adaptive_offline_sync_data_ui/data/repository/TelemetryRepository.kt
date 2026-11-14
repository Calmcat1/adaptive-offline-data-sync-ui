package com.example.adaptive_offline_sync_data_ui.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import com.example.adaptive_offline_sync_data_ui.data.entity.TelemetryEntity
import com.example.adaptive_offline_sync_data_ui.data.local.dao.TelemetryDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext



class TelemetryRepository(
    private val context: Context,
    private val telemetryDao: TelemetryDao
) {
    suspend fun captureCurrentTelemetry(): TelemetryEntity = withContext(Dispatchers.IO) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val batteryManager =
            context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)

        // Network Type & Bandwidth
        val networkType = when {
            capabilities == null -> "No Connection"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile Data (4G/5G)"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "Unknown"
        }

        val bandwidthKbps = capabilities?.linkDownstreamBandwidthKbps ?: 0

        // Battery Percentage
        val batteryPercent = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

        // Construct Entity
        val telemetry = TelemetryEntity(
            networkType = networkType,
            signalStrength = 75, // Sample signal strength percentage
            bandwidthKbps = bandwidthKbps,
            latencyMs = 184,     // Estimated or ping latency
            batteryPercent = batteryPercent,
            timestamp = System.currentTimeMillis()
        )

        // Insert into Room DB
        telemetryDao.insert(telemetry)
        return@withContext telemetry
    }

    suspend fun getTelemetryHistory(): List<TelemetryEntity> = withContext(Dispatchers.IO) {
        telemetryDao.getAllTelemetry()
    }
}