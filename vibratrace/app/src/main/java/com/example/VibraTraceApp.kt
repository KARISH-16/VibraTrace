package com.example

import android.app.Application
import com.example.data.local.VibraTraceDatabase
import com.example.data.remote.WebSocketClient
import com.example.data.repository.VibraTraceRepository
import com.example.data.security.DemoLedgerService
import com.example.domain.engine.DemoSimulationEngine
import com.example.domain.engine.NotificationHelper
import com.example.domain.engine.SmartAlertEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VibraTraceApp : Application() {

    lateinit var database: VibraTraceDatabase
        private set

    lateinit var notificationHelper: NotificationHelper
        private set

    lateinit var repository: VibraTraceRepository
        private set

    lateinit var simulationEngine: DemoSimulationEngine
        private set

    override fun onCreate() {
        super.onCreate()
        database = VibraTraceDatabase.getDatabase(this)
        notificationHelper = NotificationHelper(this)

        val alertEngine = SmartAlertEngine(database.alertDao(), notificationHelper)
        val ledgerService = DemoLedgerService()
        val wsClient = WebSocketClient()

        simulationEngine = DemoSimulationEngine(
            db = database,
            alertEngine = alertEngine,
            blockchainService = ledgerService,
            notificationHelper = notificationHelper
        )

        repository = VibraTraceRepository(
            db = database,
            api = null, // Can be configured with Retrofit instance
            wsClient = wsClient,
            simulationEngine = simulationEngine,
            blockchainService = ledgerService
        )

        // Initialize default demo state asynchronously
        CoroutineScope(Dispatchers.IO).launch {
            simulationEngine.initializeDefaultData()
            // Auto login default demo user if none exists
            repository.loginUser(
                email = "karishmaramesh16@gmail.com",
                name = "Karishma Ramesh",
                role = "ADMIN",
                token = "vibratrace-demo-session-token"
            )
        }
    }
}
