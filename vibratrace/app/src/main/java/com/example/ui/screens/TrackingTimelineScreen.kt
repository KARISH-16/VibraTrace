package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.viewmodel.VibraTraceViewModel
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Redesigned Truck Tracking Screen matching the provided modern UI specification.
 * Includes interactive live vector map, animated transit route, real-time telemetry card,
 * horizontal milestone tracking, and linked shipment batch details.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingTimelineScreen(
    viewModel: VibraTraceViewModel,
    onNavigateBack: () -> Unit = {},
    onNavigateToBatchDetail: (String) -> Unit = {},
    onAdvanceStage: () -> Unit = {}
) {
    val context = LocalContext.current
    val activeBatch by viewModel.activeBatch.collectAsStateWithLifecycle()
    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()
    val isDemoMode = appSettings?.demoMode ?: true
    val activeBatchId = activeBatch?.id ?: "FD2026-001"

    var showRouteDetailsSheet by remember { mutableStateOf(false) }

    // Map zoom and pan state
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    // Dynamic truck route progression (in demo mode animates smoothly along route)
    val infiniteTransition = rememberInfiniteTransition(label = "truckMovement")
    val animatedProgress by infiniteTransition.animateFloat(
        initialValue = 0.42f,
        targetValue = 0.68f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "routeProgress"
    )
    val currentProgress = if (isDemoMode) animatedProgress else 0.58f

    // ETA dynamically calculated (e.g. 18 min remaining)
    val etaMinutes = remember(currentProgress) {
        val totalTransitMins = 42
        val remaining = ((1.0f - currentProgress) * totalTransitMins).toInt().coerceAtLeast(3)
        "$remaining min"
    }

    Scaffold(
        topBar = {
            TrackingTopAppBar(
                onNavigateBack = onNavigateBack,
                onGlobeClick = {
                    Toast.makeText(
                        context,
                        "Route Corridor: Kallupatti Farm → NH-44 → Vellore Center",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        },
        containerColor = Color(0xFFF8FAF7)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. LIVE MAP CARD
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(22.dp))
                    .shadow(3.dp, RoundedCornerShape(22.dp))
                    .background(Color(0xFFE8F2E6))
                    .testTag("live_tracking_map_container")
            ) {
                // Interactive Vector Canvas Map
                LiveRouteCanvas(
                    progress = currentProgress,
                    zoomScale = zoomScale,
                    panOffset = panOffset,
                    onPanDelta = { delta ->
                        panOffset = Offset(
                            x = (panOffset.x + delta.x).coerceIn(-120f, 120f),
                            y = (panOffset.y + delta.y).coerceIn(-120f, 120f)
                        )
                    }
                )

                // Overlay 1: Top-Left "● Truck In Transit" Status Chip
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 3.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .padding(start = 12.dp, top = 12.dp)
                        .align(Alignment.TopStart)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF15803D))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Truck In Transit",
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                // Overlay 2: Top-Right Re-center GPS button
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 3.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .padding(end = 12.dp, top = 12.dp)
                        .size(40.dp)
                        .align(Alignment.TopEnd)
                        .clickable {
                            zoomScale = 1.0f
                            panOffset = Offset.Zero
                            Toast.makeText(context, "Centered on shipment vehicle", Toast.LENGTH_SHORT).show()
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Recenter",
                            tint = Color(0xFF15803D),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Overlay 3: Bottom-Right Zoom In (+) / Zoom Out (-) Controls
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 3.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .padding(end = 12.dp, bottom = 14.dp)
                        .align(Alignment.BottomEnd)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(36.dp)
                    ) {
                        IconButton(
                            onClick = { zoomScale = (zoomScale + 0.2f).coerceAtMost(1.8f) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Zoom In",
                                tint = Color(0xFF334155),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                        IconButton(
                            onClick = { zoomScale = (zoomScale - 0.2f).coerceAtLeast(0.8f) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Zoom Out",
                                tint = Color(0xFF334155),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Overlay 4: Bottom-Left Floating ETA Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 4.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .padding(start = 12.dp, bottom = 14.dp)
                        .align(Alignment.BottomStart)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = "ETA Truck",
                            tint = Color(0xFF15803D),
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ETA",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B),
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = etaMinutes,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF15803D)
                            )
                        }
                    }
                }
            }

            // 2. TRUCK ID & DRIVER CARD
            ElevatedCard(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp))
                    .testTag("truck_driver_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Top row: Truck icon, Truck ID & Driver, and On Route status badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE8F5E9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalShipping,
                                contentDescription = "Truck Icon",
                                tint = Color(0xFF15803D),
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Truck ID",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "TN 07 BX 4582",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Driver: Suresh Kumar",
                                fontSize = 13.sp,
                                color = Color(0xFF475569),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // On Route badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFDCFCE7),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF15803D))
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "On Route",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF15803D)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Bottom 3 metric columns: Current Location | Speed | Expected Arrival
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Col 1: Current Location
                        Column(modifier = Modifier.weight(1.1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color(0xFF15803D),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Current Location",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Near Kallupatti",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }

                        // Col 2: Speed
                        Column(modifier = Modifier.weight(0.9f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = Color(0xFF15803D),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Speed",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "32 km/h",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }

                        // Col 3: Expected Arrival
                        Column(modifier = Modifier.weight(0.9f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = Color(0xFF15803D),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Expected Arrival",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = etaMinutes,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }
                }
            }

            // 3. LIVE TRACKING SECTION
            ElevatedCard(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp))
                    .testTag("live_tracking_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = null,
                                tint = Color(0xFF15803D),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Live Tracking",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }

                        TextButton(
                            onClick = { showRouteDetailsSheet = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("view_full_route_button")
                        ) {
                            Text(
                                text = "View Full Route →",
                                color = Color(0xFF15803D),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Horizontal Live Timeline (Farm -> In Transit -> Collection Center)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        // 1. Farm Milestone
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(76.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF15803D)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Agriculture,
                                    contentDescription = "Farm Milestone",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Farm",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "10:24 AM",
                                fontSize = 10.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        // Connecting Progress Bar (Solid Green)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = 18.dp)
                                .height(3.dp)
                                .background(Color(0xFF15803D), shape = RoundedCornerShape(2.dp))
                        )

                        // 2. In Transit Milestone
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(84.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF15803D)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalShipping,
                                    contentDescription = "In Transit Milestone",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "In Transit",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "10:48 AM",
                                fontSize = 10.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        // Connecting Line to Destination (Dashed / Slate)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = 18.dp)
                                .height(2.dp)
                                .background(Color(0xFFCBD5E1), shape = RoundedCornerShape(2.dp))
                        )

                        // 3. Collection Center Milestone
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(92.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF1F5F9))
                                    .border(1.5.dp, Color(0xFF94A3B8), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Domain,
                                    contentDescription = "Collection Center Milestone",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Collection Center",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF475569),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "11:30 AM",
                                fontSize = 10.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }

            // 4. SHIPMENT DETAILS SECTION
            ElevatedCard(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp))
                    .testTag("shipment_details_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = Color(0xFF15803D),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Shipment Details",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }

                        TextButton(
                            onClick = { onNavigateToBatchDetail(activeBatchId) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("view_batch_button")
                        ) {
                            Text(
                                text = "View Batch →",
                                color = Color(0xFF15803D),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Row 1: Batch ID | Product | Quantity
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Batch ID", fontSize = 11.sp, color = Color(0xFF64748B))
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = activeBatch?.id ?: "FD2026-001",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Product", fontSize = 11.sp, color = Color(0xFF64748B))
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = activeBatch?.productName ?: "Fresh Milk",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }

                        Column(modifier = Modifier.weight(0.9f)) {
                            Text(text = "Quantity", fontSize = 11.sp, color = Color(0xFF64748B))
                            Spacer(modifier = Modifier.height(3.dp))
                            val qtyText = activeBatch?.let { "${it.quantity.toInt()} ${it.unit}" } ?: "500 L"
                            Text(
                                text = qtyText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Row 2: From | To | Transporter
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "From", fontSize = 11.sp, color = Color(0xFF64748B))
                            Spacer(modifier = Modifier.height(3.dp))
                            val fromText = activeBatch?.farmerSupplier?.let { "$it Farm" } ?: "Kallupatti Farm"
                            Text(
                                text = fromText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }

                        Column(modifier = Modifier.weight(1.3f)) {
                            Text(text = "To", fontSize = 11.sp, color = Color(0xFF64748B))
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = activeBatch?.destination ?: "Vellore Collection Center",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }

                        Column(modifier = Modifier.weight(1.1f)) {
                            Text(text = "Transporter", fontSize = 11.sp, color = Color(0xFF64748B))
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = activeBatch?.transporter ?: "Sri Lakshmi Dairy",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Modal Sheet showing Complete Route Checkpoints
    if (showRouteDetailsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showRouteDetailsSheet = false },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Full Shipment Transit Route",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Corridor: Kallupatti Farm → SH-12 → NH-44 → Vellore Dairy Hub",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(16.dp))

                RouteCheckpointRow(
                    time = "10:24 AM",
                    title = "Kallupatti Farm Gate",
                    detail = "Dispatched • Cold container temperature: 4.2°C • Seal #VBT-9921",
                    isDone = true
                )
                RouteCheckpointRow(
                    time = "10:38 AM",
                    title = "Toll Gate NH-44 Checkpoint",
                    detail = "Transit passed • Speed 54 km/h • Telemetry OK",
                    isDone = true
                )
                RouteCheckpointRow(
                    time = "10:48 AM",
                    title = "Near Kallupatti / Pudur Junction",
                    detail = "Current Position • Speed 32 km/h • Vibration 0.18g (Trickle charging)",
                    isDone = true,
                    isCurrent = true
                )
                RouteCheckpointRow(
                    time = "11:12 AM (Est.)",
                    title = "Vellore Outer Ring Road",
                    detail = "Remaining distance: 11.4 km",
                    isDone = false
                )
                RouteCheckpointRow(
                    time = "11:30 AM (Est.)",
                    title = "Vellore Milk Collection Center",
                    detail = "Final intake dock & cryptographic hand-off verification",
                    isDone = false
                )

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showRouteDetailsSheet = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close Route View")
                }
            }
        }
    }
}

/**
 * Top App Bar strictly matching the new design:
 * Back button, bold "Truck Tracking" title, subtitle "Live location of your food shipment truck",
 * and a globe icon button on the right.
 */
@Composable
private fun TrackingTopAppBar(
    onNavigateBack: () -> Unit,
    onGlobeClick: () -> Unit
) {
    Surface(
        color = Color(0xFFF8FAF7),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 8.dp)
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF0F172A)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Truck Tracking",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Live location of your food shipment truck",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }

            IconButton(onClick = onGlobeClick) {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = "Globe Route View",
                    tint = Color(0xFF15803D),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Interactive Live Vector Map Canvas:
 * Features realistic geographical terrain contours, water rivers, secondary road lines,
 * landmark town labels ("Kallupatti", "Pudur", "Vellore"),
 * curved route line with soft casing, start/end waypoint badges,
 * and an animated oriented delivery truck with cab and cargo box along the trajectory.
 */
@Composable
private fun LiveRouteCanvas(
    progress: Float,
    zoomScale: Float,
    panOffset: Offset,
    onPanDelta: (Offset) -> Unit
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, _, _ ->
                    onPanDelta(pan)
                }
            }
    ) {
        val w = size.width
        val h = size.height

        // Terrain and water background
        drawRect(color = Color(0xFFE9F3E7))

        // Natural terrain land parcel polygons
        val landPath1 = Path().apply {
            moveTo(0f, 0f)
            lineTo(w * 0.45f, 0f)
            lineTo(w * 0.35f, h * 0.35f)
            lineTo(0f, h * 0.40f)
            close()
        }
        drawPath(landPath1, color = Color(0xFFE1EFE0))

        val landPath2 = Path().apply {
            moveTo(w * 0.60f, h)
            lineTo(w, h)
            lineTo(w, h * 0.55f)
            lineTo(w * 0.70f, h * 0.70f)
            close()
        }
        drawPath(landPath2, color = Color(0xFFDEF0DC))

        // Meandering Blue River
        val riverPath = Path().apply {
            moveTo(w * 0.1f, 0f)
            cubicTo(
                w * 0.25f, h * 0.25f,
                w * 0.05f, h * 0.65f,
                w * 0.20f, h
            )
        }
        drawPath(
            path = riverPath,
            color = Color(0xFFCBE3F5),
            style = Stroke(width = 16f, cap = StrokeCap.Round)
        )

        // Road network background lines
        val secondaryRoad1 = Path().apply {
            moveTo(0f, h * 0.72f)
            lineTo(w, h * 0.65f)
        }
        drawPath(secondaryRoad1, color = Color.White, style = Stroke(width = 6f))
        drawPath(secondaryRoad1, color = Color(0xFFCBD5E1), style = Stroke(width = 1f))

        val secondaryRoad2 = Path().apply {
            moveTo(w * 0.55f, 0f)
            lineTo(w * 0.50f, h)
        }
        drawPath(secondaryRoad2, color = Color.White, style = Stroke(width = 6f))
        drawPath(secondaryRoad2, color = Color(0xFFCBD5E1), style = Stroke(width = 1f))

        // Regional landmark text labels
        drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.rgb(51, 65, 85) // Slate 700
                textSize = 34f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }
            drawText("Kallupatti", (w * 0.46f) + panOffset.x, (h * 0.22f) + panOffset.y, paint)

            paint.textSize = 30f
            paint.color = android.graphics.Color.rgb(71, 85, 105)
            drawText("Pudur", (w * 0.50f) + panOffset.x, (h * 0.84f) + panOffset.y, paint)

            drawText("Vellore", (w * 0.85f) + panOffset.x, (h * 0.48f) + panOffset.y, paint)
        }

        // Coordinates for Farm and Collection Center
        val startPt = Offset(w * 0.16f + panOffset.x, h * 0.74f + panOffset.y)
        val endPt = Offset(w * 0.84f + panOffset.x, h * 0.24f + panOffset.y)

        // Main Route Path (Cubic Bézier Spline)
        val routePath = Path().apply {
            moveTo(startPt.x, startPt.y)
            cubicTo(
                w * 0.35f + panOffset.x, h * 0.82f + panOffset.y,
                w * 0.44f + panOffset.x, h * 0.56f + panOffset.y,
                w * 0.56f + panOffset.x, h * 0.62f + panOffset.y
            )
            cubicTo(
                w * 0.68f + panOffset.x, h * 0.68f + panOffset.y,
                w * 0.72f + panOffset.x, h * 0.35f + panOffset.y,
                endPt.x, endPt.y
            )
        }

        // Soft emerald glow around route
        drawPath(
            path = routePath,
            color = Color(0xFF86EFAC).copy(alpha = 0.5f),
            style = Stroke(width = 14f * zoomScale, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Vivid dark green route line
        drawPath(
            path = routePath,
            color = Color(0xFF15803D),
            style = Stroke(width = 6f * zoomScale, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Calculate truck position (x, y) and tangent angle theta on the curve at progress t
        val (truckPos, truckAngle) = calculateSplinePointAndAngle(
            progress = progress,
            p0 = startPt,
            p1 = Offset(w * 0.35f + panOffset.x, h * 0.82f + panOffset.y),
            p2 = Offset(w * 0.44f + panOffset.x, h * 0.56f + panOffset.y),
            p3 = Offset(w * 0.56f + panOffset.x, h * 0.62f + panOffset.y),
            p4 = Offset(w * 0.68f + panOffset.x, h * 0.68f + panOffset.y),
            p5 = Offset(w * 0.72f + panOffset.x, h * 0.35f + panOffset.y),
            p6 = endPt
        )

        // Draw start marker ("Your Farm")
        drawWaypointMarker(
            center = startPt,
            label = "Your Farm",
            iconType = WaypointIcon.FARM,
            zoom = zoomScale
        )

        // Draw end marker ("Collection Center")
        drawWaypointMarker(
            center = endPt,
            label = "Collection Center",
            iconType = WaypointIcon.COLLECTION_CENTER,
            zoom = zoomScale
        )

        // Draw Animated Oriented Delivery Truck at truckPos
        drawDeliveryTruck(
            center = truckPos,
            angleDegrees = truckAngle,
            zoom = zoomScale
        )
    }
}

/**
 * Draws start and destination waypoint markers with green circular emblem and white pill label.
 */
private enum class WaypointIcon { FARM, COLLECTION_CENTER }

private fun DrawScope.drawWaypointMarker(
    center: Offset,
    label: String,
    iconType: WaypointIcon,
    zoom: Float
) {
    val radius = 18f * zoom

    // Subtle drop shadow under circle
    drawCircle(
        color = Color(0x33000000),
        radius = radius + 3f,
        center = Offset(center.x, center.y + 2f)
    )

    // Deep Green Marker Circle
    drawCircle(
        color = Color(0xFF15803D),
        radius = radius,
        center = center
    )

    // White Inner Symbol
    when (iconType) {
        WaypointIcon.FARM -> {
            // Stylized barn/roof icon
            val path = Path().apply {
                moveTo(center.x - 7f * zoom, center.y + 4f * zoom)
                lineTo(center.x - 7f * zoom, center.y - 1f * zoom)
                lineTo(center.x, center.y - 8f * zoom)
                lineTo(center.x + 7f * zoom, center.y - 1f * zoom)
                lineTo(center.x + 7f * zoom, center.y + 4f * zoom)
                close()
            }
            drawPath(path, color = Color.White)
        }
        WaypointIcon.COLLECTION_CENTER -> {
            // Stylized factory/building icon
            val path = Path().apply {
                moveTo(center.x - 7f * zoom, center.y + 5f * zoom)
                lineTo(center.x - 7f * zoom, center.y - 4f * zoom)
                lineTo(center.x - 2f * zoom, center.y - 1f * zoom)
                lineTo(center.x - 2f * zoom, center.y - 7f * zoom)
                lineTo(center.x + 3f * zoom, center.y - 4f * zoom)
                lineTo(center.x + 7f * zoom, center.y - 7f * zoom)
                lineTo(center.x + 7f * zoom, center.y + 5f * zoom)
                close()
            }
            drawPath(path, color = Color.White)
        }
    }

    // Label pill under marker
    val pillY = center.y + radius + 14f * zoom
    drawContext.canvas.nativeCanvas.apply {
        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.rgb(15, 23, 42) // Slate 900
            textSize = 26f * zoom
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }

        val textWidth = textPaint.measureText(label)
        val pillBgPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            setShadowLayer(6f, 0f, 2f, android.graphics.Color.argb(50, 0, 0, 0))
            isAntiAlias = true
        }

        val paddingH = 14f * zoom
        val rect = android.graphics.RectF(
            center.x - (textWidth / 2f) - paddingH,
            pillY - 20f * zoom,
            center.x + (textWidth / 2f) + paddingH,
            pillY + 10f * zoom
        )
        drawRoundRect(rect, 10f * zoom, 10f * zoom, pillBgPaint)
        drawText(label, center.x, pillY, textPaint)
    }
}

/**
 * Draws the green delivery truck icon with cabin, refrigerated cargo box,
 * wheels, and windshield, rotated according to route direction.
 */
private fun DrawScope.drawDeliveryTruck(
    center: Offset,
    angleDegrees: Float,
    zoom: Float
) {
    rotate(degrees = angleDegrees, pivot = center) {
        val s = zoom * 1.15f

        // Soft drop shadow under vehicle
        drawRoundRect(
            color = Color(0x35000000),
            topLeft = Offset(center.x - 24f * s, center.y - 12f * s + 3f),
            size = androidx.compose.ui.geometry.Size(48f * s, 24f * s),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f * s, 6f * s)
        )

        // 1. Cargo Box (Refrigerated white container with green branding)
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(center.x - 24f * s, center.y - 12f * s),
            size = androidx.compose.ui.geometry.Size(32f * s, 24f * s),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f * s, 4f * s)
        )
        // Green transit livery stripe
        drawRect(
            color = Color(0xFF15803D),
            topLeft = Offset(center.x - 24f * s, center.y - 2f * s),
            size = androidx.compose.ui.geometry.Size(32f * s, 6f * s)
        )

        // 2. Front Driver Cab (Deep agricultural green)
        drawRoundRect(
            color = Color(0xFF15803D),
            topLeft = Offset(center.x + 8f * s, center.y - 10f * s),
            size = androidx.compose.ui.geometry.Size(16f * s, 20f * s),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f * s, 4f * s)
        )

        // Windshield (Sky tint)
        drawRoundRect(
            color = Color(0xFFBAE6FD),
            topLeft = Offset(center.x + 13f * s, center.y - 8f * s),
            size = androidx.compose.ui.geometry.Size(8f * s, 16f * s),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f * s, 2f * s)
        )

        // Headlight glow
        drawCircle(
            color = Color(0xFFFEF08A),
            radius = 3f * s,
            center = Offset(center.x + 24f * s, center.y - 6f * s)
        )
        drawCircle(
            color = Color(0xFFFEF08A),
            radius = 3f * s,
            center = Offset(center.x + 24f * s, center.y + 6f * s)
        )

        // Wheels (Top and Bottom wheels)
        val wheelColor = Color(0xFF1E293B)
        drawRoundRect(
            color = wheelColor,
            topLeft = Offset(center.x - 18f * s, center.y - 14f * s),
            size = androidx.compose.ui.geometry.Size(8f * s, 3f * s)
        )
        drawRoundRect(
            color = wheelColor,
            topLeft = Offset(center.x - 18f * s, center.y + 11f * s),
            size = androidx.compose.ui.geometry.Size(8f * s, 3f * s)
        )
        drawRoundRect(
            color = wheelColor,
            topLeft = Offset(center.x + 10f * s, center.y - 12f * s),
            size = androidx.compose.ui.geometry.Size(8f * s, 3f * s)
        )
        drawRoundRect(
            color = wheelColor,
            topLeft = Offset(center.x + 10f * s, center.y + 9f * s),
            size = androidx.compose.ui.geometry.Size(8f * s, 3f * s)
        )
    }
}

/**
 * Calculates current position and heading angle along the dual-segment Bézier curve.
 */
private fun calculateSplinePointAndAngle(
    progress: Float,
    p0: Offset,
    p1: Offset,
    p2: Offset,
    p3: Offset,
    p4: Offset,
    p5: Offset,
    p6: Offset
): Pair<Offset, Float> {
    val t = progress.coerceIn(0f, 1f)
    val pos: Offset
    val dPos: Offset

    if (t < 0.5f) {
        val segT = t * 2f
        val oneMinusT = 1f - segT

        // Cubic Bézier formula
        val x = oneMinusT * oneMinusT * oneMinusT * p0.x +
                3f * oneMinusT * oneMinusT * segT * p1.x +
                3f * oneMinusT * segT * segT * p2.x +
                segT * segT * segT * p3.x
        val y = oneMinusT * oneMinusT * oneMinusT * p0.y +
                3f * oneMinusT * oneMinusT * segT * p1.y +
                3f * oneMinusT * segT * segT * p2.y +
                segT * segT * segT * p3.y
        pos = Offset(x, y)

        // Derivative for tangent vector
        val dx = 3f * oneMinusT * oneMinusT * (p1.x - p0.x) +
                6f * oneMinusT * segT * (p2.x - p1.x) +
                3f * segT * segT * (p3.x - p2.x)
        val dy = 3f * oneMinusT * oneMinusT * (p1.y - p0.y) +
                6f * oneMinusT * segT * (p2.y - p1.y) +
                3f * segT * segT * (p3.y - p2.y)
        dPos = Offset(dx, dy)
    } else {
        val segT = (t - 0.5f) * 2f
        val oneMinusT = 1f - segT

        val x = oneMinusT * oneMinusT * oneMinusT * p3.x +
                3f * oneMinusT * oneMinusT * segT * p4.x +
                3f * oneMinusT * segT * segT * p5.x +
                segT * segT * segT * p6.x
        val y = oneMinusT * oneMinusT * oneMinusT * p3.y +
                3f * oneMinusT * oneMinusT * segT * p4.y +
                3f * oneMinusT * segT * segT * p5.y +
                segT * segT * segT * p6.y
        pos = Offset(x, y)

        val dx = 3f * oneMinusT * oneMinusT * (p4.x - p3.x) +
                6f * oneMinusT * segT * (p5.x - p4.x) +
                3f * segT * segT * (p6.x - p5.x)
        val dy = 3f * oneMinusT * oneMinusT * (p4.y - p3.y) +
                6f * oneMinusT * segT * (p5.y - p4.y) +
                3f * segT * segT * (p6.y - p5.y)
        dPos = Offset(dx, dy)
    }

    val angle = Math.toDegrees(atan2(dPos.y.toDouble(), dPos.x.toDouble())).toFloat()
    return Pair(pos, angle)
}

/**
 * Route checkpoint row item for the modal sheet.
 */
@Composable
private fun RouteCheckpointRow(
    time: String,
    title: String,
    detail: String,
    isDone: Boolean,
    isCurrent: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCurrent -> Color(0xFF15803D)
                            isDone -> Color(0xFF16A34A)
                            else -> Color(0xFFCBD5E1)
                        }
                    )
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(38.dp)
                    .background(Color(0xFFE2E8F0))
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (isCurrent) Color(0xFF15803D) else Color(0xFF0F172A)
                )
                Text(
                    text = time,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = detail,
                fontSize = 11.sp,
                color = Color(0xFF475569),
                lineHeight = 15.sp
            )
        }
    }
}
