package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.data.local.entity.HashRecordEntity
import com.example.data.security.HashEngine
import com.example.ui.components.IntegrityBadge
import com.example.ui.theme.AgriBlueSecondary
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.StatusCritical
import com.example.ui.viewmodel.VibraTraceViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BlockchainScreen(
    viewModel: VibraTraceViewModel,
    onNavigateToBatch: (String) -> Unit
) {
    val hashRecords by viewModel.allHashRecords.collectAsStateWithLifecycle()
    val blockchainRecords by viewModel.allBlockchainRecords.collectAsStateWithLifecycle()
    val batches by viewModel.allBatches.collectAsStateWithLifecycle()
    var selectedMismatchRecord by remember { mutableStateOf<HashRecordEntity?>(null) }

    val totalVerified = hashRecords.count { it.verificationStatus == "VERIFIED" }
    val mismatches = hashRecords.count { it.verificationStatus == "MISMATCH" }
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss • MMM dd", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary Header Card
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
                                text = "IMMUTABLE LEDGER VERIFICATION",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "DEMO LEDGER • SHA-256",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = AgriBlueSecondary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE1F5FE)
                        ) {
                            Text(
                                text = "DEMO LEDGER",
                                color = AgriBlueSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatBox(label = "Verified Records", value = "$totalVerified", color = AgriGreenPrimary)
                        StatBox(label = "Anchored Blocks", value = "${blockchainRecords.size}", color = AgriBlueSecondary)
                        StatBox(label = "Mismatches", value = "$mismatches", color = if (mismatches > 0) StatusCritical else Color.Gray)
                        StatBox(label = "Verified Batches", value = "${batches.size}", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }

        // Integrity Mismatch Alert Card if any
        if (mismatches > 0) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFEF2F2),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, StatusCritical),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = StatusCritical)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "INTEGRITY MISMATCH DETECTED",
                                color = StatusCritical,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "This record does not match its original verification hash. A security alert and audit record have been generated. The original cryptographic block was preserved.",
                            color = Color(0xFF1E293B),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent SHA-256 Verification Chain",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (hashRecords.isNotEmpty()) {
                    TextButton(
                        onClick = { viewModel.simulateIntegrityMismatch(hashRecords.first().recordId) },
                        modifier = Modifier.testTag("test_tamper_mismatch_button")
                    ) {
                        Text("Simulate Tampering", color = StatusCritical, fontSize = 11.sp)
                    }
                }
            }
        }

        items(hashRecords.take(30)) { record ->
            val isVerified = record.verificationStatus == "VERIFIED"
            val calculatedExpectedHash = remember(record) {
                HashEngine.calculateSha256(record.canonicalPayload)
            }

            ElevatedCard(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = if (isVerified) MaterialTheme.colorScheme.surface else Color(0xFFFFFBFA)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hash_record_${record.recordId}")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Record: ${record.recordId}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Batch: ${record.batchId} • Node: ${record.deviceId}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IntegrityBadge(isVerified = isVerified)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Current SHA-256 Hash:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = record.currentHash,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = if (isVerified) AgriGreenPrimary else StatusCritical,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Previous Block Hash:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = record.previousHash.take(32) + "...",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (!isVerified) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEF2F2),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StatusCritical.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "INTEGRITY MISMATCH DETAILS:",
                                    color = StatusCritical,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "Expected: $calculatedExpectedHash",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Current:  ${record.currentHash}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = StatusCritical
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = timeFormat.format(Date(record.timestamp)),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        TextButton(
                            onClick = { onNavigateToBatch(record.batchId) },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("View Batch", fontSize = 11.sp, color = AgriGreenPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBox(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
