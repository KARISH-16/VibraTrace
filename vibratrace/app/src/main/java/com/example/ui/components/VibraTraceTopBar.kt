package com.example.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AgriGreenPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VibraTraceTopBar(
    title: String,
    connectionStatus: String,
    onOpenDemoControls: () -> Unit,
    onOpenQrScanner: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProfile: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Spa,
                    contentDescription = null,
                    tint = AgriGreenPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        actions = {
            ConnectionBadge(status = connectionStatus)

            IconButton(
                onClick = onOpenDemoControls,
                modifier = Modifier.testTag("demo_controls_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Demo Simulation Controls",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(
                onClick = onOpenQrScanner,
                modifier = Modifier.testTag("qr_scanner_button")
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "Scan Batch QR",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onOpenProfile,
                modifier = Modifier.testTag("profile_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}
