package com.example.adaptive_offline_sync_data_ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncStatusScreen(onBackClick: () -> Unit = {}) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Synchronization status", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Large Blue Circle with Sync Icon
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(Color(0xFF0084FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SyncAlt,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Progress Text
            Text("Sync in progress...", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Uploading 3/5 records", color = Color.Gray, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { 0.3f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = Color(0xFF0084FF),
                trackColor = Color(0xFFBBDEFB),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Status Info Rows
            StatusDetailRow(
                icon = Icons.Default.Wifi,
                title = "Network",
                chipText = "4G (Good)",
                chipBgColor = Color(0xFFB9F6CA),
                chipTextColor = Color(0xFF1B5E20)
            )

            StatusDetailRow(
                icon = Icons.Default.SyncAlt,
                title = "Strategy",
                chipText = "Immediate sync",
                chipBgColor = Color(0xFFB9F6CA),
                chipTextColor = Color(0xFF1B5E20)
            )

            StatusDetailRow(
                icon = Icons.Default.SyncAlt,
                title = "Last sync",
                chipText = "10 min ago",
                chipBgColor = Color(0xFFB9F6CA),
                chipTextColor = Color(0xFF1B5E20)
            )

            StatusDetailRow(
                icon = Icons.Default.SyncAlt,
                title = "Next Sync",
                chipText = "in 5 mins",
                chipBgColor = Color(0xFFB9F6CA),
                chipTextColor = Color(0xFF1B5E20)
            )
        }
    }
}

@Composable
fun StatusDetailRow(
    icon: ImageVector,
    title: String,
    chipText: String,
    chipBgColor: Color,
    chipTextColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color(0xFFE0E0E0), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.Black)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = chipBgColor
        ) {
            Text(
                text = chipText,
                color = chipTextColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }
    }
}