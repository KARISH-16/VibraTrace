package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.DataPoint
import com.example.ui.components.SensorChartCard
import com.example.ui.theme.AgriBlueSecondary
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.StatusWarning
import com.example.ui.viewmodel.VibraTraceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorMonitoringScreen(
    batchId: String,
    viewModel: VibraTraceViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val readings by viewModel.readingsForActiveBatch.collectAsStateWithLifecycle()
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()

    val tempThreshold = settings?.tempMax ?: 10.0
    val ethyleneThreshold = settings?.ethyleneThreshold ?: 0.5
    val nh3Threshold = settings?.nh3Threshold ?: 2.0

    // Reverse to ascending for time series
    val sortedReadings = remember(readings) { readings.sortedBy { it.timestamp } }

    val tempData = remember(sortedReadings) {
        sortedReadings.map { DataPoint(it.timestamp, it.temperature) }
    }
    val humData = remember(sortedReadings) {
        sortedReadings.map { DataPoint(it.timestamp, it.humidity) }
    }
    val ethyleneData = remember(sortedReadings) {
        sortedReadings.mapNotNull { r -> r.ethylene?.let { DataPoint(r.timestamp, it) } }
    }
    val nh3Data = remember(sortedReadings) {
        sortedReadings.map { DataPoint(it.timestamp, it.ammonia) }
    }
    val battData = remember(sortedReadings) {
        sortedReadings.map { DataPoint(it.timestamp, it.battery.toDouble()) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sensor Telemetry • $batchId", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SensorChartCard(
                    title = "Temperature vs Time",
                    unit = "°C",
                    dataPoints = tempData,
                    lineColor = AgriGreenPrimary,
                    threshold = tempThreshold
                )
            }

            item {
                SensorChartCard(
                    title = "Relative Humidity vs Time",
                    unit = "%",
                    dataPoints = humData,
                    lineColor = AgriBlueSecondary,
                    threshold = 85.0
                )
            }

            item {
                SensorChartCard(
                    title = "Ethylene (C₂H₄) vs Time",
                    unit = "ppm",
                    dataPoints = ethyleneData,
                    lineColor = Color(0xFF6A1B9A),
                    threshold = ethyleneThreshold
                )
            }

            item {
                SensorChartCard(
                    title = "Ammonia (NH₃) vs Time",
                    unit = "ppm",
                    dataPoints = nh3Data,
                    lineColor = Color(0xFFD84315),
                    threshold = nh3Threshold
                )
            }

            item {
                SensorChartCard(
                    title = "Battery Level vs Time",
                    unit = "%",
                    dataPoints = battData,
                    lineColor = StatusWarning,
                    threshold = 20.0
                )
            }
        }
    }
}
