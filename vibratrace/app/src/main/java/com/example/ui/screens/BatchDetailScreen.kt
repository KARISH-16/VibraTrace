package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ConditionBadge
import com.example.ui.components.IntegrityBadge
import com.example.ui.components.QrCodeView
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.viewmodel.VibraTraceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchDetailScreen(
    batchId: String,
    viewModel: VibraTraceViewModel,
    onBack: () -> Unit,
    onNavigateToTraceability: (String) -> Unit
) {
    BackHandler { onBack() }
    val activeBatch by viewModel.activeBatch.collectAsStateWithLifecycle()
    val batch = activeBatch
    val context = LocalContext.current
    var advanceStageDialogVisible by remember { mutableStateOf(false) }

    val stages = listOf("FARM", "PACKING", "TRANSPORT", "COLD_STORAGE", "DISTRIBUTION", "BUYER")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(batch?.id ?: batchId, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "VibraTrace Farm-to-Fork Batch ${batch?.id}\nProduct: ${batch?.productName}\nQR Token: ${batch?.qrToken}")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Batch QR"))
                        },
                        modifier = Modifier.testTag("share_qr_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share QR")
                    }
                }
            )
        }
    ) { padding ->
        if (batch == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AgriGreenPrimary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Info Card
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
                            Text(
                                text = batch.productName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            ConditionBadge(status = batch.conditionStatus)
                        }

                        Text(
                            text = batch.productCategory,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        IntegrityBadge(isVerified = batch.integrityStatus == "VERIFIED")
                    }
                }

                // QR Code Center Box
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(20.dp).fillMaxWidth()
                    ) {
                        Text(
                            text = "SECURE QR TRACEABILITY TOKEN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        QrCodeView(
                            content = "https://vibratrace.org/trace/${batch.id}?token=${batch.qrToken}",
                            size = 180.dp
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = batch.qrToken,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { onNavigateToTraceability(batch.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("public_trace_button")
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open Public Traceability View")
                        }
                    }
                }

                // Detailed Specifications Table
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Shipment Metadata",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        DetailRow("Farmer / Supplier", batch.farmerSupplier)
                        DetailRow("Quantity", "${batch.quantity} ${batch.unit}")
                        DetailRow("Harvest Date", batch.harvestDate)
                        DetailRow("Packing Date", batch.packingDate)
                        DetailRow("Destination", batch.destination)
                        DetailRow("Transporter", batch.transporter)
                        DetailRow("Assigned IoT Node", batch.assignedDeviceId ?: "Unassigned")
                        DetailRow("Current Stage", batch.currentStage)
                    }
                }

                // Stage Advancement Action
                Button(
                    onClick = { advanceStageDialogVisible = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("advance_stage_button")
                ) {
                    Icon(Icons.Default.FastForward, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Advance Farm-to-Fork Stage", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (advanceStageDialogVisible) {
        AlertDialog(
            onDismissRequest = { advanceStageDialogVisible = false },
            title = { Text("Select Next Shipment Stage") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    stages.forEach { s ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (batch?.currentStage == s) AgriGreenPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            onClick = {
                                viewModel.advanceShipmentStage(s)
                                advanceStageDialogVisible = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = s,
                                modifier = Modifier.padding(12.dp),
                                fontWeight = if (batch?.currentStage == s) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { advanceStageDialogVisible = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}
