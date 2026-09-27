#pragma once

// VibraTrace ESP32 Firmware Configuration
#define DEVICE_ID "VT-ESP32-001"
#define FIRMWARE_VERSION "v1.4.2"

// Sensor Pin Definitions (Configurable)
#define PIN_I2C_SDA 21
#define PIN_I2C_SCL 22
#define PIN_ETHYLENE_ADC 34
#define PIN_NH3_ADC 35
#define PIN_REED_SWITCH 4
#define PIN_PIEZO_HARVEST_ADC 36
#define PIN_BATTERY_ADC 39
#define PIN_SD_CS 5

// GSM / SIM800C UART Pins
#define PIN_SIM800_TX 17
#define PIN_SIM800_RX 16
#define PIN_SIM800_PWR 18

// MQTT Topics
#define TOPIC_TELEMETRY "vibratrace/device/" DEVICE_ID "/telemetry"
#define TOPIC_EVENT "vibratrace/device/" DEVICE_ID "/event"
#define TOPIC_STATUS "vibratrace/device/" DEVICE_ID "/status"
#define TOPIC_SYNC "vibratrace/device/" DEVICE_ID "/sync"

// Sampling Intervals (milliseconds)
#define SAMPLING_INTERVAL_MS 5000
#define TRANSMISSION_INTERVAL_MS 15000
#define MAX_OFFLINE_BUFFER_SIZE 2048
