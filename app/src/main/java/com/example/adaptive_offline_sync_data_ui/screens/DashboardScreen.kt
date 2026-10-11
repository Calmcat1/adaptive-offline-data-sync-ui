package com.example.adaptive_offline_sync_data_ui.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    SUCCESS("success", Color(0xFFB9F6CA), Color(0xFF1B5E20)),
    FAILURE("Failure", Color(0xFFFFCDD2), Color(0xFFB71C1C)),
    PENDING("Pending", Color(0xFFFFF59D), Color(0xFFF57F17))
}

// --- Main Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    val records = listOf(
        SyncRecord("record#101", "12 Aug 2026, 14:30", SyncStatus.SUCCESS),
        SyncRecord("record#202", "15 Aug 2026, 10:02", SyncStatus.FAILURE),
        SyncRecord("record#303", "5 Sept 2026, 15:30", SyncStatus.PENDING)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Field Collect",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /* Handle back click */ }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = { BottomNavigationBar() },
        containerColor = Color.White
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Banner
            item { StatusCard() }

            // 2x2 Grid Actions
            item { ActionGridSection() }

            // Records List Header / Spacer
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Sync Records
            items(records) { record ->
                RecordItemRow(record = record)
            }
        }
    }
}

// --- Top Status Card ---
@Composable
fun StatusCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F2F2)),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(Color(0xFF4CAF50), CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Online",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Last Sync: 10 mins ago",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
    }
}

// --- 2x2 Action Buttons Section ---
@Composable
fun ActionGridSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ActionTile(title = "Collect Data", modifier = Modifier.weight(1f))
            ActionTile(title = "My records", modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ActionTile(title = "Sync Status", modifier = Modifier.weight(1f))
            ActionTile(title = "Settings", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun ActionTile(title: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        border = CardDefaults.outlinedCardBorder(),
        onClick = { /* Tile click action */ }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.Black
            )
        }
    }
}

// --- Sync Record Row ---
@Composable
fun RecordItemRow(record: SyncRecord) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xFFE0E0E0), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SyncAlt,
                contentDescription = null,
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title and Date
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = record.id,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = record.date,
                color = Color.Gray,
                fontSize = 12.sp
            )
        }

        // Status Badge
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = record.status.backgroundColor
        ) {
            Text(
                text = record.status.label,
                color = record.status.contentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
    }
}

// --- Bottom Navigation Bar ---
@Composable
fun BottomNavigationBar() {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 4.dp
    ) {
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Default.CalendarToday, contentDescription = "Calendar") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color.Transparent, CircleShape)
                        .padding(1.dp)
                ) {
                    // Outlined circle indicator
                    Surface(
                        shape = CircleShape,
                        color = Color.Transparent,
                        border = androidx.compose.foundation.BorderStroke(2.dp, Color.Gray),
                        modifier = Modifier.fillMaxSize()
                    ) {}
                }
            }
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") }
        )
    }
}