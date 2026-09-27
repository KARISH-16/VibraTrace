package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.viewmodel.VibraTraceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(
    viewModel: VibraTraceViewModel,
    onBack: () -> Unit,
    onScanBatch: (String) -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    var manualBatchInput by remember { mutableStateOf("") }
    var scanErrorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Batch QR", fontWeight = FontWeight.Bold) },
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
            // Camera Viewfinder Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(Color.Black, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (hasCameraPermission) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                try {
                                    val cameraProvider = cameraProviderFuture.get()
                                    val preview = Preview.Builder().build().also {
                                        it.surfaceProvider = previewView.surfaceProvider
                                    }
                                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                                } catch (_: Exception) {
                                    // Camera unavailable in some emulator environments; fallback gracefully
                                }
                            }, ContextCompat.getMainExecutor(ctx))
                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Target Overlay Frame
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .background(Color.Transparent, RoundedCornerShape(12.dp))
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Camera Permission Required", color = Color.White, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                            Text("Grant Permission")
                        }
                    }
                }
            }

            if (scanErrorMessage != null) {
                Text(
                    text = scanErrorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Quick Scan Preset Buttons for Test Environments
            Text(
                text = "Quick Demo Barcodes (Emulator Testing)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        viewModel.selectBatch("FD2026-001")
                        onScanBatch("FD2026-001")
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary),
                    modifier = Modifier.weight(1f).testTag("quick_scan_batch_001")
                ) {
                    Text("Scan FD2026-001", fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        scanErrorMessage = "Invalid VibraTrace QR."
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.weight(1f).testTag("quick_scan_invalid_qr")
                ) {
                    Text("Test Invalid QR", fontSize = 12.sp)
                }
            }

            HorizontalDivider()

            // Manual Batch Entry Fallback
            OutlinedTextField(
                value = manualBatchInput,
                onValueChange = { manualBatchInput = it },
                label = { Text("Or Enter Batch ID / Token") },
                placeholder = { Text("e.g. FD2026-001") },
                singleLine = true,
                trailingIcon = {
                    IconButton(
                        onClick = {
                            val clean = manualBatchInput.trim()
                            if (clean.isNotBlank()) {
                                viewModel.selectBatch(clean)
                                onScanBatch(clean)
                            } else {
                                scanErrorMessage = "Invalid VibraTrace QR."
                            }
                        },
                        modifier = Modifier.testTag("submit_manual_batch_button")
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Lookup Batch")
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("manual_batch_input"),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}
