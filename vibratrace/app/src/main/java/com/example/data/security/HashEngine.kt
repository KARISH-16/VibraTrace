package com.example.data.security

import java.security.MessageDigest

data class CanonicalPayload(
    val recordId: String,
    val batchId: String,
    val deviceId: String,
    val timestamp: Long,
    val eventType: String,
    val temperature: Double,
    val humidity: Double,
    val ethylene: Double?,
    val ammonia: Double,
    val tamper: Boolean,
    val previousHash: String
) {
    fun serialize(): String {
        val ethStr = if (ethylene != null) String.format("%.3f", ethylene) else "0.000"
        return "recordId:$recordId," +
                "batchId:$batchId," +
                "deviceId:$deviceId," +
                "timestamp:$timestamp," +
                "eventType:$eventType," +
                "temp:${String.format("%.2f", temperature)}," +
                "hum:${String.format("%.1f", humidity)}," +
                "c2h4:$ethStr," +
                "nh3:${String.format("%.2f", ammonia)}," +
                "tamper:$tamper," +
                "prevHash:$previousHash"
    }
}

object HashEngine {
    fun calculateSha256(canonicalString: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(canonicalString.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyRecordIntegrity(
        canonicalString: String,
        storedHash: String
    ): Boolean {
        val calculated = calculateSha256(canonicalString)
        return calculated.equals(storedHash, ignoreCase = true)
    }

    const val GENESIS_HASH = "0000000000000000000000000000000000000000000000000000000000000000"
}
