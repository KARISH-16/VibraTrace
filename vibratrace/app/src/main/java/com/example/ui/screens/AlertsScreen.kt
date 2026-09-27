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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.AlertEntity
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusWarning
import com.example.ui.viewmodel.VibraTraceViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AlertsScreen(
    viewModel: VibraTraceViewModel,
    onNavigateToBatch: (String) -> Unit,
    onNavigateToSensors: (String) -> Unit
) {
    val activeAlerts by viewModel.activeAlerts.collectAsStateWithLifecycle()
    val allAlerts by viewModel.allAlerts.collectAsStateWithLifecycle()
    var showOnlyActive by remember { mutableStateOf(true) }

    val displayedAlerts = if (showOnlyActive) activeAlerts else allAlerts
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss • MMM dd", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Filter Switcher
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Smart Alert Engine",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = showOnlyActive,
                    onClick = { showOnlyActive = true },
                    label = { Text("Active (${activeAlerts.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = StatusCritical,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = !showOnlyActive,
                    onClick = { showOnlyActive = false },
                    label = { Text("All (${allAlerts.size})") }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (displayedAlerts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = AgriGreenPrimary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No active alerts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "All environmental and security parameters within threshold limits.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(displayedAlerts) { alert ->
                    AlertItemCard(
                        alert = alert,
                        timeStr = timeFormat.format(Date(alert.timestamp)),
                        onResolve = { viewModel.resolveAlert(alert.id) },
                        onViewBatch = { onNavigateToBatch(alert.batchId) },
                        onViewSensors = { onNavigateToSensors(alert.batchId) }
                    )
                }
            }
        }
    }
}

@Composable
fun AlertItemCard(
    alert: AlertEntity,
    timeStr: String,
    onResolve: () -> Unit,
    onViewBatch: () -> Unit,
    onViewSensors: () -> Unit
) {
    val isCritical = alert.severity == "CRITICAL"
    val accentColor = if (isCritical) StatusCritical else StatusWarning

    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("alert_item_${alert.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accentColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isCritical) Icons.Default.Warning else Icons.Default.Info,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${alert.severity} — ${alert.alertType}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                }

                Text(
                    text = timeStr,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = alert.message,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Batch: ${alert.batchId} • Node: ${alert.deviceId}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewBatch,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("View Batch", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onViewSensors,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Sensors", fontSize = 11.sp)
                }

                if (!alert.isResolved) {
                    Button(
                        onClick = onResolve,
                        colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("resolve_alert_${alert.id}")
                    ) {
                        Text("Resolve", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
