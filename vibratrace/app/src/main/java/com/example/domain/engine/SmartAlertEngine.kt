package com.example.domain.engine

import com.example.data.local.dao.AlertDao
import com.example.data.local.entity.AlertEntity
import com.example.data.local.entity.AppSettingsEntity
import com.example.data.local.entity.SensorReadingEntity
import java.text.SimpleDateFormat
import java.util.*

class SmartAlertEngine(
    private val alertDao: AlertDao,
    private val notificationHelper: NotificationHelper
) {
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    suspend fun evaluateReading(
        reading: SensorReadingEntity,
        settings: AppSettingsEntity
    ): List<AlertEntity> {
        val triggered = mutableListOf<AlertEntity>()
        val timeStr = timeFormat.format(Date(reading.timestamp))

        // 1. Temperature Excursion
        if (reading.temperature < settings.tempMin || reading.temperature > settings.tempMax) {
            val alert = AlertEntity(
                batchId = reading.batchId,
                deviceId = reading.deviceId,
                alertType = "TEMP_EXCURSION",
                severity = "CRITICAL",
                message = "CRITICAL — TEMPERATURE EXCURSION: ${String.format("%.1f", reading.temperature)}°C outside safe range [${settings.tempMin}°C - ${settings.tempMax}°C] at $timeStr"
            )
            alertDao.insertAlert(alert)
            triggered.add(alert)
            notificationHelper.showNotification(
                id = 101,
                title = "CRITICAL — TEMPERATURE ALERT",
                message = "Batch ${reading.batchId}: ${reading.temperature}°C exceeded threshold."
            )
        }

        // 2. Ethylene Level High
        if (reading.ethylene != null && reading.ethylene > settings.ethyleneThreshold) {
            val alert = AlertEntity(
                batchId = reading.batchId,
                deviceId = reading.deviceId,
                alertType = "ETHYLENE_HIGH",
                severity = "CRITICAL",
                message = "CRITICAL — ETHYLENE LEVEL HIGH: Spoilage accelerator detected at ${String.format("%.2f", reading.ethylene)} ppm (Threshold: ${settings.ethyleneThreshold} ppm) at $timeStr"
            )
            alertDao.insertAlert(alert)
            triggered.add(alert)
            notificationHelper.showNotification(
                id = 102,
                title = "CRITICAL — ETHYLENE LEVEL HIGH",
                message = "Ethylene level increased above configured threshold (${reading.ethylene} ppm)."
            )
        }

        // 3. NH3 Ammonia High
        if (reading.ammonia > settings.nh3Threshold) {
            val alert = AlertEntity(
                batchId = reading.batchId,
                deviceId = reading.deviceId,
                alertType = "NH3_HIGH",
                severity = "CRITICAL",
                message = "CRITICAL — AMMONIA (NH3) HIGH: Organic decomposition gas detected at ${String.format("%.2f", reading.ammonia)} ppm (Threshold: ${settings.nh3Threshold} ppm) at $timeStr"
            )
            alertDao.insertAlert(alert)
            triggered.add(alert)
            notificationHelper.showNotification(
                id = 103,
                title = "CRITICAL — AMMONIA HIGH",
                message = "Batch ${reading.batchId}: Ammonia gas ${reading.ammonia} ppm detected."
            )
        }

        // 4. Tamper / Reed Switch
        if (reading.tamper) {
            val alert = AlertEntity(
                batchId = reading.batchId,
                deviceId = reading.deviceId,
                alertType = "CONTAINER_OPENED",
                severity = "CRITICAL",
                message = "TAMPER DETECTED: Container seal reed switch triggered at $timeStr. Possible unauthorized enclosure access."
            )
            alertDao.insertAlert(alert)
            triggered.add(alert)
            notificationHelper.showNotification(
                id = 104,
                title = "TAMPER DETECTED",
                message = "Container opened for Batch ${reading.batchId} at $timeStr"
            )
        }

        // 5. Low Battery
        if (reading.battery < settings.batteryWarning) {
            val alert = AlertEntity(
                batchId = reading.batchId,
                deviceId = reading.deviceId,
                alertType = "LOW_BATTERY",
                severity = "WARNING",
                message = "WARNING — LOW BATTERY: Node battery dropped to ${reading.battery}%"
            )
            alertDao.insertAlert(alert)
            triggered.add(alert)
            notificationHelper.showNotification(
                id = 105,
                title = "WARNING — LOW BATTERY",
                message = "Device ${reading.deviceId} battery level is ${reading.battery}%",
                isCritical = false
            )
        }

        return triggered
    }
}
