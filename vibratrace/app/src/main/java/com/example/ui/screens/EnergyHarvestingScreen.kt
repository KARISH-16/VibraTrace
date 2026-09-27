package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.AgriBlueSecondary
import com.example.ui.theme.AgriGreenContainer
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.StatusGood
import com.example.ui.viewmodel.VibraTraceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnergyHarvestingScreen(
    viewModel: VibraTraceViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val device by viewModel.activeDevice.collectAsStateWithLifecycle()
    val energyEvents by viewModel.recentEnergyEvents.collectAsStateWithLifecycle()
    val latestReading by viewModel.latestReading.collectAsStateWithLifecycle()
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    val batteryPercent = latestReading?.battery ?: device?.batteryLevel ?: 84
    val lastVibrationTime = remember(energyEvents) {
        energyEvents.firstOrNull()?.timestamp?.let { timeFormat.format(Date(it)) } ?: "Just now"
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Piezoelectric Energy Harvesting", fontWeight = FontWeight.Bold) },
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
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Status Gauge Card
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(100.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape)
                                    .background(AgriGreenContainer.copy(alpha = pulseAlpha))
                            )
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = AgriGreenPrimary,
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "$batteryPercent %",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Lithium Battery • Supplementary Charging Active",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = StatusGood
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Flow diagram row
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FlowStep("Vibration\nDetected", Icons.Default.Vibration, true)
                                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                FlowStep("Energy\nHarvested", Icons.Default.ElectricBolt, true)
                                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                FlowStep("Battery\nTrickle Charge", Icons.Default.BatteryChargingFull, true)
                            }
                        }
                    }
                }
            }

            // Status Metrics Grid
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Telemetry Specifications",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        DetailRow("Battery Percentage", "$batteryPercent%")
                        DetailRow("Battery Status", if (batteryPercent > 20) "HEALTHY" else "LOW")
                        DetailRow("Transit Vibration Detected", "YES (PZT Active)")
                        DetailRow("Harvesting Status", "HARVESTING_ACTIVE")
                        DetailRow("Charging Status", "SUPPLEMENTARY_CHARGING")
                        DetailRow("Last Vibration Event", lastVibrationTime)
                    }
                }
            }

            // Mandatory Hardware Rule Notice (Page 20 / Section 48)
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFFF1F8E9)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(14.dp)) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = AgriGreenPrimary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ENERGY HARVESTING DISCLOSURE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AgriGreenPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Battery is the primary power source. Piezoelectric transit vibration harvesting provides supplementary trickle charge to extend field operating life. It does not replace the primary battery.",
                                fontSize = 11.sp,
                                color = Color(0xFF14532D),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FlowStep(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, active: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (active) AgriGreenPrimary else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
