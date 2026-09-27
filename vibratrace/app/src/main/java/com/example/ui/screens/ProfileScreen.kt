package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.ui.viewmodel.VibraTraceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: VibraTraceViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val user by viewModel.activeUser.collectAsStateWithLifecycle()

    val name = user?.fullName ?: "Karishma Ramesh"
    val email = user?.email ?: "karishmaramesh16@gmail.com"
    val role = user?.role ?: "ADMIN"
    val phone = user?.phone ?: "+1 555-019-2834"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Operator Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(AgriGreenContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = AgriGreenPrimary,
                    modifier = Modifier.size(54.dp)
                )
            }

            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = AgriGreenPrimary.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Badge, contentDescription = null, tint = AgriGreenPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ROLE: $role",
                        color = AgriGreenPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            ElevatedCard(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Account Information", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    DetailRow("Email", email)
                    DetailRow("Phone", phone)
                    DetailRow("Session Token", "active (JWT Bearer)")
                    DetailRow("Access Scope", "Full Farm-to-Fork Traceability Node Authority")
                }
            }

            ElevatedCard(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Role Permissions Granted", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("• Create and administer batches", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("• Advance supply-chain stages & anchor to ledger", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("• Configure environmental safety thresholds", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("• Acknowledge tamper & gas excursion alerts", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
