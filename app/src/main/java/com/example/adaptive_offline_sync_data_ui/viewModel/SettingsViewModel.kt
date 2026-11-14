package com.example.adaptive_offline_sync_data_ui.viewModel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.adaptive_offline_sync_data_ui.data.entity.TelemetryEntity
import com.example.adaptive_offline_sync_data_ui.data.local.AppDatabase
import com.example.adaptive_offline_sync_data_ui.data.local.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val telemetryDao = db.telemetryDao()
    private val tokenManager = TokenManager(application)

    // Collect logged-in user details dynamically from DataStore
    val userName: StateFlow<String> = tokenManager.getUserName
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "Field Worker"
        )

    val userEmail: StateFlow<String> = tokenManager.getUserEmail
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "user@example.com"
        )

    private val _latestTelemetry = MutableStateFlow<TelemetryEntity?>(null)
    val latestTelemetry: StateFlow<TelemetryEntity?> = _latestTelemetry

    init {
        startTelemetryLogging()
    }

    private fun startTelemetryLogging() {
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                val sample = sampleCurrentTelemetry()
                _latestTelemetry.value = sample
                telemetryDao.insert(sample)
                delay(5000)
            }
        }
    }

    private fun sampleCurrentTelemetry(): TelemetryEntity {
        val context = getApplication<Application>().applicationContext

        // Battery level
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
            context.registerReceiver(null, filter)
        }
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level != -1 && scale != -1) ((level / scale.toFloat()) * 100).toInt() else 100

        // Network Type & Bandwidth
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetwork
        val capabilities = cm.getNetworkCapabilities(activeNetwork)
        val netType = when {
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "Wi-Fi"
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Mobile Data (4G/5G)"
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "Ethernet"
            else -> "Disconnected"
        }

        val bandwidth = capabilities?.linkDownstreamBandwidthKbps?.toLong() ?: (1000..50000).random().toLong()

        return TelemetryEntity(
            id = 0,
            timestamp = System.currentTimeMillis(),
            networkType = netType,
            latencyMs = (40..190).random().toLong(),
            signalStrength = (60..98).random(),
            batteryPercent = batteryPct,
            bandwidthKbps = (1000..50000).random()
        )
    }

    fun exportTelemetryCsv(onResult: (String?) -> Unit) {
        viewModelScope.launch {
            val resultPath = withContext(Dispatchers.IO) {
                try {
                    val logs = telemetryDao.getAllTelemetry()
                    if (logs.isEmpty()) return@withContext null

                    val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    if (!downloadsFolder.exists()) {
                        downloadsFolder.mkdirs()
                    }

                    val fileName = "telemetry_log_${System.currentTimeMillis()}.csv"
                    val file = File(downloadsFolder, fileName)
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

                    FileWriter(file).use { writer ->
                        writer.append("ID,Timestamp,DateFormatted,NetworkType,LatencyMs,SignalStrengthPercent,BatteryPercent,BandwidthKbps\n")

                        for (log in logs) {
                            val formattedDate = dateFormat.format(Date(log.timestamp))
                            writer.append("${log.id},${log.timestamp},\"$formattedDate\",\"${log.networkType}\",${log.latencyMs},${log.signalStrength},${log.batteryPercent},${log.bandwidthKbps}\n")
                        }
                    }
                    file.absolutePath
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
            onResult(resultPath)
        }
    }

    fun clearTelemetryHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            telemetryDao.deleteAll()
        }
    }
}