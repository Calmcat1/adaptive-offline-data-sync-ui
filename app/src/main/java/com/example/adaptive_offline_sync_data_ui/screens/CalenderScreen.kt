package com.example.adaptive_offline_sync_data_ui.screens


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.adaptive_offline_sync_data_ui.data.entity.DataRecordEntity
import com.example.adaptive_offline_sync_data_ui.viewModel.CalendarViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    calendarViewModel: CalendarViewModel,
    onAddRecordForDate: (LocalDate) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val uiState by calendarViewModel.uiState.collectAsState()
    val dayRecords = uiState.selectedDayRecords
    val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Schedule & Records",
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


        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddRecordForDate(uiState.selectedDate) },
                containerColor = Color.Black,
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Record")
            }
        },
        containerColor = Color.White
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
        ) {
            // --- MONTHLY CALENDAR CARD ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        MonthHeader(
                            currentMonth = uiState.currentMonth,
                            onPreviousMonth = { calendarViewModel.previousMonth() },
                            onNextMonth = { calendarViewModel.nextMonth() }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        DaysOfWeekHeader()

                        Spacer(modifier = Modifier.height(6.dp))

                        MonthDaysGrid(
                            currentMonth = uiState.currentMonth,
                            selectedDate = uiState.selectedDate,
                            recordsByDate = uiState.recordsByDate,
                            onDateSelected = { calendarViewModel.selectDate(it) }
                        )
                    }
                }
            }

            // --- SELECTED DATE HEADER ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = uiState.selectedDate.format(dateFormatter).uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Text(
                        text = "${dayRecords.size} ${if (dayRecords.size == 1) "entry" else "entries"}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.DarkGray
                    )
                }
            }

            // --- SELECTED DAY RECORDS LIST ---
            if (dayRecords.isEmpty()) {
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
                            Icon(
                                imageVector = Icons.Default.EventNote,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No records on this date",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                            Text(
                                text = "Tap + to log a field record for ${uiState.selectedDate.format(dateFormatter)}",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(dayRecords, key = { it.clientRecordId }) { record ->
                    CalendarRecordCard(record = record)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun MonthHeader(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val monthTitle = "${
        currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    } ${currentMonth.year}"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous Month",
                tint = Color.Black
            )
        }
        Text(
            text = monthTitle,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color.Black
        )
        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next Month",
                tint = Color.Black
            )
        }
    }
}

@Composable
private fun DaysOfWeekHeader() {
    val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    Row(modifier = Modifier.fillMaxWidth()) {
        days.forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Gray
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun MonthDaysGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    recordsByDate: Map<LocalDate, List<DataRecordEntity>>,
    onDateSelected: (LocalDate) -> Unit
) {
    val firstDayOfMonth = currentMonth.atDay(1)
    // Sunday = 0, Monday = 1, ..., Saturday = 6
    val startOffset = firstDayOfMonth.dayOfWeek.value % 7
    val totalDays = currentMonth.lengthOfMonth()
    val totalCells = ((startOffset + totalDays + 6) / 7) * 7

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (rowStart in 0 until totalCells step 7) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val cellIndex = rowStart + col
                    val dayNumber = cellIndex - startOffset + 1

                    if (dayNumber in 1..totalDays) {
                        val date = currentMonth.atDay(dayNumber)
                        val isSelected = date == selectedDate
                        val hasRecords = !recordsByDate[date].isNullOrEmpty()

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(if (isSelected) Color.Black else Color.Transparent)
                                .clickable { onDateSelected(date) },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = dayNumber.toString(),
                                fontSize = 13.sp,
                                fontWeight = if (isSelected || hasRecords) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color.Black
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .background(
                                        color = when {
                                            hasRecords && isSelected -> Color.White
                                            hasRecords -> Color(0xFF2E7D32)
                                            else -> Color.Transparent
                                        },
                                        shape = CircleShape
                                    )
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarRecordCard(record: DataRecordEntity) {
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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.location.ifBlank { "Data Record #${record.id}" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                if (record.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = record.notes,
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = record.recordTime.ifBlank { "All day" },
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
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