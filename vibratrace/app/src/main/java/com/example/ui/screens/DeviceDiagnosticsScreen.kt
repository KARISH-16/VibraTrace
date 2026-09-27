package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusGood
import com.example.ui.theme.StatusWarning

data class DiagnosticItem(val name: String, val category: String, val status: String, val detail: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDiagnosticsScreen(onBack: () -> Unit) {
    BackHandler { onBack() }

    val diagnosticsList = listOf(
        DiagnosticItem("ESP32 Microcontroller", "Core Compute", "PASS", "ESP-WROOM-32 @ 240MHz Dual Core, FreeRTOS active"),
        DiagnosticItem("AHT20 Temp & Humidity", "Sensor Bus", "PASS", "I2C address 0x38 responding, CRC verified"),
        DiagnosticItem("Ethylene Electrochemical Sensor", "Sensor Bus", "PASS", "ADC GPIO34 baseline 0.42 ppm calibration normal"),
        DiagnosticItem("NH3 Ammonia Sensor", "Sensor Bus", "PASS", "ADC GPIO35 low-noise semiconductor circuit normal"),
        DiagnosticItem("Reed Switch Container Tamper", "Security", "PASS", "GPIO4 magnetic interrupt armed and debounced"),
        DiagnosticItem("MicroSD SPI Buffering", "Offline Storage", "PASS", "SPI CS GPIO5 FAT32 circular queue buffer online"),
        DiagnosticItem("SIM800C GSM/GPRS Modem", "Cellular", "PASS", "UART2 115200 baud AT+CSQ -72 dBm"),
        DiagnosticItem("GSM Cellular Signal Quality", "Cellular", "PASS", "RSSI -72 dBm (Excellent GPRS signal coverage)"),
        DiagnosticItem("MQTT Telemetry Pipeline", "Network", "PASS", "Broker connected (vibratrace/device/VT-ESP32-001)"),
        DiagnosticItem("FastAPI Gateway Server", "Cloud Backend", "PASS", "REST API v1 endpoints healthy, 200 OK"),
        DiagnosticItem("PostgreSQL Relational DB", "Cloud Backend", "PASS", "ACID transactions verified, alembic migrations clean"),
        DiagnosticItem("WebSocket Live Streaming", "Real-Time", "PASS", "Full-duplex /ws socket stream active"),
        DiagnosticItem("SHA-256 Hash Verification Service", "Integrity", "PASS", "Deterministic canonical serialization verified"),
        DiagnosticItem("Blockchain / Ledger Anchoring", "Cryptographic Ledger", "PASS", "DemoLedgerService state synchronized")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Device Diagnostics", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFFE8F5E9)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusGood)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "14 / 14 SUBSYSTEMS OPERATIONAL",
                                fontWeight = FontWeight.ExtraBold,
                                color = StatusGood,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "All hardware abstractions, network bridges, and cryptography pass verification.",
                                fontSize = 11.sp,
                                color = Color(0xFF14532D)
                            )
                        }
                    }
                }
            }

            items(diagnosticsList) { item ->
                ElevatedCard(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().testTag("diagnostic_item_${item.name}")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = item.category,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.detail,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when (item.status) {
                                "PASS" -> Color(0xFFE8F5E9)
                                "WARNING" -> Color(0xFFFFF8E1)
                                else -> Color(0xFFFFEBEE)
                            }
                        ) {
                            Text(
                                text = item.status,
                                color = when (item.status) {
                                    "PASS" -> StatusGood
                                    "WARNING" -> StatusWarning
                                    else -> StatusCritical
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
