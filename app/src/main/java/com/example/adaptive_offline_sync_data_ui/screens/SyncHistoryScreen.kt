package com.example.adaptive_offline_sync_data_ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class RecordData(val id: String, val date: String, val status: RecordStatus)

enum class RecordStatus(val label: String, val bgColor: Color, val textColor: Color) {
    SUCCESS("success", Color(0xFFB9F6CA), Color(0xFF1B5E20)),
    FAILURE("Failure", Color(0xFFFFCDD2), Color(0xFFB71C1C)),
    PENDING("Pending", Color(0xFFFFF59D), Color(0xFFF57F17))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncHistoryScreen(onBackClick: () -> Unit = {}) {
    val records = listOf(
        RecordData("record#101", "12 Aug 2026, 14:30", RecordStatus.SUCCESS),
        RecordData("record#202", "15 Aug 2026, 10:02", RecordStatus.FAILURE),
        RecordData("record#303", "5 Sept 2026, 15:30", RecordStatus.PENDING),
        RecordData("record#404", "6 Sept 2026, 14:20", RecordStatus.SUCCESS),
        RecordData("record#505", "10 Sept 2027, 20:08", RecordStatus.SUCCESS)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My records", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(records) { record ->
                RecordRowItem(record = record)
            }
        }
    }
}

@Composable
fun RecordRowItem(record: RecordData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xFFE0E0E0), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.SyncAlt, contentDescription = null, tint = Color.White)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(record.id, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(record.date, color = Color.Gray, fontSize = 12.sp)
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = record.status.bgColor
        ) {
            Text(
                text = record.status.label,
                color = record.status.textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
    }
}