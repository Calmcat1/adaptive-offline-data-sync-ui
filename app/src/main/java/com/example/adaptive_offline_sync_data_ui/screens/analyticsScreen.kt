package com.example.adaptive_offline_sync_data_ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.adaptive_offline_sync_data_ui.data.remote.MetricsSummary
import com.example.adaptive_offline_sync_data_ui.viewModel.AnalyticsViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onBackClick: () -> Unit = {},
    viewModel: AnalyticsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val adaptive = state.adaptive
    val baseline = state.baseline

    // Compute live deltas between Adaptive and Baseline for KPI subtitles
    val successRateText = adaptive?.let { "${it.overallSuccessRatePct}%" } ?: "--"
    val successDeltaText = if (adaptive != null && baseline != null) {
        val diff = adaptive.overallSuccessRatePct - baseline.overallSuccessRatePct
        String.format(Locale.US, "%+.1f%% vs Baseline", diff)
    } else {
        "${adaptive?.totalRuns ?: 0} adaptive runs"
    }

    val latencyText = adaptive?.let { "${it.avgLatencyMs.toInt()} ms" } ?: "--"
    val latencySubtitle = baseline?.let { "Baseline: ${it.avgLatencyMs.toInt()} ms" } ?: "Awaiting baseline"

    val dataUsageText = adaptive?.let { "${it.avgDataUsageKb} KB" } ?: "--"
    val dataSubtitle = baseline?.let { "Baseline: ${it.avgDataUsageKb} KB" } ?: "Avg per sync"

    val batteryText = adaptive?.let { "${it.avgBatteryDrainPct}%" } ?: "--"
    val batterySubtitle = baseline?.let { "Baseline: ${it.avgBatteryDrainPct}%" } ?: "Drain per run"

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Evaluation Results",
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
                actions = {
                    IconButton(onClick = { viewModel.loadSummary() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Metrics",
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
            if (state.isLoading) {
                item {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.Black,
                        trackColor = Color(0xFFE0E0E0)
                    )
                }
            }

            state.errorMessage?.let { err ->
                item {
                    Text(
                        text = "Could not refresh metrics: $err",
                        color = Color(0xFFC62828),
                        fontSize = 13.sp
                    )
                }
            }

            // --- 4 CORE EVALUATION KPI CARDS ---
            item {
                AnalyticsSectionHeader(title = "ADAPTIVE ENGINE PERFORMANCE")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricKpiCard(
                        title = "Success Rate",
                        value = successRateText,
                        subtitle = successDeltaText,
                        icon = Icons.Default.TrendingUp,
                        modifier = Modifier.weight(1f)
                    )
                    MetricKpiCard(
                        title = "Avg Latency",
                        value = latencyText,
                        subtitle = latencySubtitle,
                        icon = Icons.Default.NetworkCheck,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricKpiCard(
                        title = "Avg Data Usage",
                        value = dataUsageText,
                        subtitle = dataSubtitle,
                        icon = Icons.Default.DataUsage,
                        modifier = Modifier.weight(1f)
                    )
                    MetricKpiCard(
                        title = "Battery Drain",
                        value = batteryText,
                        subtitle = batterySubtitle,
                        icon = Icons.Default.BatteryChargingFull,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // --- SIDE-BY-SIDE ADAPTIVE VS BASELINE TABLE ---
            item {
                AnalyticsSectionHeader(title = "ADAPTIVE VS. STATIC BASELINE COMPARISON")
                BaselineVsAdaptiveCard(adaptive = adaptive, baseline = baseline)
            }

            // --- RUN DISTRIBUTION BAR CHART ---
            item {
                val adaptiveRuns = adaptive?.totalRuns ?: 0
                val baselineRuns = baseline?.totalRuns ?: 0
                val maxRuns = maxOf(adaptiveRuns, baselineRuns, 1)

                AnalyticsSectionHeader(title = "EVALUATED SYNC RUNS BY STRATEGY")
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
                        TransportProgressRow(
                            label = "Adaptive Strategy ($adaptiveRuns runs)",
                            percentage = adaptiveRuns.toFloat() / maxRuns.toFloat(),
                            displayPercent = "$adaptiveRuns"
                        )
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                        TransportProgressRow(
                            label = "Static Baseline ($baselineRuns runs)",
                            percentage = baselineRuns.toFloat() / maxRuns.toFloat(),
                            displayPercent = "$baselineRuns"
                        )
                    }
                }
            }

            // --- LOCAL STORAGE FOOTPRINT ---
            item {
                AnalyticsSectionHeader(title = "LOCAL ROOM STORAGE")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(Color(0xFFE0E0E0), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "SQLite Database Size",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.Black
                                )
                                Text(
                                    text = "Telemetry + Field Records",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                        Text(
                            text = "1.42 MB",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BaselineVsAdaptiveCard(
    adaptive: MetricsSummary?,
    baseline: MetricsSummary?
) {
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
            // Header Row
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("Metric", modifier = Modifier.weight(1.4f), fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Text("Adaptive", modifier = Modifier.weight(1f), fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                Text("Baseline", modifier = Modifier.weight(1f), fontSize = 12.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
            }
            HorizontalDivider(color = Color(0xFFEEEEEE))

            ComparisonMetricRow(
                metric = "Total Runs",
                adaptiveVal = "${adaptive?.totalRuns ?: 0}",
                baselineVal = "${baseline?.totalRuns ?: 0}"
            )
            ComparisonMetricRow(
                metric = "Data Usage",
                adaptiveVal = "${adaptive?.avgDataUsageKb ?: 0.0} KB",
                baselineVal = "${baseline?.avgDataUsageKb ?: 0.0} KB"
            )
            ComparisonMetricRow(
                metric = "Sync Latency",
                adaptiveVal = "${adaptive?.avgLatencyMs ?: 0.0} ms",
                baselineVal = "${baseline?.avgLatencyMs ?: 0.0} ms"
            )
            ComparisonMetricRow(
                metric = "Battery Drain",
                adaptiveVal = "${adaptive?.avgBatteryDrainPct ?: 0.0}%",
                baselineVal = "${baseline?.avgBatteryDrainPct ?: 0.0}%"
            )
            ComparisonMetricRow(
                metric = "Success Rate",
                adaptiveVal = "${adaptive?.overallSuccessRatePct ?: 0.0}%",
                baselineVal = "${baseline?.overallSuccessRatePct ?: 0.0}%"
            )
        }
    }
}

@Composable
private fun ComparisonMetricRow(
    metric: String,
    adaptiveVal: String,
    baselineVal: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = metric, modifier = Modifier.weight(1.4f), fontSize = 13.sp, color = Color.DarkGray)
        Text(text = adaptiveVal, modifier = Modifier.weight(1f), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Text(text = baselineVal, modifier = Modifier.weight(1f), fontSize = 13.sp, color = Color.Gray)
    }
}

@Composable
private fun MetricKpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                Icon(imageVector = icon, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun TransportProgressRow(
    label: String,
    percentage: Float,
    displayPercent: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 13.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
            Text(text = displayPercent, fontSize = 13.sp, color = Color.Black, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { percentage.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = Color.Black,
            trackColor = Color(0xFFE0E0E0)
        )
    }
}

@Composable
private fun AnalyticsSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Gray,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}