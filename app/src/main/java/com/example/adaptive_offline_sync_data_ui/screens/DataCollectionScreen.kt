package com.example.adaptive_offline_sync_data_ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notes
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.adaptive_offline_sync_data_ui.data.entity.DataRecordEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataCollectionFormScreen(
    onBackClick: () -> Unit = {},
    onSaveClick: (record: DataRecordEntity) -> Unit = {}
) {
    var location by remember { mutableStateOf("") }
    var selectedDateText by remember { mutableStateOf("") }
    var selectedTimeText by remember { mutableStateOf("") }
    var messageData by remember { mutableStateOf("") }
    var selectedDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDateMillis = millis
                        val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                        selectedDateText = formatter.format(Date(millis))
                    }
                    showDatePicker = false
                }) {
                    Text("OK", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    val currentTime = Calendar.getInstance()
    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = false
    )
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val hour = if (timePickerState.hour == 0 || timePickerState.hour == 12) 12 else timePickerState.hour % 12
                    val amPm = if (timePickerState.hour >= 12) "PM" else "AM"
                    selectedTimeText = String.format(Locale.getDefault(), "%02d:%02d %s", hour, timePickerState.minute, amPm)
                    showTimePicker = false
                }) {
                    Text("OK", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Data Collection", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            Column {
                Text(
                    text = "New Field Record",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Fill in details for offline data synchronization.",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Location", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            placeholder = { Text("Enter location / site name", color = Color.LightGray) },
                            leadingIcon = {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White,
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                focusedBorderColor = Color.Black
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Date", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Box {
                                OutlinedTextField(
                                    value = selectedDateText,
                                    onValueChange = {},
                                    readOnly = true,
                                    placeholder = { Text("DD-MM-YYYY", color = Color.LightGray) },
                                    leadingIcon = {
                                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.Gray)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedContainerColor = Color.White,
                                        focusedContainerColor = Color.White,
                                        unfocusedBorderColor = Color(0xFFE0E0E0),
                                        focusedBorderColor = Color.Black
                                    )
                                )
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable { showDatePicker = true }
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Record Time", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Box {
                                OutlinedTextField(
                                    value = selectedTimeText,
                                    onValueChange = {},
                                    readOnly = true,
                                    placeholder = { Text("hh:mm AM/PM", color = Color.LightGray) },
                                    leadingIcon = {
                                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color.Gray)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedContainerColor = Color.White,
                                        focusedContainerColor = Color.White,
                                        unfocusedBorderColor = Color(0xFFE0E0E0),
                                        focusedBorderColor = Color.Black
                                    )
                                )
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable { showTimePicker = true }
                                )
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Message / Notes Data", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        OutlinedTextField(
                            value = messageData,
                            onValueChange = { messageData = it },
                            placeholder = { Text("Enter observations, field notes, or status message...", color = Color.LightGray) },
                            leadingIcon = {
                                Icon(Icons.Default.Notes, contentDescription = null, tint = Color.Gray)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White,
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                focusedBorderColor = Color.Black
                            )
                        )
                    }
                }
            }

            Button(
                onClick = {
                    val record = DataRecordEntity(
                        location = location,
                        recordDate = selectedDateMillis,
                        recordTime = selectedTimeText,
                        notes = messageData,
                        isSynced = false
                    )
                    onSaveClick(record)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = location.isNotBlank() && selectedDateText.isNotBlank() && selectedTimeText.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFE0E0E0),
                    disabledContentColor = Color.Gray
                )
            ) {
                Text(
                    text = "Save Record",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}