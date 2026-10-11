package com.example.adaptive_offline_sync_data_ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp

@Composable
@Preview(showBackground = true)
fun SplashScreen() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val splashScreenText : String = "ADAS"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "ADAS",
            fontSize = 40.sp
        )

        Text(
            text = "Adaptive Data Synchronization",
            fontSize = 18.sp
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        CircularProgressIndicator()


    }


}
