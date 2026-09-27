package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    
    // Main 7 tabs
    object Dashboard : Screen("dashboard")
    object Batches : Screen("batches")
    object Analytics : Screen("analytics")
    object Tracking : Screen("tracking")
    object Alerts : Screen("alerts")
    object Blockchain : Screen("blockchain")
    object Device : Screen("device")
    
    // Sub-screens
    object BatchDetail : Screen("batch_detail/{batchId}") {
        fun createRoute(batchId: String) = "batch_detail/$batchId"
    }
    object CreateBatch : Screen("create_batch")
    object SensorMonitoring : Screen("sensor_monitoring/{batchId}") {
        fun createRoute(batchId: String) = "sensor_monitoring/$batchId"
    }
    object PublicTraceability : Screen("public_traceability/{batchId}") {
        fun createRoute(batchId: String) = "public_traceability/$batchId"
    }
    object QrScanner : Screen("qr_scanner")
    object EnergyHarvesting : Screen("energy_harvesting")
    object DeviceDiagnostics : Screen("device_diagnostics")
    object Settings : Screen("settings")
    object Profile : Screen("profile")
}
