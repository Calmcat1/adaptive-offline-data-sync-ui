package com.example.adaptive_offline_sync_data_ui

import android.os.Bundle
import android.window.SplashScreenView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.adaptive_offline_sync_data_ui.ui.theme.AdaptiveofflinesyncDatauiTheme
import com.example.adaptive_offline_sync_data_ui.screens.LoginScreen
import com.example.adaptive_offline_sync_data_ui.screens.SplashScreen
import com.example.adaptive_offline_sync_data_ui.screens.DashboardScreen
import com.example.adaptive_offline_sync_data_ui.screens.DataCollectionFormScreen
import com.example.adaptive_offline_sync_data_ui.screens.SettingsScreen
import com.example.adaptive_offline_sync_data_ui.screens.SyncHistoryScreen
import com.example.adaptive_offline_sync_data_ui.screens.SyncStatusScreen
import com.example.adaptive_offline_sync_data_ui.screens.SignUpScreen
import java.lang.Compiler.enable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginScreen()
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
fun LoginScreenPreview() {
    AdaptiveofflinesyncDatauiTheme {
        LoginScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview(){
    AdaptiveofflinesyncDatauiTheme() {
        SplashScreen()
    }
}


@Preview(showBackground = true)
@Composable
fun DashboardPreview(){
AdaptiveofflinesyncDatauiTheme() {
    DashboardScreen()
}
}


@Preview(showBackground = true)
@Composable
fun DataCollectionPreview(){
    AdaptiveofflinesyncDatauiTheme() {
        DataCollectionFormScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun SyncStatusPreview(){
    AdaptiveofflinesyncDatauiTheme() {
        SyncStatusScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun SyncHistoryPreview(){
    AdaptiveofflinesyncDatauiTheme() {
        SyncHistoryScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview(){
    AdaptiveofflinesyncDatauiTheme(){
        SignUpScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview(){
    AdaptiveofflinesyncDatauiTheme() {
        SettingsScreen()
    }
}



