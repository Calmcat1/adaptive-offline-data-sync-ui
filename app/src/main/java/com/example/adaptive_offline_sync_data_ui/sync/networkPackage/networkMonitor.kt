package com.example.adaptive_offline_sync_data_ui.sync.networkPackage

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.annotation.RequiresPermission


class NetworkMonitor(
    private val context: Context
) {

    private val connectivityManager =
        context.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun getNetworkCondition(): NetworkCondition {

        val network = connectivityManager.activeNetwork ?: return NetworkCondition(
            isConnected = false,
            connectionType = ConnectionType.NONE,
            signalStrength = null,
            latencyMs = null
        )

        val capabilities =
            connectivityManager.getNetworkCapabilities(network) ?: return NetworkCondition(
                isConnected = false,
                connectionType = ConnectionType.NONE,
                signalStrength = null,
                latencyMs = null
            )

        val connectionType = when {

            capabilities.hasTransport(
                NetworkCapabilities.TRANSPORT_WIFI
            ) -> ConnectionType.WIFI

            capabilities.hasTransport(
                NetworkCapabilities.TRANSPORT_CELLULAR
            ) -> ConnectionType.MOBILE

            capabilities.hasTransport(
                NetworkCapabilities.TRANSPORT_ETHERNET
            ) -> ConnectionType.ETHERNET

            else -> ConnectionType.OTHER
        }

        return NetworkCondition(
            isConnected = true,
            connectionType = connectionType,
            signalStrength = null,
            latencyMs = null
        )
    }
}