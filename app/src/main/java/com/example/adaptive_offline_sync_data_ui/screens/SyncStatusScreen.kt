package com.example.adaptive_offline_sync_data_ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.adaptive_offline_sync_data_ui.data.remote.OfflineRecord
import com.example.adaptive_offline_sync_data_ui.viewModel.SyncViewModel
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncStatusScreen(
    syncViewModel: SyncViewModel,
    onBackClick: () -> Unit = {},
    onViewHistoryClick: () -> Unit = {}
) {
    val uiState by syncViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val pendingRecords = uiState.pendingRecords
    val totalBytes = pendingRecords.sumOf { it.toString().toByteArray().size }
    val totalKb = (totalBytes / 1024.0).coerceAtLeast(if (pendingRecords.isEmpty()) 0.0 else 1.0).toInt()

    val isAdaptiveActive = uiState.selectedSyncMode == "ADAPTIVE"
    val isBaselineActive = uiState.selectedSyncMode == "BASELINE"

    // Live countdown driven by the adaptive/baseline policy's recommended interval
    val recommendedSeconds = uiState.activePolicy?.nextSyncIntervalSeconds
    var secondsRemaining by remember(
        recommendedSeconds,
        uiState.lastUploadedCount,
        uiState.lastDownloadedCount,
        uiState.statusMessage
    ) {
        mutableIntStateOf(recommendedSeconds ?: 0)
    }

    LaunchedEffect(recommendedSeconds, uiState.isAutoSyncEnabled, uiState.isSyncing) {
        if (recommendedSeconds != null && recommendedSeconds > 0 && uiState.isAutoSyncEnabled && !uiState.isSyncing) {
            secondsRemaining = recommendedSeconds
            while (secondsRemaining > 0) {
                delay(1000L)
                secondsRemaining--
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Sync Status",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
        ) {
            // --- MUTUALLY EXCLUSIVE STRATEGY SWITCHES ---
            item {
                SyncSectionHeader(title = "SYNCHRONIZATION STRATEGY")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        // Adaptive Sync Strategy Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Adaptive Sync Strategy",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.Black
                                )
                                Text(
                                    text = "Adjusts batch size & interval dynamically based on network and battery",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Switch(
                                checked = isAdaptiveActive,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        syncViewModel.selectSyncStrategy(context, "ADAPTIVE")
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color.Black,
                                    uncheckedThumbColor = Color.DarkGray,
                                    uncheckedTrackColor = Color(0xFFE0E0E0)
                                )
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = Color(0xFFEEEEEE)
                        )

                        // Baseline Sync Strategy Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Baseline Sync Strategy",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.Black
                                )
                                Text(
                                    text = "Fixed unthrottled full sync regardless of signal or battery conditions",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Switch(
                                checked = isBaselineActive,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        syncViewModel.selectSyncStrategy(context, "BASELINE")
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color.Black,
                                    uncheckedThumbColor = Color.DarkGray,
                                    uncheckedTrackColor = Color(0xFFE0E0E0)
                                )
                            )
                        }
                    }
                }
            }

            // --- AUTOMATIC SYNC SWITCH ---
            item {
                SyncSectionHeader(title = "AUTOMATION")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Automatic Synchronization",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                            Text(
                                text = if (uiState.isAutoSyncEnabled) {
                                    "Runs automatically when records change or connectivity returns"
                                } else {
                                    "Paused — records stay queued locally until re-enabled"
                                },
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Switch(
                            checked = uiState.isAutoSyncEnabled,
                            onCheckedChange = { enabled ->
                                syncViewModel.toggleAutoSync(context, enabled)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color.Black,
                                uncheckedThumbColor = Color.DarkGray,
                                uncheckedTrackColor = Color(0xFFE0E0E0)
                            )
                        )
                    }
                }
            }

            // --- ENGINE OVERVIEW ---
            item {
                SyncSectionHeader(title = "BIDIRECTIONAL SYNC ENGINE")
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (uiState.isSyncing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        color = Color.Black,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                color = if (uiState.errorMessage != null) Color(0xFFC62828) else Color(0xFF4CAF50),
                                                shape = CircleShape
                                            )
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (uiState.isSyncing) {
                                        "Syncing Automatically..."
                                    } else {
                                        uiState.activePolicy?.strategy ?: "Active & Monitoring"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.Black
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFEEEEEE)
                            ) {
                                Text(
                                    text = uiState.activePolicy?.syncDirection ?: uiState.selectedSyncMode,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.DarkGray,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = Color(0xFFEEEEEE)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Pending Upload", color = Color.DarkGray, fontSize = 13.sp)
                            Text(
                                text = "${pendingRecords.size} Records ($totalKb KB)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Last Transfer", color = Color.DarkGray, fontSize = 13.sp)
                            Text(
                                text = "↑ ${uiState.lastUploadedCount}  ↓ ${uiState.lastDownloadedCount}  ⚡ ${uiState.lastConflictsResolved}",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Variable Sync Interval & Live Countdown
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Recommended Interval", color = Color.DarkGray, fontSize = 13.sp)
                            Text(
                                text = if (recommendedSeconds != null) {
                                    if (uiState.isAutoSyncEnabled && secondsRemaining > 0) {
                                        "${formatSyncInterval(recommendedSeconds)} • Next in ${secondsRemaining}s"
                                    } else {
                                        formatSyncInterval(recommendedSeconds)
                                    }
                                } else {
                                    "Evaluating..."
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.Black
                            )
                        }

                        uiState.activePolicy?.let { policy ->
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Network Quality / Batch", color = Color.DarkGray, fontSize = 13.sp)
                                Text(
                                    text = "NQS ${"%.2f".format(policy.networkQualityScore)} • Max ${policy.recommendedBatchSize} recs",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = Color.Black
                                )
                            }

                            if (policy.rationale.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = policy.rationale,
                                    fontSize = 12.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }

                        if (!uiState.statusMessage.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = uiState.statusMessage!!,
                                fontSize = 12.sp,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (!uiState.errorMessage.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = uiState.errorMessage!!,
                                fontSize = 12.sp,
                                color = Color(0xFFC62828),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // --- OUTBOUND QUEUE ---
            item {
                Spacer(modifier = Modifier.height(4.dp))
                SyncSectionHeader(title = "OUTBOUND QUEUE (${pendingRecords.size})")
            }

            if (pendingRecords.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFFE8F5E9), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "All local records synchronized",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                            Text(
                                text = "New records and remote changes sync automatically.",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            } else {
                items(pendingRecords, key = { it.clientRecordId }) { item ->
                    OfflineQueueCard(
                        item = item,
                        hasError = uiState.errorMessage != null
                    )
                }
            }
        }
    }
}

private fun formatSyncInterval(seconds: Int): String {
    if (seconds < 60) return "${seconds}s"
    val mins = seconds / 60
    val remSecs = seconds % 60
    return if (remSecs == 0) "${seconds}s (${mins}m)" else "${seconds}s (${mins}m ${remSecs}s)"
}

@Composable
private fun OfflineQueueCard(
    item: OfflineRecord,
    hasError: Boolean
) {
    val payloadKb = (item.toString().toByteArray().size / 1024.0)
        .coerceAtLeast(1.0)
        .toInt()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFE0E0E0), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (hasError) Icons.Default.ErrorOutline else Icons.Default.CloudUpload,
                    contentDescription = null,
                    tint = if (hasError) Color(0xFFC62828) else Color.DarkGray,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${item.clientCreatedAt} • $payloadKb KB",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (hasError) Color(0xFFFFEBEE) else Color(0xFFFFF8E1)
            ) {
                Text(
                    text = if (hasError) "Retry Needed" else "Queued",
                    color = if (hasError) Color(0xFFC62828) else Color(0xFFF57F17),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun SyncSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Gray,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}