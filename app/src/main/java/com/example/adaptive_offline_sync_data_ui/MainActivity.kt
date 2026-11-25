package com.example.adaptive_offline_sync_data_ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.BatteryManager
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.adaptive_offline_sync_data_ui.data.local.AppDatabase
import com.example.adaptive_offline_sync_data_ui.data.local.TokenManager
import com.example.adaptive_offline_sync_data_ui.data.remote.NetworkClient
import com.example.adaptive_offline_sync_data_ui.data.remote.NetworkClient.syncServiceApi
import com.example.adaptive_offline_sync_data_ui.data.remote.NetworkTelemetry
import com.example.adaptive_offline_sync_data_ui.data.remote.SyncServiceApi
import com.example.adaptive_offline_sync_data_ui.data.repository.DataRecordRepository
import com.example.adaptive_offline_sync_data_ui.data.repository.SyncRepository
import com.example.adaptive_offline_sync_data_ui.screens.AnalyticsScreen
import com.example.adaptive_offline_sync_data_ui.screens.AppDestination
import com.example.adaptive_offline_sync_data_ui.screens.DashboardScreen
import com.example.adaptive_offline_sync_data_ui.screens.DataCollectionFormScreen
import com.example.adaptive_offline_sync_data_ui.screens.LoginScreen
import com.example.adaptive_offline_sync_data_ui.screens.RecordsScreen
import com.example.adaptive_offline_sync_data_ui.screens.SettingsScreen
import com.example.adaptive_offline_sync_data_ui.screens.SignUpScreen
import com.example.adaptive_offline_sync_data_ui.screens.SplashScreen
import com.example.adaptive_offline_sync_data_ui.screens.SyncHistoryScreen
import com.example.adaptive_offline_sync_data_ui.screens.SyncStatusScreen
import com.example.adaptive_offline_sync_data_ui.screens.WelcomeScreen
import com.example.adaptive_offline_sync_data_ui.ui.theme.AdaptiveofflinesyncDatauiTheme
import com.example.adaptive_offline_sync_data_ui.viewModel.SettingsViewModel
import com.example.adaptive_offline_sync_data_ui.viewModel.CalendarViewModel
import com.example.adaptive_offline_sync_data_ui.viewModel.SyncViewModel
import com.example.adaptive_offline_sync_data_ui.screens.CalendarScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tokenManager = TokenManager(applicationContext)
        val database = AppDatabase.getDatabase(applicationContext)
        val recordRepository = DataRecordRepository(database.dataRecordDao(), tokenManager)
        val syncRepository = SyncRepository(syncServiceApi, database.dataRecordDao(), tokenManager)



        setContent {
            AdaptiveofflinesyncDatauiTheme {
                var currentDestination by remember { mutableStateOf<AppDestination>(AppDestination.Splash) }
                val token by tokenManager.tokenFlow.collectAsState(initial = null)
                val scope = rememberCoroutineScope()

                val settingsViewModel: SettingsViewModel = viewModel(
                    factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
                )


                var selectedCollectionDate by remember { mutableStateOf<LocalDate?>(null) }

                val calendarViewModel: CalendarViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @RequiresApi(Build.VERSION_CODES.O)
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return CalendarViewModel(recordRepository = recordRepository) as T
                        }
                    }
                )
                val latestTelemetry by settingsViewModel.latestTelemetry.collectAsState()

                val deviceId = remember {
                    android.provider.Settings.Secure.getString(
                        contentResolver,
                        android.provider.Settings.Secure.ANDROID_ID
                    ) ?: "android-device-01"
                }

                val syncViewModel: SyncViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @RequiresApi(Build.VERSION_CODES.O)
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return SyncViewModel(
                                syncServiceApi = NetworkClient.syncServiceApi,
                                syncRepository = syncRepository,
                                recordsAPI = NetworkClient.recordsApi,
                                analyticsAPIService = NetworkClient.analyticsApiService,
                                dataRecordDao = database.dataRecordDao(),
                                recordRepository = recordRepository,
                                deviceId = deviceId,
                                appContext = applicationContext,
                                readTelemetry = { pendingCount, pendingBytes ->
                                    val sample = latestTelemetry
                                    val mappedNetworkType = when (sample?.networkType) {
                                        "Wi-Fi", "Ethernet" -> "WIFI"
                                        "Mobile Data (4G/5G)" -> "4G"
                                        else -> "NONE"
                                    }

                                    val signalPct = sample?.signalStrength ?: 85
                                    val estimatedDbm = -115 + ((signalPct / 100.0) * 60).toInt()

                                    val batteryStatus = registerReceiver(
                                        null,
                                        android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED)
                                    )
                                    val status = batteryStatus?.getIntExtra(
                                        android.os.BatteryManager.EXTRA_STATUS,
                                        -1
                                    ) ?: -1
                                    val charging =
                                        status == android.os.BatteryManager.BATTERY_STATUS_CHARGING ||
                                                status == android.os.BatteryManager.BATTERY_STATUS_FULL

                                    NetworkTelemetry(
                                        deviceId = deviceId,
                                        signalStrengthDbm = estimatedDbm,
                                        networkType = mappedNetworkType,
                                        connectionStabilityDrops = 0,
                                        pendingRecordsCount = pendingCount,
                                        pendingPayloadBytes = pendingBytes,
                                        batteryLevel = sample?.batteryPercent ?: 90,
                                        isCharging = charging
                                    )
                                },
                                readBatteryPct = {
                                    val bm = getSystemService(BATTERY_SERVICE) as BatteryManager
                                    bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                                        .toDouble()
                                }
                            ) as T
                        }
                    }
                )

                // 1. Auto Session Router: Automatically routes based on DataStore token presence
                LaunchedEffect(token) {
                    if (!token.isNullOrEmpty() && currentDestination == AppDestination.Splash) {
                        currentDestination = AppDestination.Dashboard
                    } else if (currentDestination == AppDestination.Splash) {
                        currentDestination = AppDestination.Welcome
                    }
                }

                when (currentDestination) {
                    is AppDestination.Splash -> {
                        SplashScreen()
                    }

                    is AppDestination.Welcome -> {
                        WelcomeScreen(
                            onLogInClick = {
                                currentDestination = AppDestination.Login
                            },
                            onGetStartedClick = {
                                currentDestination = AppDestination.SignUp
                            }
                        )
                    }

                    is AppDestination.Login -> {
                        LoginScreen(
                            onBackClick = {
                                currentDestination = AppDestination.Welcome
                            },
                            onLoginSuccess = { newToken, name, email ->
                                scope.launch {
                                    tokenManager.saveUserSession(
                                        token = newToken,
                                        name = name,
                                        email = email
                                    )
                                }
                                currentDestination = AppDestination.Dashboard
                            },
                            onCreateAccountClick = {
                                currentDestination = AppDestination.SignUp
                            }
                        )
                    }

                    is AppDestination.SignUp -> {
                        SignUpScreen(
                            onBackClick = {
                                currentDestination = AppDestination.Welcome
                            },
                            onContinueClick = {
                                currentDestination = AppDestination.Login
                            }
                        )
                    }

                    is AppDestination.Dashboard -> {
                        DashboardScreen(
                            onCollectDataClick = {
                                currentDestination = AppDestination.DataCollection
                            },
                            onMyRecordsClick = { currentDestination = AppDestination.UserRecords },
                            onSyncStatusClick = { currentDestination = AppDestination.SyncStatus },
                            onSettingsClick = { currentDestination = AppDestination.Settings },
                            onAnalyticsClick = { currentDestination = AppDestination.Analytics },
                            onCalenderClick = { currentDestination = AppDestination.Calender },
                            settingsViewModel = settingsViewModel
                        )
                    }

                    is AppDestination.Settings -> {
                        SettingsScreen(
                            onBackClick = { currentDestination = AppDestination.Dashboard },
                            onLogoutClick = {
                                scope.launch {
                                    tokenManager.clearToken()
                                }
                                currentDestination = AppDestination.Welcome
                            },
                            viewModel = settingsViewModel
                        )
                    }

                    is AppDestination.SyncStatus -> {
                        SyncStatusScreen(
                            syncViewModel = syncViewModel,
                            onBackClick = { currentDestination = AppDestination.Dashboard },
//                        onViewHistoryClick = { currentDestination = AppDestination.SyncHistory }
                        )
                    }

//                is AppDestination.SyncHistory -> {
//                    SyncHistoryScreen(
//                        onBackClick = { currentDestination = AppDestination.SyncStatus }
//                    )
//                }

                    is AppDestination.DataCollection -> {
                        DataCollectionFormScreen(
                            onBackClick = {
                                currentDestination = AppDestination.Dashboard
                            },
                            onSaveClick = { record ->
                                scope.launch(Dispatchers.IO) {
                                    recordRepository.saveRecordForCurrentUser(record)
                                }
                                Toast.makeText(
                                    applicationContext,
                                    "Record saved locally",
                                    Toast.LENGTH_SHORT
                                ).show()
                                currentDestination = AppDestination.Dashboard
                            }
                        )
                    }

                    is AppDestination.Analytics -> {
                        AnalyticsScreen(
                            onBackClick = { currentDestination = AppDestination.Dashboard }
                        )
                    }

                    is AppDestination.UserRecords -> {
                        RecordsScreen(
                            onBackClick = { currentDestination = AppDestination.Dashboard }
                        )
                    }

                    is AppDestination.Calender -> {
                        CalendarScreen(
                            calendarViewModel = calendarViewModel,
                            onAddRecordForDate = { pickedDate ->
                                selectedCollectionDate = pickedDate
                                currentDestination = AppDestination.DataCollection
                            },
                            onBackClick = { currentDestination = AppDestination.Dashboard }


                        )
                    }


                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    AdaptiveofflinesyncDatauiTheme {
        WelcomeScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    AdaptiveofflinesyncDatauiTheme {
        LoginScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    AdaptiveofflinesyncDatauiTheme {
        SplashScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    AdaptiveofflinesyncDatauiTheme {
        DashboardScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun DataCollectionPreview() {
    AdaptiveofflinesyncDatauiTheme {
        DataCollectionFormScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun SyncHistoryPreview() {
    AdaptiveofflinesyncDatauiTheme {
        SyncHistoryScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    AdaptiveofflinesyncDatauiTheme {
        SignUpScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    AdaptiveofflinesyncDatauiTheme {
        SettingsScreen()
    }
}