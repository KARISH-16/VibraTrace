# VibraTrace System Architecture

## Core Pipelines
1. **IoT Edge Sensing**:
   - ESP32 gathers temperature & relative humidity from AHT20 via I2C (`0x38`).
   - Ethylene and NH3 gas readings via analog-to-digital converters with calibrated operational amplifier scaling.
   - Container lid reed switch triggers immediate interrupt on magnetic break.
   - Piezoelectric vibration harvesting transducer charges secondary storage buffer during truck transit.
2. **Offline-First Buffering**:
   - If GSM (SIM800C) network is lost, sensor payloads are written to circular queue on MicroSD SPI.
   - Upon cellular restoration, buffered records are dispatched idempotently to MQTT topic `vibratrace/device/{deviceId}/sync`.
3. **Cryptographic SHA-256 Chaining**:
   - Every reading generates a canonical ASCII string: `batchId:...,deviceId:...,eventId:...,temp:...,hum:...,c2h4:...,nh3:...,tamper:...,prevHash:...`
   - SHA-256 digest is produced and embedded into the payload. The next reading uses this hash as `previousHash`.
4. **Hybrid Blockchain / Ledger Integration**:
   - Routine readings are persisted in PostgreSQL.
   - Stage transitions, tamper triggers, and batch completions are committed to an immutable ledger (DemoLedgerService / Polygon/Ethereum bridge).
5. **Real-Time WebSocket Ingestion**:
   - Backend broadcasts `/ws` messages to Android app.
   - Android displays LIVE / RECONNECTING / OFFLINE / DEMO badges with zero lag.
