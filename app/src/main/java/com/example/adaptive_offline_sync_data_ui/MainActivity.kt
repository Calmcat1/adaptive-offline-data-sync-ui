package com.example.adaptive_offline_sync_data_ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.example.adaptive_offline_sync_data_ui.data.local.AppDatabase
import com.example.adaptive_offline_sync_data_ui.data.local.TokenManager
import com.example.adaptive_offline_sync_data_ui.data.repository.DataRepository
import com.example.adaptive_offline_sync_data_ui.screens.AppDestination
import com.example.adaptive_offline_sync_data_ui.screens.DashboardScreen
import com.example.adaptive_offline_sync_data_ui.screens.DataCollectionFormScreen
import com.example.adaptive_offline_sync_data_ui.screens.LoginScreen
import com.example.adaptive_offline_sync_data_ui.screens.SettingsScreen
import com.example.adaptive_offline_sync_data_ui.screens.SignUpScreen
import com.example.adaptive_offline_sync_data_ui.screens.SplashScreen
import com.example.adaptive_offline_sync_data_ui.screens.SyncHistoryScreen
import com.example.adaptive_offline_sync_data_ui.screens.SyncStatusScreen
import com.example.adaptive_offline_sync_data_ui.screens.WelcomeScreen
import com.example.adaptive_offline_sync_data_ui.ui.theme.AdaptiveofflinesyncDatauiTheme
import com.example.adaptive_offline_sync_data_ui.viewModel.DataCollectionViewModel
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tokenManager = TokenManager(applicationContext)
        val database = AppDatabase.getDatabase(applicationContext)


        setContent {
            var currentDestination by remember { mutableStateOf<AppDestination>(AppDestination.Splash) }
            val token by tokenManager.tokenFlow.collectAsState(initial = null)
            val scope = rememberCoroutineScope()

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
                        onLoginSuccess = { token, name, email ->
                            // Save Token, Name, and Email to DataStore
                            scope.launch {
                                tokenManager.saveUserSession(
                                    token = token,
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
                         onCollectDataClick = { currentDestination = AppDestination.DataCollection },
//                       onMyRecordsClick = { currentDestination = AppDestination.MyRecords },
                         onSyncStatusClick = { currentDestination = AppDestination.SyncStatus },
                         onSettingsClick = { currentDestination = AppDestination.Settings }
                    )
                }



                is AppDestination.Settings -> {
                    SettingsScreen(
                        onBackClick = { currentDestination = AppDestination.Dashboard}
                    )
                }

                is AppDestination.SyncStatus -> {
                    SyncStatusScreen(
                        onBackClick = {currentDestination = AppDestination.Dashboard}
                    )
                }

                is AppDestination.DataCollection -> {
                    DataCollectionFormScreen(
                        onBackClick = {
                            currentDestination = AppDestination.Dashboard
                        },
                        onSaveClick = { record ->
                            scope.launch(Dispatchers.IO) {
                                 val db = null
                                db.dataRecordDao().insert(record)
                            }
                            Toast.makeText(applicationContext, "Record saved locally", Toast.LENGTH_SHORT).show()
                            currentDestination = AppDestination.Dashboard
                        }
                    )
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
fun SplashScreenPreview(){
    AdaptiveofflinesyncDatauiTheme {
        SplashScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview(){
    AdaptiveofflinesyncDatauiTheme {
        DashboardScreen()
    }
}

@Preview(showBackground = true)
@Composable
//fun DataCollectionPreview(){
//    AdaptiveofflinesyncDatauiTheme {
//        DataCollectionFormScreen()
//    }
//}

@Preview(showBackground = true)
fun SyncStatusPreview(){
    AdaptiveofflinesyncDatauiTheme {
        SyncStatusScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun SyncHistoryPreview(){
    AdaptiveofflinesyncDatauiTheme {
        SyncHistoryScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview(){
    AdaptiveofflinesyncDatauiTheme {
        SignUpScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview(){
    AdaptiveofflinesyncDatauiTheme {
        SettingsScreen()
    }
}

