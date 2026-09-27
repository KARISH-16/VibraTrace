package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.ui.components.ConditionBadge
import com.example.ui.components.IntegrityBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.VibraTraceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: VibraTraceViewModel,
    onNavigateToSensorCharts: (String) -> Unit,
    onNavigateToTimeline: (String) -> Unit,
    onNavigateToQrView: (String) -> Unit,
    onNavigateToAlerts: () -> Unit
) {
    val activeBatch by viewModel.activeBatch.collectAsStateWithLifecycle()
    val allBatches by viewModel.allBatches.collectAsStateWithLifecycle()
    val latestReading by viewModel.latestReading.collectAsStateWithLifecycle()
    val activeAlerts by viewModel.activeAlerts.collectAsStateWithLifecycle()
    val pendingSyncCount by viewModel.pendingSyncCount.collectAsStateWithLifecycle()
    val syncMessage by viewModel.syncMessage.collectAsStateWithLifecycle()
    val isSimulationRunning by viewModel.isLiveSimulationRunning.collectAsStateWithLifecycle()

    var batchSelectorExpanded by remember { mutableStateOf(false) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    val batchId = activeBatch?.id ?: "FD2026-001"
    val tempVal = latestReading?.temperature ?: 6.8
    val humVal = latestReading?.humidity ?: 72.0
    val c2h4Val = latestReading?.ethylene ?: 0.42
    val nh3Val = latestReading?.ammonia ?: 1.80
    val battVal = latestReading?.battery ?: 84
    val condition = activeBatch?.conditionStatus ?: "GOOD"
    val isVerified = activeBatch?.integrityStatus != "MISMATCH"
    val stage = activeBatch?.currentStage ?: "TRANSPORT"
    val lastUpdated = latestReading?.timestamp?.let { timeFormatter.format(Date(it)) } ?: "14:32:08"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Active Batch Card
        item {
            ElevatedCard(
                shape = RoundedCornerShape(18.dp),
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
                                text = "FARM-TO-FORK",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AgriGreenPrimary,
                                letterSpacing = 1.sp
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { batchSelectorExpanded = true }
                                    .testTag("batch_selector_dropdown")
                            ) {
                                Text(
                                    text = "BATCH: $batchId",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Switch Batch")
                            }

                            DropdownMenu(
                                expanded = batchSelectorExpanded,
                                onDismissRequest = { batchSelectorExpanded = false }
                            ) {
                                allBatches.forEach { b ->
                                    DropdownMenuItem(
                                        text = { Text("${b.id} - ${b.productName}") },
                                        onClick = {
                                            viewModel.selectBatch(b.id)
                                            batchSelectorExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        ConditionBadge(status = condition)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = activeBatch?.productName ?: "Organic Produce Shipment",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Stage and Device Info row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AgriBlueContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "STAGE: $stage",
                                color = AgriBlueSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = "Node: VT-ESP32-001  •  $lastUpdated",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Active Alerts Warning Banner if any
        if (activeAlerts.isNotEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFEF2F2),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StatusCritical.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToAlerts() }
                        .testTag("dashboard_alert_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = StatusCritical,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${activeAlerts.size} ACTIVE CRITICAL ALERTS",
                                color = StatusCritical,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = activeAlerts.first().message,
                                color = Color(0xFF1E293B),
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = StatusCritical
                        )
                    }
                }
            }
        }

        // Offline Records & Sync Notice Banner
        if (pendingSyncCount > 0 || syncMessage != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFFFBEB),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StatusWarning.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = syncMessage ?: "Offline records waiting to sync: $pendingSyncCount",
                                color = Color(0xFF78350F),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Idempotent MicroSD buffer queue ready",
                                color = Color(0xFF92400E),
                                fontSize = 11.sp
                            )
                        }

                        Button(
                            onClick = { viewModel.synchronizeOfflineQueue() },
                            colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("sync_now_button")
                        ) {
                            Text(text = "Sync Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 6 Sensor Telemetry Cards Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TelemetryCard(
                        title = "Temperature",
                        value = "${String.format("%.1f", tempVal)} °C",
                        subtitle = "Range: 2.0 - 10.0 °C",
                        icon = Icons.Default.Thermostat,
                        color = if (tempVal in 2.0..10.0) AgriGreenPrimary else StatusCritical,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToSensorCharts(batchId) }
                    )
                    TelemetryCard(
                        title = "Humidity",
                        value = "${String.format("%.0f", humVal)} %",
                        subtitle = "Target: 60 - 85%",
                        icon = Icons.Default.WaterDrop,
                        color = AgriBlueSecondary,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToSensorCharts(batchId) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TelemetryCard(
                        title = "Ethylene (C₂H₄)",
                        value = "${String.format("%.2f", c2h4Val)} ppm",
                        subtitle = "Ripening gas sensor",
                        icon = Icons.Default.Science,
                        color = if (c2h4Val < 0.5) AgriGreenPrimary else StatusCritical,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToSensorCharts(batchId) }
                    )
                    TelemetryCard(
                        title = "Ammonia (NH₃)",
                        value = "${String.format("%.2f", nh3Val)} ppm",
                        subtitle = "Spoilage gas monitor",
                        icon = Icons.Default.Air,
                        color = if (nh3Val < 2.0) AgriGreenPrimary else StatusCritical,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToSensorCharts(batchId) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TelemetryCard(
                        title = "Battery",
                        value = "$battVal %",
                        subtitle = "Piezo supplementary",
                        icon = Icons.Default.BatteryChargingFull,
                        color = if (battVal > 20) AgriGreenPrimary else StatusWarning,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryCard(
                        title = "Network",
                        value = "Connected",
                        subtitle = "GSM / SIM800C (-72 dBm)",
                        icon = Icons.Default.CellTower,
                        color = AgriBlueSecondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Integrity & Ledger Verification Banner
        item {
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CRYPTOGRAPHIC INTEGRITY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        IntegrityBadge(isVerified = isVerified)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "SHA-256 block chain anchored • DEMO LEDGER",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedButton(
                        onClick = { onNavigateToQrView(batchId) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("view_qr_trace_button")
                    ) {
                        Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("QR Trace")
                    }
                }
            }
        }

        // Quick Navigation Tiles
        item {
            Text(
                text = "Supply Chain Controls",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionTile(
                    title = "Charts & Sensor Feeds",
                    icon = Icons.Default.Timeline,
                    onClick = { onNavigateToSensorCharts(batchId) },
                    modifier = Modifier.weight(1f)
                )
                ActionTile(
                    title = "Farm-to-Fork Timeline",
                    icon = Icons.Default.Route,
                    onClick = { onNavigateToTimeline(batchId) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun TelemetryCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ActionTile(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(AgriGreenContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AgriGreenPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
