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
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ConditionBadge
import com.example.ui.components.IntegrityBadge
import com.example.ui.theme.AgriBlueSecondary
import com.example.ui.theme.AgriGreenContainer
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.StatusCritical
import com.example.ui.viewmodel.VibraTraceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicTraceabilityScreen(
    batchId: String,
    viewModel: VibraTraceViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val activeBatch by viewModel.activeBatch.collectAsStateWithLifecycle()
    val readings by viewModel.readingsForActiveBatch.collectAsStateWithLifecycle()
    val events by viewModel.timelineEvents.collectAsStateWithLifecycle()
    val tampers by viewModel.allTamperEvents.collectAsStateWithLifecycle()
    val blockchainRecords by viewModel.allBlockchainRecords.collectAsStateWithLifecycle()

    val batch = activeBatch
    val isVerified = batch?.integrityStatus == "VERIFIED"
    val latestReading = readings.firstOrNull()
    val latestTx = blockchainRecords.firstOrNull { it.batchId == batchId }?.transactionId ?: "0x98f4e21a8d052c1e7f90"
    val timeFormat = remember { SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Consumer Traceability Certificate", fontWeight = FontWeight.Bold) },
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
            contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Certificate Banner
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = if (isVerified) AgriGreenContainer.copy(alpha = 0.4f) else Color(0xFFFFEBEE)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "VIBRATRACE PROVENANCE CERTIFICATE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isVerified) AgriGreenPrimary else StatusCritical
                                )
                                Text(
                                    text = "BATCH ${batch?.id ?: batchId}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            ConditionBadge(status = batch?.conditionStatus ?: "GOOD")
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isVerified) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isVerified) AgriGreenPrimary else StatusCritical,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isVerified) "Monitoring Status: VERIFIED SAFE COLD-CHAIN" else "Warning: Potential Integrity Mismatch",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isVerified) AgriGreenPrimary else StatusCritical
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Blockchain Tx: $latestTx • DEMO LEDGER",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Origin & Product Details
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Origin & Farm Authentication",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        DetailRow("Product Name", batch?.productName ?: "Fresh Produce")
                        DetailRow("Farm Origin", batch?.farmerSupplier ?: "Organic Collective")
                        DetailRow("Quantity", "${batch?.quantity ?: 100} ${batch?.unit ?: "kg"}")
                        DetailRow("Harvest Date", batch?.harvestDate ?: "2026-09-24")
                        DetailRow("Packing Date", batch?.packingDate ?: "2026-09-25")
                        DetailRow("Destination Terminal", batch?.destination ?: "Central Hub")
                        DetailRow("Current Stage", batch?.currentStage ?: "TRANSPORT")
                    }
                }
            }

            // Environmental History Summary
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Environmental Cold-Chain Integrity",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Temperature", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${String.format("%.1f", latestReading?.temperature ?: 6.8)} °C", fontWeight = FontWeight.Bold, color = AgriGreenPrimary)
                            }
                            Column {
                                Text("Humidity", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${String.format("%.0f", latestReading?.humidity ?: 72.0)} %", fontWeight = FontWeight.Bold, color = AgriBlueSecondary)
                            }
                            Column {
                                Text("Ethylene (C₂H₄)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${String.format("%.2f", latestReading?.ethylene ?: 0.42)} ppm", fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Ammonia (NH₃)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${String.format("%.2f", latestReading?.ammonia ?: 1.80)} ppm", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Journey Timeline
            item {
                Text(
                    text = "Supply Chain Journey",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(events) { ev ->
                ElevatedCard(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(ev.stage, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("By: ${ev.responsibleParty}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Location: ${ev.location}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(timeFormat.format(Date(ev.timestamp)), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
