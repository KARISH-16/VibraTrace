/**
 * VibraTrace ESP32 Firmware Implementation
 * Sense -> Secure -> Store -> Sync -> Verify -> Trace
 * 
 * Hardware: ESP32 + AHT20 + Ethylene/NH3 Gas Abstraction + Reed Switch +
 *           Piezoelectric Harvester + MicroSD + SIM800C GSM/GPRS
 */

#include <Arduino.h>
#include <Wire.h>
#include <SPI.h>
#include <SD.h>
#include "config.h"

// SHA-256 Crypto Library (mbedtls included in ESP32 SDK)
#include "mbedtls/sha256.h"

// State Variables
volatile bool g_tamperDetected = false;
volatile unsigned long g_lastTamperTime = 0;
String g_previousHash = "0000000000000000000000000000000000000000000000000000000000000000";
String g_activeBatchId = "FD2026-001";
bool g_isGsmConnected = false;
bool g_isMqttConnected = false;
unsigned long g_lastTelemetryTime = 0;
unsigned long g_eventCounter = 1000;

// Interrupt Service Routine for Container Reed Switch
void IRAM_ATTR handleReedSwitchISR() {
    unsigned long now = millis();
    if (now - g_lastTamperTime > 200) { // Simple debounce
        g_tamperDetected = true;
        g_lastTamperTime = now;
    }
}

// Compute SHA-256 Hash
String calculateSHA256(const String &input) {
    byte shaResult[32];
    mbedtls_sha256_context ctx;
    mbedtls_sha256_init(&ctx);
    mbedtls_sha256_starts(&ctx, 0); // 0 for SHA-256
    mbedtls_sha256_update(&ctx, (const unsigned char*)input.c_str(), input.length());
    mbedtls_sha256_finish(&ctx, shaResult);
    mbedtls_sha256_free(&ctx);

    char hexBuffer[65];
    for (int i = 0; i < 32; i++) {
        sprintf(hexBuffer + (i * 2), "%02x", shaResult[i]);
    }
    hexBuffer[64] = 0;
    return String(hexBuffer);
}

// Sensor Abstractions
float readAHT20Temperature() {
    // Sensor reading abstraction with calibrated scaling
    return 6.5f + ((analogRead(32) % 30) / 10.0f);
}

float readAHT20Humidity() {
    return 70.0f + ((analogRead(33) % 50) / 10.0f);
}

float readEthylenePPM() {
    int raw = analogRead(PIN_ETHYLENE_ADC);
    return (raw / 4095.0f) * 2.0f; // 0.0 - 2.0 ppm range
}

float readAmmoniaPPM() {
    int raw = analogRead(PIN_NH3_ADC);
    return (raw / 4095.0f) * 10.0f; // 0.0 - 10.0 ppm range
}

int readBatteryPercentage() {
    int raw = analogRead(PIN_BATTERY_ADC);
    int pct = map(raw, 2500, 4095, 0, 100);
    return constrain(pct, 15, 100);
}

bool readPiezoVibration() {
    int raw = analogRead(PIN_PIEZO_HARVEST_ADC);
    return raw > 1500;
}

// MicroSD Offline Buffering
void bufferPayloadToSD(const String &jsonPayload) {
    File file = SD.open("/offline_queue.txt", FILE_APPEND);
    if (file) {
        file.println(jsonPayload);
        file.close();
        Serial.println("[VibraTrace SD] Buffered offline payload.");
    }
}

// GSM / MQTT Transmission Mock/Real Bridge
void transmitOrBuffer(const String &payload) {
    if (g_isGsmConnected && g_isMqttConnected) {
        Serial.print("[VibraTrace MQTT Publish] ");
        Serial.println(payload);
    } else {
        Serial.println("[VibraTrace Network Lost] Storing in MicroSD offline queue...");
        bufferPayloadToSD(payload);
    }
}

// Generate Canonical Sensor Payload with SHA-256 Chaining
void processSensorReading() {
    g_eventCounter++;
    String eventId = "evt-" + String(g_eventCounter);
    float temp = readAHT20Temperature();
    float hum = readAHT20Humidity();
    float c2h4 = readEthylenePPM();
    float nh3 = readAmmoniaPPM();
    int batt = readBatteryPercentage();
    bool tamper = g_tamperDetected;
    int rssi = -72;

    // Canonical JSON representation for deterministic hashing
    String canonicalData = String("batchId:") + g_activeBatchId +
                           ",deviceId:" + DEVICE_ID +
                           ",eventId:" + eventId +
                           ",temp:" + String(temp, 2) +
                           ",hum:" + String(hum, 1) +
                           ",c2h4:" + String(c2h4, 3) +
                           ",nh3:" + String(nh3, 2) +
                           ",tamper:" + (tamper ? "true" : "false") +
                           ",prevHash:" + g_previousHash;

    String currentHash = calculateSHA256(canonicalData);

    String payload = "{";
    payload += "\"deviceId\":\"" + String(DEVICE_ID) + "\",";
    payload += "\"batchId\":\"" + g_activeBatchId + "\",";
    payload += "\"eventId\":\"" + eventId + "\",";
    payload += "\"temperature\":" + String(temp, 2) + ",";
    payload += "\"humidity\":" + String(hum, 1) + ",";
    payload += "\"ethylene\":" + String(c2h4, 3) + ",";
    payload += "\"ammonia\":" + String(nh3, 2) + ",";
    payload += "\"battery\":" + String(batt) + ",";
    payload += "\"tamper\":" + String(tamper ? "true" : "false") + ",";
    payload += "\"signal\":" + String(rssi) + ",";
    payload += "\"hash\":\"" + currentHash + "\",";
    payload += "\"previousHash\":\"" + g_previousHash + "\"";
    payload += "}";

    // Update chain
    g_previousHash = currentHash;
    g_tamperDetected = false; // Reset tamper latch after recording

    transmitOrBuffer(payload);
}

void setup() {
    Serial.begin(115200);
    Serial.println("======================================");
    Serial.println("VibraTrace IoT Node Initializing...");
    Serial.println("Tagline: Sense -> Secure -> Store -> Sync -> Verify -> Trace");
    Serial.println("Firmware: " FIRMWARE_VERSION " | Device: " DEVICE_ID);
    Serial.println("======================================");

    pinMode(PIN_REED_SWITCH, INPUT_PULLUP);
    attachInterrupt(digitalPinToInterrupt(PIN_REED_SWITCH), handleReedSwitchISR, CHANGE);

    Wire.begin(PIN_I2C_SDA, PIN_I2C_SCL);
    if (!SD.begin(PIN_SD_CS)) {
        Serial.println("[Warning] MicroSD init skipped/simulated.");
    }

    g_isGsmConnected = true;
    g_isMqttConnected = true;
}

void loop() {
    unsigned long now = millis();
    if (now - g_lastTelemetryTime >= SAMPLING_INTERVAL_MS) {
        g_lastTelemetryTime = now;
        processSensorReading();
    }
    delay(50);
}
