package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AgriBlueSecondary
import com.example.ui.theme.AgriGreenContainer
import com.example.ui.theme.AgriGreenPrimary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateToOnboarding: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1600)
        onNavigateToOnboarding()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = AgriGreenContainer,
                modifier = Modifier.size(100.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.vibratrace_icon_1790433837517),
                        contentDescription = "VibraTrace Logo",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "VibraTrace",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = AgriGreenPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Sense → Secure → Store → Sync → Verify → Trace",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = AgriBlueSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Low-Cost IoT Blockchain Node for Secure Farm-to-Fork Traceability",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = AgriGreenPrimary,
                strokeWidth = 2.5.dp
            )
        }
    }
}

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    var currentPage by remember { mutableStateOf(0) }

    val pages = listOf(
        OnboardingItem(
            title = "Monitor Every Journey",
            desc = "Continuous multi-sensor tracking with AHT20 temperature, humidity, ethylene, ammonia, and container tamper detection.",
            icon = Icons.Default.Sensors
        ),
        OnboardingItem(
            title = "Secure Every Record",
            desc = "Tamper-aware cryptographic SHA-256 hash chaining paired with offline MicroSD storage that synchronizes automatically upon network recovery.",
            icon = Icons.Default.Lock
        ),
        OnboardingItem(
            title = "Verify Every Batch",
            desc = "Seamless immutable ledger anchoring and instant QR scanning for consumers and retailers across the entire Farm-to-Fork supply chain.",
            icon = Icons.Default.Verified
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Skip Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = onFinish,
                modifier = Modifier.testTag("onboarding_skip_button")
            ) {
                Text(text = "Skip", color = AgriGreenPrimary, fontWeight = FontWeight.Bold)
            }
        }

        // Center Content
        val item = pages[currentPage]
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = AgriGreenContainer,
                modifier = Modifier.size(110.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = AgriGreenPrimary,
                        modifier = Modifier.size(54.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = item.desc,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Bottom Controls
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            // Page Indicator Dots
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pages.indices.forEach { index ->
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(if (index == currentPage) 24.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (index == currentPage) AgriGreenPrimary else Color.LightGray)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    if (currentPage < pages.size - 1) {
                        currentPage++
                    } else {
                        onFinish()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("onboarding_next_button")
            ) {
                Text(
                    text = if (currentPage < pages.size - 1) "Continue" else "Get Started",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null
                )
            }
        }
    }
}

data class OnboardingItem(
    val title: String,
    val desc: String,
    val icon: ImageVector
)
