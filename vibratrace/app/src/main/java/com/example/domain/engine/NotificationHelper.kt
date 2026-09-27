package com.example.domain.engine

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_CRITICAL_ALERTS = "vibratrace_critical_alerts"
        const val CHANNEL_OPERATIONAL = "vibratrace_operational"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val criticalChannel = NotificationChannel(
                CHANNEL_CRITICAL_ALERTS,
                "Critical Food Integrity Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent alerts for temperature excursions, gas spoilage, and tamper events"
                enableVibration(true)
            }

            val operationalChannel = NotificationChannel(
                CHANNEL_OPERATIONAL,
                "VibraTrace Operational Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Synchronization, network status, and verification notices"
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(criticalChannel)
            manager.createNotificationChannel(operationalChannel)
        }
    }

    fun showNotification(
        id: Int,
        title: String,
        message: String,
        isCritical: Boolean = true
    ) {
        val channelId = if (isCritical) CHANNEL_CRITICAL_ALERTS else CHANNEL_OPERATIONAL
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(if (isCritical) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(id, builder.build())
        } catch (_: SecurityException) {
            // Permission might be denied by user; handled gracefully
        }
    }
}
