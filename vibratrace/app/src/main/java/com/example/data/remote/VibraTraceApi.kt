package com.example.data.remote

import com.example.data.remote.model.*
import retrofit2.Response
import retrofit2.http.*

interface VibraTraceApi {
    @POST("auth/register")
    suspend fun register(@Body request: ApiAuthRequest): Response<ApiAuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: ApiAuthRequest): Response<ApiAuthResponse>

    @GET("batches")
    suspend fun getBatches(): Response<List<ApiBatchDto>>

    @POST("batches")
    suspend fun createBatch(@Body batch: ApiBatchDto): Response<ApiBatchDto>

    @GET("batches/{id}")
    suspend fun getBatch(@Path("id") id: String): Response<ApiBatchDto>

    @POST("sensor-readings")
    suspend fun submitSensorReading(@Body reading: ApiSensorPayload): Response<Map<String, Any>>

    @POST("sync")
    suspend fun syncOfflineRecords(@Body request: ApiSyncRequest): Response<ApiSyncResponse>

    @GET("trace/{batchId}")
    suspend fun getTraceability(@Path("batchId") batchId: String): Response<Map<String, Any>>
}
