package com.example.adaptive_offline_sync_data_ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
//import com.example.adaptive_offline_sync_data_ui.data.local.TokenManager
import com.example.adaptive_offline_sync_data_ui.screens.*

sealed interface AppDestination {
    object Splash : AppDestination
    object Login : AppDestination
    object SignUp : AppDestination
    object Welcome : AppDestination
    object Dashboard : AppDestination
    object UserRecords : AppDestination
    object Settings : AppDestination
    object DataCollection : AppDestination
    object SyncStatus: AppDestination
    object Analytics : AppDestination
    object Calender: AppDestination

}