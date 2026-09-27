package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.*
import com.example.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        BatchEntity::class,
        DeviceEntity::class,
        SensorReadingEntity::class,
        SupplyChainEventEntity::class,
        TamperEventEntity::class,
        AlertEntity::class,
        SyncQueueEntity::class,
        HashRecordEntity::class,
        BlockchainRecordEntity::class,
        EnergyEventEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class VibraTraceDatabase : RoomDatabase() {
    abstract fun batchDao(): BatchDao
    abstract fun sensorReadingDao(): SensorReadingDao
    abstract fun supplyChainEventDao(): SupplyChainEventDao
    abstract fun tamperEventDao(): TamperEventDao
    abstract fun alertDao(): AlertDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun hashRecordDao(): HashRecordDao
    abstract fun blockchainRecordDao(): BlockchainRecordDao
    abstract fun deviceDao(): DeviceDao
    abstract fun userDao(): UserDao
    abstract fun energyEventDao(): EnergyEventDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: VibraTraceDatabase? = null

        fun getDatabase(context: Context): VibraTraceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VibraTraceDatabase::class.java,
                    "vibratrace_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
