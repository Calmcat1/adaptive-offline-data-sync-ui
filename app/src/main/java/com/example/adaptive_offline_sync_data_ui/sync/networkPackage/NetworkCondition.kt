package com.example.adaptive_offline_sync_data_ui.sync.networkPackage

data class NetworkCondition(
    val isConnected: Boolean,
    val connectionType: ConnectionType,
    val signalStrength: Int?,
    val latencyMs: Long?,
    val timestamp: Long = System.currentTimeMillis()
)

enum class ConnectionType {
    WIFI,
    MOBILE,
    ETHERNET,
    OTHER,
    NONE
}