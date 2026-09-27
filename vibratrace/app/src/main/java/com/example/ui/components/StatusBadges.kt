package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun ConditionBadge(status: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (status.uppercase()) {
        "GOOD" -> Pair(Color(0xFFE8F5E9), StatusGood)
        "WARNING" -> Pair(Color(0xFFFFFBEB), StatusWarning)
        "CRITICAL" -> Pair(Color(0xFFFEF2F2), StatusCritical)
        else -> Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, textColor.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = status.uppercase(),
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun ConnectionBadge(status: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor, text) = when (status.uppercase()) {
        "LIVE" -> Triple(Color(0xFFE8F5E9), StatusGood, "● LIVE")
        "RECONNECTING" -> Triple(Color(0xFFFFFBEB), StatusWarning, "◌ RECONNECTING")
        "OFFLINE" -> Triple(Color(0xFFFEF2F2), StatusCritical, "✕ OFFLINE")
        "DEMO_MODE", "DEMO MODE", "DEMO" -> Triple(Color(0xFFE0F2FE), AgriBlueSecondary, "⚡ DEMO MODE")
        else -> Triple(Color(0xFFF1F5F9), Color(0xFF475569), status)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, textColor.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun IntegrityBadge(isVerified: Boolean, modifier: Modifier = Modifier) {
    val (bgColor, textColor, text, icon) = if (isVerified) {
        listOf(Color(0xFFE8F5E9), StatusGood, "INTEGRITY: VERIFIED", Icons.Default.CheckCircle)
    } else {
        listOf(Color(0xFFFEF2F2), StatusCritical, "INTEGRITY MISMATCH", Icons.Default.Warning)
    }

    Surface(
        color = bgColor as Color,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, (textColor as Color).copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon as androidx.compose.ui.graphics.vector.ImageVector,
                contentDescription = null,
                tint = textColor as Color,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text as String,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
