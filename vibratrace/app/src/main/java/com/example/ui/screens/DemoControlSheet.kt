package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.AgriBlueSecondary
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusWarning
import com.example.ui.viewmodel.VibraTraceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoControlSheet(
    viewModel: VibraTraceViewModel,
    onDismiss: () -> Unit,
    onNavigateToQrView: (String) -> Unit
) {
    val activeBatch by viewModel.activeBatch.collectAsStateWithLifecycle()
    val isTeleRunning by viewModel.isLiveSimulationRunning.collectAsStateWithLifecycle()
    val hashRecords by viewModel.allHashRecords.collectAsStateWithLifecycle()
    val batchId = activeBatch?.id ?: "FD2026-001"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "⚡ HACKATHON DEMO SIMULATOR",
                        fontWeight = FontWeight.ExtraBold,
                        color = AgriBlueSecondary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Acceptance Test Scenario Controller (Page 14 & 21)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxHeight(0.75f)
            ) {
                // Section 1: Telemetry & Streaming
                item {
                    DemoSectionHeader("1. Live Sensor Streaming")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DemoButton(
                            text = "Send Reading",
                            icon = Icons.Default.Sensors,
                            color = AgriGreenPrimary,
                            onClick = { viewModel.triggerSimulatedReading() },
                            modifier = Modifier.weight(1f).testTag("demo_send_reading")
                        )
                        DemoButton(
                            text = if (isTeleRunning) "Pause Auto Telemetry" else "Resume Auto Telemetry",
                            icon = if (isTeleRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            color = AgriBlueSecondary,
                            onClick = { viewModel.toggleContinuousTelemetry() },
                            modifier = Modifier.weight(1f).testTag("demo_toggle_telemetry")
                        )
                    }
                }

                // Section 2: Excursions & Smart Alerts
                item {
                    DemoSectionHeader("2. Alert Engine Excursions (Section 16)")
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DemoButton(
                                text = "Temp Alert (14.8°C)",
                                icon = Icons.Default.Thermostat,
                                color = StatusCritical,
                                onClick = { viewModel.triggerTemperatureExcursion() },
                                modifier = Modifier.weight(1f).testTag("demo_temp_alert")
                            )
                            DemoButton(
                                text = "Ethylene (1.25 ppm)",
                                icon = Icons.Default.Science,
                                color = StatusCritical,
                                onClick = { viewModel.triggerEthyleneAlert() },
                                modifier = Modifier.weight(1f).testTag("demo_ethylene_alert")
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DemoButton(
                                text = "NH3 Gas (4.8 ppm)",
                                icon = Icons.Default.Air,
                                color = StatusCritical,
                                onClick = { viewModel.triggerAmmoniaAlert() },
                                modifier = Modifier.weight(1f).testTag("demo_nh3_alert")
                            )
                            DemoButton(
                                text = "Low Battery (14%)",
                                icon = Icons.Default.BatteryAlert,
                                color = StatusWarning,
                                onClick = { viewModel.triggerLowBattery() },
                                modifier = Modifier.weight(1f).testTag("demo_low_batt")
                            )
                        }
                    }
                }

                // Section 3: Container Tamper Detection
                item {
                    DemoSectionHeader("3. Security: Container Tamper Event")
                    DemoButton(
                        text = "Trigger Reed Switch Tamper (Lid Open)",
                        icon = Icons.Default.Warning,
                        color = StatusCritical,
                        onClick = { viewModel.triggerTamperEvent() },
                        modifier = Modifier.fillMaxWidth().testTag("demo_tamper_event")
                    )
                }

                // Section 4: Offline-First Buffering & 127/127 Sync
                item {
                    DemoSectionHeader("4. Offline Buffering & Sync (Acceptance 13-18)")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DemoButton(
                            text = "Network OFF",
                            icon = Icons.Default.SignalCellularOff,
                            color = StatusCritical,
                            onClick = { viewModel.setNetworkState(false) },
                            modifier = Modifier.weight(1f).testTag("demo_network_off")
                        )
                        DemoButton(
                            text = "Network ON",
                            icon = Icons.Default.CellTower,
                            color = AgriGreenPrimary,
                            onClick = { viewModel.setNetworkState(true) },
                            modifier = Modifier.weight(1f).testTag("demo_network_on")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DemoButton(
                            text = "Seed 127 Offline Records",
                            icon = Icons.Default.SdCard,
                            color = Color(0xFF6A1B9A),
                            onClick = { viewModel.seed127OfflineRecords() },
                            modifier = Modifier.weight(1f).testTag("demo_seed_127")
                        )
                        DemoButton(
                            text = "Sync 127/127 Records",
                            icon = Icons.Default.Sync,
                            color = AgriGreenPrimary,
                            onClick = { viewModel.synchronizeOfflineQueue() },
                            modifier = Modifier.weight(1f).testTag("demo_sync_queue")
                        )
                    }
                }

                // Section 5: Cryptographic Hash & Tampering Mismatch
                item {
                    DemoSectionHeader("5. SHA-256 Hash & Tamper Detection (Acceptance 23-25)")
                    DemoButton(
                        text = "Corrupt Historical Record (Trigger Integrity Mismatch)",
                        icon = Icons.Default.LockReset,
                        color = StatusCritical,
                        onClick = {
                            hashRecords.firstOrNull()?.let {
                                viewModel.simulateIntegrityMismatch(it.recordId)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("demo_corrupt_record")
                    )
                }

                // Section 6: Farm-to-Fork Timeline Progression
                item {
                    DemoSectionHeader("6. Farm-to-Fork Stage Progression")
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("PACKING", "TRANSPORT", "COLD_STORAGE", "BUYER").forEach { st ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AgriBlueSecondary.copy(alpha = 0.15f),
                                onClick = { viewModel.advanceShipmentStage(st) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = st.take(4),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AgriBlueSecondary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                // Section 7: QR Code View
                item {
                    DemoSectionHeader("7. Batch QR & Public Traceability")
                    DemoButton(
                        text = "View Batch QR & Blockchain Provenance",
                        icon = Icons.Default.QrCode,
                        color = AgriGreenPrimary,
                        onClick = {
                            onDismiss()
                            onNavigateToQrView(batchId)
                        },
                        modifier = Modifier.fillMaxWidth().testTag("demo_view_qr")
                    )
                }
            }
        }
    }
}

@Composable
private fun DemoSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun DemoButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
        modifier = modifier.height(44.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}
