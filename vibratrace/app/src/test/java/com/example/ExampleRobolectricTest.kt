package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.security.CanonicalPayload
import com.example.data.security.HashEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("VibraTrace", appName)
    }

    @Test
    fun `verify canonical SHA256 cryptographic chain`() {
        val canonical = CanonicalPayload(
            recordId = "evt-1001",
            batchId = "FD2026-001",
            deviceId = "VT-ESP32-001",
            timestamp = 1790433837000L,
            eventType = "TELEMETRY",
            temperature = 6.80,
            humidity = 72.0,
            ethylene = 0.42,
            ammonia = 1.80,
            tamper = false,
            previousHash = HashEngine.GENESIS_HASH
        )
        val serialized = canonical.serialize()
        val hash = HashEngine.calculateSha256(serialized)
        assertEquals(64, hash.length)

        val isValid = HashEngine.verifyRecordIntegrity(serialized, hash)
        assertEquals(true, isValid)
    }
}
