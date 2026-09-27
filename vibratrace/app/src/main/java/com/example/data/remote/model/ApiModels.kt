package com.example.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiAuthRequest(
    val email: String,
    val password: String,
    @Json(name = "full_name") val fullName: String? = null,
    val phone: String? = null,
    val role: String? = null
)

@JsonClass(generateAdapter = true)
data class ApiAuthResponse(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "token_type") val tokenType: String,
    val role: String,
    @Json(name = "user_id") val userId: Long,
    @Json(name = "full_name") val fullName: String
)

@JsonClass(generateAdapter = true)
data class ApiSensorPayload(
    val deviceId: String,
    val batchId: String,
    val eventId: String,
    val timestamp: String? = null,
    val temperature: Double,
    val humidity: Double,
    val ethylene: Double? = null,
    val ammonia: Double,
    val battery: Int,
    val tamper: Boolean = false,
    val signal: Int = -72,
    val hash: String? = null,
    val previousHash: String? = null
)

@JsonClass(generateAdapter = true)
data class ApiSyncRequest(
    val records: List<ApiSensorPayload>
)

@JsonClass(generateAdapter = true)
data class ApiSyncResponse(
    val received: Int,
    val synchronized: Int,
    val failed: Int,
    val duplicates: Int
)

@JsonClass(generateAdapter = true)
data class ApiBatchDto(
    val id: String,
    @Json(name = "product_name") val productName: String,
    @Json(name = "product_category") val productCategory: String? = "Fresh Produce",
    @Json(name = "farmer_supplier") val farmerSupplier: String,
    val quantity: Double,
    val unit: String = "kg",
    @Json(name = "harvest_date") val harvestDate: String,
    @Json(name = "packing_date") val packingDate: String,
    val destination: String,
    val transporter: String,
    @Json(name = "assigned_device_id") val assignedDeviceId: String? = null,
    @Json(name = "current_stage") val currentStage: String? = "FARM",
    @Json(name = "qr_token") val qrToken: String? = null,
    @Json(name = "condition_status") val conditionStatus: String? = "GOOD",
    @Json(name = "integrity_status") val integrityStatus: String? = "VERIFIED"
)
