package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.DataPoint
import com.example.ui.components.SensorChartCard
import com.example.ui.theme.AgriBlueSecondary
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusWarning
import com.example.ui.viewmodel.VibraTraceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(viewModel: VibraTraceViewModel) {
    val batches by viewModel.allBatches.collectAsStateWithLifecycle()
    val allAlerts by viewModel.allAlerts.collectAsStateWithLifecycle()
    val tamperEvents by viewModel.allTamperEvents.collectAsStateWithLifecycle()
    val readings by viewModel.readingsForActiveBatch.collectAsStateWithLifecycle()
    var selectedDateFilter by remember { mutableStateOf("7 Days") }

    val totalBatches = batches.size
    val verifiedBatches = batches.count { it.integrityStatus == "VERIFIED" }
    val activeShipments = batches.count { it.currentStage != "BUYER" }

    val sortedReadings = remember(readings) { readings.sortedBy { it.timestamp } }
    val avgTemp = if (sortedReadings.isNotEmpty()) sortedReadings.map { it.temperature }.average() else 6.8
    val minTemp = sortedReadings.minOfOrNull { it.temperature } ?: 6.5
    val maxTemp = sortedReadings.maxOfOrNull { it.temperature } ?: 7.2
    val avgHum = if (sortedReadings.isNotEmpty()) sortedReadings.map { it.humidity }.average() else 72.0
    val maxEthylene = sortedReadings.mapNotNull { it.ethylene }.maxOrNull() ?: 0.42
    val maxNh3 = sortedReadings.maxOfOrNull { it.ammonia } ?: 1.80

    val criticalAlerts = allAlerts.count { it.severity == "CRITICAL" }
    val warningAlerts = allAlerts.count { it.severity == "WARNING" }

    val tempData = remember(sortedReadings) { sortedReadings.map { DataPoint(it.timestamp, it.temperature) } }
    val ethyleneData = remember(sortedReadings) { sortedReadings.mapNotNull { r -> r.ethylene?.let { DataPoint(r.timestamp, it) } } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Date Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Supply Chain Analytics",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Today", "7 Days", "30 Days").forEach { f ->
                        val isSelected = selectedDateFilter == f
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedDateFilter = f },
                            label = { Text(f, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AgriGreenPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Key KPI Stat Grid
        item {
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Fleet & Shipment KPIs",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        KpiItem("Total Batches", "$totalBatches", AgriGreenPrimary)
                        KpiItem("Verified Batches", "$verifiedBatches", AgriBlueSecondary)
                        KpiItem("Active In-Transit", "$activeShipments", MaterialTheme.colorScheme.onSurface)
                        KpiItem("Tamper Events", "${tamperEvents.size}", if (tamperEvents.isEmpty()) Color.Gray else StatusCritical)
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        KpiItem("Avg Temp", "${String.format("%.1f", avgTemp)}°C", AgriGreenPrimary)
                        KpiItem("Temp Range", "${String.format("%.1f", minTemp)} - ${String.format("%.1f", maxTemp)}°C", MaterialTheme.colorScheme.onSurface)
                        KpiItem("Max C₂H₄", "${String.format("%.2f", maxEthylene)} ppm", if (maxEthylene > 0.5) StatusCritical else AgriGreenPrimary)
                        KpiItem("Max NH₃", "${String.format("%.2f", maxNh3)} ppm", if (maxNh3 > 2.0) StatusCritical else AgriGreenPrimary)
                    }
                }
            }
        }

        // Alert Severity Breakdown
        item {
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Alert Severity Distribution",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        SeverityPill("CRITICAL", criticalAlerts, StatusCritical)
                        SeverityPill("WARNING", warningAlerts, StatusWarning)
                        SeverityPill("RESOLVED", allAlerts.count { it.isResolved }, AgriGreenPrimary)
                    }
                }
            }
        }

        // Charts
        item {
            SensorChartCard(
                title = "Historical Temperature Trend",
                unit = "°C",
                dataPoints = tempData,
                lineColor = AgriGreenPrimary,
                threshold = 10.0
            )
        }

        item {
            SensorChartCard(
                title = "Ethylene Accumulation Trend",
                unit = "ppm",
                dataPoints = ethyleneData,
                lineColor = Color(0xFF6A1B9A),
                threshold = 0.50
            )
        }
    }
}

@Composable
private fun KpiItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = color)
        Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SeverityPill(title: String, count: Int, color: Color) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.15f),
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "$count", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}
