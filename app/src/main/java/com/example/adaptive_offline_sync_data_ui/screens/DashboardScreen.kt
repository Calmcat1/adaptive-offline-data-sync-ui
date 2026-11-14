package com.example.adaptive_offline_sync_data_ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Data Models ---
data class SyncRecord(
    val id: String,
    val date: String,
    val status: SyncStatus
)

enum class SyncStatus(val label: String, val backgroundColor: Color, val contentColor: Color) {
    SUCCESS("Success", Color(0xFFE8F5E9), Color(0xFF2E7D32)),
    FAILURE("Failure", Color(0xFFFFEBEE), Color(0xFFC62828)),
    PENDING("Pending", Color(0xFFFFF8E1), Color(0xFFF57F17))
}

// --- Main Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onCollectDataClick: () -> Unit = {},
    onMyRecordsClick: () -> Unit = {},
    onSyncStatusClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val records = listOf(
        SyncRecord("Record #101", "12 Aug 2026, 14:30", SyncStatus.SUCCESS),
        SyncRecord("Record #202", "15 Aug 2026, 10:02", SyncStatus.FAILURE),
        SyncRecord("Record #303", "5 Sept 2026, 15:30", SyncStatus.PENDING)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Dashboard",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1A1A1A)
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFF8F9FA)
                )
            )
        },
        bottomBar = { BottomNavigationBar(onSettingsClick = onSettingsClick) },
        containerColor = Color(0xFFF8F9FA)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Modern Status Banner Card
            item { ModernStatusCard() }

            // Section Label
            item {
                Text(
                    text = "Quick Actions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1A1A1A)
                )
            }

            // 2x2 Grid Actions with Custom Icons & Colors
            item {
                ModernActionGridSection(
                    onCollectDataClick = onCollectDataClick,
                    onMyRecordsClick = onMyRecordsClick,
                    onSyncStatusClick = onSyncStatusClick,
                    onSettingsClick = onSettingsClick
                )
            }

            // Recent Sync Activity Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Activity",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF1A1A1A)
                    )
                    Text(
                        text = "View All",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2196F3),
                        modifier = Modifier.clickable { onSyncStatusClick() }
                    )
                }
            }

            // Sync Records List
            items(records) { record ->
                ModernRecordItemCard(record = record)
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

// --- Modern Top Status Card ---
@Composable
fun ModernStatusCard() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1E293B), // Slate dark header
                            Color(0xFF0F172A)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(Color(0xFF4ADE80).copy(alpha = alpha), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "System Online",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }

                    // Live Pill Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Auto-Sync Active",
                            color = Color(0xFF818CF8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "LAST SYNCHRONIZATION",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "10 minutes ago",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TelemetryBadge(icon = Icons.Default.Wifi, label = "4G")
                        TelemetryBadge(icon = Icons.Default.BatteryFull, label = "98%")
                    }
                }
            }
        }
    }
}

@Composable
private fun TelemetryBadge(icon: ImageVector, label: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.White.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// --- Modern 2x2 Action Tiles ---
@Composable
fun ModernActionGridSection(
    onCollectDataClick: () -> Unit = {},
    onMyRecordsClick: () -> Unit = {},
    onSyncStatusClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ModernActionTile(
                title = "Collect Data",
                subtitle = "New Entry",
                icon = Icons.Default.Add,
                accentColor = Color(0xFF2563EB),
                modifier = Modifier.weight(1f),
                onClick = onCollectDataClick
            )
            ModernActionTile(
                title = "My Records",
                subtitle = "Room Storage",
                icon = Icons.Default.Folder,
                accentColor = Color(0xFF059669),
                modifier = Modifier.weight(1f),
                onClick = onMyRecordsClick
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ModernActionTile(
                title = "Sync Status",
                subtitle = "Pending: 12",
                icon = Icons.Default.Sync,
                accentColor = Color(0xFFD97706),
                modifier = Modifier.weight(1f),
                onClick = onSyncStatusClick
            )
            ModernActionTile(
                title = "Settings",
                subtitle = "Preferences",
                icon = Icons.Default.Settings,
                accentColor = Color(0xFF4B5563),
                modifier = Modifier.weight(1f),
                onClick = onSettingsClick
            )
        }
    }
}

@Composable
fun ModernActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.height(96.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.LightGray,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

// --- Modern Record Row Item ---
@Composable
fun ModernRecordItemCard(record: SyncRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SyncAlt,
                    contentDescription = null,
                    tint = Color(0xFF475569),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.id,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = record.date,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = record.status.backgroundColor
            ) {
                Text(
                    text = record.status.label,
                    color = record.status.contentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// --- Modern Bottom Navigation Bar ---
@Composable
fun BottomNavigationBar(onSettingsClick: () -> Unit = {}) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF1E293B),
                indicatorColor = Color(0xFFF1F5F9)
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Outlined.CalendarToday, contentDescription = "Calendar") },
            label = { Text("Calendar", fontSize = 11.sp) }
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Outlined.BarChart, contentDescription = "Analytics") },
            label = { Text("Analytics", fontSize = 11.sp) }
        )
        NavigationBarItem(
            selected = false,
            onClick = onSettingsClick,
            icon = { Icon(Icons.Outlined.Settings, contentDescription = "Settings") },
            label = { Text("Settings", fontSize = 11.sp) }
        )
    }
}