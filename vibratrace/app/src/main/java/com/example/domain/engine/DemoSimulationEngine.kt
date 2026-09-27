package com.example.domain.engine

import com.example.data.local.VibraTraceDatabase
import com.example.data.local.entity.*
import com.example.data.security.BlockchainService
import com.example.data.security.CanonicalPayload
import com.example.data.security.HashEngine
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.firstOrNull
import kotlin.random.Random

class DemoSimulationEngine(
    private val db: VibraTraceDatabase,
    private val alertEngine: SmartAlertEngine,
    private val blockchainService: BlockchainService,
    private val notificationHelper: NotificationHelper
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var telemetryJob: Job? = null
    var isSimulatedNetworkOnline: Boolean = true
        private set

    private var eventCounter: Long = 2000
    private var previousHash: String = HashEngine.GENESIS_HASH

    suspend fun initializeDefaultData() {
        val batchDao = db.batchDao()
        val deviceDao = db.deviceDao()
        val settingsDao = db.appSettingsDao()

        // Default device
        val existingDevice = deviceDao.getDeviceById("VT-ESP32-001").firstOrNull()
        if (existingDevice == null) {
            deviceDao.insertOrUpdateDevice(
                DeviceEntity(
                    id = "VT-ESP32-001",
                    assignedBatchId = "FD2026-001",
                    firmwareVersion = "v1.4.2",
                    batteryLevel = 84,
                    gsmSignal = -72,
                    networkStatus = "ONLINE",
                    mqttStatus = "ONLINE",
                    sdCardStatus = "ONLINE",
                    enclosureStatus = "CLOSED",
                    tamperDetected = false
                )
            )
        }

        // Default batch FD2026-001
        val existingBatch = batchDao.getBatchById("FD2026-001")
        if (existingBatch == null) {
            batchDao.insertBatch(
                BatchEntity(
                    id = "FD2026-001",
                    productName = "Organic Alphonso Mangoes",
                    productCategory = "Fresh Fruit / Export",
                    farmerSupplier = "Sahyadri Agro Collective (Maharashtra)",
                    quantity = 850.0,
                    unit = "kg",
                    harvestDate = "2026-09-24",
                    packingDate = "2026-09-25",
                    destination = "Metro Cold-Chain Hub, Rotterdam Terminal",
                    transporter = "ThermoTrans Reefer Logistics #402",
                    assignedDeviceId = "VT-ESP32-001",
                    currentStage = "TRANSPORT",
                    qrToken = "VT-TRACE-FD2026-001-TOKEN-9942",
                    conditionStatus = "GOOD",
                    integrityStatus = "VERIFIED"
                )
            )

            // Seed initial Farm-to-Fork Timeline events
            val eventDao = db.supplyChainEventDao()
            eventDao.insertEvent(
                SupplyChainEventEntity(
                    batchId = "FD2026-001",
                    stage = "FARM",
                    timestamp = System.currentTimeMillis() - 86400000L * 2,
                    location = "Location unavailable",
                    responsibleParty = "Sahyadri Agro Organic Cluster #12",
                    status = "COMPLETED",
                    sensorCondition = "NORMAL",
                    verificationStatus = "VERIFIED"
                )
            )
            eventDao.insertEvent(
                SupplyChainEventEntity(
                    batchId = "FD2026-001",
                    stage = "PACKING",
                    timestamp = System.currentTimeMillis() - 86400000L,
                    location = "Location unavailable",
                    responsibleParty = "AgriPack Solutions Dock 4",
                    status = "COMPLETED",
                    sensorCondition = "NORMAL",
                    verificationStatus = "VERIFIED"
                )
            )
            eventDao.insertEvent(
                SupplyChainEventEntity(
                    batchId = "FD2026-001",
                    stage = "TRANSPORT",
                    timestamp = System.currentTimeMillis() - 3600000L * 6,
                    location = "Location unavailable",
                    responsibleParty = "ThermoTrans Reefer Logistics #402",
                    status = "IN_PROGRESS",
                    sensorCondition = "NORMAL",
                    verificationStatus = "VERIFIED"
                )
            )

            // Seed baseline readings
            generateSensorReading(
                batchId = "FD2026-001",
                temp = 6.8,
                hum = 72.0,
                c2h4 = 0.42,
                nh3 = 1.8,
                tamper = false,
                battery = 84
            )
        }

        // Default app settings
        val settings = settingsDao.getSettings().firstOrNull()
        if (settings == null) {
            settingsDao.saveSettings(AppSettingsEntity())
        }
    }

    suspend fun generateSensorReading(
        batchId: String = "FD2026-001",
        deviceId: String = "VT-ESP32-001",
        temp: Double? = null,
        hum: Double? = null,
        c2h4: Double? = null,
        nh3: Double? = null,
        tamper: Boolean = false,
        battery: Int = 84
    ): SensorReadingEntity {
        eventCounter++
        val eventId = "evt-$eventCounter"
        val timestamp = System.currentTimeMillis()

        val finalTemp = temp ?: (6.6 + Random.nextDouble(-0.3, 0.5))
        val finalHum = hum ?: (71.5 + Random.nextDouble(-1.0, 1.5))
        val finalEthylene = c2h4 ?: (0.40 + Random.nextDouble(-0.02, 0.05))
        val finalNh3 = nh3 ?: (1.75 + Random.nextDouble(-0.1, 0.15))

        val canonical = CanonicalPayload(
            recordId = eventId,
            batchId = batchId,
            deviceId = deviceId,
            timestamp = timestamp,
            eventType = "TELEMETRY",
            temperature = finalTemp,
            humidity = finalHum,
            ethylene = finalEthylene,
            ammonia = finalNh3,
            tamper = tamper,
            previousHash = previousHash
        )
        val canonicalString = canonical.serialize()
        val currentHash = HashEngine.calculateSha256(canonicalString)

        val reading = SensorReadingEntity(
            batchId = batchId,
            deviceId = deviceId,
            eventId = eventId,
            timestamp = timestamp,
            temperature = finalTemp,
            humidity = finalHum,
            ethylene = finalEthylene,
            ammonia = finalNh3,
            battery = battery,
            tamper = tamper,
            signal = -72,
            hash = currentHash,
            previousHash = previousHash
        )

        // Store hash record
        val hashRecord = HashRecordEntity(
            recordId = eventId,
            batchId = batchId,
            deviceId = deviceId,
            currentHash = currentHash,
            previousHash = previousHash,
            canonicalPayload = canonicalString,
            eventType = "TELEMETRY",
            timestamp = timestamp,
            verificationStatus = "VERIFIED"
        )
        db.hashRecordDao().insertHashRecord(hashRecord)

        // Advance chain
        previousHash = currentHash

        // Check if offline
        if (!isSimulatedNetworkOnline) {
            // Buffer to offline sync queue
            val syncRecord = SyncQueueEntity(
                eventId = eventId,
                batchId = batchId,
                deviceId = deviceId,
                payloadJson = canonicalString,
                syncStatus = "PENDING"
            )
            db.syncQueueDao().enqueueRecord(syncRecord)
        } else {
            // Online direct store
            db.sensorReadingDao().insertReading(reading)
        }

        // Evaluate smart alerts
        val settings = db.appSettingsDao().getSettings().firstOrNull() ?: AppSettingsEntity()
        alertEngine.evaluateReading(reading, settings)

        // Record energy harvesting event
        db.energyEventDao().insertEnergyEvent(
            EnergyEventEntity(
                deviceId = deviceId,
                batteryPercentage = battery,
                batteryStatus = if (battery < 20) "LOW" else "GOOD",
                vibrationDetected = true,
                harvestingStatus = "HARVESTING_ACTIVE",
                chargingStatus = "SUPPLEMENTARY_CHARGING"
            )
        )

        return reading
    }

    suspend fun setNetworkState(online: Boolean) {
        isSimulatedNetworkOnline = online
        val status = if (online) "ONLINE" else "OFFLINE"
        db.deviceDao().updateNetworkStatus("VT-ESP32-001", status)

        if (online) {
            notificationHelper.showNotification(
                id = 201,
                title = "Network Restored",
                message = "GSM/MQTT connection re-established with backend.",
                isCritical = false
            )
            synchronizeOfflineQueue()
        } else {
            notificationHelper.showNotification(
                id = 202,
                title = "Network Lost",
                message = "Switching to MicroSD offline buffering. Telemetry queued locally.",
                isCritical = false
            )
        }
    }

    suspend fun seed127OfflineBufferRecords() {
        val batchId = "FD2026-001"
        val deviceId = "VT-ESP32-001"
        isSimulatedNetworkOnline = false
        db.deviceDao().updateNetworkStatus(deviceId, "OFFLINE")

        for (i in 1..127) {
            val evtId = "offline-evt-$i"
            val canon = "offline_reading_$i"
            db.syncQueueDao().enqueueRecord(
                SyncQueueEntity(
                    eventId = evtId,
                    batchId = batchId,
                    deviceId = deviceId,
                    payloadJson = canon,
                    syncStatus = "PENDING",
                    timestamp = System.currentTimeMillis() - (128 - i) * 60000L
                )
            )
        }
    }

    suspend fun synchronizeOfflineQueue(): Pair<Int, Int> {
        val pendingCount = db.syncQueueDao().getPendingSyncRecords().firstOrNull()?.size ?: 0
        if (pendingCount > 0) {
            db.syncQueueDao().markAllSynced()
            notificationHelper.showNotification(
                id = 203,
                title = "Synchronization Completed",
                message = "$pendingCount/$pendingCount records synchronized successfully with PostgreSQL.",
                isCritical = false
            )
        }
        return Pair(pendingCount, pendingCount)
    }

    suspend fun triggerTamperEvent(batchId: String = "FD2026-001", deviceId: String = "VT-ESP32-001") {
        val eventId = "tamper-${System.currentTimeMillis()}"
        val tamper = TamperEventEntity(
            eventId = eventId,
            batchId = batchId,
            deviceId = deviceId,
            location = "Location unavailable",
            severity = "HIGH",
            hash = HashEngine.calculateSha256("tamper:$eventId:$batchId:$deviceId")
        )
        db.tamperEventDao().insertTamperEvent(tamper)
        generateSensorReading(batchId = batchId, deviceId = deviceId, tamper = true)
    }

    suspend fun triggerTemperatureExcursionAlert(batchId: String = "FD2026-001") {
        generateSensorReading(batchId = batchId, temp = 14.8) // High above 10°C
    }

    suspend fun triggerEthyleneAlert(batchId: String = "FD2026-001") {
        generateSensorReading(batchId = batchId, c2h4 = 1.25) // High above 0.50 ppm
    }

    suspend fun triggerAmmoniaAlert(batchId: String = "FD2026-001") {
        generateSensorReading(batchId = batchId, nh3 = 4.80) // High above 2.0 ppm
    }

    suspend fun triggerLowBattery(batchId: String = "FD2026-001") {
        generateSensorReading(batchId = batchId, battery = 14) // Low under 20%
    }

    suspend fun simulateIntegrityMismatch(recordId: String) {
        // Corrupt canonical payload in database to demonstrate cryptographic tampering detection
        db.hashRecordDao().simulateTampering(
            recordId = recordId,
            corruptedPayload = "CORRUPTED_TAMPERED_DATA_EXCURSION_MUTATED"
        )
        val alert = AlertEntity(
            batchId = "FD2026-001",
            deviceId = "VT-ESP32-001",
            alertType = "INTEGRITY_MISMATCH",
            severity = "CRITICAL",
            message = "INTEGRITY MISMATCH: Historical record $recordId hash does not match original SHA-256 verification digest. Possible database manipulation detected."
        )
        db.alertDao().insertAlert(alert)
        notificationHelper.showNotification(
            id = 204,
            title = "INTEGRITY MISMATCH DETECTED",
            message = "Record $recordId cryptographic hash mismatch. Audit log alerted."
        )
    }

    suspend fun anchorEventToBlockchain(batchId: String, eventType: String): BlockchainRecordEntity {
        val recordId = "anchor-${System.currentTimeMillis()}"
        val digest = HashEngine.calculateSha256("$batchId:$eventType:${System.currentTimeMillis()}")
        val tx = blockchainService.submitHash(batchId, recordId, digest, eventType)

        val entity = BlockchainRecordEntity(
            transactionId = tx.transactionId,
            batchId = batchId,
            recordId = recordId,
            hash = digest,
            ledgerType = tx.ledgerType,
            blockNumber = tx.blockNumber,
            timestamp = tx.timestamp,
            status = "COMMITTED"
        )
        db.blockchainRecordDao().insertRecord(entity)
        return entity
    }

    suspend fun advanceShipmentStage(batchId: String, nextStage: String) {
        val batch = db.batchDao().getBatchById(batchId) ?: return
        db.batchDao().updateBatch(batch.copy(currentStage = nextStage, updatedAt = System.currentTimeMillis()))

        val event = SupplyChainEventEntity(
            batchId = batchId,
            stage = nextStage,
            timestamp = System.currentTimeMillis(),
            location = "Location unavailable",
            responsibleParty = when (nextStage) {
                "COLD_STORAGE" -> "Rotterdam DeepFreeze Facility Dock 2"
                "DISTRIBUTION" -> "EuroFresh Central Logistics DC"
                "BUYER" -> "GreenGrocer Retail Supermarket #108"
                else -> "Carrier Partner"
            },
            status = "COMPLETED",
            sensorCondition = "NORMAL",
            verificationStatus = "VERIFIED"
        )
        db.supplyChainEventDao().insertEvent(event)
        anchorEventToBlockchain(batchId, "STAGE_TRANSITION_$nextStage")
    }

    fun startContinuousTelemetry(intervalMillis: Long = 4000) {
        telemetryJob?.cancel()
        telemetryJob = scope.launch {
            while (isActive) {
                delay(intervalMillis)
                try {
                    generateSensorReading()
                } catch (_: Exception) {}
            }
        }
    }

    fun stopContinuousTelemetry() {
        telemetryJob?.cancel()
    }
}
