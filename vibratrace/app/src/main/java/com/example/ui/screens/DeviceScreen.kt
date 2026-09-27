package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.TamperEventEntity
import com.example.ui.components.ConnectionBadge
import com.example.ui.theme.AgriBlueSecondary
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusGood
import com.example.ui.theme.StatusWarning
import com.example.ui.viewmodel.VibraTraceViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DeviceScreen(
    viewModel: VibraTraceViewModel,
    onNavigateToDiagnostics: () -> Unit,
    onNavigateToEnergy: () -> Unit,
    onNavigateToBatch: (String) -> Unit
) {
    val device by viewModel.activeDevice.collectAsStateWithLifecycle()
    val tamperEvents by viewModel.allTamperEvents.collectAsStateWithLifecycle()
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss • MMM dd", Locale.getDefault()) }

    val dev = device
    val networkStatus = dev?.networkStatus ?: "ONLINE"
    val enclosure = dev?.enclosureStatus ?: "CLOSED"
    val isTampered = dev?.tamperDetected == true || tamperEvents.any { !it.acknowledged }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Node Overview Card
        item {
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "IoT EDGE NODE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = dev?.id ?: "VT-ESP32-001",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        ConnectionBadge(status = networkStatus)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        InfoLabel("Assigned Batch", dev?.assignedBatchId ?: "FD2026-001")
                        InfoLabel("Firmware", dev?.firmwareVersion ?: "v1.4.2")
                        InfoLabel("Battery", "${dev?.batteryLevel ?: 84}%")
                        InfoLabel("GSM Signal", "${dev?.gsmSignal ?: -72} dBm")
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onNavigateToDiagnostics,
                            colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("run_diagnostics_button")
                        ) {
                            Icon(Icons.Default.HealthAndSafety, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Diagnostics", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onNavigateToEnergy,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_energy_harvesting_button")
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Harvesting", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Enclosure & Tamper Monitor Card
        item {
            OutlinedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = if (isTampered) Color(0xFFFFFBFA) else MaterialTheme.colorScheme.surface
                ),
                border = if (isTampered) androidx.compose.foundation.BorderStroke(1.5.dp, StatusCritical) else CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isTampered) Icons.Default.DoorSliding else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (isTampered) StatusCritical else AgriGreenPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Container Seal & Enclosure",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isTampered) StatusCritical.copy(alpha = 0.15f) else StatusGood.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (isTampered) "TAMPER DETECTED" else "SEALED / CLOSED",
                                color = if (isTampered) StatusCritical else StatusGood,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Reed switch interrupt sensor on container access lid.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Sensor Health Grid
        item {
            Text(
                text = "Sensor Health Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SensorHealthRow("AHT20 Temp & Relative Humidity (I2C)", "ONLINE", Icons.Default.Thermostat)
                SensorHealthRow("Ethylene C₂H₄ Spoilage Sensor (ADC)", "ONLINE", Icons.Default.Science)
                SensorHealthRow("Ammonia NH₃ Spoilage Sensor (ADC)", "ONLINE", Icons.Default.Air)
                SensorHealthRow("Reed Switch Tamper Detector (GPIO4)", "ONLINE", Icons.Default.DoorSliding)
                SensorHealthRow("MicroSD SPI Offline Buffering Queue", "ONLINE", Icons.Default.SdCard)
                SensorHealthRow("SIM800C GSM/GPRS Cellular Engine", "ONLINE", Icons.Default.CellTower)
                SensorHealthRow("MQTT Telemetry Pipeline Broker", "ONLINE", Icons.Default.CloudSync)
                SensorHealthRow("FastAPI Gateway & PostgreSQL Database", "ONLINE", Icons.Default.Storage)
            }
        }

        // Tamper Events History
        if (tamperEvents.isNotEmpty()) {
            item {
                Text(
                    text = "Tamper Log History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(tamperEvents) { ev ->
                TamperEventCard(
                    event = ev,
                    timeStr = timeFormat.format(Date(ev.timestamp)),
                    onAcknowledge = { viewModel.acknowledgeTamper(ev.eventId) },
                    onViewBatch = { onNavigateToBatch(ev.batchId) }
                )
            }
        }
    }
}

@Composable
fun SensorHealthRow(name: String, status: String, icon: ImageVector) {
    ElevatedCard(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AgriGreenPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFE8F5E9)
            ) {
                Text(
                    text = status,
                    color = AgriGreenPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun TamperEventCard(
    event: TamperEventEntity,
    timeStr: String,
    onAcknowledge: () -> Unit,
    onViewBatch: () -> Unit
) {
    OutlinedCard(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, StatusCritical.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TAMPER DETECTED",
                    color = StatusCritical,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                )
                Text(
                    text = timeStr,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Device: ${event.deviceId} • Batch: ${event.batchId}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "Location: ${event.location}",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onViewBatch,
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("View Batch", fontSize = 11.sp)
                }

                if (!event.acknowledged) {
                    Button(
                        onClick = onAcknowledge,
                        colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Acknowledge", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoLabel(title: String, value: String) {
    Column {
        Text(text = title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
