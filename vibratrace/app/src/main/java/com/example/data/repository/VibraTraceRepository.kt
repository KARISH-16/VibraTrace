package com.example.data.repository

import com.example.data.local.VibraTraceDatabase
import com.example.data.local.entity.*
import com.example.data.remote.ConnectionStatus
import com.example.data.remote.VibraTraceApi
import com.example.data.remote.WebSocketClient
import com.example.data.remote.model.ApiBatchDto
import com.example.data.security.BlockchainService
import com.example.domain.engine.DemoSimulationEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class VibraTraceRepository(
    private val db: VibraTraceDatabase,
    private val api: VibraTraceApi?,
    private val wsClient: WebSocketClient,
    val simulationEngine: DemoSimulationEngine,
    val blockchainService: BlockchainService
) {
    val allBatches: Flow<List<BatchEntity>> = db.batchDao().getAllBatches()
    val activeAlerts: Flow<List<AlertEntity>> = db.alertDao().getActiveAlerts()
    val allAlerts: Flow<List<AlertEntity>> = db.alertDao().getAllAlerts()
    val pendingSyncCount: Flow<Int> = db.syncQueueDao().getPendingSyncCount()
    val allTamperEvents: Flow<List<TamperEventEntity>> = db.tamperEventDao().getAllTamperEvents()
    val allHashRecords: Flow<List<HashRecordEntity>> = db.hashRecordDao().getAllHashRecords()
    val allBlockchainRecords: Flow<List<BlockchainRecordEntity>> = db.blockchainRecordDao().getAllBlockchainRecords()
    val recentEnergyEvents: Flow<List<EnergyEventEntity>> = db.energyEventDao().getRecentEnergyEvents()
    val appSettings: Flow<AppSettingsEntity?> = db.appSettingsDao().getSettings()
    val activeUser: Flow<UserEntity?> = db.userDao().getActiveUser()
    val connectionState: StateFlow<ConnectionStatus> = wsClient.connectionState

    fun getBatch(id: String): Flow<BatchEntity?> = db.batchDao().getBatchByIdFlow(id)
    fun getReadingsForBatch(id: String): Flow<List<SensorReadingEntity>> = db.sensorReadingDao().getReadingsForBatch(id)
    fun getLatestReading(id: String): Flow<SensorReadingEntity?> = db.sensorReadingDao().getLatestReadingForBatch(id)
    fun getTimelineEvents(id: String): Flow<List<SupplyChainEventEntity>> = db.supplyChainEventDao().getEventsForBatch(id)
    fun getDevice(id: String): Flow<DeviceEntity?> = db.deviceDao().getDeviceById(id)

    suspend fun createBatch(batch: BatchEntity) {
        db.batchDao().insertBatch(batch)
        // Anchor to blockchain
        simulationEngine.anchorEventToBlockchain(batch.id, "BATCH_CREATION")
        // Try remote if available
        try {
            api?.createBatch(
                ApiBatchDto(
                    id = batch.id,
                    productName = batch.productName,
                    productCategory = batch.productCategory,
                    farmerSupplier = batch.farmerSupplier,
                    quantity = batch.quantity,
                    unit = batch.unit,
                    harvestDate = batch.harvestDate,
                    packingDate = batch.packingDate,
                    destination = batch.destination,
                    transporter = batch.transporter,
                    assignedDeviceId = batch.assignedDeviceId,
                    currentStage = batch.currentStage,
                    qrToken = batch.qrToken,
                    conditionStatus = batch.conditionStatus,
                    integrityStatus = batch.integrityStatus
                )
            )
        } catch (_: Exception) {}
    }

    suspend fun resolveAlert(alertId: Long) {
        db.alertDao().resolveAlert(alertId)
    }

    suspend fun acknowledgeTamper(eventId: String) {
        db.tamperEventDao().acknowledgeTamper(eventId)
    }

    suspend fun saveSettings(settings: AppSettingsEntity) {
        db.appSettingsDao().saveSettings(settings)
    }

    suspend fun loginUser(email: String, name: String, role: String, token: String) {
        db.userDao().logoutAll()
        db.userDao().insertUser(
            UserEntity(
                fullName = name,
                email = email,
                phone = "+1 555-019-2834",
                role = role,
                token = token,
                isLoggedIn = true
            )
        )
    }

    suspend fun logout() {
        db.userDao().logoutAll()
    }
}
