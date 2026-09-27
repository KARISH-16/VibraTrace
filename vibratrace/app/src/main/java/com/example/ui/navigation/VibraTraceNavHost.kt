package com.example.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.ui.components.VibraTraceBottomBar
import com.example.ui.components.VibraTraceTopBar
import com.example.ui.screens.*
import com.example.ui.viewmodel.VibraTraceViewModel

@Composable
fun VibraTraceAppRoot(
    viewModel: VibraTraceViewModel,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val activeAlerts by viewModel.activeAlerts.collectAsStateWithLifecycle()
    val activeBatch by viewModel.activeBatch.collectAsStateWithLifecycle()
    val device by viewModel.activeDevice.collectAsStateWithLifecycle()
    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()

    var showDemoControls by remember { mutableStateOf(false) }

    val mainRoutes = setOf(
        Screen.Dashboard.route,
        Screen.Batches.route,
        Screen.Tracking.route,
        Screen.Alerts.route,
        Screen.Blockchain.route,
        Screen.Device.route,
        Screen.Analytics.route
    )

    val isMainTab = currentRoute in mainRoutes

    Scaffold(
        topBar = {
            if (isMainTab && currentRoute != Screen.Tracking.route) {
                val connStatus = if (appSettings?.demoMode == true) "DEMO" else (device?.networkStatus ?: "LIVE")
                VibraTraceTopBar(
                    title = "VibraTrace",
                    connectionStatus = connStatus,
                    onOpenDemoControls = { showDemoControls = true },
                    onOpenQrScanner = { navController.navigate(Screen.QrScanner.route) },
                    onOpenSettings = { navController.navigate(Screen.Settings.route) },
                    onOpenProfile = { navController.navigate(Screen.Profile.route) }
                )
            }
        },
        bottomBar = {
            if (isMainTab) {
                VibraTraceBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    activeAlertsCount = activeAlerts.size
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route
            ) {
                composable(Screen.Splash.route) {
                    SplashScreen(
                        onNavigateToOnboarding = {
                            navController.navigate(Screen.Onboarding.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Onboarding.route) {
                    OnboardingScreen(
                        onFinish = {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Login.route) {
                    LoginScreen(
                        viewModel = viewModel,
                        onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                        onNavigateToForgotPassword = { navController.navigate(Screen.Dashboard.route) },
                        onLoginSuccess = {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Register.route) {
                    RegisterScreen(
                        viewModel = viewModel,
                        onNavigateToLogin = { navController.popBackStack() },
                        onRegisterSuccess = {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Register.route) { inclusive = true }
                            }
                        }
                    )
                }

                // 7 Main Tabs
                composable(Screen.Dashboard.route) {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToSensorCharts = { bId ->
                            navController.navigate(Screen.SensorMonitoring.createRoute(bId))
                        },
                        onNavigateToTimeline = { bId ->
                            viewModel.selectBatch(bId)
                            navController.navigate(Screen.Tracking.route)
                        },
                        onNavigateToQrView = { bId ->
                            navController.navigate(Screen.BatchDetail.createRoute(bId))
                        },
                        onNavigateToAlerts = {
                            navController.navigate(Screen.Alerts.route)
                        }
                    )
                }

                composable(Screen.Batches.route) {
                    BatchesScreen(
                        viewModel = viewModel,
                        onNavigateToBatchDetail = { bId ->
                            navController.navigate(Screen.BatchDetail.createRoute(bId))
                        },
                        onNavigateToCreateBatch = {
                            navController.navigate(Screen.CreateBatch.route)
                        }
                    )
                }

                composable(Screen.Tracking.route) {
                    TrackingTimelineScreen(
                        viewModel = viewModel,
                        onNavigateBack = {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Dashboard.route) { inclusive = false }
                            }
                        },
                        onNavigateToBatchDetail = { bId ->
                            viewModel.selectBatch(bId)
                            navController.navigate(Screen.BatchDetail.createRoute(bId))
                        }
                    )
                }

                composable(Screen.Alerts.route) {
                    AlertsScreen(
                        viewModel = viewModel,
                        onNavigateToBatch = { bId ->
                            viewModel.selectBatch(bId)
                            navController.navigate(Screen.BatchDetail.createRoute(bId))
                        },
                        onNavigateToSensors = { bId ->
                            navController.navigate(Screen.SensorMonitoring.createRoute(bId))
                        }
                    )
                }

                composable(Screen.Blockchain.route) {
                    BlockchainScreen(
                        viewModel = viewModel,
                        onNavigateToBatch = { bId ->
                            viewModel.selectBatch(bId)
                            navController.navigate(Screen.BatchDetail.createRoute(bId))
                        }
                    )
                }

                composable(Screen.Device.route) {
                    DeviceScreen(
                        viewModel = viewModel,
                        onNavigateToDiagnostics = {
                            navController.navigate(Screen.DeviceDiagnostics.route)
                        },
                        onNavigateToEnergy = {
                            navController.navigate(Screen.EnergyHarvesting.route)
                        },
                        onNavigateToBatch = { bId ->
                            viewModel.selectBatch(bId)
                            navController.navigate(Screen.BatchDetail.createRoute(bId))
                        }
                    )
                }

                composable(Screen.Analytics.route) {
                    AnalyticsScreen(viewModel = viewModel)
                }

                // Sub-screens
                composable(Screen.BatchDetail.route) { backStackEntry ->
                    val bId = backStackEntry.arguments?.getString("batchId") ?: "FD2026-001"
                    BatchDetailScreen(
                        batchId = bId,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onNavigateToTraceability = { id ->
                            navController.navigate(Screen.PublicTraceability.createRoute(id))
                        }
                    )
                }

                composable(Screen.CreateBatch.route) {
                    CreateBatchScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onCreated = { bId ->
                            navController.popBackStack()
                            navController.navigate(Screen.BatchDetail.createRoute(bId))
                        }
                    )
                }

                composable(Screen.SensorMonitoring.route) { backStackEntry ->
                    val bId = backStackEntry.arguments?.getString("batchId") ?: "FD2026-001"
                    SensorMonitoringScreen(
                        batchId = bId,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.PublicTraceability.route) { backStackEntry ->
                    val bId = backStackEntry.arguments?.getString("batchId") ?: "FD2026-001"
                    PublicTraceabilityScreen(
                        batchId = bId,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.QrScanner.route) {
                    QrScannerScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onScanBatch = { bId ->
                            navController.popBackStack()
                            navController.navigate(Screen.PublicTraceability.createRoute(bId))
                        }
                    )
                }

                composable(Screen.EnergyHarvesting.route) {
                    EnergyHarvestingScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.DeviceDiagnostics.route) {
                    DeviceDiagnosticsScreen(
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onLogout = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Dashboard.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Profile.route) {
                    ProfileScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }

    if (showDemoControls) {
        DemoControlSheet(
            viewModel = viewModel,
            onDismiss = { showDemoControls = false },
            onNavigateToQrView = { bId ->
                navController.navigate(Screen.BatchDetail.createRoute(bId))
            }
        )
    }
}
