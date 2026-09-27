# VibraTrace — Full-Stack IoT Farm-to-Fork Food Traceability System

**Tagline:** *Sense → Secure → Store → Sync → Verify → Trace*  
**Product Concept:** Low-Cost IoT Blockchain Node for Secure Farm-to-Fork Traceability.

---

## 1. Problem & Solution
Perishable agricultural goods (fruits, vegetables, dairy, meat) suffer up to 40% post-harvest loss due to cold chain failures, delayed transit, and undetected spoilage gas accumulation (Ethylene, Ammonia). Traditional loggers are either expensive or lack cryptographic proof against tampering.

**VibraTrace** solves this by pairing:
1. **Low-cost ESP32 IoT Nodes**: Equipped with AHT20 (Temp/Humidity), Ethylene electrochemical sensors, NH3 gas sensors, Reed Switch container tamper sensors, and Piezoelectric vibration energy harvesting.
2. **Offline-First Buffering**: Uses MicroSD circular buffering during cellular transit outages, with idempotent automatic MQTT synchronization upon network reconnection.
3. **Cryptographic SHA-256 Hash Chaining**: Every sensor payload is canonicalized and chained to the previous block hash.
4. **Hybrid Blockchain/Ledger Anchoring**: Raw high-frequency telemetry is stored locally/PostgreSQL, while state transitions, tamper triggers, and batch completions are anchored to an immutable ledger.
5. **Consumer Farm-to-Fork QR Traceability**: Instant QR scanning allows buyers and retailers to inspect temperature history, gas levels, and cryptographic authenticity.

---

## 2. System Architecture

```text
  [ AHT20 + Ethylene + NH3 + Reed + Piezo ]
                    │
                    ▼
           [ ESP32 IoT Node ]
             ├── MicroSD Buffer (Offline-First)
             └── SIM800C GSM/GPRS (MQTT Client)
                    │
                    ▼  (MQTT Topics: vibratrace/device/{id}/telemetry)
          [ Mosquitto MQTT Broker ]
                    │
                    ▼
          [ FastAPI Backend Gateway ]
             ├── PostgreSQL Database (Relational persistence)
             ├── Hash Engine (SHA-256 chain verification)
             ├── Blockchain Ledger Service
             └── WebSocket Broadcast (/ws)
                    │
                    ▼
       [ VibraTrace Android Application ]
             ├── Jetpack Compose Material 3 UI
             ├── Room Local Database & Offline Cache
             ├── Real-time Sensor Charts & Analytics
             ├── Farm-to-Fork Timeline
             ├── QR Generation & Camera Scanner
             ├── Smart Alert Engine & Notifications
             └── Dual Mode: Real Hardware & Hackathon Demo Simulator
```

---

## 3. Directory Layout
- `app/` — Android Application (Kotlin, Jetpack Compose, Material 3, Room, Retrofit, CameraX, WorkManager)
- `backend/` — FastAPI Backend (Python, SQLAlchemy, PostgreSQL, MQTT Client, WebSockets, SHA-256 verification)
- `esp32/` — ESP32 Firmware (C++, AHT20, Gas abstractions, Reed switch, MicroSD buffer, SIM800C MQTT)
- `docker/` — Docker Compose configuration (PostgreSQL, Mosquitto, Backend)
- `docs/` — Architecture, Hardware Wiring, and API documentation

---

## 4. Quickstart Guide

### Running Backend with Docker
```bash
cd docker
docker-compose up -d
```
FastAPI Swagger docs will be accessible at `http://localhost:8000/docs`.

### Running Android App
Open the project in Android Studio or compile directly via AI Studio tools. The app includes a built-in **Demo Mode** with a full physical IoT simulator to run complete hackathon acceptance flows without needing physical hardware attached.
