package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AgriBlueSecondary
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.StatusCritical
import kotlin.math.max
import kotlin.math.min

data class DataPoint(val timestamp: Long, val value: Double)

@Composable
fun SensorChartCard(
    title: String,
    unit: String,
    dataPoints: List<DataPoint>,
    lineColor: Color = AgriGreenPrimary,
    threshold: Double? = null,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("Live") }
    val timeFilters = listOf("Live", "1h", "6h", "24h", "7d")

    // Filter points based on filter selection
    val filteredPoints = remember(dataPoints, selectedFilter) {
        val now = System.currentTimeMillis()
        when (selectedFilter) {
            "Live" -> dataPoints.takeLast(20)
            "1h" -> dataPoints.filter { now - it.timestamp <= 3600_000L }
            "6h" -> dataPoints.filter { now - it.timestamp <= 6 * 3600_000L }
            "24h" -> dataPoints.filter { now - it.timestamp <= 24 * 3600_000L }
            else -> dataPoints
        }.ifEmpty { dataPoints.takeLast(10) }
    }

    val currentVal = filteredPoints.lastOrNull()?.value ?: 0.0
    val minVal = filteredPoints.minOfOrNull { it.value } ?: 0.0
    val maxVal = filteredPoints.maxOfOrNull { it.value } ?: 0.0
    val avgVal = if (filteredPoints.isNotEmpty()) filteredPoints.map { it.value }.average() else 0.0

    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${String.format("%.2f", currentVal)} $unit",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = lineColor
                    )
                }

                // Time Filter Chips
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    timeFilters.forEach { filter ->
                        val isSelected = selectedFilter == filter
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) lineColor else MaterialTheme.colorScheme.surfaceVariant,
                            onClick = { selectedFilter = filter },
                            modifier = Modifier.height(28.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = filter,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stats row (Min, Max, Avg, Threshold)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(label = "Min", value = "${String.format("%.1f", minVal)}$unit")
                StatItem(label = "Max", value = "${String.format("%.1f", maxVal)}$unit")
                StatItem(label = "Avg", value = "${String.format("%.1f", avgVal)}$unit")
                if (threshold != null) {
                    StatItem(label = "Limit", value = "${String.format("%.1f", threshold)}$unit", isWarning = currentVal > threshold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (filteredPoints.size < 2) return@Canvas

                    val width = size.width
                    val height = size.height

                    val yMin = (min(minVal, threshold ?: minVal) * 0.9).toFloat()
                    val yMax = (max(maxVal, threshold ?: maxVal) * 1.1).toFloat()
                    val yRange = if (yMax - yMin == 0f) 1f else (yMax - yMin)

                    // Draw grid reference lines
                    for (i in 1..3) {
                        val gridY = height * (i / 4f)
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.4f),
                            start = Offset(0f, gridY),
                            end = Offset(width, gridY),
                            strokeWidth = 1f
                        )
                    }

                    // Draw threshold dashed line if set
                    if (threshold != null) {
                        val threshY = height - ((threshold.toFloat() - yMin) / yRange * height)
                        if (threshY in 0f..height) {
                            drawLine(
                                color = StatusCritical.copy(alpha = 0.7f),
                                start = Offset(0f, threshY),
                                end = Offset(width, threshY),
                                strokeWidth = 1.5.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    }

                    val stepX = width / (filteredPoints.size - 1)
                    val points = filteredPoints.mapIndexed { index, dp ->
                        val x = index * stepX
                        val y = height - ((dp.value.toFloat() - yMin) / yRange * height)
                        Offset(x, y.coerceIn(0f, height))
                    }

                    // Build path and gradient area
                    val path = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            val pPrev = points[i - 1]
                            val pCurr = points[i]
                            val controlX = (pPrev.x + pCurr.x) / 2
                            cubicTo(controlX, pPrev.y, controlX, pCurr.y, pCurr.x, pCurr.y)
                        }
                    }

                    val fillPath = Path().apply {
                        addPath(path)
                        lineTo(points.last().x, height)
                        lineTo(points.first().x, height)
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(lineColor.copy(alpha = 0.35f), lineColor.copy(alpha = 0.02f)),
                            startY = 0f,
                            endY = height
                        )
                    )

                    drawPath(
                        path = path,
                        color = lineColor,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw point dots
                    points.takeLast(5).forEach { p ->
                        drawCircle(color = Color.White, radius = 4.dp.toPx(), center = p)
                        drawCircle(color = lineColor, radius = 2.5.dp.toPx(), center = p)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, isWarning: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isWarning) StatusCritical else MaterialTheme.colorScheme.onSurface
        )
    }
}
