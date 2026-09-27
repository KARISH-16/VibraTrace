package com.example.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.navigation.Screen

data class NavItem(
    val title: String,
    val route: String,
    val icon: ImageVector,
    val badgeCount: Int = 0
)

@Composable
fun VibraTraceBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    activeAlertsCount: Int = 0
) {
    val items = listOf(
        NavItem("Dashboard", Screen.Dashboard.route, Icons.Default.Dashboard),
        NavItem("Batches", Screen.Batches.route, Icons.Default.Inventory2),
        NavItem("Tracking", Screen.Tracking.route, Icons.Default.LocalShipping),
        NavItem("Alerts", Screen.Alerts.route, Icons.Default.WarningAmber, activeAlertsCount),
        NavItem("Chain", Screen.Blockchain.route, Icons.Default.Security),
        NavItem("Device", Screen.Device.route, Icons.Default.Memory),
        NavItem("Analytics", Screen.Analytics.route, Icons.Default.InsertChart)
    )

    NavigationBar(
        windowInsets = WindowInsets.navigationBars,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (currentRoute != item.route) {
                        onNavigate(item.route)
                    }
                },
                modifier = Modifier.testTag("nav_item_${item.route}"),
                icon = {
                    BadgedBox(
                        badge = {
                            if (item.badgeCount > 0) {
                                Badge { Text(item.badgeCount.toString()) }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                label = {
                    Text(
                        text = item.title,
                        fontSize = 9.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
