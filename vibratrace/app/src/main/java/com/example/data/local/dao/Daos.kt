package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BatchDao {
    @Query("SELECT * FROM batches ORDER BY updatedAt DESC")
    fun getAllBatches(): Flow<List<BatchEntity>>

    @Query("SELECT * FROM batches WHERE id = :id LIMIT 1")
    fun getBatchByIdFlow(id: String): Flow<BatchEntity?>

    @Query("SELECT * FROM batches WHERE id = :id LIMIT 1")
    suspend fun getBatchById(id: String): BatchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: BatchEntity)

    @Update
    suspend fun updateBatch(batch: BatchEntity)

    @Query("DELETE FROM batches WHERE id = :id")
    suspend fun deleteBatchById(id: String)
}

@Dao
interface SensorReadingDao {
    @Query("SELECT * FROM sensor_readings WHERE batchId = :batchId ORDER BY timestamp DESC")
    fun getReadingsForBatch(batchId: String): Flow<List<SensorReadingEntity>>

    @Query("SELECT * FROM sensor_readings WHERE batchId = :batchId ORDER BY timestamp DESC LIMIT 1")
    fun getLatestReadingForBatch(batchId: String): Flow<SensorReadingEntity?>

    @Query("SELECT * FROM sensor_readings ORDER BY timestamp DESC LIMIT 100")
    fun getRecentReadings(): Flow<List<SensorReadingEntity>>

    @Query("SELECT * FROM sensor_readings WHERE batchId = :batchId ORDER BY timestamp ASC")
    suspend fun getAllReadingsForBatchAsc(batchId: String): List<SensorReadingEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertReading(reading: SensorReadingEntity): Long

    @Query("SELECT COUNT(*) FROM sensor_readings WHERE eventId = :eventId")
    suspend fun countByEventId(eventId: String): Int
}

@Dao
interface SupplyChainEventDao {
    @Query("SELECT * FROM supply_chain_events WHERE batchId = :batchId ORDER BY timestamp ASC")
    fun getEventsForBatch(batchId: String): Flow<List<SupplyChainEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: SupplyChainEventEntity)

    @Query("SELECT * FROM supply_chain_events WHERE batchId = :batchId ORDER BY timestamp DESC")
    suspend fun getEventsForBatchList(batchId: String): List<SupplyChainEventEntity>
}

@Dao
interface TamperEventDao {
    @Query("SELECT * FROM tamper_events ORDER BY timestamp DESC")
    fun getAllTamperEvents(): Flow<List<TamperEventEntity>>

    @Query("SELECT * FROM tamper_events WHERE batchId = :batchId ORDER BY timestamp DESC")
    fun getTamperEventsForBatch(batchId: String): Flow<List<TamperEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTamperEvent(event: TamperEventEntity)

    @Query("UPDATE tamper_events SET acknowledged = 1 WHERE eventId = :eventId")
    suspend fun acknowledgeTamper(eventId: String)
}

@Dao
interface AlertDao {
    @Query("SELECT * FROM alerts WHERE isResolved = 0 ORDER BY timestamp DESC")
    fun getActiveAlerts(): Flow<List<AlertEntity>>

    @Query("SELECT * FROM alerts ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<AlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertEntity)

    @Query("UPDATE alerts SET isResolved = 1, resolvedAt = :resolvedAt WHERE id = :alertId")
    suspend fun resolveAlert(alertId: Long, resolvedAt: Long = System.currentTimeMillis())
}

@Dao
interface SyncQueueDao {
    @Query("SELECT * FROM sync_queue WHERE syncStatus = 'PENDING' ORDER BY timestamp ASC")
    fun getPendingSyncRecords(): Flow<List<SyncQueueEntity>>

    @Query("SELECT COUNT(*) FROM sync_queue WHERE syncStatus = 'PENDING'")
    fun getPendingSyncCount(): Flow<Int>

    @Query("SELECT * FROM sync_queue ORDER BY timestamp DESC")
    fun getAllSyncRecords(): Flow<List<SyncQueueEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueueRecord(record: SyncQueueEntity)

    @Query("UPDATE sync_queue SET syncStatus = :status, retryCount = retryCount + 1, errorMessage = :error WHERE eventId = :eventId")
    suspend fun updateSyncStatus(eventId: String, status: String, error: String? = null)

    @Query("UPDATE sync_queue SET syncStatus = 'SYNCED' WHERE syncStatus = 'PENDING'")
    suspend fun markAllSynced()

    @Query("DELETE FROM sync_queue WHERE syncStatus = 'SYNCED'")
    suspend fun clearSynced()
}

@Dao
interface HashRecordDao {
    @Query("SELECT * FROM hash_records ORDER BY timestamp DESC")
    fun getAllHashRecords(): Flow<List<HashRecordEntity>>

    @Query("SELECT * FROM hash_records WHERE batchId = :batchId ORDER BY timestamp ASC")
    suspend fun getHashChainForBatch(batchId: String): List<HashRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHashRecord(record: HashRecordEntity)

    @Query("UPDATE hash_records SET canonicalPayload = :corruptedPayload, verificationStatus = 'MISMATCH' WHERE recordId = :recordId")
    suspend fun simulateTampering(recordId: String, corruptedPayload: String)

    @Query("UPDATE hash_records SET verificationStatus = :status WHERE recordId = :recordId")
    suspend fun updateVerificationStatus(recordId: String, status: String)
}

@Dao
interface BlockchainRecordDao {
    @Query("SELECT * FROM blockchain_records ORDER BY timestamp DESC")
    fun getAllBlockchainRecords(): Flow<List<BlockchainRecordEntity>>

    @Query("SELECT COUNT(*) FROM blockchain_records")
    fun getCommittedCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: BlockchainRecordEntity)
}

@Dao
interface DeviceDao {
    @Query("SELECT * FROM devices")
    fun getAllDevices(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE id = :id LIMIT 1")
    fun getDeviceById(id: String): Flow<DeviceEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDevice(device: DeviceEntity)

    @Query("UPDATE devices SET networkStatus = :status WHERE id = :id")
    suspend fun updateNetworkStatus(id: String, status: String)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    fun getActiveUser(): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("UPDATE users SET isLoggedIn = 0")
    suspend fun logoutAll()
}

@Dao
interface EnergyEventDao {
    @Query("SELECT * FROM energy_events ORDER BY timestamp DESC LIMIT 20")
    fun getRecentEnergyEvents(): Flow<List<EnergyEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnergyEvent(event: EnergyEventEntity)
}

@Dao
interface AppSettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<AppSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: AppSettingsEntity)
}
