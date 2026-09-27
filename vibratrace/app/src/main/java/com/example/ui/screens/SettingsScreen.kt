package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.AppSettingsEntity
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.StatusCritical
import com.example.ui.viewmodel.VibraTraceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: VibraTraceViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    BackHandler { onBack() }
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()

    var tempMin by remember(settings) { mutableStateOf(settings?.tempMin?.toString() ?: "2.0") }
    var tempMax by remember(settings) { mutableStateOf(settings?.tempMax?.toString() ?: "10.0") }
    var humMin by remember(settings) { mutableStateOf(settings?.humidityMin?.toString() ?: "60.0") }
    var humMax by remember(settings) { mutableStateOf(settings?.humidityMax?.toString() ?: "85.0") }
    var ethyleneThreshold by remember(settings) { mutableStateOf(settings?.ethyleneThreshold?.toString() ?: "0.50") }
    var nh3Threshold by remember(settings) { mutableStateOf(settings?.nh3Threshold?.toString() ?: "2.00") }
    var batteryWarning by remember(settings) { mutableStateOf(settings?.batteryWarning?.toString() ?: "20") }
    var selectedLanguage by remember(settings) { mutableStateOf(settings?.language ?: "en") }
    var demoModeEnabled by remember(settings) { mutableStateOf(settings?.demoMode ?: true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Thresholds", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val newSettings = AppSettingsEntity(
                                tempMin = tempMin.toDoubleOrNull() ?: 2.0,
                                tempMax = tempMax.toDoubleOrNull() ?: 10.0,
                                humidityMin = humMin.toDoubleOrNull() ?: 60.0,
                                humidityMax = humMax.toDoubleOrNull() ?: 85.0,
                                ethyleneThreshold = ethyleneThreshold.toDoubleOrNull() ?: 0.50,
                                nh3Threshold = nh3Threshold.toDoubleOrNull() ?: 2.00,
                                batteryWarning = batteryWarning.toIntOrNull() ?: 20,
                                language = selectedLanguage,
                                demoMode = demoModeEnabled
                            )
                            viewModel.saveSettings(newSettings)
                        },
                        modifier = Modifier.testTag("save_settings_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save Settings", tint = AgriGreenPrimary)
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mode Toggle
            ElevatedCard(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Demo / Simulation Mode", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            "Emulate ESP32 hardware telemetry and acceptance tests",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = demoModeEnabled,
                        onCheckedChange = { demoModeEnabled = it },
                        modifier = Modifier.testTag("demo_mode_switch")
                    )
                }
            }

            // Localization
            ElevatedCard(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = AgriGreenPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Language / பிராந்தியம் / भाषा", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("en" to "English", "ta" to "தமிழ் (Tamil)", "hi" to "हिन्दी (Hindi)").forEach { (code, name) ->
                            val isSel = selectedLanguage == code
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedLanguage = code },
                                label = { Text(name, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }

            // Environmental Thresholds Card
            ElevatedCard(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Configurable Sensor Thresholds", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = tempMin,
                            onValueChange = { tempMin = it },
                            label = { Text("Temp Min (°C)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = tempMax,
                            onValueChange = { tempMax = it },
                            label = { Text("Temp Max (°C)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = humMin,
                            onValueChange = { humMin = it },
                            label = { Text("Hum Min (%)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = humMax,
                            onValueChange = { humMax = it },
                            label = { Text("Hum Max (%)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = ethyleneThreshold,
                            onValueChange = { ethyleneThreshold = it },
                            label = { Text("Ethylene Max (ppm)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = nh3Threshold,
                            onValueChange = { nh3Threshold = it },
                            label = { Text("NH3 Max (ppm)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    OutlinedTextField(
                        value = batteryWarning,
                        onValueChange = { batteryWarning = it },
                        label = { Text("Battery Warning Cutoff (%)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Save Action Button
            Button(
                onClick = {
                    val newSettings = AppSettingsEntity(
                        tempMin = tempMin.toDoubleOrNull() ?: 2.0,
                        tempMax = tempMax.toDoubleOrNull() ?: 10.0,
                        humidityMin = humMin.toDoubleOrNull() ?: 60.0,
                        humidityMax = humMax.toDoubleOrNull() ?: 85.0,
                        ethyleneThreshold = ethyleneThreshold.toDoubleOrNull() ?: 0.50,
                        nh3Threshold = nh3Threshold.toDoubleOrNull() ?: 2.00,
                        batteryWarning = batteryWarning.toIntOrNull() ?: 20,
                        language = selectedLanguage,
                        demoMode = demoModeEnabled
                    )
                    viewModel.saveSettings(newSettings)
                    onBack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Save Configuration", fontWeight = FontWeight.Bold)
            }

            // Logout Button
            OutlinedButton(
                onClick = {
                    viewModel.logout()
                    onLogout()
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusCritical),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("settings_logout_button")
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
