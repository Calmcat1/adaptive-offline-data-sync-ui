package com.example.adaptive_offline_sync_data_ui.screens

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.adaptive_offline_sync_data_ui.viewModel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    context: Application = LocalContext.current.applicationContext as Application,
    viewModel: SettingsViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(context)
    )
) {
    var autoSyncEnabled by remember { mutableStateOf(true) }
    var syncAlertsEnabled by remember { mutableStateOf(true) }
    var isExporting by remember { mutableStateOf(false) }

    val currentContext = LocalContext.current
    val userName by viewModel.userName.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val telemetry by viewModel.latestTelemetry.collectAsState()

    if (isExporting) {
        AlertDialog(
            onDismissRequest = { },
            confirmButton = { },
            title = { Text("Exporting CSV", fontWeight = FontWeight.Bold) },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(color = Color.Black)
                    Text("Generating dataset files...")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // --- ACCOUNT ---
            SectionHeader(title = "ACCOUNT")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFFE0E0E0), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = userName.ifEmpty{"Data Officer"},
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = userEmail.ifEmpty{"dataofficer@gmail.com"},
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- DATA EXPORT / DATASETS ---
            SectionHeader(title = "DATA EXPORT & DATASETS")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Export collected Room database records as CSV files to device storage.",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
//
//                    // Button 1: Field Records CSV
//                    OutlinedButton(
//                        onClick = {
//                            isExporting = true
//                            viewModel.exportFieldRecordsCsv { filePath ->
//                                isExporting = false
//                                val msg = if (filePath != null) "Exported: $filePath" else "Export failed"
//                                Toast.makeText(currentContext, msg, Toast.LENGTH_LONG).show()
//                            }
//                        },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(44.dp),
//                        shape = RoundedCornerShape(8.dp),
//                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
//                    ) {
//                        Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(18.dp))
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text("Export Field Records (CSV)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
//                    }

                    // Button 2: Telemetry Log CSV
                    OutlinedButton(
                        onClick = {
                            isExporting = true
                            viewModel.exportTelemetryCsv { filePath ->
                                isExporting = false
                                val msg = if (filePath != null) "Exported: $filePath" else "Export failed"
                                Toast.makeText(currentContext, msg, Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export Telemetry Log (CSV)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- SYNCHRONIZATION ---
            SectionHeader(title = "SYNCHRONIZATION")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sync Status", color = Color.DarkGray, fontSize = 14.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF4CAF50), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Online",
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEEEEEE))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Pending Records", color = Color.DarkGray, fontSize = 14.sp)
                        Text("12", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEEEEEE))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Last Sync", color = Color.DarkGray, fontSize = 14.sp)
                        Text("7:42 PM", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Automatic Synchronization",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                Switch(
                    checked = autoSyncEnabled,
                    onCheckedChange = { autoSyncEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color.Black
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- NETWORK (ROOM METRICS) ---
            SectionHeader(title = "NETWORK & SYSTEM METRICS")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = if (telemetry != null) Color(0xFF4CAF50) else Color.Gray,
                                        shape = CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = telemetry?.networkType ?: "Mobile Data",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.Black
                            )
                        }
                        Text(
                            text = if (telemetry != null) "Active" else "Offline",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    HorizontalDivider(color = Color(0xFFEEEEEE))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Latency", color = Color.DarkGray, fontSize = 13.sp)
                        Text(
                            text = "${telemetry?.latencyMs ?: 184} ms",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = when {
                                (telemetry?.latencyMs ?: 184) < 100 -> Color(0xFF2E7D32)
                                (telemetry?.latencyMs ?: 184) < 300 -> Color(0xFFF57F17)
                                else -> Color(0xFFC62828)
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Signal Strength", color = Color.DarkGray, fontSize = 13.sp)
                        Text(
                            text = "${telemetry?.signalStrength ?: 72}%",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Color.Black
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Battery Level", color = Color.DarkGray, fontSize = 13.sp)
                        Text(
                            text = "${telemetry?.batteryPercent ?: 100}%",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = when {
                                (telemetry?.batteryPercent ?: 100) > 20 -> Color.Black
                                else -> Color(0xFFC62828)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- NOTIFICATIONS ---
            SectionHeader(title = "NOTIFICATIONS")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Synchronization Alerts", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Switch(
                    checked = syncAlertsEnabled,
                    onCheckedChange = { syncAlertsEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color.Black
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- ABOUT ---
            SectionHeader(title = "ABOUT")
            Text("Version 1.0.0", color = Color.Gray, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(32.dp))

            // Logout Button
            OutlinedButton(
                onClick = onLogoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)

            ) {
                Text("Logout", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Gray,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}