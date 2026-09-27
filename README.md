# 🌾 VibraTrace

### Low-Cost IoT Blockchain Node for Secure Farm-to-Fork Traceability

> **Sense → Secure → Store → Sync → Verify → Trace**

VibraTrace is an **IoT-powered Farm-to-Fork traceability platform** designed to monitor perishable agricultural products throughout their supply chain.

The system combines **ESP32-based IoT sensing, offline-first data buffering, MQTT communication, SHA-256 hash chaining, PostgreSQL persistence, blockchain-style event anchoring, and an Android application** to provide transparent and tamper-evident food traceability.

---

## 📌 Overview

Perishable agricultural products such as fruits, vegetables, dairy products, and meat can experience significant losses because of:

* Temperature fluctuations
* Excess humidity
* Spoilage gases
* Transportation delays
* Cold-chain failures
* Container tampering
* Network connectivity problems
* Lack of trustworthy traceability records

VibraTrace addresses these challenges by continuously collecting environmental and security data from an IoT device attached to a shipment.

The collected information can be buffered locally when connectivity is unavailable, synchronized when the network returns, cryptographically chained using **SHA-256**, stored by the backend, and presented through an Android application.

---

# 🎯 Key Objectives

* Monitor food transportation conditions in real time.
* Detect temperature and humidity variations.
* Monitor ethylene and ammonia gas levels.
* Detect unauthorized container opening.
* Continue recording data during network outages.
* Synchronize buffered data after connectivity is restored.
* Protect telemetry integrity using SHA-256 hash chaining.
* Maintain shipment and device information centrally.
* Provide Farm-to-Fork traceability.
* Support QR-based batch verification.
* Provide real-time telemetry through WebSockets.
* Support a hardware-independent demo/simulation workflow.

---

# ✨ Features

## 🌡️ Environmental Monitoring

The ESP32 node collects:

* Temperature
* Humidity
* Ethylene concentration
* Ammonia (NH₃) concentration
* Battery level
* Network signal
* Vibration status

These measurements help identify potentially unsafe transportation conditions.

---

## 🔐 Tamper Detection

A magnetic reed switch monitors the shipment container.

If the container is opened unexpectedly:

```text
Container Opened
       ↓
Reed Switch Triggered
       ↓
Tamper Event Generated
       ↓
SHA-256 Hash Created
       ↓
Backend Alert
       ↓
Traceability Record
```

---

## 📡 Offline-First IoT Communication

The system is designed to continue working even when cellular/network connectivity is temporarily unavailable.

When the network is unavailable:

```text
ESP32
  ↓
Sensor Reading
  ↓
MicroSD Offline Queue
```

When connectivity returns:

```text
MicroSD Queue
      ↓
Synchronization
      ↓
MQTT / Backend
      ↓
Database
```

This prevents sensor readings from being lost during transportation.

---

## 🔗 SHA-256 Hash Chaining

Each telemetry record contains:

* Current hash
* Previous hash

Conceptually:

```text
Block 1
Hash = H1
   ↓
Block 2
Previous Hash = H1
Hash = H2
   ↓
Block 3
Previous Hash = H2
Hash = H3
```

This creates a tamper-evident chain of telemetry records.

The ESP32 uses SHA-256 hashing to generate the cryptographic digest.

---

## ⛓️ Hybrid Blockchain / Ledger Architecture

VibraTrace separates high-frequency telemetry from important state transitions.

### Regular telemetry

Stored in the backend database:

```text
Temperature
Humidity
Gas levels
Battery
Signal
Telemetry timestamps
```

### Critical events

Can be anchored through the ledger service:

```text
Tamper events
Shipment stage changes
Batch completion
Critical state transitions
```

This reduces unnecessary blockchain storage while preserving important verification events.

---

## 📱 Android Application

The Android application provides the user interface for interacting with the VibraTrace platform.

The app uses:

* Jetpack Compose
* Material 3
* Retrofit
* Room
* CameraX
* WorkManager-compatible architecture
* Kotlin Coroutines
* WebSockets
* Firebase AI integration

The application supports real-time telemetry, shipment tracking, QR-based traceability, analytics, alerts, and offline caching.

---

## 📲 QR-Based Farm-to-Fork Traceability

Each shipment batch can have a QR token.

A consumer or authorized user can scan the QR code to access the shipment traceability information.

Example flow:

```text
Farm
 ↓
Packing
 ↓
Transport
 ↓
Cold Storage
 ↓
Distribution
 ↓
Buyer
```

The traceability view can provide information about the shipment and its recorded environmental history.

---

# 🏗️ System Architecture

```text
 ┌───────────────────────────────────────────────┐
 │             IoT SENSOR LAYER                 │
 │                                               │
 │  AHT20 │ Ethylene │ NH3 │ Reed │ Piezo       │
 └──────────────────────┬────────────────────────┘
                        │
                        ▼
              ┌───────────────────┐
              │      ESP32        │
              │   IoT Node        │
              └─────────┬─────────┘
                        │
             ┌──────────┴──────────┐
             │                     │
             ▼                     ▼
      ┌─────────────┐       ┌──────────────┐
      │   MicroSD   │       │ SIM800C GSM  │
      │ Offline     │       │ / MQTT       │
      │ Buffer      │       │              │
      └─────────────┘       └──────┬───────┘
                                    │
                                    ▼
                         ┌────────────────────┐
                         │ Mosquitto MQTT     │
                         │ Broker             │
                         └─────────┬──────────┘
                                   │
                                   ▼
                         ┌────────────────────┐
                         │ FastAPI Backend    │
                         │                    │
                         │ SHA-256 Engine     │
                         │ Database Service   │
                         │ Ledger Service     │
                         │ WebSocket Server   │
                         └─────────┬──────────┘
                                   │
                     ┌─────────────┴─────────────┐
                     │                           │
                     ▼                           ▼
             ┌──────────────┐            ┌──────────────┐
             │ PostgreSQL   │            │ Android App  │
             │ Database     │            │              │
             └──────────────┘            └──────────────┘
```

---

# 🛠️ Technologies Used

## Android

| Technology         | Purpose                         |
| ------------------ | ------------------------------- |
| Kotlin             | Android application development |
| Jetpack Compose    | Modern UI development           |
| Material 3         | UI components and design        |
| Room               | Local database / offline cache  |
| Retrofit           | REST API communication          |
| OkHttp             | HTTP networking                 |
| Moshi              | JSON serialization              |
| CameraX            | QR/barcode camera functionality |
| Kotlin Coroutines  | Asynchronous operations         |
| Navigation Compose | Application navigation          |
| DataStore          | Local preferences               |
| Firebase AI        | AI integration                  |

---

## Backend

| Technology | Purpose                               |
| ---------- | ------------------------------------- |
| Python     | Backend development                   |
| FastAPI    | REST API framework                    |
| Uvicorn    | ASGI server                           |
| SQLAlchemy | Database ORM                          |
| PostgreSQL | Persistent database                   |
| SQLite     | Lightweight testing/fallback database |
| MQTT       | IoT messaging                         |
| WebSockets | Real-time telemetry                   |
| SHA-256    | Data integrity verification           |
| JWT        | Authentication                        |

---

## IoT / Embedded

| Technology           | Purpose                     |
| -------------------- | --------------------------- |
| ESP32                | IoT controller              |
| C++                  | Firmware development        |
| AHT20                | Temperature & humidity      |
| Ethylene sensor      | Spoilage-gas monitoring     |
| NH₃ sensor           | Gas monitoring              |
| Reed switch          | Tamper detection            |
| Piezoelectric sensor | Vibration/energy harvesting |
| MicroSD              | Offline data buffering      |
| SIM800C              | GSM/GPRS communication      |
| MQTT                 | IoT telemetry transmission  |

---

## Infrastructure

| Technology     | Purpose                     |
| -------------- | --------------------------- |
| Docker         | Containerization            |
| Docker Compose | Multi-service orchestration |
| Mosquitto      | MQTT broker                 |
| PostgreSQL     | Backend database            |

---

# 📁 Project Structure

```text
VibraTrace/
│
├── app/
│   ├── src/
│   │   ├── androidTest/
│   │   ├── main/
│   │   └── test/
│   ├── build.gradle.kts
│   └── proguard-rules.pro
│
├── backend/
│   ├── app/
│   │   ├── main.py
│   │   ├── database.py
│   │   ├── models.py
│   │   ├── schemas.py
│   │   ├── security.py
│   │   ├── hash_engine.py
│   │   ├── blockchain_service.py
│   │   └── mqtt_client.py
│   │
│   ├── tests/
│   │   └── test_backend.py
│   │
│   └── requirements.txt
│
├── esp32/
│   ├── include/
│   │   └── config.h
│   ├── src/
│   │   └── main.cpp
│   └── README.md
│
├── docker/
│   ├── docker-compose.yml
│   └── mosquitto.conf
│
├── docs/
│   ├── ARCHITECTURE.md
│   ├── API_DOCUMENTATION.md
│   └── HARDWARE_WIRING.md
│
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/
│
├── .env.example
├── .gitignore
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── metadata.json
└── README.md
```

---

# ⚙️ Prerequisites

Before running VibraTrace, install/configure the following:

### Android

* Android Studio
* JDK 11-compatible Java environment
* Android SDK
* Android SDK Platform 36
* Android device or emulator

### Backend

* Python 3.x
* pip
* PostgreSQL

### IoT

* ESP32 development board
* Arduino IDE or PlatformIO
* Required sensors
* MicroSD module
* SIM800C module for cellular deployment

### Optional

* Docker
* Docker Compose

---

# 🚀 Installation & Setup

## 1. Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/VibraTrace.git
cd VibraTrace
```

Replace `YOUR_USERNAME` with your GitHub username.

---

# 📱 2. Setup Android Application

Open the project folder in **Android Studio**.

The Android project uses:

```text
Kotlin
Jetpack Compose
Material 3
Gradle Kotlin DSL
```

### Build the project

On Windows:

```bash
gradlew.bat build
```

On Linux/macOS:

```bash
./gradlew build
```

### Install debug APK

```bash
gradlew.bat installDebug
```

or:

```bash
./gradlew installDebug
```

### Run tests

```bash
gradlew.bat test
```

or:

```bash
./gradlew test
```

---

# 🔑 3. Environment Configuration

The project includes:

```text
.env.example
```

Create your local environment file:

```text
.env
```

Do **not** commit `.env` to GitHub.

The repository `.gitignore` already excludes `.env`.

If Firebase/Gemini functionality is enabled, configure the required API credentials through your local development environment or Android Studio/AI Studio secrets configuration.

### Important

Never commit:

```text
.env
API keys
passwords
private keys
keystore files
Firebase secret credentials
```

---

# 🐍 4. Setup Backend

Navigate to the backend directory:

```bash
cd backend
```

Create a virtual environment:

### Windows

```bash
python -m venv venv
venv\Scripts\activate
```

### Linux/macOS

```bash
python3 -m venv venv
source venv/bin/activate
```

Install dependencies:

```bash
pip install -r requirements.txt
```

---

# 🗄️ 5. Configure Database

The backend uses PostgreSQL by default.

Example database configuration:

```text
DATABASE_URL=postgresql://vibratrace:vibratrace_secret@localhost:5432/vibratrace_db
```

The backend also supports SQLite for isolated/testing environments.

For example:

```text
DATABASE_URL=sqlite:///./vibratrace.db
```

Make sure the selected database is running before starting the backend.

---

# ▶️ 6. Run FastAPI Backend

From the `backend` directory:

```bash
uvicorn app.main:app --reload
```

The backend will start at:

```text
http://localhost:8000
```

FastAPI Swagger documentation:

```text
http://localhost:8000/docs
```

ReDoc documentation:

```text
http://localhost:8000/redoc
```

---

# 🐳 7. Run Using Docker

VibraTrace includes Docker configuration for the backend infrastructure.

Navigate to:

```bash
cd docker
```

Start the services:

```bash
docker compose up -d
```

Check running containers:

```bash
docker compose ps
```

View logs:

```bash
docker compose logs -f
```

Stop the services:

```bash
docker compose down
```

The Docker environment is intended to provide:

```text
PostgreSQL
Mosquitto MQTT Broker
Backend services
```

---

# 📡 8. MQTT Architecture

The IoT communication layer uses MQTT.

Telemetry topics follow the pattern:

```text
vibratrace/device/{deviceId}/telemetry
```

Offline synchronization uses:

```text
vibratrace/device/{deviceId}/sync
```

Example:

```text
vibratrace/device/VT-ESP32-001/telemetry
```

---

# 🔌 9. ESP32 Setup

The ESP32 firmware is located at:

```text
esp32/src/main.cpp
```

Configuration is located at:

```text
esp32/include/config.h
```

The firmware is designed for an ESP32 development board.

### Hardware components

* ESP32 DevKit
* AHT20
* Ethylene sensor
* NH₃ sensor
* Reed switch
* Piezoelectric vibration sensor
* MicroSD module
* SIM800C GSM/GPRS module

---

# 🔧 ESP32 Pin Configuration

| Component       |    GPIO |
| --------------- | ------: |
| AHT20 SDA       | GPIO 21 |
| AHT20 SCL       | GPIO 22 |
| Ethylene Sensor | GPIO 34 |
| NH₃ Sensor      | GPIO 35 |
| Reed Switch     |  GPIO 4 |
| Piezo Sensor    | GPIO 36 |
| Battery Sense   | GPIO 39 |
| MicroSD CS      |  GPIO 5 |
| SIM800C TX      | GPIO 16 |
| SIM800C RX      | GPIO 17 |

Detailed wiring information is available in:

```text
docs/HARDWARE_WIRING.md
```

---

# 🧪 10. ESP32 Firmware Flow

The firmware follows:

```text
Initialize ESP32
      ↓
Initialize Sensors
      ↓
Read Sensor Values
      ↓
Generate Event ID
      ↓
Create Canonical Payload
      ↓
Calculate SHA-256
      ↓
Attach Previous Hash
      ↓
Check Network
   ↙          ↘
Online       Offline
  ↓              ↓
MQTT          MicroSD
  ↓              ↓
Backend       Offline Queue
                 ↓
          Network Restored
                 ↓
              Sync
```

---

# 🔐 11. SHA-256 Verification

Each sensor event contains:

```json
{
  "deviceId": "VT-ESP32-001",
  "batchId": "FD2026-001",
  "eventId": "evt-1001",
  "temperature": 6.5,
  "humidity": 72.0,
  "ethylene": 0.8,
  "ammonia": 3.2,
  "battery": 94,
  "tamper": false,
  "signal": -72,
  "hash": "...",
  "previousHash": "..."
}
```

The next event references the previous event's hash.

This allows the backend to identify modifications to the chained telemetry history.

---

# 🌐 API Endpoints

The main API endpoints include:

### Authentication

```text
POST /auth/register
POST /auth/login
GET  /auth/me
```

### Batches

```text
GET    /batches
POST   /batches
GET    /batches/{id}
PUT    /batches/{id}
DELETE /batches/{id}
```

### Devices

```text
GET  /devices
POST /devices
```

### Sensor Telemetry

```text
POST /sensor-readings
GET  /batches/{id}/sensor-readings
POST /sync
```

### Verification

```text
POST /verification/verify
```

### Blockchain / Ledger

```text
POST /blockchain/submit
```

### Consumer Traceability

```text
GET /trace/{batchId}
```

### Real-Time Data

```text
WS /ws
```

Complete API documentation:

```text
docs/API_DOCUMENTATION.md
```

---

# 🚨 Smart Alerts

The backend can generate alerts for critical shipment conditions.

For example, the current backend logic identifies temperature excursions outside:

```text
2°C – 10°C
```

as a critical temperature event.

Tamper detection can also generate a high-severity event.

Example:

```text
Temperature Excursion
        ↓
CRITICAL Alert
```

or:

```text
Container Opened
        ↓
Tamper Event
        ↓
HIGH Severity
```

---

# 📊 Real-Time Monitoring

The backend exposes a WebSocket endpoint:

```text
/ws
```

The Android application can receive telemetry updates without repeatedly polling the REST API.

Conceptually:

```text
ESP32
  ↓
MQTT
  ↓
FastAPI
  ↓
WebSocket
  ↓
Android Dashboard
```

---

# 🧑‍🌾 Farm-to-Fork Workflow

The system represents the shipment lifecycle as:

```text
┌─────────┐
│  FARM   │
└────┬────┘
     ↓
┌─────────┐
│ PACKING │
└────┬────┘
     ↓
┌───────────┐
│ TRANSPORT │
└─────┬─────┘
      ↓
┌──────────────┐
│ COLD STORAGE │
└──────┬───────┘
       ↓
┌──────────────┐
│ DISTRIBUTION │
└──────┬───────┘
       ↓
┌─────────┐
│  BUYER  │
└─────────┘
```

At every stage, the system can maintain the associated batch and telemetry history.

---

# 📦 Example Shipment

Example batch:

```text
Batch ID:
FD2026-001

Product:
Fresh Produce

Device:
VT-ESP32-001
```

The associated IoT device continuously produces telemetry.

Example:

```text
Temperature: 6.5°C
Humidity: 72%
Ethylene: 0.8 ppm
NH3: 3.2 ppm
Battery: 94%
Signal: -72 dBm
Tamper: FALSE
```

---

# 🧪 Testing

Backend tests are located at:

```text
backend/tests/test_backend.py
```

Run:

```bash
cd backend
pytest
```

For Android tests:

```bash
gradlew.bat test
```

For instrumentation tests:

```bash
gradlew.bat connectedAndroidTest
```

---

# 📚 Documentation

Additional project documentation:

| Document                    | Description                            |
| --------------------------- | -------------------------------------- |
| `docs/ARCHITECTURE.md`      | System architecture and data pipelines |
| `docs/API_DOCUMENTATION.md` | REST and WebSocket APIs                |
| `docs/HARDWARE_WIRING.md`   | ESP32 hardware wiring                  |
| `esp32/README.md`           | ESP32 firmware setup                   |

---

# 🔒 Security Considerations

VibraTrace incorporates multiple security mechanisms:

* SHA-256 telemetry hashing
* Hash chaining
* JWT authentication
* Password hashing
* API validation
* Idempotent event handling
* Tamper-event recording
* Environment-based secret configuration
* Separation of telemetry and critical ledger events

### Never commit secrets

Do not upload:

```text
.env
*.jks
*.keystore
debug.keystore
API keys
Database passwords
Private credentials
```

---

# 🌱 Future Scope

Potential future enhancements include:

* Production-grade blockchain integration
* Polygon/Ethereum anchoring
* Advanced gas calibration
* AI-based spoilage prediction
* Predictive cold-chain analytics
* Automatic anomaly detection
* GPS shipment tracking
* LoRaWAN support
* NB-IoT support
* Advanced energy harvesting
* Multi-tenant enterprise dashboards
* Consumer mobile traceability
* Cloud deployment
* Automated compliance reports
* Digital product passports

---

# 💡 Innovation

VibraTrace combines several technologies into one traceability workflow:

```text
IoT Sensors
     +
Offline-First Storage
     +
MQTT
     +
SHA-256 Hash Chaining
     +
Database
     +
Ledger Anchoring
     +
Real-Time WebSockets
     +
QR Traceability
     =
Farm-to-Fork Trust
```

The key concept is that the system does not depend entirely on continuous internet connectivity. Sensor data can continue to be collected locally and synchronized later.

---

# 🎯 Use Cases

VibraTrace can be adapted for:

* Fruits and vegetables
* Dairy products
* Meat transportation
* Seafood logistics
* Pharmaceutical cold chains
* Food distribution
* Cold-storage monitoring
* Agricultural supply chains
* Warehouse monitoring
* Temperature-sensitive logistics

---

# 📌 Project Status

**Project:** VibraTrace
**Version:** 1.0
**Platform:** Android + FastAPI + ESP32
**Architecture:** IoT + Backend + Database + Ledger + Mobile Application

This repository contains the project implementation, embedded firmware, backend services, Docker configuration, and technical documentation.

---

# 🤝 Contributing

Contributions are welcome.

### Fork the repository

```bash
git clone https://github.com/YOUR_USERNAME/VibraTrace.git
```

Create a feature branch:

```bash
git checkout -b feature/your-feature
```

Commit your changes:

```bash
git add .
git commit -m "Add your feature"
```

Push:

```bash
git push origin feature/your-feature
```

Then open a Pull Request on GitHub.

---

# 📄 License

This project is currently intended for **educational, research, prototype, and hackathon purposes**.

Add an appropriate open-source license before distributing the project publicly for production use.

---

# 👩‍💻 Developed By

**VibraTrace Team**

### Project Theme

> **Secure • Connected • Transparent • Traceable**

---

## ⭐ VibraTrace

**From Farm to Fork — Every Journey Can Be Traced.**

```text
SENSE → SECURE → STORE → SYNC → VERIFY → TRACE
```
