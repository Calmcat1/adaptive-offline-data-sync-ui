package com.example.adaptive_offline_sync_data_ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataCollectionFormScreen(
    onBackClick: () -> Unit = {},
    onSaveClick: () -> Unit = {}
) {
    var location by remember { mutableStateOf("") }
    var dateTime by remember { mutableStateOf("") }
    var recordTime by remember { mutableStateOf("") }
    var messageData by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Field Collect", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Location
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Location", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            placeholder = { Text("Enter Location", color = Color.LightGray) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    }

                    // Date and Time
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Date and Time", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        OutlinedTextField(
                            value = dateTime,
                            onValueChange = { dateTime = it },
                            placeholder = { Text("DD-MM-YY", color = Color.LightGray) },
                            trailingIcon = {
                                Icon(Icons.Default.CalendarToday, contentDescription = "Select Date")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    }

                    // Record Time
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Record Time", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        OutlinedTextField(
                            value = recordTime,
                            onValueChange = { recordTime = it },
                            placeholder = { Text("Value", color = Color.LightGray) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    }

                    // Message Data
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Message Data", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        OutlinedTextField(
                            value = messageData,
                            onValueChange = { messageData = it },
                            placeholder = { Text("Value", color = Color.LightGray) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Save Button
                    Button(
                        onClick = onSaveClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0084FF))
                    ) {
                        Text("Save Record", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}