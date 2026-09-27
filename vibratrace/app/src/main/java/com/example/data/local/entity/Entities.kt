package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val email: String,
    val phone: String,
    val role: String, // FARMER, TRANSPORTER, STORAGE_OPERATOR, DISTRIBUTOR, RETAILER, ADMIN
    val token: String,
    val isLoggedIn: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "batches")
data class BatchEntity(
    @PrimaryKey val id: String, // e.g. "FD2026-001"
    val productName: String,
    val productCategory: String = "Fresh Produce",
    val farmerSupplier: String,
    val quantity: Double,
    val unit: String = "kg",
    val harvestDate: String,
    val packingDate: String,
    val destination: String,
    val transporter: String,
    val assignedDeviceId: String?,
    val currentStage: String = "FARM", // FARM, PACKING, TRANSPORT, COLD_STORAGE, DISTRIBUTION, BUYER
    val qrToken: String,
    val conditionStatus: String = "GOOD", // GOOD, WARNING, CRITICAL
    val integrityStatus: String = "VERIFIED", // VERIFIED, MISMATCH
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey val id: String, // e.g. "VT-ESP32-001"
    val assignedBatchId: String?,
    val firmwareVersion: String = "v1.4.2",
    val batteryLevel: Int = 84,
    val gsmSignal: Int = -72,
    val networkStatus: String = "ONLINE", // ONLINE, OFFLINE, RECONNECTING, DEMO
    val mqttStatus: String = "ONLINE",
    val sdCardStatus: String = "ONLINE",
    val enclosureStatus: String = "CLOSED", // CLOSED, OPEN
    val tamperDetected: Boolean = false,
    val aht20Status: String = "ONLINE",
    val ethyleneSensorStatus: String = "ONLINE",
    val nh3SensorStatus: String = "ONLINE",
    val reedSwitchStatus: String = "ONLINE",
    val lastCommunication: Long = System.currentTimeMillis()
)

@Entity(tableName = "sensor_readings")
data class SensorReadingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val batchId: String,
    val deviceId: String,
    val eventId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val temperature: Double,
    val humidity: Double,
    val ethylene: Double?, // optional sensor
    val ammonia: Double,
    val battery: Int,
    val tamper: Boolean = false,
    val signal: Int = -72,
    val hash: String,
    val previousHash: String
)

@Entity(tableName = "supply_chain_events")
data class SupplyChainEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val batchId: String,
    val stage: String, // FARM, PACKING, TRANSPORT, COLD_STORAGE, DISTRIBUTION, BUYER
    val timestamp: Long = System.currentTimeMillis(),
    val location: String = "Location unavailable", // per Rule 49: Do not fabricate GPS coordinates
    val responsibleParty: String,
    val status: String = "COMPLETED", // PENDING, IN_PROGRESS, COMPLETED
    val sensorCondition: String = "NORMAL",
    val verificationStatus: String = "VERIFIED",
    val hash: String? = null
)

@Entity(tableName = "tamper_events")
data class TamperEventEntity(
    @PrimaryKey val eventId: String,
    val batchId: String,
    val deviceId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val location: String = "Location unavailable",
    val severity: String = "HIGH", // LOW, MEDIUM, HIGH, CRITICAL
    val hash: String,
    val syncStatus: String = "SYNCED",
    val acknowledged: Boolean = false
)

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val batchId: String,
    val deviceId: String,
    val alertType: String, // TEMP_EXCURSION, HUMIDITY_EXCURSION, ETHYLENE_HIGH, NH3_HIGH, TAMPER, LOW_BATTERY
    val severity: String = "CRITICAL", // INFO, WARNING, CRITICAL
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isResolved: Boolean = false,
    val resolvedAt: Long? = null
)

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey val eventId: String,
    val batchId: String,
    val deviceId: String,
    val payloadJson: String,
    val syncStatus: String = "PENDING", // PENDING, SYNCING, SYNCED, FAILED
    val retryCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val errorMessage: String? = null
)

@Entity(tableName = "hash_records")
data class HashRecordEntity(
    @PrimaryKey val recordId: String,
    val batchId: String,
    val deviceId: String,
    val currentHash: String,
    val previousHash: String,
    val canonicalPayload: String,
    val eventType: String, // TELEMETRY, TAMPER, STAGE_TRANSITION, BATCH_CREATED
    val timestamp: Long = System.currentTimeMillis(),
    val verificationStatus: String = "VERIFIED" // VERIFIED, MISMATCH
)

@Entity(tableName = "blockchain_records")
data class BlockchainRecordEntity(
    @PrimaryKey val transactionId: String,
    val batchId: String,
    val recordId: String,
    val hash: String,
    val ledgerType: String = "DEMO_LEDGER", // DEMO_LEDGER or PUBLIC_LEDGER
    val blockNumber: Long = 10428,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "COMMITTED"
)

@Entity(tableName = "energy_events")
data class EnergyEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deviceId: String,
    val batteryPercentage: Int,
    val batteryStatus: String = "GOOD", // GOOD, LOW, CHARGING
    val vibrationDetected: Boolean = true,
    val harvestingStatus: String = "HARVESTING_ACTIVE",
    val chargingStatus: String = "SUPPLEMENTARY_CHARGING",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val tempMin: Double = 2.0,
    val tempMax: Double = 10.0,
    val humidityMin: Double = 60.0,
    val humidityMax: Double = 85.0,
    val ethyleneThreshold: Double = 0.50,
    val nh3Threshold: Double = 2.00,
    val batteryWarning: Int = 20,
    val samplingIntervalSec: Int = 5,
    val transmissionIntervalSec: Int = 15,
    val notificationsEnabled: Boolean = true,
    val language: String = "en", // en, ta (Tamil), hi (Hindi)
    val demoMode: Boolean = true, // default demo mode enables effortless hackathon testing
    val backendUrl: String = "https://ais-dev-czoimda43vp3qoqief52cg-154597152771.asia-southeast1.run.app"
)
