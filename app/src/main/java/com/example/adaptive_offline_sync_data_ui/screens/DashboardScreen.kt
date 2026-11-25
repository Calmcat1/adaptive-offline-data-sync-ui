package com.example.adaptive_offline_sync_data_ui.screens

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.adaptive_offline_sync_data_ui.data.entity.DataRecordEntity
import com.example.adaptive_offline_sync_data_ui.data.entity.TelemetryEntity
import com.example.adaptive_offline_sync_data_ui.viewModel.RecordsUiState
import com.example.adaptive_offline_sync_data_ui.viewModel.RecordsViewModel
import com.example.adaptive_offline_sync_data_ui.viewModel.SettingsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --- Main Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onCollectDataClick: () -> Unit = {},
    onMyRecordsClick: () -> Unit = {},
    onSyncStatusClick: () -> Unit = {},
    onAnalyticsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onCalenderClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    context: Application = LocalContext.current.applicationContext as Application,
    settingsViewModel: SettingsViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(context)
    ),
    recordsViewModel: RecordsViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(context)
    )
) {
    val telemetry by settingsViewModel.latestTelemetry.collectAsState()
    val recordsState by recordsViewModel.uiState.collectAsState()

    val userRecords = (recordsState as? RecordsUiState.Success)?.records ?: emptyList()
    val pendingCount = userRecords.count { !it.isSynced }
    val recentRecords = userRecords.take(5)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Dashboard",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(
                onSettingsClick = onSettingsClick,
                onAnalyticsClick = onAnalyticsClick,
                onCalenderClick = onCalenderClick
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // --- SYSTEM STATUS (LIVE TELEMETRY) ---
            item {
                Column {
                    DashboardSectionHeader(title = "SYSTEM STATUS")
                    SystemStatusCard(telemetry = telemetry)
                }
            }

            // --- QUICK ACTIONS ---
            item {
                Column {
                    DashboardSectionHeader(title = "QUICK ACTIONS")
                    ActionGridSection(
                        totalRecordsCount = userRecords.size,
                        pendingRecordsCount = pendingCount,
                        onCollectDataClick = onCollectDataClick,
                        onMyRecordsClick = onMyRecordsClick,
                        onSyncStatusClick = onSyncStatusClick,
                        onSettingsClick = onSettingsClick
                    )
                }
            }

            // --- RECENT ACTIVITY (LIVE ROOM RECORDS) ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT ACTIVITY (${recentRecords.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Text(
                        text = "View All",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.clickable { onMyRecordsClick() }
                    )
                }
            }

            when (recordsState) {
                is RecordsUiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color.Black,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }

                is RecordsUiState.Error -> {
                    item {
                        Text(
                            text = (recordsState as RecordsUiState.Error).message,
                            color = Color(0xFFC62828),
                            fontSize = 13.sp
                        )
                    }
                }

                is RecordsUiState.Success -> {
                    if (recentRecords.isEmpty()) {
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
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "No field records yet",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color.Black
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Tap Collect Data above to log your first offline record.",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    } else {
                        items(recentRecords, key = { it.id }) { record ->
                            DashboardRoomRecordCard(
                                record = record,
                                onClick = onMyRecordsClick
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

// --- System Status Card (Bound to Live Room Telemetry) ---
@Composable
fun SystemStatusCard(telemetry: TelemetryEntity? = null) {
    val isOnline = telemetry != null && telemetry.networkType.lowercase() != "offline"
    val networkLabel = telemetry?.networkType ?: "Offline"
    val latencyMs = telemetry?.latencyMs ?: 0
    val signalStrength = telemetry?.signalStrength ?: 0
    val batteryPercent = telemetry?.batteryPercent ?: 100

    val formattedTimestamp = if (telemetry != null) {
        SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date(telemetry.timestamp))
    } else {
        "Awaiting telemetry..."
    }

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
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (isOnline) Color(0xFF4CAF50) else Color(0xFFC62828),
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isOnline) "System Online" else "System Offline",
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
                        text = if (isOnline) "Auto-Sync Active" else "Queued Locally",
                        color = if (isOnline) Color(0xFF2E7D32) else Color.DarkGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

//            HorizontalDivider(
//                modifier = Modifier.padding(vertical = 12.dp),
//                color = Color(0xFFEEEEEE)
//            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
//                TelemetryChip(
//                    icon = Icons.Default.Wifi,
//                    label = networkLabel,
//                    modifier = Modifier.weight(1f)
//                )
//                TelemetryChip(
//                    icon = Icons.Default.Speed,
//                    label = "${latencyMs}ms",
//                    valueColor = when {
//                        !isOnline -> Color.Gray
//                        latencyMs < 100 -> Color(0xFF2E7D32)
//                        latencyMs < 300 -> Color(0xFFF57F17)
//                        else -> Color(0xFFC62828)
//                    },
//                    modifier = Modifier.weight(1f)
//                )
//                TelemetryChip(
//                    icon = Icons.Default.SignalCellularAlt,
//                    label = "$signalStrength%",
//                    modifier = Modifier.weight(1f)
//                )
//                TelemetryChip(
//                    icon = Icons.Default.BatteryFull,
//                    label = "$batteryPercent%",
//                    valueColor = if (batteryPercent <= 20) Color(0xFFC62828) else Color.Black,
//                    modifier = Modifier.weight(1f)
//                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Last Telemetry Sample",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                Text(
                    text = formattedTimestamp,
                    color = Color.DarkGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun TelemetryChip(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: Color = Color.Black
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.DarkGray,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = valueColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

// --- 2x2 Action Grid (Dynamic Counts from Room) ---
@Composable
fun ActionGridSection(
    totalRecordsCount: Int = 0,
    pendingRecordsCount: Int = 0,
    onCollectDataClick: () -> Unit = {},
    onMyRecordsClick: () -> Unit = {},
    onSyncStatusClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ActionTile(
                title = "Collect Data",
                subtitle = "New Entry",
                icon = Icons.Default.Add,
                modifier = Modifier.weight(1f),
                onClick = onCollectDataClick
            )
            ActionTile(
                title = "My Records",
                subtitle = "$totalRecordsCount Stored",
                icon = Icons.Default.Folder,
                modifier = Modifier.weight(1f),
                onClick = onMyRecordsClick
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ActionTile(
                title = "Sync Status",
                subtitle = "Pending: $pendingRecordsCount",
                icon = Icons.Default.Sync,
                modifier = Modifier.weight(1f),
                onClick = onSyncStatusClick
            )
            ActionTile(
                title = "Settings",
                subtitle = "Preferences",
                icon = Icons.Default.Settings,
                modifier = Modifier.weight(1f),
                onClick = onSettingsClick
            )
        }
    }
}

@Composable
fun ActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.height(92.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        border = CardDefaults.outlinedCardBorder(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFE0E0E0), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

// --- Live Room Record Row Card ---
@Composable
fun DashboardRoomRecordCard(
    record: DataRecordEntity,
    onClick: () -> Unit = {}
) {
    val formattedDate = remember(record.recordDate) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(record.recordDate))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        border = CardDefaults.outlinedCardBorder(),
        onClick = onClick
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
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.DarkGray,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.location.ifBlank { "Record #${record.id}" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$formattedDate • ${record.recordTime}",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (record.isSynced) Color(0xFFE8F5E9) else Color(0xFFFFF8E1)
            ) {
                Text(
                    text = if (record.isSynced) "Synced" else "Pending",
                    color = if (record.isSynced) Color(0xFF2E7D32) else Color(0xFFF57F17),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// --- Bottom Navigation Bar ---
@Composable
fun BottomNavigationBar(
    onSettingsClick: () -> Unit = {},
    onAnalyticsClick: () -> Unit = {},
    onCalenderClick: () -> Unit = {}
) {
    Column {
        HorizontalDivider(color = Color(0xFFEEEEEE))
        NavigationBar(
            containerColor = Color.White,
            tonalElevation = 0.dp
        ) {
            NavigationBarItem(
                selected = true,
                onClick = { },
                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    selectedTextColor = Color.Black,
                    indicatorColor = Color(0xFFEEEEEE),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
            NavigationBarItem(
                selected = false,
                onClick = onCalenderClick,
                icon = { Icon(Icons.Outlined.CalendarToday, contentDescription = "Calendar") },
                label = { Text("Calendar", fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
            NavigationBarItem(
                selected = false,
                onClick = onAnalyticsClick,
                icon = { Icon(Icons.Outlined.BarChart, contentDescription = "Analytics") },
                label = { Text("Analytics", fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
            NavigationBarItem(
                selected = false,
                onClick = onSettingsClick,
                icon = { Icon(Icons.Outlined.Settings, contentDescription = "Settings") },
                label = { Text("Settings", fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
        }
    }
}

@Composable
private fun DashboardSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Gray,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}