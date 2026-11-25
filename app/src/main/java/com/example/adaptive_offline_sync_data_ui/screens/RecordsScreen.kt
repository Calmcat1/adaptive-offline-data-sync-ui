package com.example.adaptive_offline_sync_data_ui.screens

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.adaptive_offline_sync_data_ui.data.entity.DataRecordEntity
import com.example.adaptive_offline_sync_data_ui.viewModel.RecordsUiState
import com.example.adaptive_offline_sync_data_ui.viewModel.RecordsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsScreen(
    onBackClick: () -> Unit = {},
    context: Application = LocalContext.current.applicationContext as Application,
    viewModel: RecordsViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(context)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Synced", "Pending")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "My Records",
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            when (val state = uiState) {
                is RecordsUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Black
                    )
                }

                is RecordsUiState.Error -> {
                    Text(
                        text = state.message,
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is RecordsUiState.Success -> {
                    val filteredRecords = when (selectedFilter) {
                        "Synced" -> state.records.filter { it.isSynced }
                        "Pending" -> state.records.filter { !it.isSynced }
                        else -> state.records
                    }
                    val syncedCount = state.records.count { it.isSynced }
                    val pendingCount = state.records.size - syncedCount

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
                    ) {
                        // --- USER STORAGE SUMMARY ---
                        item {
                            RecordsSectionHeader(title = "USER REPOSITORY")
                            UserRecordsSummaryCard(
                                userEmail = state.userEmail,
                                totalCount = state.records.size,
                                syncedCount = syncedCount,
                                pendingCount = pendingCount
                            )
                        }

                        // --- FILTER TABS ---
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            RecordsSectionHeader(title = "FIELD ENTRIES (${filteredRecords.size})")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                filters.forEach { filter ->
                                    val isSelected = selectedFilter == filter
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedFilter = filter },
                                        label = {
                                            Text(
                                                text = filter,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            )
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = Color(0xFFFAFAFA),
                                            labelColor = Color.DarkGray,
                                            selectedContainerColor = Color.Black,
                                            selectedLabelColor = Color.White
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = isSelected,
                                            borderColor = Color(0xFFE0E0E0),
                                            selectedBorderColor = Color.Black
                                        )
                                    )
                                }
                            }
                        }

                        // --- EMPTY STATE OR RECORD CARDS ---
                        if (filteredRecords.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                                    border = CardDefaults.outlinedCardBorder()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(Color(0xFFE0E0E0), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.FolderOpen,
                                                contentDescription = null,
                                                tint = Color.DarkGray
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "No records found",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color.Black
                                        )
                                        Text(
                                            text = "Entries collected under ${state.userEmail} will appear here.",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        } else {
                            items(filteredRecords, key = { it.id }) { record ->
                                UserRecordCardItem(record = record)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserRecordsSummaryCard(
    userEmail: String,
    totalCount: Int,
    syncedCount: Int,
    pendingCount: Int
) {
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Active Account",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = userEmail,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEEEEEE)
                ) {
                    Text(
                        text = "$totalCount Total",
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF4CAF50), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Synced: $syncedCount",
                        fontSize = 13.sp,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFFF57F17), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Pending: $pendingCount",
                        fontSize = 13.sp,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun UserRecordCardItem(record: DataRecordEntity) {
    val formattedDate = remember(record.recordDate) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(record.recordDate))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFFE0E0E0), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SyncAlt,
                            contentDescription = null,
                            tint = Color.DarkGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = record.location.ifBlank { "Record #${record.id}" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
                                text = "$formattedDate • ${record.recordTime}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Sync Status Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (record.isSynced) Color(0xFFE8F5E9) else Color(0xFFFFF8E1)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (record.isSynced) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = if (record.isSynced) "Synced" else "Pending",
                            color = if (record.isSynced) Color(0xFF2E7D32) else Color(0xFFF57F17),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (record.notes.isNotBlank()) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = Color(0xFFEEEEEE)
                )
                Text(
                    text = record.notes,
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun RecordsSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Gray,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}