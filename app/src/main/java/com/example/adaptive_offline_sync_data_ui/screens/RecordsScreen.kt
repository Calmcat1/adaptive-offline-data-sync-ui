//package com.example.adaptive_offline_sync_data_ui.screens
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ArrowBack
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.lifecycle.viewmodel.compose.viewModel
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun RecordsScreen(
//    onBackClick: () -> Unit = {},
//    viewModel: RecordsViewModel = viewModel()
//
//
//) {
//    val uiState = viewModel.uiState
//
//    Scaffold(
//        topBar = {
//            CenterAlignedTopAppBar(
//                title = { Text("Records", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
//                navigationIcon = {
//                    IconButton(onClick = onBackClick) {
//                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
//                    }
//                },
//                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
//            )
//        },
//        containerColor = Color.White
//    ) { innerPadding ->
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(innerPadding)
//                .padding(horizontal = 24.dp)
//        ) {
//            when (uiState) {
//                is RecordsUiState.Loading -> {
//                    CircularProgressIndicator(
//                        modifier = Modifier.align(Alignment.Center),
//                        color = Color.Black
//                    )
//                }
//
//                is RecordsUiState.Error -> {
//                    Text(
//                        text = uiState.message,
//                        color = MaterialTheme.colorScheme.error,
//                        modifier = Modifier.align(Alignment.Center)
//                    )
//                }
//
//                is RecordsUiState.Success -> {
//                    if (uiState.records.isEmpty()) {
//                        Text(
//                            text = "No records found.",
//                            color = Color.Gray,
//                            modifier = Modifier.align(Alignment.Center)
//                        )
//                    } else {
//                        LazyColumn(
//                            modifier = Modifier.fillMaxSize(),
//                            verticalArrangement = Arrangement.spacedBy(12.dp),
//                            contentPadding = PaddingValues(vertical = 16.dp)
//                        ) {
//                            items(uiState.records, key = { it.id }) { record ->
//                                RecordCardItem(record = record)
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun RecordCardItem(record: RecordItem) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        shape = RoundedCornerShape(8.dp),
//        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
//        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Column(modifier = Modifier.weight(1f)) {
//                Text(
//                    text = record.title,
//                    fontWeight = FontWeight.Bold,
//                    fontSize = 15.sp,
//                    color = Color.Black
//                )
//                Spacer(modifier = Modifier.height(4.dp))
//                Text(
//                    text = record.subtitle,
//                    fontSize = 13.sp,
//                    color = Color.Gray
//                )
//                Spacer(modifier = Modifier.height(4.dp))
//                Text(
//                    text = record.date,
//                    fontSize = 11.sp,
//                    color = Color.LightGray
//                )
//            }
//
//            // Sync Status Badge
//            val isSynced = record.syncStatus.lowercase() == "synced"
//            Box(
//                modifier = Modifier
//                    .background(
//                        color = if (isSynced) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
//                        shape = RoundedCornerShape(12.dp)
//                    )
//                    .padding(horizontal = 10.dp, vertical = 4.dp)
//            ) {
//                Text(
//                    text = record.syncStatus,
//                    color = if (isSynced) Color(0xFF2E7D32) else Color(0xFFE65100),
//                    fontSize = 11.sp,
//                    fontWeight = FontWeight.Medium
//                )
//            }
//        }
//    }
//}